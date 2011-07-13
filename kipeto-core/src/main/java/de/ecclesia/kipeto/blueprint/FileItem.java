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
package de.ecclesia.kipeto.blueprint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.ecclesia.kipeto.common.util.Assert;
import de.ecclesia.kipeto.repository.Blob;

/**
 * Repräsentation eine Dateieintrages (d.h. eines Blobs) in einem Directory.
 * 
 * @author Daniel Hintze
 * @since 21.01.2010
 */
public class FileItem extends Item {

	/**
	 * Version des Binärformats (wichtig, falls später weitere binäre Felder
	 * hinzugefügt werden.
	 */
	private static final byte CURRENT_BINARY_VERSION = 1;

	public static final String TYPE = "BLOB";

	/** Größe der Datei im Dateisystem in Byte */
	private long length;

	/** Last-Modified Zeitstempel der Datei */
	private long lastModified;

	/**
	 * Konstruktor, der bei der Deserialisierung von FileItems durch
	 * <code>fromStream</code> verwendet wird.
	 * 
	 * @param name
	 *            Name der Datei im Dateisystem
	 * @param blobId
	 *            Id des Blobs im Repository
	 * @param length
	 *            Größe der Datei im Dateisystem in Byte
	 * @param lastModified
	 *            Last-Modified Zeitstempel der Datei
	 */
	private FileItem(String name, String blobId, long length, long lastModified) {
		super(name, blobId);

		this.length = length;
		this.lastModified = lastModified;
	}

	/**
	 * Erzeugt ein neues FileItem aus einem bereits im Repository gepeicherten
	 * Blob zur Verwendung in einem Directory.
	 * 
	 * @param name
	 *            Name der Datei im Dateisystem
	 * @param blob
	 *            Blob, auf welches Referenziert wird. Muss bereits im
	 *            Repository gepeichert sein.
	 * @param lastModified
	 *            Last-Modified Zeitstempel der Datei
	 */
	public FileItem(String name, Blob blob, long lastModified) {
		this(name, blob.id(), blob.contentLength(), lastModified);
	}

	/**
	 * Größe der Datei im Dateisystem in Byte
	 */
	public long length() {
		return length;
	}

	/** Last-Modified Zeitstempel der Datei */
	public long lastModified() {
		return lastModified;
	}

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public void writeToStream(DataOutputStream dataOutputStream) throws IOException {
		Assert.isNotNull(itemId(), "Item '" + name() + "' hat keine Id, wurde also noch nicht im Repository gespeichert");

		// Immer erster Stelle die aktuelle Version schreiben
		dataOutputStream.writeByte(CURRENT_BINARY_VERSION);

		// Die Reihenfolge der Felder darf sich aus Gründen der
		// Kompatibilität niemals ändern. Neue Felder können hinzugefügt
		// werden. Dann ist die Version hochzusetzten.
		dataOutputStream.writeUTF(name());
		dataOutputStream.writeLong(length());
		dataOutputStream.writeLong(lastModified());
		dataOutputStream.writeUTF(itemId());
	}

	/**
	 * Deserialisiert ein FileItem aus einem übergebenen InputStream.
	 * 
	 * @param dataInputStream
	 *            Quelle der Deserialisierung
	 * @return FileItem
	 * @throws IOException
	 */
	public static FileItem fromStream(DataInputStream dataInputStream) throws IOException {
		// Über die Version kann gesteuert werden, wieviele Felder gelesen
		// werden. Das ist aus Gründen der Abwärtskompatibilität notwendig.
		// Neue Felder dürfen nur am Ende hinzugefügt werden. Die Version
		// muss dann heraufgesetzt werden.
		byte binaryVersion = dataInputStream.readByte();

		if (binaryVersion >= 1) {
			String name = dataInputStream.readUTF();
			long length = dataInputStream.readLong();
			long lastModified = dataInputStream.readLong();
			String blobId = dataInputStream.readUTF();

			return new FileItem(name, blobId, length, lastModified);
		} else {
			throw new IllegalStateException("Unsupported binary version <" + binaryVersion + ">");
		}

	}

}
