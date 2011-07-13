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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.repository.AuthenticationProvider;

public class AuthenticationProviderFactory {

	private static Logger logger = LoggerFactory.getLogger(AuthenticationProviderFactory.class);

	public static AuthenticationProvider getProvider(ToolOptions options) {
		if (options.getPrivateKey() != null) {
			File file = new File(options.getPrivateKey());

			logger.debug("Private Key File is {}", file);
			if (!file.exists()) {
				logger.error("Private Key File {} does not exist", file);
			}

			return new AuthenticationProvider(username(options.getRepositoryUrl()), new File(options.getPrivateKey()));
		} else if (options.getPassword() != null) {
			return new AuthenticationProvider(username(options.getRepositoryUrl()), options.getPassword());
		} else {
			return new AuthenticationProvider(username(options.getRepositoryUrl()), new CommandLineUserInfo());
		}
	}

	private static String username(String url) {
		// ssh://user@server:path
		String[] split = url.split("@");
		if (split.length <= 1) {
			return System.getProperty("user.name");
		}

		String username = split[0].substring(split[0].lastIndexOf("/") + 1);

		logger.debug("Username '{}', Url '{}'", username, url);

		return username;
	}
}
