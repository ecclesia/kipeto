/*
 * #%L
 * Kipeto Bootstrapper
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
package de.ecclesia.kipeto.bootstrap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

import javax.swing.JFrame;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.common.AWTExceptionErrorDialog;
import de.ecclesia.kipeto.common.util.ByteTransferEvent;
import de.ecclesia.kipeto.common.util.ByteTransferListener;
import de.ecclesia.kipeto.common.util.CountingInputStream;
import de.ecclesia.kipeto.common.util.FileSizeFormatter;
import de.ecclesia.kipeto.common.util.LoggerConfigurer;
import de.ecclesia.kipeto.common.util.Streams;

/**
 * @author Daniel Hintze
 * @since 15.02.2010
 */
public class BootstrapApp {

	public static final String TEMP_DIR = "temp";

	public static final String JAR_FILENAME = "kipeto.jar";

	public static final String DIST_DIR = "dist";

	private static final long MS_TO_WAIT_UNTIL_WINDOW = 1500;

	private static final String KIPETO_CLASS = "de.ecclesia.kipeto.KipetoApp";

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(BootstrapApp.class);

	private final String[] args;

	private final BootOptions options;

	private File tempDir;

	private File jar;

	private HttpClient client;

	private BootstrapWindow window;

	private String updateUrl;

	private long contentLength;

	private FileAppender appender;

