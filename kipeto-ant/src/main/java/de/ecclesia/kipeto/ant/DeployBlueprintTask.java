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

import java.io.File;

import org.apache.tools.ant.BuildException;

import de.ecclesia.kipeto.tools.deploy.DeployBlueprintApp;
import de.ecclesia.kipeto.tools.deploy.DeployBlueprintOptions;

/**
 * @author Daniel Hintze
 * @since 20.08.2010
 */
public class DeployBlueprintTask extends AbstractTask {

	private DeployBlueprintOptions options;

	public DeployBlueprintTask() {
		options = new DeployBlueprintOptions();
	}

	@Override
	public void execute() throws BuildException {
		assertNotNull("blueprint", options.getBlueprint());
		assertNotNull("repositoryUrl", options.getRepositoryUrl());
		assertNotNull("dataDir", options.getDataDir());

		new DeployBlueprintApp(options).run();
	}

	public String getBlueprint() {
		return options.getBlueprint();
	}

	public File getDataDir() {
		return new File(options.getDataDir());
	}

	public String getLogLevel() {
		return options.getLogLevel();
	}

	public String getPassword() {
		return options.getPassword();
	}

	public String getRepositoryUrl() {
		return options.getRepositoryUrl();
	}

	public boolean getSkipExistingBlueprints() {
		return options.isSkipExistingBlueprints();
	}

	public boolean getTimestampSuffix() {
		return options.isTimestampSuffix();
	}

	public void setBlueprint(String blueprint) {
		options.setBlueprint(blueprint);
	}

	public void setDataDir(File data) {
		options.setDataDir(data.getAbsolutePath());
	}

	public void setLogLevel(String logLevel) {
		options.setLogLevel(logLevel);
	}

	public void setPassword(String password) {
		options.setPassword(password);
	}

	public void setRepositoryUrl(String repositoryUrl) {
		options.setRepositoryUrl(repositoryUrl);
	}

	public void setSkipExistingBlueprints(boolean skipExistingBlueprints) {
		options.setSkipExistingBlueprints(skipExistingBlueprints);
	}

	public void setTimestampSuffix(boolean timestampSuffix) {
		options.setTimestampSuffix(timestampSuffix);
	}

	public String getPrivateKey() {
		return options.getPrivateKey();
	}

	public void setPrivateKey(String privateKey) {
		options.setPrivateKey(privateKey);
	}

}
