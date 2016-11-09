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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.exception.ConnectException;

/**
 * @author Daniel Hintze
 * @since 01.02.2010
 */
public class HttpRepositoryStrategy extends ReadingRepositoryStrategy {

	private final Logger logger = LoggerFactory.getLogger(HttpRepositoryStrategy.class);

	private final CloseableHttpClient client;
	private final String repository;

	public HttpRepositoryStrategy(String repository) {
		this.repository = repository;

		client = HttpClients.createDefault();
	}

	@Override
	public String resolveReference(String reference) throws IOException {
		StringBuilder builder = new StringBuilder();

		builder.append(repository);
		builder.append("/");
		builder.append(REFERENCE_DIR);
		builder.append("/");
		builder.append(reference);

		HttpGet httpget = new HttpGet(builder.toString());

		CloseableHttpResponse response;
		try {
			response = client.execute(httpget);
		} catch (java.net.ConnectException e) {
			throw new ConnectException(repository, builder.toString(), e);
		} catch (UnknownHostException e) {
			throw new ConnectException(repository, builder.toString(), e);
		} catch (ConnectTimeoutException e) {
			throw new ConnectException(repository, builder.toString(), e);
		}

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			return null;
		}

		HttpEntity entity = response.getEntity();

		BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));

		String id = reader.readLine();

		response.close();
		reader.close();

		return id;
	}

	@Override
	protected InputStream retrieve(String id) throws IOException {
		String url = buildUrlForObject(id);

		logger.debug("Retrieve Stream {} from {}", id, url);

		HttpGet httpget = new HttpGet(url);
		CloseableHttpResponse response = client.execute(httpget);
		HttpEntity entity = response.getEntity();

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_NOT_FOUND) {
			throw new IdNotFoundException(id);
		} else if (statusCode != HttpStatus.SC_OK) {
			throw new RuntimeException(response.getStatusLine().toString());
		}

		return entity.getContent();
	}

	@Override
	public long sizeInRepository(String id) throws IOException {
		HttpHead httpHead = new HttpHead(buildUrlForObject(id));
		CloseableHttpResponse response = client.execute(httpHead);

		Header contentLength = response.getFirstHeader("Content-Length");
		response.close();

		return Long.parseLong(contentLength.getValue());
	}

	private String buildUrlForObject(String id) {
		StringBuilder builder = new StringBuilder();

		builder.append(repository);
		builder.append("/");
		builder.append(OBJECT_DIR);
		builder.append("/");
		builder.append(id.substring(0, SUBDIR_POLICY));
		builder.append("/");
		builder.append(id.substring(SUBDIR_POLICY));

		return builder.toString();
	}

	@Override
	public boolean contains(String id) throws IOException {
		HttpHead httpHead = new HttpHead(buildUrlForObject(id));
		CloseableHttpResponse response = client.execute(httpHead);

		int statusCode = response.getStatusLine().getStatusCode();

		response.close();

		return statusCode == HttpStatus.SC_OK;
	}

	@Override
	public void close() {
		try {
			client.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Reference> allReferences() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> allObjects() {
		throw new UnsupportedOperationException();
	}
}
