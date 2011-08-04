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

import static de.ecclesia.kipeto.common.util.FileSizeFormatter.formateBytes;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Level;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.ecclesia.kipeto.common.util.LoggerConfigurer;
import de.ecclesia.kipeto.common.util.Tuple.Tuple2;
import de.ecclesia.kipeto.repository.AuthenticationProvider;
import de.ecclesia.kipeto.repository.AuthenticationProviderFactory;
import de.ecclesia.kipeto.repository.StrategySelector;
import de.ecclesia.kipeto.repository.WritingRepository;
import de.ecclesia.kipeto.repository.WritingRepositoryStrategy;

public class CollectGarbageApp {

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(CollectGarbageApp.class);

	private final CollectGarbageOptions options;

	public static void main(String[] args) throws JSchException, SftpException, IOException {
		CollectGarbageOptions options = new CollectGarbageOptions(args);

		CollectGarbageApp collectGarbage = new CollectGarbageApp(options);
		collectGarbage.run();
	}

	public CollectGarbageApp(CollectGarbageOptions options) {
		this.options = options;
	}

	public void run() {

		LoggerConfigurer.configureConsoleAppender(Level.toLevel(options.getLogLevel(), Level.INFO));

		WritingRepository repos = null;

		try {
			AuthenticationProvider authenticationProvider = AuthenticationProviderFactory.getProvider(options);
			WritingRepositoryStrategy strategy = StrategySelector.getWritingStrategy(options.getRepositoryUrl(), authenticationProvider, null);

			GarbageCollector collector = new GarbageCollector(strategy);

			List<Tuple2<String, Long>> unusedObjects = collector.detectUnusedObjects();

			if (options.getDelete()) {
				repos = new WritingRepository(strategy);
				logger.info("Removing unused objects...");

				int removed = 0;
				int notRemoved = 0;
				long removedBytes = 0;
				long notRemovedBytes = 0;

				for (Tuple2<String, Long> tupel : unusedObjects) {
					String id = tupel.get1();
					Long size = tupel.get2();

					if (repos.remove(id)) {
						removed++;
						removedBytes += size;
						logger.debug("Removed {}", id);
					} else {
						notRemoved++;
						notRemovedBytes += size;
						logger.warn("Could not remove {}", id);
					}
				}

				logger.info("{} object(s) with a total size of {} removed", removed, formateBytes(removedBytes, 2));
				if (notRemoved > 0) {
					logger.warn("{} object(s) with a total size of {} could not be removed", formateBytes(notRemovedBytes, 2));
				}

			} else {
				long bytes = 0;

				for (Tuple2<String, Long> tupel : unusedObjects) {
					bytes += tupel.get2();
				}

				logger.info("To remove objects, call with '--delete'");
			}

		} catch (Exception e) {
			logger.error("Error: ", e);
		} finally {
			if (repos != null) {
				repos.close();
			}
		}
	}

}
