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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import de.ecclesia.kipeto.compressor.Compressor;
import de.ecclesia.kipeto.repository.Blob;

/**
 * Repräsentation eines Verzeichnisses. Enthält Einträge in Form von FileItems
 * und DirectoryItems.
 * 
 * @author Daniel Hintze
 * @since 25.01.2010
 */
public class Directory extends Blob {

	private static final String TYPE = "DIR";

	/** Einträge wie Dateien und Unterverzeichnisse */
	private final Item[] items;

	/**
	 * Konstruktor zum erzeugen von Directory`s, die aus dem Repository geladen
	 * werden.
	 * 
	 * @param id
	 *            Id im Repository
	 * @param compression
	 *            Kompression
	 * @param contentStream
	 *            Content
	 * @param length
	 *            Länge des Content in Byte
	 */
	public Directory(String id, String compression, InputStream contentStream, Long contentLength) {
		super(id, compression, contentStream, contentLength);

		try {
			DataInputStream dataInputStream = new DataInputStream(contentStream);
			int numberOfItemsToRead = dataInputStream.readInt();

			items = new Item[numberOfItemsToRead];

			for (int i = 0; i < numberOfItemsToRead; i++) {
				String itemType = dataInputStream.readUTF();

				if (itemType.equals(FileItem.TYPE)) {
					items[i] = FileItem.fromStream(dataInputStream);
				} else if (itemType.equals(DirectoryItem.TYPE)) {
					items[i] = DirectoryItem.fromStream(dataInputStream);
				} else {
					contentStream.close();
					dataInputStream.close();

					throw new UnsupportedOperationException("Type '" + itemType + "' nicht bekannt");
				}

			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				contentStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Erstellt ein neues Directory mit den angegebenen Einträgen.
	 * 
	 * @param items
	 */
	public Directory(Item... items) {
		super(Compressor.GZIP, new ByteArrayInputStream(persist(items)), (long) persist(items).length);
		this.items = items;
	}

	/**
	 * Serialisiert die übergebenen Items.
	 * 
	 * @param items
	 * @return
	 */
	private static byte[] persist(Item... items) {
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(arrayOutputStream);

		// Items sortieren, damit sich die Id nicht unnötig ändert.
		Arrays.sort(items);

		try {
			// Anzahl der Items schreiben, um sie später einfacher wieder lesen
			// zu können.
			dataOutputStream.writeInt(items.length);

			// Die einzelnen Items schreiben
			for (Item item : items) {
				dataOutputStream.writeUTF(item.type());
				item.writeToStream(dataOutputStream);
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return arrayOutputStream.toByteArray();
	}

	/** Gibt eine Kopie der Verzeichniseinträge zurück */
	public Item[] getItems() {
		return items.clone();
	}

	@Override
	protected String type() {
		return TYPE;
	}

}
