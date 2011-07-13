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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.blueprint.Directory;
import de.ecclesia.kipeto.blueprint.DirectoryItem;
import de.ecclesia.kipeto.blueprint.Item;
import de.ecclesia.kipeto.repository.ReadingRepository;

public class DiffAnalyzer {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ReadingRepository localRepos;
	private final ReadingRepository remoteRepos;

	private Map<String, String> objsToUpload;
	private int objectsAlreadyPresent;

	public DiffAnalyzer(ReadingRepository localRepos, ReadingRepository remoteRepos) {
		this.localRepos = localRepos;
		this.remoteRepos = remoteRepos;
	}

	public Map<String, String> itemsToUpload(String blueprint) throws IOException {
		this.objsToUpload = new HashMap<String, String>();

		String localBlueprintRef = localRepos.resolveReference(blueprint);
		if (localBlueprintRef == null) {
			throw new IllegalStateException("unknown reference: " + blueprint);
		}

		Blueprint localBlueprint = localRepos.retrieve(localBlueprintRef, Blueprint.class);
		String iconId = localBlueprint.getIconId();

		checkAndAdd(localBlueprintRef, "##Blueprint " + blueprint);

		if (iconId != null) {
			checkAndAdd(iconId, "##Blueprint Icon");
		}

		Directory root = localRepos.retrieve(localBlueprint.getRootDirId(), Directory.class);
		processDirectory(root, "##Root-Dir");

		logger.info("Objects to upload: {}", objsToUpload.size());
		logger.info("Objects already present: {}", objectsAlreadyPresent);

		return objsToUpload;
	}

	private void checkAndAdd(String id, String name) throws IOException {
		if (!remoteRepos.contains(id)) {
			objsToUpload.put(id, name);
			logger.info("Object {} ({}) not present in remote repository", id, name);
		} else {
			objectsAlreadyPresent++;
			logger.debug("Object  {} ({}) present in remote repository", id, name);
		}
	}

	private void processDirectory(Directory dir, String name) throws IOException {
		checkAndAdd(dir.id(), name);

		for (Item item : dir.getItems()) {
			logger.debug("Processing item {}", item.name());

			if (item instanceof DirectoryItem) {
				Directory directory = localRepos.retrieve(item.itemId(), Directory.class);
				processDirectory(directory, "Directory " + item.name());
			} else {
				checkAndAdd(item.itemId(), "File " + item.name());
			}
		}
	}

}
