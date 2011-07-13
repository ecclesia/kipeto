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
package de.ecclesia.kipeto.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.blueprint.Directory;
import de.ecclesia.kipeto.blueprint.DirectoryItem;
import de.ecclesia.kipeto.blueprint.FileItem;
import de.ecclesia.kipeto.blueprint.Item;
import de.ecclesia.kipeto.common.util.Assert;
import de.ecclesia.kipeto.exception.FileLockException;
import de.ecclesia.kipeto.job.UpdateJob;
import de.ecclesia.kipeto.repository.Blob;
import de.ecclesia.kipeto.repository.ReadingRepository;

/**
 * @author Daniel Hintze
 * @since 03.02.2010
 */
public class Planner {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ReadingRepository repository;
	private final Blueprint bluePrint;
	private final File target;

	private List<MakeDirAction> makeDirActions;
	private List<RemoveDirAction> removeDirActions;
	private List<RemoveFileAction> removeFileActions;
	private List<InstallFileAction> installFileActions;
	private List<UpdateFileMetadataAction> updateFileMetadataActions;
	private List<UpdateFileAction> updateFileActions;

	private List<PlannerListener> listeners = new ArrayList<PlannerListener>();

	public Planner(ReadingRepository repository, Blueprint blueprint, File rootTarget) {
		Assert.isNotNull(repository);
		Assert.isNotNull(blueprint);
		Assert.isNotNull(rootTarget);

		this.repository = repository;
		this.bluePrint = blueprint;
		this.target = rootTarget;
	}

	public Plan plan() throws IOException {
		makeDirActions = new ArrayList<MakeDirAction>();
		removeDirActions = new ArrayList<RemoveDirAction>();
		removeFileActions = new ArrayList<RemoveFileAction>();
		installFileActions = new ArrayList<InstallFileAction>();
		updateFileMetadataActions = new ArrayList<UpdateFileMetadataAction>();
		updateFileActions = new ArrayList<UpdateFileAction>();

		if (!target.exists()) {
			makeDirActions.add(new MakeDirAction(target));
		}

		Directory root = repository.retrieve(bluePrint.getRootDirId(), Directory.class);
		processDirectory(root, target);

		return new Plan(makeDirActions, removeDirActions, removeFileActions, installFileActions,
				updateFileMetadataActions, updateFileActions);
	}

	private void processDirectory(Directory directory, File targetDir) throws IOException {
		// Prüfen, ob das Verzeichnis manuell von Updates ausgeschlossen wurde.
		File ignoreFile = new File(targetDir, UpdateJob.IGNORE_FILE);
		if (ignoreFile.exists()) {
			// Prüfen, ob die Ignore-File im Blueprint auch vorhanden ist:
			Item[] items = directory.getItems();
			for (Item item : items) {
				// Dieses Verzeichnis komplett ignorieren
				if (item instanceof FileItem && item.name().equals(UpdateJob.IGNORE_FILE)) {
					logger.info("Ignoring Directory {} because of {}", targetDir, ignoreFile);
					return;
				}
			}
		}

		// Speichert zunächst alle Elemente, die im Zielverzeichnis vorhanden
		// sind. Jedes Element, welches im zu verarbeitenden Directory vorkommt,
		// wird nach der Verarbeitung aus dieser Liste entfernt. Was nach der
		// Verarbeitung des Directory`s übrig bleibt sind Elemente, die im
		// aktuellen Blueprint nicht mehr enthalten sind. Sie werden gelöscht.
		File[] files = targetDir.listFiles();
		List<File> filesInTargetDir;
		if (files != null) {
			filesInTargetDir = new ArrayList<File>(Arrays.asList(files));
		} else {
			filesInTargetDir = new ArrayList<File>();
		}

		for (Item item : directory.getItems()) {
			// File, die nach(!) erfolgreichem Update im Zielverzeichnis
			// existieren soll.
			File target = new File(targetDir, item.name());

			if (item instanceof DirectoryItem) {
				processDirectoryItem((DirectoryItem) item, target);
			} else if (item instanceof FileItem) {
				processFileItem((FileItem) item, target);
			} else {
				throw new UnsupportedOperationException("Item-Type not supported: " + item.getClass().getSimpleName());
			}

			// Das verarbeitete Item aus der Liste der Elemente in dem
			// Zielverzeichnis entfernen (so es denn schon existiert hat).
			filesInTargetDir.remove(target);
		}

		// Alle Items des Directory`s wurden verarbeitet. Files, die sich
		// jetzt noch in der Liste befinden, kommen im Directory nicht vor und
		// werden gelöscht.
		for (File file : filesInTargetDir) {
			if (file.isDirectory()) {
				removeDirRecursively(file);
			} else {
				if (fileToIgnore(file)) {
					logger.info("Ignoring {}", file);
				} else {
					addAction(new RemoveFileAction(file));
				}
			}
		}
	}

	private void processDirectoryItem(DirectoryItem item, File file) throws IOException {
		logger.debug("Processing DirectoryItem {} ({} ", item.name(), item.itemId());
		firePlannerEvent(new PlannerProcessEvent(file));

		if (file.isFile()) {
			addAction(new RemoveFileAction(file));
		}

		if (!file.isDirectory()) {
			addAction(new MakeDirAction(file));
		}

		Directory directory = repository.retrieve(item.itemId(), Directory.class);
		processDirectory(directory, file);
	}

