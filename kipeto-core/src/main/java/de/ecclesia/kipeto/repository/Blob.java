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
package de.ecclesia.kipeto.repository;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.common.util.Assert;
import de.ecclesia.kipeto.common.util.HashUtil;
import de.ecclesia.kipeto.common.util.Streams;
import de.ecclesia.kipeto.compressor.Compressor;
import de.ecclesia.kipeto.compressor.CompressorFactory;

public class Blob {

	private static final Logger logger = LoggerFactory.getLogger(Blob.class);

	private static final String TYPE = "BLOB";

	/**
	 * Version des Binärformats (wichtig, falls später weitere binäre Felder hinzugefügt werden.
	 */
	private static final byte CURRENT_BINARY_VERSION = 1;

	/** Hash-Algorithmus zur Berechnung der Id */
	public static final String HASH_ALGORITHM = "SHA-256";

	/** Zu verwendende Kompression */
	private final String compression;

	/** Content des Blob */
	private InputStream contentStream;

	/** Länge des dekomprimierten Content in Byte */
	private long contentLength;

	/** Id (Hash) des Blobs */
	private String id;

	/**
	 * Da der Content des Blobs als InputStream vorliegt, kann er nur ein einziges Mal gelesen werden. Dieses Attribut
	 * gibt an, ob der Stream bereits gelesen, d.h. "verbraucht" worden ist.
	 */
	private boolean exhausted;

	/**
	 * Erzeugt ein neues Blob-Objekt. Primär für die Erzeugung von Blobs aus Repository-Streams.
	 * 
	 * @param id
	 *            Id (Hash) des Objektes
	 * @param type
	 *            Type (z.B. BLOB, DIR oder BLUEPRINT) des Inhalts
	 * @param compression
	 *            Art der Kompression des Contents
	 * @param contentStream
	 *            der unkomprimierte Content
	 * @param contentLength
	 *            Länge des Contents in byte
	 */
	protected Blob(String id, String compression, InputStream contentStream, Long contentLength) {
		Assert.isNotNull(contentLength);

		this.id = id;
		this.compression = compression;
		this.contentStream = contentStream;
		this.contentLength = contentLength;
	}

	/**
	 * Erzeugt ein neues Blob-Objekt. Primär um neue Blobs im Repository zu speichern.
	 * 
	 * @param compression
	 *            Art der Kompression des Contents
	 * @param contentStream
	 *            der unkomprimierte Content
	 * @param length
	 *            Länge des Contents in byte
	 */
	public Blob(String compression, InputStream contentStream, Long contentLength) {
		this(null, compression, contentStream, contentLength);
	}

	/**
	 * Serialisiert das Objekt in den übergebenen OutputStream. Zunächst wird der Header (Type, Kompressions und Länge)
	 * in den Stream geschrieben. Anschließen der Content des Blobs, ggf. komprimiert. <br/>
	 * <br/>
	 * Der OutputStream wird am Ende der Operation geschlossen. Die geschriebenen Bytes werden gehasht und der Hash in
	 * der Id des Blobs gespeichert. Da der InputStream gelesen wird, kann diese Methode nur ein Mal aufgerufen werden.
	 * 
	 * @param outputStream
	 *            Ziel der Serialisierung
	 * @throws UnsupportedOperationException
	 *             falls der InputStream des Blobs bereits eingelesen wurde.
	 * @throws IOException
	 */
	public void writeToStream(OutputStream outputStream) throws IOException {
		if (exhausted) {
			throw new UnsupportedOperationException("Der Content-InputStream des Blobs wurde bereits einmal gelesen");
		}

		MessageDigest digest = getDigest();
		DigestOutputStream digestOutputStream = new DigestOutputStream(outputStream, digest);

		// Headerdaten und Inhalt schreiben
		DataOutputStream dataOutputStream = new DataOutputStream(digestOutputStream);

		// Immer erster Stelle die aktuelle Version schreiben
		dataOutputStream.writeByte(CURRENT_BINARY_VERSION);

		// Die Reihenfolge der Felder darf sich aus Gründen der
		// Kompatibilität niemals ändern. Neue Felder können hinzugefügt
		// werden. Dann ist die Version hochzusetzten.
		dataOutputStream.writeUTF(type());
		dataOutputStream.writeUTF(compression);
		dataOutputStream.writeLong(contentLength);

		// Nur der Content wird komprimiert
		Compressor compressor = CompressorFactory.getCompressor(compression());
		OutputStream compressingStream = compressor.compress(dataOutputStream);

		Streams.copyStream(contentStream, compressingStream, true);

		byte[] hashBytes = digest.digest();

		id = HashUtil.convertHashBytesToString(hashBytes);
	}

