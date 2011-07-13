/*
 * #%L
 * Kipeto Core
 * %%
 * Copyright (C) 2010 - 2011 Ecclesia Versicherungsdienst GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.ecclesia.kipeto.job;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.common.util.ByteTransferEvent;
import de.ecclesia.kipeto.common.util.ByteTransferListener;
import de.ecclesia.kipeto.common.util.FileSizeFormatter;
import de.ecclesia.kipeto.engine.ActionCompletedEvent;
import de.ecclesia.kipeto.engine.ActionEvent;
import de.ecclesia.kipeto.engine.ActionListener;
import de.ecclesia.kipeto.engine.ActionProgressEvent;
import de.ecclesia.kipeto.engine.DefaultEngine;
import de.ecclesia.kipeto.engine.Engine;
import de.ecclesia.kipeto.engine.InstallFileAction;
import de.ecclesia.kipeto.engine.Plan;
import de.ecclesia.kipeto.engine.Planner;
import de.ecclesia.kipeto.engine.PlannerEvent;
import de.ecclesia.kipeto.engine.PlannerListener;
import de.ecclesia.kipeto.engine.UpdateFileAction;
import de.ecclesia.kipeto.job.event.BeginPhaseEvent;
import de.ecclesia.kipeto.job.event.BlueprintReceivedEvent;
import de.ecclesia.kipeto.job.event.ByteTransferSubProgressEvent;
import de.ecclesia.kipeto.job.event.EndPhaseEvent;
import de.ecclesia.kipeto.job.event.JobEvent;
import de.ecclesia.kipeto.job.event.JobProgressEvent;
import de.ecclesia.kipeto.job.event.PhaseProgressEvent;
import de.ecclesia.kipeto.job.event.SubProgressEvent;
import de.ecclesia.kipeto.repository.Blob;
import de.ecclesia.kipeto.repository.CachedReadingStrategy;
import de.ecclesia.kipeto.repository.ReadingRepository;
import de.ecclesia.kipeto.repository.ReadingRepositoryStrategy;
import de.ecclesia.kipeto.repository.WritingRepositoryStrategy;

/**
 * @author Daniel Hintze
 * @since 05.02.2010
 */
public class UpdateJob {

	public static final String LOCK_FILE = ".kipeto";

	public static final String IGNORE_FILE = ".kipeto_ignore";

	private static final int LOCKFILE_BINARY_VERSION = 1;

	private final Logger logger = LoggerFactory.getLogger(UpdateJob.class);

	private List<JobListener<JobEvent>> jobEventListener = new ArrayList<JobListener<JobEvent>>();

	private final String bluePrintRef;
	private final File target;

	private final ReadingRepositoryStrategy remoteRepository;
	private final CachedReadingStrategy cachedStrategy;
	private final WritingRepositoryStrategy cache;
	private final ReadingRepository repository;

	private Blueprint bluePrint;
	private Plan plan;

	private int stepsDone;
	private int stepsTotal;

	private FileLock lock;
	private FileChannel lockChannel;

	private String lastBlueprint;
	private Long lastSuccessfullUpdate;
	private RandomAccessFile randomAccessLockFile;

	public enum Phase {
		LOOKUP, PLAN, FILL_CACHE, PROCESS
	}

	public UpdateJob(ReadingRepositoryStrategy remoteRepository, WritingRepositoryStrategy cache, String bluePrintRef,
			File target) {
		this.cache = cache;
		this.remoteRepository = remoteRepository;
		this.cachedStrategy = new CachedReadingStrategy(remoteRepository, cache);
		this.repository = new ReadingRepository(cachedStrategy);
		this.bluePrintRef = bluePrintRef;
		this.target = target;
	}

	public void run() throws IOException {
		try {
			lockTarget();

			readLockFile();

			beginPhase(Phase.LOOKUP);
			lookup();
			endPhase(Phase.LOOKUP);

			beginPhase((Phase.PLAN));
			plan();
			endPhase(Phase.PLAN);

			beginPhase((Phase.FILL_CACHE));
			updateCache();
			endPhase(Phase.FILL_CACHE);

			beginPhase((Phase.PROCESS));
			process();
			endPhase(Phase.PROCESS);

			writeLockFile();

			logStatistics();
		} finally {
			unlockTarget();
		}

	}

