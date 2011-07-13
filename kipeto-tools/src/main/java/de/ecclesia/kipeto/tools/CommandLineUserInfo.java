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

import java.io.Console;

import com.jcraft.jsch.UserInfo;

public class CommandLineUserInfo implements UserInfo {

	private String passwordOrPassphrase;

	public String getPassphrase() {
		return passwordOrPassphrase;
	}

	public String getPassword() {
		return passwordOrPassphrase;
	}

	public boolean promptPassphrase(String message) {
		return promptPassword(message);
	}

	public boolean promptPassword(String message) {
		Console console = System.console();
		char password[] = console.readPassword("Enter password: ");

		passwordOrPassphrase = new String(password);
		return true;
	}

	public boolean promptYesNo(String message) {
		return true;
	}

	public void showMessage(String message) {
		System.out.println(message);
	}

}
