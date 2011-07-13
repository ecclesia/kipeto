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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.blueprint.FileItem;
import de.ecclesia.kipeto.common.util.ByteTransferEvent;
import de.ecclesia.kipeto.common.util.ByteTransferListener;
import de.ecclesia.kipeto.common.util.CountingInputStream;
import de.ecclesia.kipeto.common.util.Streams;
import de.ecclesia.kipeto.exception.FileDeleteException;
import de.ecclesia.kipeto.repository.Blob;
import de.ecclesia.kipeto.repository.ReadingRepository;

/**
 * @author Daniel Hintze
 * @since 03.02.2010
 */
public class DefaultEngine extends Engine {

	private final Logger logger = LoggerFactory.getLogger(DefaultEngine.class);

	public DefaultEngine(ReadingRepository repository) {
		super(repository);
	}

	@Override
	protected void handle(final InstallFileAction action) throws IOException {
		FileItem fileItem = action.getFileItem();

		File target = action.getTarget();
		String id = fileItem.itemId();
		final long length = fileItem.length();
		long lastModified = fileItem.lastModified();

		Blob blob = getRepository().retrieve(id, Blob.class);

		CountingInputStream countingInputStream = new CountingInputStream(blob.contentStream());
		countingInputStream.addByteTransferListener(new ByteTransferListener() {

			public void handleByteTransfer(ByteTransferEvent event) {
				long bytesSinceBeginOfOperation = event.getBytesSinceBeginOfOperation();

				int stepsDone = scaleLongToInt(bytesSinceBeginOfOperation);
				int stepsTotal = scaleLongToInt(length);

				fireActionEvent(new ActionProgressEvent<Action>(action, stepsDone, stepsTotal));
			}
		});

		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target));

		logger.debug("Install Blob {} to {}", id, target);

		fireActionEvent(new ActionProgressEvent<Action>(action, 0, scaleLongToInt(length)));

		Streams.copyStream(countingInputStream, outputStream, true);
		target.setLastModified(lastModified);
	}

	// FIXME: Häh?
	private int scaleLongToInt(long l) {
		return l > Integer.MAX_VALUE ? (int) l / 1000000000 : (int) l;
	}

	@Override
	protected void handle(RemoveDirAction action) {
		File dir = action.getTarget();

		logger.debug("Remove dir {}", dir);

		if (!dir.delete()) {
			throw new FileDeleteException(dir);
		}
	}

	@Override
	protected void handle(RemoveFileAction action) {
		File file = action.getTarget();

		logger.debug("Remove file {}", file);

		if (!file.delete()) {
			throw new FileDeleteException(file);
		}
	}

	@Override
	protected void handle(UpdateFileAction action) throws IOException {
		logger.debug("Update file {}", action.getInstallFileAction().getTarget());

		handle(action.getRemoveFileAction());
		handle(action.getInstallFileAction());
	}

	@Override
	protected void handle(UpdateFileMetadataAction action) {
		long lastModified = action.getItem().lastModified();

		File target = action.getTarget();

		logger.debug("Update metadata for file {}, setting lastModified to {}", target, lastModified);
		target.setLastModified(lastModified);

		if (target.lastModified() != lastModified) {
			logger.warn("Update metadata for file {} faild, could not set lastModified to {}", target, lastModified);
		}

	}

	@Override
	protected void handle(MakeDirAction action) {
		File dir = action.getTarget();

		logger.debug("Make dir {}", dir);

		if (dir.isDirectory()) {
			return;
		}

		if (!dir.mkdirs()) {
			throw new RuntimeException("Could not create dir: " + dir.getAbsolutePath());
		}
	}
}
