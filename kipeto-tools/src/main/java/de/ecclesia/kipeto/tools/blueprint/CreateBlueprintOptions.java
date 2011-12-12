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
package de.ecclesia.kipeto.tools.blueprint;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import de.ecclesia.kipeto.common.util.BaseOptions;

public class CreateBlueprintOptions extends BaseOptions {

	@Option(name = "-d", aliases = { "--data" }, usage = "Local data directory like 'C:/temp/kipeto", metaVar = "DIR")
	private String data;

	@Option(name = "-s", aliases = { "--source" }, usage = "Local directory to make Blueprint from 'C:/Programme/Anwendung'", metaVar = "DIR")
	private String source;

	@Option(name = "-b", aliases = { "--blueprint" }, usage = "Blueprint reference name", metaVar = "REF")
	private String blueprint;

	@Option(name = "-n", aliases = { "--description" }, usage = "Blueprint description")
	private String description;

	@Option(name = "-i", aliases = { "--icon" }, usage = "Blueprint icon")
	private String icon;

	public CreateBlueprintOptions(String[] args) throws IOException {
		super(args);
	}

	public CreateBlueprintOptions() {
	}

	public String getDataDir() {
		return data;
	}

	public String getSource() {
		return source;
	}

	public String getBlueprint() {
		return blueprint;
	}

	public String getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}

	public void setDataDir(String data) {
		this.data = data;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setBlueprint(String blueprint) {
		this.blueprint = blueprint;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	protected void checkRequiredArguments() throws CmdLineException {
		super.checkRequiredArguments();
		checkRequiredArgument(CreateBlueprintOptions.class, "data");
		checkRequiredArgument(CreateBlueprintOptions.class, "source");
		checkRequiredArgument(CreateBlueprintOptions.class, "blueprint");
		checkRequiredArgument(CreateBlueprintOptions.class, "description");
	}
	
}
