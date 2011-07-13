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
package de.ecclesia.kipeto.exception;

public class ConnectException extends KipetoException {

	private static final long serialVersionUID = 1L;

	private final String repository;
	private final String url;

	public ConnectException(String repository, String url, Exception e) {
		super(e.getMessage(), e);

		this.repository = repository;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public String getRepository() {
		return repository;
	}

}