	public static void main(String[] args) throws Exception {
		BootstrapApp bootstrapper = new BootstrapApp(args);

		if (!bootstrapper.options.noSelfUpdate()) {
			try {
				bootstrapper.checkForUpdate();
			} catch (Exception e) {
				bootstrapper.logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}

		try {
			bootstrapper.launchKipeto();
		} catch (Exception e) {
			bootstrapper.logger.error(e.getMessage(), e);

			if (bootstrapper.options.isGui()) {
				AWTExceptionErrorDialog errorDialog = new AWTExceptionErrorDialog(new JFrame(), e);
				errorDialog.setVisible(true);
			} else {
				e.printStackTrace();
			}
		}

		System.exit(0);
	}

	private void checkForUpdate() throws Exception {
		if (!options.getRepository().toLowerCase().startsWith("http")) {
			return;
		}

		updateUrl = String.format("%s/%s/%s", options.getRepository(), DIST_DIR, JAR_FILENAME);
		logger.info("Looking for new Kipeto Jar at {}", updateUrl);

		client = new DefaultHttpClient();
		window = new BootstrapWindow();

		new WindowThread(window).start();

		window.label.setText("Connecting to repository " + options.getRepository());
		HttpHead httpHead = new HttpHead(updateUrl);
		HttpResponse headResponse = client.execute(httpHead);
		int statusCode = headResponse.getStatusLine().getStatusCode();
		logger.debug("HttpHead at {}, Status is {}", updateUrl, statusCode);
		if (statusCode != HttpStatus.SC_OK) {
			throw new RuntimeException(headResponse.getStatusLine().toString());
		}

		Header lastModifiedHeader = headResponse.getFirstHeader("Last-Modified");
		Date lastModified = DateUtils.parseDate(lastModifiedHeader.getValue());
		logger.debug("HttpHead {} - Last-Modified: {}", updateUrl, lastModified);

		Header contentLengthHeader = headResponse.getFirstHeader("Content-Length");
		contentLength = Long.parseLong(contentLengthHeader.getValue());
		logger.debug("HttpHead {} - Content-Length: {}", updateUrl, contentLength);

		logger.debug("Local Kipeto Jar {} - Last-Modified: {}", jar, new Date(jar.lastModified()));
		logger.debug("Local Kipeto Jar {} - Content-Length: {}", jar, jar.length());

		if (!jar.exists() || lastModified.getTime() != jar.lastModified() || contentLength != jar.length()) {
			logger.info("Update found for {}", jar);
			update();
		} else {
			logger.info("No update found for {}", jar);
		}

		window.setEnabled(false);
		window.dispose();
	}

	private void launchKipeto() throws Exception {
		logger.debug("Launching {}", jar);

		if (!jar.exists()) {
			throw new FileNotFoundException("File not found: " + jar.getAbsolutePath());
		}

		URL[] urls = new URL[] { new URL("file:" + jar.getAbsolutePath()) };

		ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();

		ClassLoader classLoader = new URLClassLoader(urls, bootstrapClassLoader);

		Class<?> loadClass = classLoader.loadClass(KIPETO_CLASS);
		Method method = loadClass.getMethod("main", new Class[] { String[].class });

		Logger.getRootLogger().removeAppender(appender);
		method.invoke(null, new Object[] { args });
		Logger.getRootLogger().addAppender(appender);
	}

	public BootstrapApp(String[] args) throws FileNotFoundException {
		this.args = args;
		this.options = new BootOptions(args);

		appender = LoggerConfigurer.configureFileAppender(options.getData(), "bootstrapper");
		LoggerConfigurer.configureConsoleAppender(Level.toLevel(options.getDebugLevel(), Level.INFO));

		logger.debug("Options: {}", options);

		File rootDir = new File(BootstrapApp.class.getProtectionDomain().getCodeSource().getLocation().getPath())
				.getParentFile();
		logger.debug("RootDir: {}", rootDir);

		File data = new File(options.getData());
		logger.debug("DataDir: {}", data);

		tempDir = new File(data, TEMP_DIR);
		logger.debug("TempDir: {}", tempDir);

		jar = new File(data, JAR_FILENAME);
		logger.debug("Kipeto-Jar: {}", jar);

		tempDir.mkdirs();
	}

	private void update() throws DateParseException, IOException {
		window.setVisible(true);

		HttpGet httpget = new HttpGet(updateUrl);
		HttpResponse contentResponse = client.execute(httpget);
		HttpEntity entity = contentResponse.getEntity();

		Header lastModifiedHeader = contentResponse.getFirstHeader("Last-Modified");
		Date lastModified = DateUtils.parseDate(lastModifiedHeader.getValue());

		int statusCode = contentResponse.getStatusLine().getStatusCode();
		logger.debug("HttpGet at {}, Status is {}", updateUrl, statusCode);
		if (statusCode != HttpStatus.SC_OK) {
			throw new RuntimeException(contentResponse.getStatusLine().toString());
		}

		File tempDir = new File(options.getData(), TEMP_DIR);
		if (!tempDir.exists()) {
			if (!tempDir.mkdirs()) {
				throw new RuntimeException("Could not create <" + tempDir + ">");
			}
		}

		File tempFile = File.createTempFile(getClass().getName(), ".jar", tempDir);
		logger.debug("Downloading {} to {}", updateUrl, tempFile);

		CountingInputStream inputStream = new CountingInputStream(new BufferedInputStream(entity.getContent()));

		inputStream.addByteTransferListener(new ProgressListener());

		Streams.copyStream(inputStream, new FileOutputStream(tempFile), true);

		if (jar.exists()) {
			logger.debug("Deleting {}", jar);
			if (!jar.delete()) {
				throw new RuntimeException("Could not delete <" + jar + ">");
			}
		}

		logger.debug("Moving {} to {}", tempFile, jar);
		tempFile.setLastModified(lastModified.getTime());
		if (!tempFile.renameTo(jar)) {
			throw new RuntimeException("Could not rename <" + tempFile + "> to <" + jar + ">");
		}
	}

	private final class ProgressListener implements ByteTransferListener {
		public void handleByteTransfer(ByteTransferEvent event) {
			window.progressBar.setMaximum((int) contentLength);
			window.progressBar.setValue(((int) event.getBytesSinceBeginOfOperation()));

			String progress = FileSizeFormatter.formateBytes(event.getBytesSinceBeginOfOperation(), 2);
			String total = FileSizeFormatter.formateBytes(contentLength, 2);

			window.label.setText(String.format("Downloading %s (%s von %s)", updateUrl, progress, total));
		}
	}

	private class WindowThread extends Thread {
		public WindowThread(final JFrame window) {
			super(new Runnable() {

				public void run() {
					try {
						Thread.sleep(MS_TO_WAIT_UNTIL_WINDOW);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (window.isEnabled()) {
						window.setVisible(true);
					} else {
					}
				}
			});
		}
	}
}
