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
package de.ecclesia.kipeto.tools.rmref;

import java.io.IOException;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.ecclesia.kipeto.common.util.LoggerConfigurer;
import de.ecclesia.kipeto.repository.AuthenticationProvider;
import de.ecclesia.kipeto.repository.AuthenticationProviderFactory;
import de.ecclesia.kipeto.repository.Reference;
import de.ecclesia.kipeto.repository.StrategySelector;
import de.ecclesia.kipeto.repository.WritingRepository;
import de.ecclesia.kipeto.repository.WritingRepositoryStrategy;

public class RemoveReferenceApp {

	private final RemoveReferenceOptions options;

	Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) throws JSchException, SftpException, IOException {
		RemoveReferenceOptions options = new RemoveReferenceOptions(args);

		RemoveReferenceApp listReferences = new RemoveReferenceApp(options);
		listReferences.run();
	}

	public RemoveReferenceApp(RemoveReferenceOptions options) {
		this.options = options;
	}

	public void run() {
		LoggerConfigurer.configureConsoleAppender(Level.toLevel(options.getLogLevel(), Level.INFO));

		WritingRepositoryStrategy strategy = null;
		WritingRepository repository = null;

		try {
			AuthenticationProvider authenticationProvider = AuthenticationProviderFactory.getProvider(options);

			strategy = StrategySelector.getWritingStrategy(options.getRepositoryUrl(), authenticationProvider, null);
			repository = new WritingRepository(strategy);

			String reference = options.getReference();
			if (reference.length() > 1 && reference.endsWith("*")) {
				for (Reference ref : strategy.allReferences()) {
					if (ref.name().startsWith(reference.substring(0, reference.length() - 1))) {
						logger.info("Removing reference '{}'", ref.name());
						repository.removeReference(ref.name());
					}
				}
			} else {
				logger.info("Removing reference '{}'", options.getReference());
				repository.removeReference(options.getReference());
			}
		} catch (Exception e) {
			logger.error("Error: ", e);
		} finally {
			if (repository != null) {
				repository.close();
			}
		}
	}

}