	private void readLockFile() throws IOException {
		int binaryVersion;
		try {
			binaryVersion = randomAccessLockFile.readInt();
		} catch (EOFException e) {
			// Die LockDatei ist offensichtlich noch leer.
			return;
		}

		if (binaryVersion >= 1) {
			lastBlueprint = randomAccessLockFile.readUTF();
			lastSuccessfullUpdate = randomAccessLockFile.readLong();
		}
	}

	private void writeLockFile() throws IOException {
		randomAccessLockFile.seek(0);

		randomAccessLockFile.writeInt(LOCKFILE_BINARY_VERSION);
		randomAccessLockFile.writeUTF(bluePrintRef);
		randomAccessLockFile.writeLong(System.currentTimeMillis());
	}

	private void unlockTarget() throws IOException {
		logger.debug("unlocking target({})", UpdateJob.LOCK_FILE);

		if (lock != null) {
			lock.release();
		}

		if (lockChannel != null) {
			lockChannel.close();
		}
	}

	private void lockTarget() throws IOException {
		logger.debug("locking target ({})", UpdateJob.LOCK_FILE);

		if (target.exists() && !target.isDirectory()) {
			throw new IllegalStateException("Not a directory: " + target);
		}

		if (!target.exists()) {
			logger.debug("{} does not exist. creating...", target);

			if (!target.mkdirs()) {
				throw new RuntimeException("Could not create dir: " + target.getAbsolutePath());
			}
		}

		File file = new File(target, UpdateJob.LOCK_FILE);

		if (target.list().length == 0) {
			logger.debug("{} does not exist. creating...", file);
			file.createNewFile();
		} else {
			if (!file.exists()) {
				throw new IllegalStateException("Dir is not empty and no " + UpdateJob.LOCK_FILE
						+ " file found. Probably wrong target: " + target);
			}
		}

		randomAccessLockFile = new RandomAccessFile(file, "rw");
		lockChannel = randomAccessLockFile.getChannel();

		lock = lockChannel.lock();
	}

	public void addJobListener(JobListener<JobEvent> listener) {
		jobEventListener.add(listener);
	}

	public void removeJobListener(JobListener<JobEvent> listener) {
		jobEventListener.remove(listener);
	}

	private void lookup() throws IOException {
		String bluePrintId = repository.resolveReference(bluePrintRef);
		if (bluePrintId == null) {
			throw new RuntimeException("Blueprint not found: " + bluePrintRef);
		}

		stepsDone = 5;
		stepsTotal = 100;

		bluePrint = repository.retrieve(bluePrintId, Blueprint.class);

		InputStream iconInputStream = null;
		if (bluePrint.getIconId() != null) {
			Blob iconBlob = repository.retrieve(bluePrint.getIconId(), Blob.class);
			iconInputStream = iconBlob.contentStream();
		}

		// Event feuern, damit die GUI den Blueprint erhält und ggf. den Namen
		// im Titel anzeigen kann
		fireJobEvent(new BlueprintReceivedEvent(Phase.LOOKUP, bluePrint, iconInputStream));
	}

	private void plan() throws IOException {
		Planner planner = new Planner(repository, bluePrint, target);
		planner.addPlannerListener(new PlannerProgressListener());
		plan = planner.plan();

		stepsDone = 10;
		stepsTotal = 100;
	}

	private void updateCache() throws IOException {
		List<InstallFileAction> installFileActions = new ArrayList<InstallFileAction>(plan.getInstallFileActions());
		for (UpdateFileAction updateAction : plan.getUpdateFileActions()) {
			installFileActions.add(updateAction.getInstallFileAction());
		}

		Set<String> toCache = new HashSet<String>();

		for (InstallFileAction action : installFileActions) {
			String id = action.getFileItem().itemId();

			if (!cache.contains(id)) {
				toCache.add(id);
			}
		}

		int steps = (int) toCache.size() + plan.getTotalNumberOfActions();

		stepsTotal = (int) (steps > 0 ? steps * 1.1 : 100);
		stepsDone = steps > 0 ? stepsTotal / 10 : 100;

		int i = 0;
		for (String id : toCache) {
			long bytesToLoad = repository.sizeInRepository(id);
			cachedStrategy.load(id, new FillCacheListener(id, bytesToLoad));
			fireJobEvent(new PhaseProgressEvent(Phase.FILL_CACHE, ++i, toCache.size()));
			fireJobEvent(new JobProgressEvent(Phase.FILL_CACHE, ++stepsDone, stepsTotal));
		}
	}

