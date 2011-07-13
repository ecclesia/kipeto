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

import de.ecclesia.kipeto.compressor.Compressor;
import de.ecclesia.kipeto.repository.Blob;

/**
 * Repräsentation eines Blueprints.
 * 
 * @author Daniel Hintze
 * @since 21.01.2010
 */
public class Blueprint extends Blob {

	private static final String TYPE = "BLUEPRINT";

	/**
	 * Version des Binärformats (wichtig, falls später weitere binäre Felder hinzugefügt werden.
	 */
	private static final byte CURRENT_BINARY_VERSION = 1;

	/** Beschreibung des Blueprints */
	private final String description;

	/** Id des Wurzelverzeichnisses */
	private final String rootDirId;

	/** Id des Icons. Optional */
	private final String iconId;

	/**
	 * Erstellt einen neuen Blueprint
	 * 
	 * @param programId
	 *            Name des Programms
	 * @param description
	 *            Beschreibung des Blueprints
	 * @param rootDir
	 *            Id des Wurzelverzeichnisses
	 */
	public Blueprint(String programId, String description, Directory rootDir, Blob icon) {
		super(Compressor.NONE, new ByteArrayInputStream(persist(description, rootDir, icon)), (long) persist(
				description, rootDir, icon).length);

		this.description = description;
		this.rootDirId = rootDir.id();
		this.iconId = icon != null ? icon.id() : null;
	}

	/**
	 * Erstellt einen Blueprint aus dem Repository.
	 * 
	 * @param id
	 *            Id im Repository
	 * @param type
	 *            Type
	 * @param compression
	 *            Kompression
	 * @param dataInputStream
	 *            Content
	 * @param length
	 *            Länge des Content in Byte
	 */
	public Blueprint(String id, String compression, InputStream contentStream, Long contentLength) {
		super(id, compression, contentStream, contentLength);

		try {
			DataInputStream dataInputStream = new DataInputStream(contentStream);

			// Über die Version kann gesteuert werden, wieviele Felder gelesen
			// werden. Das ist aus Gründen der Abwärtskompatibilität notwendig.
			// Neue Felder dürfen nur am Ende hinzugefügt werden. Die Version
			// muss dann heraufgesetzt werden.
			byte binaryVersion = dataInputStream.readByte();

			if (binaryVersion >= 1) {
				rootDirId = dataInputStream.readUTF();
				description = dataInputStream.readUTF();

				String tempIconId = dataInputStream.readUTF();
				iconId = tempIconId.equals("") ? null : tempIconId;
			} else {
				throw new IllegalStateException("Unsupported binary version <" + binaryVersion + ">");
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
	 * Serialisiert den Blueprint
	 */
	private static byte[] persist(String description, Directory rootDir, Blob icon) {
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(arrayOutputStream);

		try {
			// Immer erster Stelle die aktuelle Version schreiben
			dataOutputStream.writeByte(CURRENT_BINARY_VERSION);

			// Die Reihenfolge der Felder darf sich aus Gründen der
			// Kompatibilität niemals ändern. Neue Felder können hinzugefügt
			// werden. Dann ist die Version hochzusetzten.
			dataOutputStream.writeUTF(rootDir.id());
			dataOutputStream.writeUTF(description);
			dataOutputStream.writeUTF(icon != null ? icon.id() : "");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return arrayOutputStream.toByteArray();
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the rootDirId
	 */
	public String getRootDirId() {
		return rootDirId;
	}

	/**
	 * Id des Blobs, welcher das Icon enthält. Kann NULL sein.
	 * 
	 * @return
	 */
	public String getIconId() {
		return iconId;
	}

	@Override
	protected String type() {
		return TYPE;
	}
}
