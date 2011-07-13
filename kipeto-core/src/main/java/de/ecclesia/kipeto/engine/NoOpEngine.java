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
/**
 * 
 */
package de.ecclesia.kipeto.engine;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.repository.ReadingRepository;

/**
 * @author Daniel Hintze
 * @since 03.02.2010
 */
public class NoOpEngine extends Engine {

	public NoOpEngine(ReadingRepository repository) {
		super(repository);
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void handle(InstallFileAction action) throws IOException {
		logger.debug("Install Blob {} to {}", action.getFileItem().itemId(), action.getFileItem());
	}

	@Override
	protected void handle(UpdateFileAction action) throws IOException {
		logger.debug("Update file {}", action.getInstallFileAction().getTarget());
	}

	@Override
	protected void handle(RemoveDirAction action) {
		logger.debug("Remove dir {}", action.getTarget());
	}

	@Override
	protected void handle(RemoveFileAction action) {
		logger.debug("Remove file {}", action.getTarget());
	}

	@Override
	protected void handle(MakeDirAction action) {
		logger.debug("Make dir {}", action.getTarget());
	}

	@Override
	protected void handle(UpdateFileMetadataAction action) {
		logger.debug("Update metadata for file {}", action.getTarget());
	}

}