	private void process() throws IOException {
		Engine engine = new DefaultEngine(repository);
		engine.addActionListener(new ProcessListener());
		engine.process(plan);
		stepsDone = stepsTotal;
	}

	private void beginPhase(Phase phase) {
		logger.info("Begin phase {}", phase);
		fireJobEvent(new BeginPhaseEvent(phase));
		fireJobEvent(new JobProgressEvent(phase, stepsDone, stepsTotal));
	}

	private void endPhase(Phase phase) {
		logger.info("End phase {}", phase);
		fireJobEvent(new EndPhaseEvent(phase));
		fireJobEvent(new JobProgressEvent(phase, stepsDone, stepsTotal));
	}

	private void logStatistics() {
		logger.info("Statistics: {} read from remote repository", readableBytes(remoteRepository.bytesRead()));
		logger.info("Statistics: {} read from cache", readableBytes(cache.bytesRead()));
		logger.info("Statistics: {} written to cache", readableBytes(cache.bytesWritten()));
	}

	private String readableBytes(long bytes) {
		String string = FileSizeFormatter.formateBytes(bytes, 2);
		if (bytes > (8 * 1024)) {
			string += String.format(" (%s bytes)", NumberFormat.getInstance().format(bytes));
		}

		return string;
	}

	private void fireJobEvent(JobEvent jobEvent) {
		for (JobListener<JobEvent> listener : jobEventListener) {
			listener.handleJobEvent(jobEvent);
		}
	}

	private class FillCacheListener implements ByteTransferListener {

		private final long bytesToRead;
		private final String id;

		public FillCacheListener(String id, long bytesToRead) {
			this.id = id;
			this.bytesToRead = bytesToRead;
		}

		private int scaleLongToInt(long l) {
			return l > Integer.MAX_VALUE ? (int) l / 1000000000 : (int) l;
		}

		public void handleByteTransfer(ByteTransferEvent event) {
			int read = scaleLongToInt(event.getBytesSinceBeginOfOperation());
			int total = scaleLongToInt(bytesToRead);

			fireJobEvent(new ByteTransferSubProgressEvent(Phase.FILL_CACHE, id, read, total));
		}

		@Override
		public String toString() {
			return this.getClass().toString();
		}

	}

	private class ProcessListener implements ActionListener {

		public void handleActionEvent(ActionEvent<?> event) {
			if (event instanceof ActionCompletedEvent<?>) {
				ActionCompletedEvent<?> completedEvent = (ActionCompletedEvent<?>) event;

				int actionsWorked = completedEvent.actionsWorkedTotal();
				int numberOfActions = completedEvent.plan().getTotalNumberOfActions();

				fireJobEvent(new JobProgressEvent(Phase.PROCESS, ++stepsDone, stepsTotal));
				fireJobEvent(new PhaseProgressEvent(Phase.PROCESS, actionsWorked, numberOfActions));
			} else if (event instanceof ActionProgressEvent<?>) {
				ActionProgressEvent<?> actionProgressEvent = (ActionProgressEvent<?>) event;

				int stepsDone = actionProgressEvent.stepsDone();
				int stepsTotal = actionProgressEvent.stepsTotal();

				fireJobEvent(new SubProgressEvent(Phase.PROCESS, event, stepsDone, stepsTotal));
			}

		}

	}

	private class PlannerProgressListener implements PlannerListener {

		public void handlePlannerEvent(PlannerEvent event) {
			fireJobEvent(new SubProgressEvent(Phase.PROCESS, event, stepsDone, stepsTotal));
		}
	}

	public String getLastBlueprint() {
		return lastBlueprint;
	}

	public Long getLastSuccessfullUpdate() {
		return lastSuccessfullUpdate;
	}
}
