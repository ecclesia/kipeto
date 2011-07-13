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
package de.ecclesia.kipeto.tools.lsref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.common.util.LoggerConfigurer;
import de.ecclesia.kipeto.common.util.Strings;
import de.ecclesia.kipeto.repository.AuthenticationProvider;
import de.ecclesia.kipeto.repository.ReadingRepository;
import de.ecclesia.kipeto.repository.ReadingRepositoryStrategy;
import de.ecclesia.kipeto.repository.Reference;
import de.ecclesia.kipeto.repository.StrategySelector;
import de.ecclesia.kipeto.tools.AuthenticationProviderFactory;

public class ListReferencesApp {

	private final ListReferencesOptions options;

	Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) throws JSchException, SftpException, IOException {
		ListReferencesOptions options = new ListReferencesOptions(args);

		ListReferencesApp listReferences = new ListReferencesApp(options);
		listReferences.run();
	}

	public ListReferencesApp(ListReferencesOptions options) {
		this.options = options;
	}

	public void run() {
		LoggerConfigurer.configureConsoleAppender(Level.toLevel(options.getLogLevel(), Level.INFO));

		ReadingRepositoryStrategy strategy = null;
		ReadingRepository repository = null;

		try {
			AuthenticationProvider authenticationProvider = AuthenticationProviderFactory.getProvider(options);

			strategy = StrategySelector.getReadingStrategy(options.getRepositoryUrl(), authenticationProvider, null);
			repository = new ReadingRepository(strategy);

			List<Reference> references = strategy.allReferences();
			List<String> descriptions = new ArrayList<String>();
			Collections.sort(references);

			int largestName = "Reference".length();
			int largestDescription = "Description".length();
			for (Reference ref : references) {
				largestName = Math.max(largestName, ref.name().length());

				Blueprint blueprint = repository.retrieve(ref.id(), Blueprint.class);
				String description = blueprint.getDescription();
				largestDescription = Math.max(largestDescription, description.length());
				descriptions.add(blueprint.getDescription());
			}

			// Überschriften
			String name = Strings.padRight("Reference", largestName + 2, " ");
			String description = Strings.padRight("Description", largestDescription + 2, " ");
			String shortId = "Id";
			System.out.println("\n" + name + description + shortId);
			System.out.println(Strings.padRight("", largestName + largestDescription + 19, "-"));

			for (int i = 0; i < references.size(); i++) {
				Reference ref = references.get(i);
				name = Strings.padRight(ref.name(), largestName + 2, " ");
				description = Strings.padRight(descriptions.get(i), largestDescription + 2, " ");
				shortId = ref.id().substring(0, 6) + "..." + ref.id().substring(58);

				System.out.println(name + description + shortId);
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