	/**
	 * Erzeugt aus einem Roh-Stream (d.h. mit Header und ggf. Kompression) ein passendes Blob-Objekt. Wenn das Blob vom
	 * Typ <i>BLOB, DIR oder BLUEPRINT</i> ist, so wird eine korrekte Instanz der entsprechenden Klasse zurückgegeben.
	 * 
	 * @param id
	 *            Id, unter der das gewünschte Blob abgelegt wurde
	 * @return Blob-Objekt (z.B. Directory oder Blueprint)
	 * @throws IdNotFoundException
	 *             falls unter <code>id</code> kein Blob abgelegt ist
	 * @throws IOException
	 */
	public static <E extends Blob> E retrieveFromStream(String id, InputStream inputStream, Class<E> clazz)
			throws IOException {
		logger.debug("trying to retrieve from Stream. id = {}, class = {}", id, clazz);

		DataInputStream dataInputStream = new DataInputStream(inputStream);

		byte binaryVersion = dataInputStream.readByte();

		String compression;
		String type;
		long contentLength;
		if (binaryVersion >= 1) {
			type = dataInputStream.readUTF();
			compression = dataInputStream.readUTF();
			contentLength = dataInputStream.readLong();
		} else {
			throw new IllegalStateException("Unsupported binary version <" + binaryVersion + ">");
		}

		logger.debug("retrieving item from Stream. type = " + type + " , compression = {}, content-length = {}",
				compression, contentLength);

		Compressor compressor = CompressorFactory.getCompressor(compression);
		InputStream contentStream = compressor.decompress(dataInputStream);

		try {
			Constructor<E> constructor = clazz.getDeclaredConstructor(String.class, String.class, InputStream.class,
					Long.class);
			return constructor.newInstance(new Object[] { id, compression, contentStream, contentLength });
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Art der Kompression.
	 * 
	 * @see Compressor
	 * @return
	 */
	public String compression() {
		return compression;
	}

	/**
	 * Liefert den Content als InputStream. Da der Stream naturgemäß nur ein einziges Mal gelesen werden kann, können
	 * anschließend etwa die Methoden <code>writeToStream()</code> und <code>calculateId()</code> nicht mehr verwendet
	 * werden.
	 * 
	 * @return Content-InputStream
	 * @throws IllegalStateException
	 *             falls der InputStream des Blobs bereits eingelesen wurde.
	 */
	public InputStream contentStream() {
		// Da wir nicht wissen, was mit dem ContentStream passiert, müssen wir
		// annehmen, das er gelesen wird und daher anschließen nicht mehr von
		// uns genutzt werden kann.
		if (exhausted) {
			throw new IllegalStateException("Der Content-InputStream des Blobs wurde bereits einmal gelesen");
		}

		exhausted = true;

		return contentStream;
	}

	/**
	 * Liefert die Id (den Hash) des Blobs. Falls das Blob nicht aus dem Repository erzeugt wurde, muss die Id erst
	 * durch das Serialisieren des Objektes ermittelt werden. Die Id ist also <code>NULL</code>, bis
	 * <code>writeToStream()</code> oder <code>calculateId()</code> aufgerufen wurden.
	 * 
	 * @return Id oder <code>NULL</code> wenn die Id nicht bekannt und noch nicht berechnet wurde
	 */
	public String id() {
		return id;
	}

	/**
	 * Liefert eine Instanz des zu verwendenden Hash-Algorithmus.
	 * 
	 * @return MessageDigest
	 */
	private static MessageDigest getDigest() {
		try {
			return MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	protected String type() {
		return TYPE;
	}

	public long contentLength() {
		return contentLength;
	}
}
