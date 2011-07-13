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

/**
 * Repräsentation eines Unterverzeichnis-Eintrages (d.h. eines Directory`s) in
 * einem Directory.
 * 
 * @author Daniel Hintze
 * @since 21.01.2010
 */
public class DirectoryItem extends Item {

	/**
	 * Version des Binärformats (wichtig, falls später weitere binäre Felder
	 * hinzugefügt werden.
	 */
	private static final byte CURRENT_BINARY_VERSION = 1;

	public static final String TYPE = "DIR";

	/**
	 * Konstruktor, der bei der Deserialisierung von DirectoryItems durch
	 * <code>fromStream</code> verwendet wird.
	 * 
	 * @param name
	 *            Name der Unterverzeichnis im Verzeichnis
	 * @param directoryId
	 *            Id des Directory-Blobs im Repository
	 */
	private DirectoryItem(String name, String directoryId) {
		super(name, directoryId);
	}

	/**
	 * Erzeugt ein neues DirectoryItem aus einem bereits im Repository
	 * gepeicherten Directorys zur Verwendung in einem Directory.
	 * 
	 * @param name
	 *            Name der Unterverzeichnis im Verzeichnis
	 * @param directoryId
	 *            Id des Directory-Blobs im Repository
	 */
	public DirectoryItem(String name, Directory directory) {
		super(name, directory);
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
		dataOutputStream.writeUTF(itemId());
	}

	/**
	 * Deserialisiert ein DirectoryItem aus einem übergebenen InputStream.
	 * 
	 * @param dataInputStream
	 *            Quelle der Deserialisierung
	 * @return DirectoryItem
	 * @throws IOException
	 */
	public static DirectoryItem fromStream(DataInputStream dataInputStream) throws IOException {
		// Über die Version kann gesteuert werden, wieviele Felder gelesen
		// werden. Das ist aus Gründen der Abwärtskompatibilität notwendig.
		// Neue Felder dürfen nur am Ende hinzugefügt werden. Die Version
		// muss dann heraufgesetzt werden.
		byte binaryVersion = dataInputStream.readByte();

		if (binaryVersion >= 1) {
			String name = dataInputStream.readUTF();
			String directoryId = dataInputStream.readUTF();

			return new DirectoryItem(name, directoryId);
		} else {
			throw new IllegalStateException("Unsupported binary version <" + binaryVersion + ">");
		}

	}

}
