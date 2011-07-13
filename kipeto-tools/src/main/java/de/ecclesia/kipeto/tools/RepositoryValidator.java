/*
 * #%L
 * Kipeto Tools
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
package de.ecclesia.kipeto.tools;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.blueprint.Directory;
import de.ecclesia.kipeto.blueprint.DirectoryItem;
import de.ecclesia.kipeto.blueprint.Item;
import de.ecclesia.kipeto.common.util.HashUtil;
import de.ecclesia.kipeto.repository.Blob;
import de.ecclesia.kipeto.repository.ReadingRepository;
import de.ecclesia.kipeto.repository.ReadingRepositoryStrategy;

public class RepositoryValidator {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ReadingRepository repository;

	public RepositoryValidator(ReadingRepository repository) {
		this.repository = repository;
	}

	public void validate(String blueprintId) throws IOException {
		String blueprintRef = repository.resolveReference(blueprintId);
		Blueprint blueprint = repository.retrieve(blueprintRef, Blueprint.class);

		validateHash(blueprintRef);

		Directory root = repository.retrieve(blueprint.getRootDirId(), Directory.class);
		processDirectory(root);
		
		logger.info("Blueprint {} is valid", blueprintId);
	}

	private void processDirectory(Directory dir) throws IOException {
		validateHash(dir.id());

		for (Item item : dir.getItems()) {
			logger.debug("Processing item {}", item.name());

			if (item instanceof DirectoryItem) {
				Directory directory = repository.retrieve(item.itemId(), Directory.class);
				processDirectory(directory);
			} else {
				validateHash(item.itemId());
			}
		}
	}

	private void validateHash(String id) throws IOException {
		ReadingRepositoryStrategy strategy = repository.strategy();
		InputStream stream = strategy.retrieveStream(id);

		String hash = HashUtil.hashStream(stream, Blob.HASH_ALGORITHM);

		if (hash.equals(id)) {
			logger.debug("item {} has an valid hash", id);
		} else {
			logger.error("item {} has an invalid hash ({})", id, hash);
			throw new IllegalStateException("item " + id + " has an invalid hash");
		}

	}

}
