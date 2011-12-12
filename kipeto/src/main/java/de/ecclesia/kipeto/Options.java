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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import de.ecclesia.kipeto.common.util.BaseOptions;

public class Options extends BaseOptions {

	@Option(name = "-d", aliases = { "--data" }, usage = "Local data directory like 'C:/temp/kipeto", metaVar = "DIR")
	protected String data;

	@Option(name = "-b", aliases = { "--blueprint" }, usage = "Blueprint reference name", metaVar = "REF")
	protected String blueprint;

	@Option(name = "-t", aliases = { "--target" }, usage = "Local directory to update like 'C:/Programme/Anwendung'", metaVar = "DIR")
	protected String target;

	@Option(name = "-g", aliases = { "--gui" }, usage = "Display output in fancy GUI")
	protected boolean gui;

	@Option(name = "-c", aliases = { "--call" }, usage = "Run after update")
	protected String afterUpdate;

	@Option(name = "-o", aliases = { "--suppressOfflineErrorMsg" }, usage = "Suppress Offline Error Message")
	protected boolean suppressOfflineErrorMsg;
	
	@Option(name = "-s", aliases = { "--static-repository" }, usage = "Disable dynamic repository lookup")
	protected boolean useStaticRepository;
	
	@Option(name = "-nsu", aliases = { "--no-self-update" }, usage = "no self Update")
	protected boolean noSelfUpdate;

	public Options() {
	}
	
	public Options(String[] args) {
		super(args);
	}
	
	@Override
	protected void checkRequiredArguments() throws CmdLineException {
		super.checkRequiredArguments();
		checkRequiredArgument(Options.class, "data");
		checkRequiredArgument(Options.class, "blueprint");
		checkRequiredArgument(Options.class, "target");
	}
	
	@Override
	protected void checkRequiredArgument(Class<? extends BaseOptions> optionClass, String fieldName) throws CmdLineException {
		super.checkRequiredArgument(optionClass, fieldName);
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

	public boolean noSelfUpdate() {
		return noSelfUpdate;
	}
	
	public boolean isGui() {
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
