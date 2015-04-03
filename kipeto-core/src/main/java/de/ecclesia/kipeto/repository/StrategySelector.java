/*
 * #%L
 * Kipeto Core
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
package de.ecclesia.kipeto.repository;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrategySelector {

	private static final Logger logger = LoggerFactory.getLogger(StrategySelector.class);

	public static ReadingRepositoryStrategy getReadingStrategy(String url) {
		return getReadingStrategy(url, null, null);
	}

	public static ReadingRepositoryStrategy getReadingStrategy(String url, File tempDir) {
		return getReadingStrategy(url, null, tempDir);
	}

	public static ReadingRepositoryStrategy getReadingStrategy(String url, AuthenticationProvider authenticationProvider) {
		return getReadingStrategy(url, authenticationProvider, null);
	}

	public static ReadingRepositoryStrategy getReadingStrategy(String url, AuthenticationProvider authenticationProvider, File tempDir) {
		url = url.toLowerCase();
		ReadingRepositoryStrategy repository;

		if (url.startsWith("http:") || url.startsWith("https:")) {
			repository = new HttpRepositoryStrategy(url);
		} else if (url.toLowerCase().startsWith("ssh:")) {
			repository = new SFTPRepositoryStrategy(url, authenticationProvider, tempDir);
		} else {
			repository = new FileRepositoryStrategy(new File(url), tempDir);
		}

		logger.debug("Selecting reading strategy {} for URL {}", repository.getClass().getSimpleName(), url);

		return repository;
	}

	public static WritingRepositoryStrategy getWritingStrategy(String url) {
		return getWritingStrategy(url, null);
	}

	public static WritingRepositoryStrategy getWritingStrategy(String url, File tempDir) {
		return getWritingStrategy(url, null, tempDir);
	}

	public static WritingRepositoryStrategy getWritingStrategy(String url, AuthenticationProvider authenticationProvider, File tempDir) {
		WritingRepositoryStrategy repository;

		if (url.toLowerCase().startsWith("http:")) {
			throw new UnsupportedOperationException("There is no WritingRepositoryStrategy for http-Repositorys");
		} else if (url.toLowerCase().startsWith("ssh:")) {
			repository = new SFTPRepositoryStrategy(url, authenticationProvider, tempDir);
		} else {
			repository = new FileRepositoryStrategy(new File(url), tempDir);
		}

		logger.debug("Selecting writing strategy {} for URL {}", repository.getClass().getSimpleName(), url);

		return repository;
	}
}
