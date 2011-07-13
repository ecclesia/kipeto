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
package de.ecclesia.kipeto.tools.gbc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.blueprint.Directory;
import de.ecclesia.kipeto.blueprint.DirectoryItem;
import de.ecclesia.kipeto.blueprint.Item;
import de.ecclesia.kipeto.common.util.FileSizeFormatter;
import de.ecclesia.kipeto.common.util.Tuple;
import de.ecclesia.kipeto.common.util.Tuple.Tuple2;
import de.ecclesia.kipeto.repository.Reference;
import de.ecclesia.kipeto.repository.WritingRepository;
import de.ecclesia.kipeto.repository.WritingRepositoryStrategy;

public class GarbageCollector {

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(GarbageCollector.class);

	private final WritingRepositoryStrategy strategy;

	private WritingRepository repository;

	public GarbageCollector(WritingRepositoryStrategy strategy) {
		this.strategy = strategy;
		this.repository = new WritingRepository(strategy);
	}

	public List<Tuple2<String, Long>> detectUnusedObjects() throws IOException {
		logger.info("Detecting unused objects...");

		List<String> objects = strategy.allObjects();
		List<Reference> references = strategy.allReferences();

		logger.info("Repository contains {} references", references.size());
		logger.info("Repository contains {} objects", objects.size());

		for (Reference reference : references) {
			logger.info("Processing reference <{}>", reference.name());

			Blueprint blueprint = repository.retrieve(reference.id(), Blueprint.class);
			objects.remove(reference.id());

			if (blueprint.getIconId() != null) {
				objects.remove(blueprint.getIconId());
			}

			processDirectory(blueprint.getRootDirId(), objects);
		}

		logger.info("Calculating size of unused objects...");
		long totalSize = 0;
		ArrayList<Tuple2<String, Long>> list = new ArrayList<Tuple2<String, Long>>();
		for (String id : objects) {
			long size = strategy.sizeInRepository(id);
			totalSize += size;
			list.add(Tuple.newTuple(id, size));
		}

		logger.info("Detected {} unused Objects with a total size of {}", list.size(), FileSizeFormatter.formateBytes(totalSize, 2));

		return list;
	}

	private void processDirectory(String id, List<String> objects) throws IOException {
		objects.remove(id);

		Directory directory = repository.retrieve(id, Directory.class);
		for (Item item : directory.getItems()) {
			if (item instanceof DirectoryItem) {
				processDirectory(item.itemId(), objects);
			} else {
				objects.remove(item.itemId());
			}
		}
	}
}
