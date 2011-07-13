/*
 * #%L
 * Kipeto Common
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
package de.ecclesia.kipeto.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public abstract class AbstractOption {

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

	private String[] preProcessArguments(String[] argsArray) {
		List<String> args = new ArrayList<String>();
		args.addAll(Arrays.asList(argsArray));
		args.remove("-console");
		return args.toArray(new String[args.size()]);
	}

}
