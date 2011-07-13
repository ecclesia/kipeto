/*
 * #%L
 * Kipeto
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
package de.ecclesia.kipeto;

import org.kohsuke.args4j.Option;

import de.ecclesia.kipeto.common.util.AbstractOption;

public class Options extends AbstractOption {

	@Option(name = "-r", aliases = { "--repository" }, required = true, usage = "Remote-Repository URL like 'http://hostname/repos", metaVar = "URL")
	private String repository;

	@Option(name = "-d", aliases = { "--data" }, required = true, usage = "Local data directory like 'C:/temp/kipeto", metaVar = "DIR")
	private String data;

	@Option(name = "-b", aliases = { "--blueprint" }, required = true, usage = "Blueprint reference name", metaVar = "REF")
	private String blueprint;

	@Option(name = "-t", aliases = { "--target" }, required = true, usage = "Local directory to update like 'C:/Programme/Anwendung'", metaVar = "DIR")
	private String target;

	@Option(name = "-g", aliases = { "--gui" }, required = false, usage = "Display output in fancy GUI")
	private Boolean gui = Boolean.FALSE;

	@Option(name = "-c", aliases = { "--call" }, required = false, usage = "Run after update")
	private String afterUpdate;

	@Option(name = "-l", aliases = { "--debug-level" }, required = false, usage = "Debug Level ")
	private String debugLevel;
	
	@Option(name = "-o", aliases = { "--suppressOfflineErrorMsg" }, required = false, usage = "Suppress Offline Error Message")
	private boolean suppressOfflineErrorMsg;
	
	@Option(name = "-s", aliases = { "--static-repository" }, required = false, usage = "Disable dynamic repository lookup")
	private boolean useStaticRepository;
	
	@Option(name = "-nsu", aliases = { "--no-self-update" }, required = false, usage = "no self Update")
	private Boolean noSelfUpdate = Boolean.FALSE;

	public Options() {
	}
	
	public Options(String[] args) {
		parse(args);
	}

	public String getRepository() {
		return repository;
	}

	public String getData() {
		return data;
	}

	public String getBlueprint() {
		return blueprint;
	}

	public String getTarget() {
		return target;
	}

	public String getDebugLevel() {
		return debugLevel;
	}

	public boolean noSelfUpdate() {
		return noSelfUpdate;
	}
	
	public Boolean isGui() {
		return gui;
	}

	public String getAfterUpdate() {
		return afterUpdate;
	}

	public boolean useStaticRepository() {
		return useStaticRepository;
	}
	
	public boolean isSuppressOfflineErrorMsg() {
		return suppressOfflineErrorMsg;
	}
}
