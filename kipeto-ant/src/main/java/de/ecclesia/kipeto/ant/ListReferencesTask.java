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

import de.ecclesia.kipeto.tools.lsref.ListReferencesApp;
import de.ecclesia.kipeto.tools.lsref.ListReferencesOptions;

public class ListReferencesTask extends AbstractTask {

	private ListReferencesOptions options;

	public ListReferencesTask() {
		options = new ListReferencesOptions();
	}

	@Override
	public void execute() throws BuildException {
		assertNotNull("repositoryUrl", getRepositoryUrl());

		new ListReferencesApp(options).run();
	}

	public String getLogLevel() {
		return options.getLogLevel();
	}

	public String getPassword() {
		return options.getPassword();
	}

	public String getPrivateKey() {
		return options.getPrivateKey();
	}

	public String getRepositoryUrl() {
		return options.getRepositoryUrl();
	}

	public void setLogLevel(String logLevel) {
		options.setLogLevel(logLevel);
	}

	public void setPassword(String password) {
		options.setPassword(password);
	}

	public void setPrivateKey(String privateKey) {
		options.setPrivateKey(privateKey);
	}

	public void setRepositoryUrl(String repositoryUrl) {
		options.setRepositoryUrl(repositoryUrl);
	}



}
