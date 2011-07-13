/*
 * #%L
 * Kipeto Ant Tasks
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
package de.ecclesia.kipeto.ant;

import org.apache.tools.ant.BuildException;

import de.ecclesia.kipeto.tools.gbc.CollectGarbageApp;
import de.ecclesia.kipeto.tools.gbc.CollectGarbageOptions;

/**
 * @author Daniel Hintze
 * @since 23.08.2010
 */
public class CollectGarbageTask extends AbstractTask {

	private CollectGarbageOptions options;

	public CollectGarbageTask() {
		options = new CollectGarbageOptions();
	}

	@Override
	public void execute() throws BuildException {
		assertNotNull("repositoryUrl", options.getRepositoryUrl());

		new CollectGarbageApp(options).run();
	}

	public boolean getDelete() {
		return options.getDelete();
	}

	public String getPassword() {
		return options.getPassword();
	}

	public String getPrivateKey() {
		return options.getPrivateKey();
	}

	public void setPassword(String password) {
		options.setPassword(password);
	}

	public void setPrivateKey(String privateKey) {
		options.setPrivateKey(privateKey);
	}

	public String getLogLevel() {
		return options.getLogLevel();
	}

	public String getRepositoryUrl() {
		return options.getRepositoryUrl();
	}

	public void setDelete(boolean delete) {
		options.setDelete(delete);
	}

	public void setLogLevel(String logLevel) {
		options.setLogLevel(logLevel);
	}

	public void setRepositoryUrl(String repository) {
		options.setRepositoryUrl(repository);
	}

}
