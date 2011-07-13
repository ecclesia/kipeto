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
package de.ecclesia.kipeto.tools.cpref;

import java.io.IOException;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.ecclesia.kipeto.common.util.LoggerConfigurer;
import de.ecclesia.kipeto.repository.AuthenticationProvider;
import de.ecclesia.kipeto.repository.StrategySelector;
import de.ecclesia.kipeto.repository.WritingRepositoryStrategy;
import de.ecclesia.kipeto.tools.AuthenticationProviderFactory;

public class CopyReferenceApp {

	private final CopyReferenceOptions options;

	Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) throws JSchException, SftpException, IOException {
		CopyReferenceOptions options = new CopyReferenceOptions(args);

		CopyReferenceApp copyReference = new CopyReferenceApp(options);
		copyReference.run();
	}

	public CopyReferenceApp(CopyReferenceOptions options) {
		this.options = options;
	}

	public void run() {
		LoggerConfigurer.configureConsoleAppender(Level.toLevel(options.getLogLevel(), Level.INFO));

		WritingRepositoryStrategy strategy = null;

		try {
			AuthenticationProvider authenticationProvider = AuthenticationProviderFactory.getProvider(options);
			strategy = StrategySelector.getWritingStrategy(options.getRepositoryUrl(), authenticationProvider, null);

			String id = strategy.resolveReference(options.getSource());
			if (id == null) {
				throw new RuntimeException("Reference <" + options.getSource() + "> not found in repository");
			}

			strategy.createReference(options.getDestination(), id);

			logger.info("Copied reference {} to {}", options.getSource(), options.getDestination());
		} catch (Exception e) {
			logger.error("Error: ", e);
		} finally {
			if (strategy != null) {
				strategy.close();
			}
		}
	}

}
