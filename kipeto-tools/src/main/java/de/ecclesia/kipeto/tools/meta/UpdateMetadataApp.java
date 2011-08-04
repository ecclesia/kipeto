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
package de.ecclesia.kipeto.tools.meta;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.common.util.LoggerConfigurer;
import de.ecclesia.kipeto.engine.Engine;
import de.ecclesia.kipeto.engine.Plan;
import de.ecclesia.kipeto.engine.Planner;
import de.ecclesia.kipeto.engine.UpdateFileMetadataEngine;
import de.ecclesia.kipeto.repository.AuthenticationProvider;
import de.ecclesia.kipeto.repository.AuthenticationProviderFactory;
import de.ecclesia.kipeto.repository.ReadingRepository;
import de.ecclesia.kipeto.repository.ReadingRepositoryStrategy;
import de.ecclesia.kipeto.repository.StrategySelector;

public class UpdateMetadataApp {

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateMetadataApp.class);

	private final UpdateMetadataOptions options;

	public static void main(String[] args) throws JSchException, SftpException, IOException {
		UpdateMetadataOptions options = new UpdateMetadataOptions(args);

		UpdateMetadataApp updateMetadata = new UpdateMetadataApp(options);
		updateMetadata.run();
	}

	public UpdateMetadataApp(UpdateMetadataOptions options) {
		this.options = options;
	}

	public void run() {
		try {
			LoggerConfigurer.configureConsoleAppender(Level.toLevel(options.getLogLevel(), Level.INFO));

			logger.info("Begin to adjust metadata");

			AuthenticationProvider authenticationProvider = AuthenticationProviderFactory.getProvider(options);
			ReadingRepositoryStrategy strategy = StrategySelector.getReadingStrategy(options.getRepositoryUrl(), authenticationProvider, null);
			ReadingRepository repository = new ReadingRepository(strategy);

			String reference = repository.resolveReference(options.getBlueprint());
			
			// UPD-18: Nur eine Warnung, wenn der Blueprint nicht existiert  
			if(reference == null) {
				logger.warn("Reference <{}> could not be resolved", options.getBlueprint());
				return;
			}
			
			Blueprint blueprint = repository.retrieve(reference, Blueprint.class);

			File target = new File(options.getTarget());

			Planner planner = new Planner(repository, blueprint, target);
			Plan plan = planner.plan();

			Engine engine = new UpdateFileMetadataEngine(repository);
			engine.process(plan);

			logger.info("Finished adjusting metadata");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