	private void processFileItem(FileItem item, File file) {
		logger.debug("Processing FileItem {} ({}) ", item.name(), item.itemId());
		firePlannerEvent(new PlannerProcessEvent(file));

		if (file.exists()) {
			if (file.isDirectory()) {
				// Ein Verzeichnis dass so heißt wie eine Datei die wird
				// installieren wollen, muss gelöscht werden.
				removeDirRecursively(file);

				// Und jetzt die neue Datei installieren
				addAction(new InstallFileAction(item, file));
			} else if (file.lastModified() != item.lastModified() || file.length() != item.length()) {
				long iLm = item.lastModified();
				long tLm = file.lastModified();
				long iLh = item.length();
				long tLh = file.length();

				if (iLh != tLh) {
					logger.debug(String.format("%s differs from blueprint. Length is %d, should be %d", file, tLh, iLh));
					addAction(new UpdateFileAction(file, item));
				} else if (hashDiffers(item.itemId(), file)) {
					logger.debug(String.format("%s differs from blueprint. Hash should be %s", file, item.itemId()));
					addAction(new UpdateFileAction(file, item));
				} else {
					logger.debug(String.format("%s differs from blueprint. LastModified is %d, should be %d", file,
							tLm, iLm));
					addAction(new UpdateFileMetadataAction(file, item));
				}
			}
		}

		else {
			addAction(new InstallFileAction(item, file));
		}
	}

	private boolean hashDiffers(String id, File file) {
		try {
			firePlannerEvent(new PlannerHashEvent(file));

			Blob blob = repository.retrieve(id, Blob.class);
			String compression = blob.compression();

			// Nicht-benötigten ContentStream schließen, da sonst ggf. eine
			// HTTP-Connection offen bleibt.
			blob.contentStream().close();

			Blob localBlob = new Blob(compression, new BufferedInputStream(new FileInputStream(file)), file.length());
			localBlob.writeToStream(new BufferedOutputStream(new OutputStream() {

				@Override
				public void write(int b) throws IOException {
				}
			}));

			if (id.equals(localBlob.id())) {
				logger.debug(file + "`s hash is {}, equals id {}", localBlob.id(), id);
				return false;
			} else {
				logger.debug(file + "`s hash is {}, differs from id {}", localBlob.id(), id);
				return true;
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private boolean fileToIgnore(File file) {
		if (file.getParentFile().equals(target) && file.getName().equals(UpdateJob.LOCK_FILE)) {
			return true;
		} else {
			return false;
		}
	}

	private void removeDirRecursively(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				removeDirRecursively(file);
			} else {
				addAction(new RemoveFileAction(file));
			}
		}

		addAction(new RemoveDirAction(dir));
	}

	private void addAction(Action a) {
		logger.debug("Add {} for {}", a.getClass().getSimpleName(), a.getTarget());

		if (a instanceof MakeDirAction) {
			makeDirActions.add((MakeDirAction) a);
		} else if (a instanceof RemoveDirAction) {
			removeDirActions.add((RemoveDirAction) a);
		} else if (a instanceof RemoveFileAction) {
			tryLock(a.getTarget());
			removeFileActions.add((RemoveFileAction) a);
		} else if (a instanceof InstallFileAction) {
			installFileActions.add((InstallFileAction) a);
		} else if (a instanceof UpdateFileMetadataAction) {
			updateFileMetadataActions.add((UpdateFileMetadataAction) a);
		} else if (a instanceof UpdateFileAction) {
			tryLock(a.getTarget());
			updateFileActions.add((UpdateFileAction) a);
		} else {
			throw new IllegalStateException("Unknown Action " + a.getClass());
		}
	}

	private void tryLock(File file) {
		FileChannel channel = null;
		FileLock lock = null;

		try {
			channel = new RandomAccessFile(file, "rw").getChannel();
			lock = channel.tryLock();

			if (lock == null) {
				logger.error("Lock for {} could not be acquired because another program holds an overlapping lock",
						file);
				throw new FileLockException(file,
						"Lock could not be acquired because another program holds an overlapping lock");
			} else {
				logger.debug("Lock for {} could be acquired, no another program holds an overlapping lock", file);
			}

			// Scheinbar gibt es auch FileNotFoundExceptions, wenn eine Datei
			// bereits gelockt ist.
		} catch (FileNotFoundException e) {
			if (file.exists()) {
				logger.error("Lock for {} could not be acquired because another program holds an overlapping lock",
						file);
				throw new FileLockException(file,
						"Lock could not be acquired because another program holds an overlapping lock");
			} else {
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (lock != null) {
					lock.release();
				}

				if (channel != null) {
					channel.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void addPlannerListener(PlannerListener listener) {
		listeners.add(listener);
	}

	public void removeActionListener(ActionListener listener) {
		listeners.remove(listener);
	}

	protected void firePlannerEvent(PlannerEvent event) {
		for (PlannerListener listener : listeners) {
			listener.handlePlannerEvent(event);
		}
	}
}
