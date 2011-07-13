/*
 * #%L
 * Kipeto Ant Tasks
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
package de.ecclesia.kipeto.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author Daniel Hintze
 * @since 20.08.2010
 */
public abstract class AbstractTask extends Task {

	@Override
	public abstract void execute() throws BuildException;

	protected void assertNotNull(String name, Object object) {
		if (object == null) {
			throw new BuildException(name + " must be specified");
		}
	}

}
