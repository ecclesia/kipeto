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
package de.ecclesia.kipeto.tools;

import org.kohsuke.args4j.Option;

import de.ecclesia.kipeto.common.util.AbstractOption;

public class ToolOptions extends AbstractOption {

	@Option(name = "-l", aliases = { "--logLevel" }, required = false, usage = "Log Level")
	private String logLevel;

	@Option(name = "-r", aliases = { "--repository" }, required = true, usage = "Remote-Repository URL like 'ssh://user@updates01.ecclesia:/srv/www/htdocs/repos", metaVar = "URL")
	private String repositoryUrl;
	
	@Option(name = "-p", aliases = { "--password" }, required = false, usage = "Password", metaVar = "URL")
	private String password;

	@Option(name = "-k", aliases = { "--privateKeyFile" }, required = false, usage = "Private Key File", metaVar = "URL")
	private String privateKey;
	
	public ToolOptions() {
	}

	public ToolOptions(String[] args) {
		parse(args);
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
}
