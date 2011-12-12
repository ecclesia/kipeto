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
package de.ecclesia.kipeto.tools.cpref;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import de.ecclesia.kipeto.common.util.BaseOptions;

public class CopyReferenceOptions extends BaseOptions {

	@Option(name = "-s", aliases = { "--source" }, usage = "Reference to copy", metaVar = "VAR")
	private String source;

	@Option(name = "-d", aliases = { "--destination" }, usage = "Reference to create", metaVar = "VAR")
	private String destination;

	public CopyReferenceOptions() {
	}

	public CopyReferenceOptions(String[] args) {
		super(args);
	}

	public String getDestination() {
		return destination;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	@Override
	protected void checkRequiredArguments() throws CmdLineException {
		super.checkRequiredArguments();
		checkRequiredArgument(CopyReferenceOptions.class, "source");
		checkRequiredArgument(CopyReferenceOptions.class, "destination");
	}
	
}
