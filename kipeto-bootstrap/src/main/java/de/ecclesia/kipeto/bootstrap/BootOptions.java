/*
 * #%L
 * Kipeto Bootstrapper
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
package de.ecclesia.kipeto.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kohsuke.args4j.Option;

import de.ecclesia.kipeto.common.util.BaseOptions;

public class BootOptions extends BaseOptions {

	@Option(name = "-d", aliases = { "--data" }, usage = "Local data directory like 'C:/temp/kipeto", metaVar = "DIR")
	private String data;

	@Option(name = "-g", aliases = { "--gui" }, usage = "Display output in fancy GUI")
	private boolean gui;

	@Option(name = "-nsu", aliases = { "--no-self-update" }, usage = "no self Update")
	private boolean noSelfUpdate;

	private static final String[] optionWhiteList = { "-r", "-d", "-g", "-l", "-nsu", "--repository", "--data",
			"--gui", "--debug-level", "--no-self-update", "-pf", "--parameterFile" };

	public BootOptions() {
	}

	public BootOptions(String[] args) {
		parse(args);
	}

	public String getData() {
		return data;
	}

	public boolean noSelfUpdate() {
		return noSelfUpdate;
	}

	public boolean isGui() {
		return gui;
	}

	protected String[] preProcessArguments(String[] args) {
		args = super.preProcessArguments(args);
		
		List<String> cleanArgs = new ArrayList<String>();

		List<String> whiteList = new ArrayList<String>();

		whiteList.addAll(Arrays.asList(optionWhiteList));

		for (int i = 0; i < args.length; i++) {
			if ((whiteList.contains(args[i]))) {
				cleanArgs.add(args[i]);

				if (i < args.length - 1 && !args[i + 1].startsWith("-")) {
					cleanArgs.add(args[i + 1]);
				}
			}
		}

		return cleanArgs.toArray(new String[cleanArgs.size()]);
	}
}
