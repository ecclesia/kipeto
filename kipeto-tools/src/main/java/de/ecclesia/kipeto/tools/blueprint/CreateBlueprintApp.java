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
package de.ecclesia.kipeto.tools.blueprint;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.common.util.LoggerConfigurer;
import de.ecclesia.kipeto.repository.FileRepositoryStrategy;
import de.ecclesia.kipeto.repository.WritingRepository;

public class CreateBlueprintApp {

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(CreateBlueprintApp.class);
	private CreateBlueprintOptions options;

	public static void main(String[] args) throws IOException {
		CreateBlueprintOptions options = new CreateBlueprintOptions(args);

		CreateBlueprintApp createBlueprint = new CreateBlueprintApp(options);
		createBlueprint.run();
	}

	public CreateBlueprintApp(CreateBlueprintOptions options) {
		this.options = options;
	}

	public void run() {
		String data = options.getDataDir();
		String blueprint = options.getBlueprint();
		String logLevel = options.getLogLevel();
		String description = options.getDescription();
		String icon = options.getIcon();
		String source = options.getSource();

		LoggerConfigurer.configureFileAppender(data, "create_blueprint");
		LoggerConfigurer.configureConsoleAppender(Level.toLevel(logLevel, Level.INFO));

		WritingRepository repos = null;

		try {
			File dataDir = new File(options.getDataDir());
			File reposDir = new File(dataDir, "repos");
			File tempDir = new File(dataDir, "temp");
			
			tempDir.mkdirs();
			reposDir.mkdirs();

			repos = new WritingRepository(new FileRepositoryStrategy(reposDir, tempDir));

			BlueprintFactory factory = new BlueprintFactory(repos);

			Blueprint bp;
			if (options.getIcon() != null) {
				bp = factory.fromDir(blueprint, description, new File(source), new File(icon));
			} else {
				bp = factory.fromDir(blueprint, description, new File(source));
			}

			String bluePrintId = repos.store(bp);

			logger.info("added blueprint {} -> {}", blueprint, bluePrintId);

			repos.createReference(blueprint, bluePrintId);

			logger.info("done");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (repos != null) {
				repos.close();
			}
		}
	}

}
