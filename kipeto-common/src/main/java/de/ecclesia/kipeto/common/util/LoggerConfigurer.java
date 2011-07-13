/*
 * #%L
 * Kipeto Common
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
package de.ecclesia.kipeto.common.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class LoggerConfigurer {

	private static final PatternLayout LAYOUT = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %p %c %x - %m%n");
	private static final PatternLayout SIMPLE_LAYOUT = new PatternLayout("[%-5p] %m%n");

	public static FileAppender configureFileAppender(String dataDir, String fileName) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss-SSS");
		Logger logger = Logger.getRootLogger();
		FileAppender appender = null;

		try {
			String logFile = dataDir + "/log/" + fileName + simpleDateFormat.format(new Date()) + ".log";
			appender = new FileAppender(LAYOUT, logFile, true);
			appender.setName("LOGFILE");
			
			logger.addAppender(appender);
			logger.setLevel(Level.INFO);

			Logger.getLogger("de.ecclesia").setLevel(Level.DEBUG);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Printing ERROR Statements", e);
		}

		return appender;
	}
	
	public static void configureConsoleAppender(Level level) {
		ConsoleAppender consoleAppender = new ConsoleAppender(LAYOUT);
		consoleAppender.setName("CONSOLE");

		if (level.isGreaterOrEqual(Level.INFO)) {
			consoleAppender.setLayout(SIMPLE_LAYOUT);
		} else {
			consoleAppender.setLayout(LAYOUT);
		}

		consoleAppender.setThreshold(level);

		Logger.getLogger("de.ecclesia").addAppender(consoleAppender);
	}
}
