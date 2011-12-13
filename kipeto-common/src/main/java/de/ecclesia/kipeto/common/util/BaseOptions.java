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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class BaseOptions {

	@Option(name = "-l", aliases = { "--logLevel" }, usage = "Log Level")
	protected String logLevel;

	@Option(name = "-r", aliases = { "--repository" }, usage = "Remote-Repository URL like 'ssh://user@updates01.ecclesia:/srv/www/htdocs/repos", metaVar = "URL")
	protected String repositoryUrl;

	@Option(name = "-p", aliases = { "--password" }, usage = "Password")
	protected String password;

	@Option(name = "-k", aliases = { "--privateKeyFile" }, usage = "Private Key File", metaVar = "URL")
	protected String privateKey;

	@Option(name = "-pf", aliases = { "--parmFile" }, usage = "File which contains command line parameters")
	protected File parameterFile;

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

	public File getParameterFile() {
		return parameterFile;
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

	protected void parse(String[] args) {
		String[] preProcessedArgs = preProcessArguments(args);

		CmdLineParser parser = new CmdLineParser(this);

		// if you have a wider console, you could increase the value;
		// here 80 is also the default
		parser.setUsageWidth(130);

		try {
			// parse the arguments.
			parser.parseArgument(preProcessedArgs);
			String[] pfArgs = getParameterFileArguments(parameterFile);
			parser.parseArgument(pfArgs);
			checkRequiredArguments();

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

	public String[] getParameterFileArguments(File parameterFile) {
		if (parameterFile == null) return new String[0];
		try {
			String[] parameterFileArguments = unsafeGetParameterFileArguments(parameterFile);
			return preProcessArguments(parameterFileArguments);
		} catch (IOException e) {
			return new String[0];
		}
	}

	private String[] unsafeGetParameterFileArguments(File parameterFile) throws IOException {
		Properties properties = new Properties();
		FileInputStream parmFileStream = new FileInputStream(parameterFile);
		properties.load(parmFileStream);
		parmFileStream.close();

		ArrayList<String> parmList = new ArrayList<String>();
		for (String key : properties.stringPropertyNames()) {
			parmList.add(key);
			String property = properties.getProperty(key);
			if (property != null && !property.isEmpty()) parmList.add(property);
		}
		String[] newArgs = parmList.toArray(new String[parmList.size()]);
		return newArgs;
	}
	
	protected void checkRequiredArguments() throws CmdLineException {
		checkRequiredArgument(BaseOptions.class, "repositoryUrl");
	}

	protected void checkRequiredArgument(Class<? extends BaseOptions> optionClass, String fieldName) throws CmdLineException {
		try {
			Field field = optionClass.getDeclaredField(fieldName);
			try {
				field.setAccessible(true);
				Option option = field.getAnnotation(Option.class);
				Object value = field.get(this);
				if (value != null) return;
				String optionName = option.name();
				throw new CmdLineException("Option must be set: " + optionName);
			} finally {
				field.setAccessible(false);
			}
		} catch (SecurityException e) {
			throw new RuntimeException("Field access failed. This is a bug!", e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Field access failed. This is a bug!", e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Field access failed. This is a bug!", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Field access failed. This is a bug!", e);
		}
	}

	protected String[] preProcessArguments(String[] argsArray) {
		List<String> args = new ArrayList<String>();
		args.addAll(Arrays.asList(argsArray));
		args.remove("-console");
		return args.toArray(new String[args.size()]);
	}

}
