/*
 * #%L
 * Kipeto Core
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
package de.ecclesia.kipeto.repository;

import java.io.File;

import com.jcraft.jsch.UserInfo;

public class AuthenticationProvider {

	private UserInfo userInfo;
	private String username;
	private String password;
	private File privateKey;

	public AuthenticationProvider(String username, UserInfo userInfo) {
		this.username = username;
		this.userInfo = userInfo;
	}

	public AuthenticationProvider(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public AuthenticationProvider(String username, File privateKey) {
		this.username = username;
		this.privateKey = privateKey;
	}

	public boolean isUserInfo() {
		return userInfo != null;
	}

	public boolean isPassword() {
		return password != null;
	}

	public boolean isPrivateKey() {
		return privateKey != null;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public File getPrivateKey() {
		return privateKey;
	}

}
