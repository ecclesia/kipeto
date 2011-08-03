/*
 * #%L
 * Kipeto
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
package de.ecclesia.kipeto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryResolver {

	private static final Logger log = LoggerFactory.getLogger(RepositoryResolver.class);

	/** Name des Distribution-Verzeichnisses im Repository */
	private static final String DIST_DIR = "dist";

	private static final String RESOLVE_CONFIG_FILE = "repos_resolve.properties";

	private final String defaultRepositoryUrl;

	public RepositoryResolver(String defaultRepositoryUrl) {
		this.defaultRepositoryUrl = defaultRepositoryUrl;
	}

	/**
	 * Versucht, im übergebenen Repository die Konfigurations-Datei zu finden
	 * und daraus ein passendes Repository abzuleiten. Schlägt dies fehlt oder
	 * tritt ein Fehler auf, wird dieser Fehler gelogt, und das übergebene
	 * Repository zurückgegeben.
	 * 
	 * @return
	 * @throws IOException
	 */
	public String resolveReposUrl() throws IOException {
		// Zur Zeit nur für HTTP implementiert.
		try {
			URL url;

			// TODO: Schön machen
			try {
				url = new URL(defaultRepositoryUrl);
			} catch (MalformedURLException e) {
				url = new File(defaultRepositoryUrl).toURI().toURL();
			}
			if (!url.getProtocol().equals("http")) {
				log.info("Resolving repository-config not implemented for protocol {} yet", url.getProtocol());
				return defaultRepositoryUrl;
			}

			Properties config = loadConfig();

			if (config == null) {
				return defaultRepositoryUrl;
			}

			String localIp = determinateLocalIP();

			return resolveRepos(localIp, config);
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			return defaultRepositoryUrl;
		}
	}

	/**
	 * Ermittelt anhand der Default-Repository-URL die IP-Adresse der
	 * Netzwerkkarte, die Verbindung zum Repository hat.
	 */
	private String determinateLocalIP() throws IOException {
		Socket socket = null;

		try {
			URL url = new URL(defaultRepositoryUrl);
			int port = url.getPort() > -1 ? url.getPort() : url.getDefaultPort();

			log.debug("Determinating local IP-Adress by connect to {}:{}", defaultRepositoryUrl, port);
			InetAddress address = Inet4Address.getByName(url.getHost());

			socket = new Socket();
			socket.connect(new InetSocketAddress(address, port), 3000);
			InputStream stream = socket.getInputStream();
			InetAddress localAddress = socket.getLocalAddress();
			stream.close();

			String localIp = localAddress.getHostAddress();

			log.info("Local IP-Adress is {}", localIp);

			return localIp;
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}

	/**
	 * Läd die Config vom Default-Repository herunter.
	 */
	private Properties loadConfig() throws IOException {
		String configUrl = String.format("%s/%s/%s", defaultRepositoryUrl, DIST_DIR, RESOLVE_CONFIG_FILE);
		log.info("Looking for repository-config at {}", configUrl);

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, (int) TimeUnit.SECONDS.toMillis(3));
		HttpConnectionParams.setSoTimeout(httpParams, (int) TimeUnit.SECONDS.toMillis(10));
		HttpClient client = new DefaultHttpClient(httpParams);

		HttpGet httpget = new HttpGet(configUrl);
		HttpResponse contentResponse;
		try {
			contentResponse = client.execute(httpget);
		} catch (ConnectTimeoutException e) {
			throw new ConnectException(e.getMessage());
		}

		int statusCode = contentResponse.getStatusLine().getStatusCode();
		log.debug("HttpGet at {}, Status is {}", configUrl, statusCode);
		if (statusCode == HttpStatus.SC_NOT_FOUND) {
			log.info("No repository-config found at {}", configUrl);

			return null;
		} else if (statusCode != HttpStatus.SC_OK) {
			String msg = "Error loading " + configUrl + ":\n";
			msg += contentResponse.getStatusLine().toString();

			throw new RuntimeException(msg);
		}

		HttpEntity entity = contentResponse.getEntity();
		BufferedInputStream inputStream = new BufferedInputStream(entity.getContent());

		Properties properties = new Properties();
		properties.load(inputStream);

		return properties;
	}

	/**
	 * Ermittelt anhand der lokalen IP-Adresse und der übergebenen
	 * Konfiguration, welches Repository für den Update-Vorgang verwendet werden
	 * soll.
	 */
	private String resolveRepos(String localIp, Properties config) {
		for (Object key : config.keySet()) {
			String ipPraefix = (String) key;
			String repos = config.getProperty(ipPraefix);

			if (localIp.startsWith(ipPraefix)) {
				log.info("Local IP " + localIp + " starts with '{}', selecting [{}]", ipPraefix, repos);
				return repos;
			} else {
				log.debug("Local IP " + localIp + " does not start with '{}' --> {}", ipPraefix, repos);
			}
		}

		log.warn("No matching config-entry found for {}, falling back to default-repository {}", localIp, defaultRepositoryUrl);

		return defaultRepositoryUrl;
	}
}
