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
package de.ecclesia.kipeto.tools.meta;

import org.kohsuke.args4j.Option;

import de.ecclesia.kipeto.tools.ToolOptions;

public class UpdateMetadataOptions extends ToolOptions {

	@Option(name = "-b", aliases = { "--blueprint" }, required = true, usage = "Blueprint reference name", metaVar = "REF")
	private String blueprint;

	@Option(name = "-t", aliases = { "--target" }, required = true, usage = "Local directory to adjust like 'C:/Programme/Anwendung'", metaVar = "DIR")
	private String target;

	public UpdateMetadataOptions() {
	}

	public UpdateMetadataOptions(String[] args) {
		parse(args);
	}

	public String getBlueprint() {
		return blueprint;
	}

	public String getTarget() {
		return target;
	}

	public void setBlueprint(String blueprint) {
		this.blueprint = blueprint;
	}

	public void setTarget(String target) {
		this.target = target;
	}

}
