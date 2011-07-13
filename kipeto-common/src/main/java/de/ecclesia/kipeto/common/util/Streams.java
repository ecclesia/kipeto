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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Dominik Mähl <dmaehl@ecclesia.de>
 */
public class Streams {

	/**
	 * Kopiert den Inhalt eines Datenstroms in einen anderen.
	 * 
	 * @param sourceStream
	 *            Quelldatenstrom
	 * @param destinationStream
	 *            Zieldatenstrom
	 * @throws IOException
	 */
	public static void copyBufferedStream(BufferedInputStream sourceStream, BufferedOutputStream destinationStream,
			boolean closeStreams) throws IOException {
		byte[] buffer = new byte[8192];
		int bytesRead = 0;

		try {
			while ((bytesRead = sourceStream.read(buffer)) != -1) {
				destinationStream.write(buffer, 0, bytesRead);
			}
		} finally {
			if (closeStreams) {
				sourceStream.close();
				destinationStream.close();
			}
		}
	}

	/**
	 * Kopiert den Inhalt eines Datenstroms in einen anderen. Aus GeschwindigkeitsgrÃ¼nden werden die StrÃ¶me gebuffert.
	 * Diese Funktion sollte verwendet werden, falls die DatenstrÃ¶me noch nicht Buffered*Stream implementieren.
	 * 
	 * @param sourceStream
	 *            Quelldatenstrom
	 * @param destinationStream
	 *            Zieldatenstrom
	 * @throws IOException
	 */
	public static void copyStream(InputStream sourceStream, OutputStream destinationStream, boolean closeStreams)
			throws IOException {
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(destinationStream);
		try {
			copyBufferedStream(new BufferedInputStream(sourceStream), bufferedOutputStream, closeStreams);
		} finally {
			if (!closeStreams) {
				bufferedOutputStream.flush();
			}
		}
	}

	public static void ensureClosed(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Kopiert den Inhalt eines Datenstroms in ein ByteArray.
	 * 
	 * @param sourceStream
	 *            Quelldatenstrom
	 * @param closeStream
	 * @throws IOException
	 */
	public static byte[] getBytes(InputStream sourceStream, boolean closeStream) throws IOException {
		ByteArrayOutputStream destinationStream = new ByteArrayOutputStream();
		Streams.copyStream(sourceStream, destinationStream, closeStream);
		return destinationStream.toByteArray();
	}
}
