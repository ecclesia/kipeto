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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Level;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.common.AWTExceptionErrorDialog;
import de.ecclesia.kipeto.common.util.LoggerConfigurer;
import de.ecclesia.kipeto.gui.GUI;
import de.ecclesia.kipeto.repository.AuthenticationProvider;
import de.ecclesia.kipeto.repository.AuthenticationProviderFactory;
import de.ecclesia.kipeto.repository.FileRepositoryStrategy;
import de.ecclesia.kipeto.repository.ReadingRepositoryStrategy;
import de.ecclesia.kipeto.repository.StrategySelector;
import de.ecclesia.kipeto.tui.TUI;

/**
 * @author Daniel Hintze
 * @since 02.02.2010
 */
public class KipetoApp {

	public static final String TITLE = "Kipeto";

	public static final String VERSION_NO = "0.0.1";

	public static final String BUILD_NO = "UNKNOWN_BUILD";

	public static final String VERSION = VERSION_NO + "b" + BUILD_NO;

	public static final String TEMP_DIR = "temp";

	public static final String REPOS_DIR = "repos";

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(KipetoApp.class);

	private final Options options;

	private boolean launchTarget = true;

	public static void main(String[] args) throws IOException {
		KipetoApp kipeto = new KipetoApp(new Options(args));

		kipeto.run();

		if (kipeto.launchTarget) {
			kipeto.launchTarget();
		}
	}

	public KipetoApp(Options options) {
		this.options = options;
	}

	private void run() throws IOException {
		LoggerConfigurer.configureFileAppender(options.getData(), "kipeto");
		LoggerConfigurer.configureConsoleAppender(Level.toLevel(options.getLogLevel(), Level.INFO));

		ReadingRepositoryStrategy remoteStrategy = null;
		FileRepositoryStrategy fileRepositoryStrategy = null;

		try {
			File repositoryDir = new File(options.getData(), REPOS_DIR);

			repositoryDir = ensureDirExists(new File(repositoryDir.getAbsolutePath()));

			File tempDir = ensureDirExists(new File(options.getData(), TEMP_DIR));

			String repositoryUrl;
			if (options.useStaticRepository()) {
				repositoryUrl = options.getRepositoryUrl();
			} else {
				RepositoryResolver resolver = new RepositoryResolver(options.getRepositoryUrl());
				repositoryUrl = resolver.resolveReposUrl();
			}

			AuthenticationProvider authenticationProvider = AuthenticationProviderFactory.getProvider(options);
			remoteStrategy = StrategySelector.getReadingStrategy(repositoryUrl, authenticationProvider);
			fileRepositoryStrategy = new FileRepositoryStrategy(repositoryDir, tempDir);
			boolean suppressOfflineErrorMsg = options.isSuppressOfflineErrorMsg();

			if (options.isGui()) {
				GUI gui = new GUI(remoteStrategy, fileRepositoryStrategy, options.getBlueprint(), new File(options.getTarget()),
						suppressOfflineErrorMsg);
				gui.run();
				launchTarget = gui.isLaunchTarget();
			} else {
				TUI tui = new TUI(remoteStrategy, fileRepositoryStrategy, options.getBlueprint(), new File(options.getTarget()));
				tui.run();
			}

			deleteOldLogs();
		} catch (Exception e) {
			logger.error("Error: ", e);

			if (options.isGui()) {
				AWTExceptionErrorDialog errorDialog = new AWTExceptionErrorDialog(e);
				errorDialog.setVisible(true);
			} else {
				e.printStackTrace();
			}
		} finally {
			if (remoteStrategy != null) {
				remoteStrategy.close();
			}

			if (fileRepositoryStrategy != null) {
				fileRepositoryStrategy.close();
			}
		}
	}

	private File ensureDirExists(File dir) {
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new RuntimeException("Could not create dir: " + dir.getAbsolutePath());
		}

		return dir;
	}

	private void deleteOldLogs() {
		String data = options.getData();
		File logDir = new File(data + "/log/");

		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_YEAR, -30);
		Date line = cal.getTime();

		for (File file : logDir.listFiles()) {
			if (file.getName().endsWith(".log") && new Date(file.lastModified()).before(line)) {
				logger.debug("Deleting old logfile {}", file.getAbsolutePath());
				file.delete();
			}
		}
	}

	private void launchTarget() throws IOException {
		String command = options.getAfterUpdate();

		if (command != null) {
			logger.debug("Launching Target {}", command);
			Runtime.getRuntime().exec(command);
		}
	}

}
