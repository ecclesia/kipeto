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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

import javax.swing.JFrame;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.common.AWTExceptionErrorDialog;
import de.ecclesia.kipeto.common.util.ByteTransferEvent;
import de.ecclesia.kipeto.common.util.ByteTransferListener;
import de.ecclesia.kipeto.common.util.CountingOutputStream;
import de.ecclesia.kipeto.common.util.FileSizeFormatter;
import de.ecclesia.kipeto.common.util.LoggerConfigurer;

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

	private BootstrapWindow window;

	private long updateLength;

	private FileAppender appender;

	private IUpdateStrategy updateStrategy;

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

	private void launchKipeto() throws Exception {
		logger.debug("Launching {}", jar);

		if (!jar.exists()) {
			throw new FileNotFoundException("File not found: " + jar.getAbsolutePath());
		}

		URL[] urls = new URL[] { new URL("file:" + jar.getAbsolutePath()) };

		ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();

		URLClassLoader classLoader = new URLClassLoader(urls, bootstrapClassLoader);

		Class<?> loadClass = classLoader.loadClass(KIPETO_CLASS);
		Method method = loadClass.getMethod("main", new Class[] { String[].class });

		Logger.getRootLogger().removeAppender(appender);
		method.invoke(null, new Object[] { args });
		Logger.getRootLogger().addAppender(appender);

		classLoader.close();
	}

	public BootstrapApp(String[] args) throws FileNotFoundException {
		this.args = args;
		this.options = new BootOptions(args);

		appender = LoggerConfigurer.configureFileAppender(options.getData(), "bootstrapper");
		LoggerConfigurer.configureConsoleAppender(Level.toLevel(options.getLogLevel(), Level.INFO));

		logger.debug("Options: {}", options);

		File rootDir = new File(BootstrapApp.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
		logger.debug("RootDir: {}", rootDir);

		File data = new File(options.getData());
		logger.debug("DataDir: {}", data);

		tempDir = new File(data, TEMP_DIR);
		logger.debug("TempDir: {}", tempDir);

		jar = new File(data, JAR_FILENAME);
		logger.debug("Kipeto-Jar: {}", jar);

		tempDir.mkdirs();
	}

	private void checkForUpdate() throws Exception {
		File localRepositoryFile = new File(options.getRepositoryUrl());

		if (options.getRepositoryUrl().toLowerCase().startsWith("http")) {
			updateStrategy = new HttpUpdateStrategy(options.getRepositoryUrl());
		} else if (localRepositoryFile.exists()) {
			updateStrategy = new FileUpdateStrategy(options.getRepositoryUrl());
		} else {
			return;
		}

		window = new BootstrapWindow();
		new WindowThread(window).start();

		window.label.setText("Connecting to repository " + options.getRepositoryUrl());
		logger.info("Looking for new Kipeto Jar at {}", updateStrategy.getUpdateUrl());
		boolean updateFound = updateStrategy.isUpdateAvailable(jar);

		if (updateFound) {
			logger.info("Update needed");
			update();
		} else {
			logger.info("No Update needed");
		}

		window.setEnabled(false);
		window.dispose();
	}

	private void update() throws Exception {
		window.setVisible(true);

		File tempDir = new File(options.getData(), TEMP_DIR);
		if (!tempDir.exists()) {
			if (!tempDir.mkdirs()) {
				throw new RuntimeException("Could not create <" + tempDir + ">");
			}
		}

		File tempFile = File.createTempFile(getClass().getName(), ".jar", tempDir);
		logger.debug("Downloading {} to {}", updateStrategy.getUpdateUrl(), tempFile);

		CountingOutputStream destinationStream = new CountingOutputStream(new FileOutputStream(tempFile));
		destinationStream.addByteTransferListener(new ProgressListener());
		updateLength = updateStrategy.getUpdateSize();
		Date lastModified = updateStrategy.downloadUpdate(destinationStream);

		if (jar.exists()) {
			logger.debug("Deleting {}", jar);
			if (!jar.delete()) throw new RuntimeException("Could not delete <" + jar + ">");
		}

		tempFile.setLastModified(lastModified.getTime());

		logger.debug("Moving {} to {}", tempFile, jar);
		if (!tempFile.renameTo(jar)) throw new RuntimeException("Could not rename <" + tempFile + "> to <" + jar + ">");
	}

	private final class ProgressListener implements ByteTransferListener {

		public void handleByteTransfer(ByteTransferEvent event) {
			window.progressBar.setMaximum((int) updateLength);
			window.progressBar.setValue(((int) event.getBytesSinceBeginOfOperation()));

			String progress = FileSizeFormatter.formateBytes(event.getBytesSinceBeginOfOperation(), 2);
			String total = FileSizeFormatter.formateBytes(updateLength, 2);

			window.label.setText(String.format("Downloading %s (%s von %s)", updateStrategy.getUpdateUrl(), progress, total));
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
