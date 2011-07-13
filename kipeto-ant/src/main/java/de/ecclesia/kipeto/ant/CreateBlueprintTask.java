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

import de.ecclesia.kipeto.tools.blueprint.CreateBlueprintApp;
import de.ecclesia.kipeto.tools.blueprint.CreateBlueprintOptions;

/**
 * @author Daniel Hintze
 * @since 23.08.2010
 */
public class CreateBlueprintTask extends AbstractTask {

	private CreateBlueprintOptions options;

	public CreateBlueprintTask() {
		options = new CreateBlueprintOptions();
	}

	@Override
	public void execute() throws BuildException {
		assertNotNull("blueprint", options.getBlueprint());
		assertNotNull("dataDir", options.getDataDir());
		assertNotNull("source", options.getSource());
		assertNotNull("blueprintDescription", options.getDescription());

		new CreateBlueprintApp(options).run();
	}

	public String getBlueprint() {
		return options.getBlueprint();
	}

	public String getBlueprintDescription() {
		return options.getDescription();
	}

	public File getDataDir() {
		return new File(options.getDataDir());
	}

	public String getIcon() {
		return options.getIcon();
	}

	public String getLogLevel() {
		return options.getLogLevel();
	}

	public File getSource() {
		return new File(options.getSource());
	}

	public int hashCode() {
		return options.hashCode();
	}

	public void setBlueprint(String blueprint) {
		options.setBlueprint(blueprint);
	}

	public void setBlueprintDescription(String description) {
		options.setDescription(description);
	}

	public void setDataDir(File data) {
		options.setDataDir(data.getAbsolutePath());
	}

	public void setIcon(String icon) {
		options.setIcon(icon);
	}

	public void setLogLevel(String logLevel) {
		options.setLogLevel(logLevel);
	}

	public void setSource(File source) {
		options.setSource(source.getAbsolutePath());
	}

}
