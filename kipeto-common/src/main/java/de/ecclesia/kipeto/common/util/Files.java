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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Dominik Maehl <dmaehl@ecclesia.de>
 */
public class Files {

	/**
	 * LÃ¶scht ein Verzeichnis inklusive aller Unterverzeichnisse und Dateien
	 * 
	 * @param directory
	 *            Zu löschendes Verzeichnis
	 * @return
	 */
	public static boolean deleteDirectory(File directory) {
		clearDirectory(directory);
		return directory.delete();
	}

	/**
	 * LÃ¶scht alle Dateien und Verzeichnisse in dem Ã¼bergebenen Verzeichnis
	 * 
	 * @param directory
	 *            Zu sÃ¤uberndes Verzeichnis
	 */
	public static boolean clearDirectory(File directory) {
		Assert.isTrue(directory.isDirectory(), "Parameter ist kein Verzeichnis");
		File[] files = directory.listFiles();
		boolean allDeleted = true;
		for (File file : files) {
			if (file.isDirectory()) {
				clearDirectory(file);
			}
			allDeleted &= file.delete();
		}

		return allDeleted;
	}

	/**
	 * Kopiert eine Datei
	 * 
	 * @param sourceFile
	 *            Quelldatei
	 * @param destinationFile
	 *            Zieldatei
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void copyFile(File sourceFile, File destinationFile) throws FileNotFoundException, IOException {
		Streams.copyStream(new FileInputStream(sourceFile), new FileOutputStream(destinationFile), true);
	}

	public static void writeBytesToFile(byte[] bytes, File destinationFile) throws FileNotFoundException, IOException {
		Streams.copyStream(new ByteArrayInputStream(bytes), new FileOutputStream(destinationFile), true);
	}

	public static byte[] readBytesFromFile(File sourceFile) throws FileNotFoundException, IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream((int) sourceFile.length());
		Streams.copyStream(new FileInputStream(sourceFile), byteStream, true);
		return byteStream.toByteArray();
	}

	public static void writeStringToFile(String string, File destinationFile) throws FileNotFoundException, IOException {
		writeBytesToFile(string.getBytes("UTF-8"), destinationFile);
	}

	public static String readStringFromFile(File sourceFile) throws FileNotFoundException, IOException {
		byte[] bytes = readBytesFromFile(sourceFile);
		String string = new String(bytes, "UTF-8");
		return string.intern();
	}

}
