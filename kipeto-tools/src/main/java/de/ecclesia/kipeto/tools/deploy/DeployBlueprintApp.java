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
package de.ecclesia.kipeto.tools.deploy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Level;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.ecclesia.kipeto.common.util.LoggerConfigurer;
import de.ecclesia.kipeto.repository.AuthenticationProvider;
import de.ecclesia.kipeto.repository.AuthenticationProviderFactory;
import de.ecclesia.kipeto.repository.FileRepositoryStrategy;
import de.ecclesia.kipeto.repository.ReadingRepository;
import de.ecclesia.kipeto.repository.Reference;
import de.ecclesia.kipeto.repository.StrategySelector;
import de.ecclesia.kipeto.repository.WritingRepository;
import de.ecclesia.kipeto.repository.WritingRepositoryStrategy;
import de.ecclesia.kipeto.tools.DiffAnalyzer;

public class DeployBlueprintApp {

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(DeployBlueprintApp.class);

	private final DeployBlueprintOptions options;

	public static void main(String[] args) throws JSchException, SftpException, IOException {
		DeployBlueprintOptions options = new DeployBlueprintOptions(args);

		DeployBlueprintApp deployBlueprint = new DeployBlueprintApp(options);
		deployBlueprint.run();
	}

	public DeployBlueprintApp(DeployBlueprintOptions options) {
		this.options = options;
	}

	public void run() {
		LoggerConfigurer.configureFileAppender(options.getDataDir(), "deploy_blueprint");
		LoggerConfigurer.configureConsoleAppender(Level.toLevel(options.getLogLevel(), Level.INFO));

		ReadingRepository localRepos = null;
		WritingRepository remoteRepos = null;

		try {
			File repositoryDir = new File(options.getDataDir(), "repos");
			File tempDir = new File(options.getDataDir(), "temp");

			tempDir.mkdir();
			String blueprint = options.getBlueprint();

			String url = options.getRepositoryUrl();
			AuthenticationProvider authenticationProvider = AuthenticationProviderFactory.getProvider(options);

			WritingRepositoryStrategy remoteStrategy = StrategySelector.getWritingStrategy(url, authenticationProvider, tempDir);
			FileRepositoryStrategy localStrategy = new FileRepositoryStrategy(repositoryDir, tempDir);

			localRepos = new ReadingRepository(localStrategy);
			remoteRepos = new WritingRepository(remoteStrategy);

			DiffAnalyzer diffAnalyzer = new DiffAnalyzer(localRepos, remoteRepos);
			Map<String, String> toUpload = diffAnalyzer.itemsToUpload(blueprint);

			String blueprintId = localRepos.resolveReference(blueprint);

			for (String id : toUpload.keySet()) {
				InputStream stream = localStrategy.retrieveStream(id);
				remoteStrategy.storeStream(id, stream);
			}

			if (options.isSkipExistingBlueprints()) {
				for (Reference ref : remoteStrategy.allReferences()) {
					if (ref.id().equals(blueprintId)) {
						logger.info("{} already deployed as {}", blueprintId, ref.name());
						return;
					}
				}
			}

			String reference = options.isTimestampSuffix() ? blueprintDate(blueprint) : blueprint;
			remoteRepos.createReference(reference, blueprintId);

			logger.info("blueprint {} deployed as {}", blueprint, reference);
		} catch (Exception e) {
			logger.error("Error: ", e);
			throw new RuntimeException(e);
		} finally {
			if (localRepos != null) {
				localRepos.close();
			}

			if (remoteRepos != null) {
				remoteRepos.close();
			}
		}
	}

	private String blueprintDate(String blueprint) {
		SimpleDateFormat df = new SimpleDateFormat("_yyyyMMdd-HHmmss");
		return blueprint + df.format(new Date());
	}

}
