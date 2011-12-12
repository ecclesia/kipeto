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
package de.ecclesia.kipeto.tools.deploy;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import de.ecclesia.kipeto.common.util.BaseOptions;

public class DeployBlueprintOptions extends BaseOptions {

	@Option(name = "-d", aliases = { "--data" }, usage = "Local data directory like 'C:/temp/kipeto", metaVar = "DIR")
	private String dataDir;

	@Option(name = "-b", aliases = { "--blueprint" }, usage = "Blueprint reference name", metaVar = "REF")
	private String blueprint;

	private boolean timestampSuffix = true;

	private boolean skipExistingBlueprints = true;

	public DeployBlueprintOptions() {
	}

	public DeployBlueprintOptions(String[] args) {
		super(args);
	}

	public String getDataDir() {
		return dataDir;
	}

	public String getBlueprint() {
		return blueprint;
	}

	public void setDataDir(String data) {
		this.dataDir = data;
	}

	public void setBlueprint(String blueprint) {
		this.blueprint = blueprint;
	}

	public boolean isSkipExistingBlueprints() {
		return skipExistingBlueprints;
	}

	public void setSkipExistingBlueprints(boolean skipExistingBlueprints) {
		this.skipExistingBlueprints = skipExistingBlueprints;
	}

	public void setTimestampSuffix(boolean timestampSuffix) {
		this.timestampSuffix = timestampSuffix;
	}

	public boolean isTimestampSuffix() {
		return timestampSuffix;
	}

	@Override
	protected void checkRequiredArguments() throws CmdLineException {
		super.checkRequiredArguments();
		checkRequiredArgument(DeployBlueprintOptions.class, "data");
		checkRequiredArgument(DeployBlueprintOptions.class, "blueprint");
	}
	
}
