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

import de.ecclesia.kipeto.tools.meta.UpdateMetadataApp;
import de.ecclesia.kipeto.tools.meta.UpdateMetadataOptions;

/**
 * @author Daniel Hintze
 * @since 20.08.2010
 */
public class UpdateMetadataTask extends AbstractTask {

	
	public static void main(String[] args) {
		UpdateMetadataTask task = new UpdateMetadataTask();
		task.setBlueprint("gibtsnich");
		task.setRepositoryUrl("http://updates01.ecclesia/repos");
		task.setTargetDir(new File("C:/dev/temp"));
		
		task.execute();
	}
	
	private UpdateMetadataOptions options;

	public UpdateMetadataTask() {
		options = new UpdateMetadataOptions();
	}

	@Override
	public void execute() throws BuildException {
		assertNotNull("blueprint", options.getBlueprint());
		assertNotNull("repositoryUrl", options.getRepositoryUrl());
		assertNotNull("targetDir", options.getTarget());

		new UpdateMetadataApp(options).run();
	}

	public File getTargetDir() {
		return new File(options.getTarget());
	}

	public void setTargetDir(File target) {
		options.setTarget(target.getAbsolutePath());
	}

	public String getBlueprint() {
		return options.getBlueprint();
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

	public void setBlueprint(String blueprint) {
		options.setBlueprint(blueprint);
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
