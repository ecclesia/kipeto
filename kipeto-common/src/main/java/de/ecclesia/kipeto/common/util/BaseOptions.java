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
import org.kohsuke.args4j.Option;

public class BaseOptions {

	@Option(name = "-l", aliases = { "--logLevel" }, required = false, usage = "Log Level")
	private String logLevel;

	@Option(name = "-r", aliases = { "--repository" }, required = true, usage = "Remote-Repository URL like 'ssh://user@updates01.ecclesia:/srv/www/htdocs/repos", metaVar = "URL")
	private String repositoryUrl;
	
	@Option(name = "-p", aliases = { "--password" }, required = false, usage = "Password")
	private String password;

	@Option(name = "-k", aliases = { "--privateKeyFile" }, required = false, usage = "Private Key File", metaVar = "URL")
	private String privateKey;
	
	public BaseOptions() {
	}

	public BaseOptions(String[] args) {
		parse(args);
	}

	public String getLogLevel() {
		return logLevel;
	}

	public String getPassword() {
		return password;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}
	
	
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}
	
	private void parse(String[] args) {
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
