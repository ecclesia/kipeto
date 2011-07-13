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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class BootOptions {

	@Option(name = "-r", aliases = { "--repository" }, required = true, usage = "Remote-Repository URL like 'http://hostname/repos", metaVar = "URL")
	private String repository;

	@Option(name = "-d", aliases = { "--data" }, required = true, usage = "Local data directory like 'C:/temp/kipeto", metaVar = "DIR")
	private String data;

	@Option(name = "-g", aliases = { "--gui" }, required = false, usage = "Display output in fancy GUI")
	private Boolean gui = Boolean.FALSE;

	@Option(name = "-l", aliases = { "--debug-level" }, required = false, usage = "Debug Level ")
	private String debugLevel;

	@Option(name = "-nsu", aliases = { "--no-self-update" }, required = false, usage = "no self Update")
	private Boolean noSelfUpdate = Boolean.FALSE;

	private static final String[] optionWhiteList = { "-r", "-d", "-g", "-l", "-nsu", "--repository", "--data",
			"--gui", "--debug-level", "--no-self-update" };

	public BootOptions() {
	}

	public BootOptions(String[] args) {
		parse(args);
	}

	public String getRepository() {
		return repository;
	}

	public String getData() {
		return data;
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

	public void parse(String[] args) {
		String[] preProcessedArgs = preProcessArguments(args);

		CmdLineParser parser = new CmdLineParser(this);

		// if you have a wider console, you could increase the value;
		// here 80 is also the default
		parser.setUsageWidth(130);

		try {
			// parse the arguments.
			parser.parseArgument(preProcessedArgs);

		} catch (CmdLineException e) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			// System.err.println("java -jar " + jarName() +
			// " [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			// System.err.println("  Example: java -jar " + jarName() + " " +
			// parser.printExample(ALL));

			System.exit(1);
		}
	}

	private String[] preProcessArguments(String[] args) {
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
