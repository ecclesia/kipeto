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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.common.util.Assert;
import de.ecclesia.kipeto.common.util.ByteTransferEvent;
import de.ecclesia.kipeto.common.util.ByteTransferListener;
import de.ecclesia.kipeto.common.util.CountingInputStream;

/**
 * @author Daniel Hintze
 * @since 21.01.2010
 */
public abstract class ReadingRepositoryStrategy {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected static final String OBJECT_DIR = "objs";

	protected static final String REFERENCE_DIR = "refs";

	private long bytesRead;

	private long filesRead;

	/**
	 * Gibt die Anzahl von Stellen der Id an, welche zur Optimierung der Dateisystemnutzung zur Bildung eines Unterordners genutzt werden
	 * sollen.
	 * 
	 * Mit SUBDIR_POLICY = 2 wird ein Item mit der Id 'abcdefg' also im Unterverzeichnis /ab/cdefg abgelegt.
	 * 
	 */
	protected static final int SUBDIR_POLICY = 2;

	/**
	 * Läd das Objekt mit der angegebenen Id aus dem Speicher.
	 * 
	 * @param id
	 *            Id, unter der das gewünschte Objekt abgelegt wurde
	 * @return InputStream des gespeichernten Objektes
	 * @throws IdNotFoundException
	 *             falls unter <code>id</code> kein Objekt abgelegt ist
	 * @throws IOException
	 */
	public InputStream retrieveStream(final String id, ByteTransferListener listener) throws IOException {
		Assert.isNotNull(id, "Es muss die Id des Items übergeben werden");

		InputStream inputStream = retrieve(id);
		CountingInputStream countingInputStream = new CountingInputStream(inputStream);
		countingInputStream.addByteTransferListener(new ByteTransferListener() {

			public void handleByteTransfer(ByteTransferEvent event) {
				bytesRead += event.getBytesSinceLastEvent();
				logger.trace(event.getBytesSinceLastEvent() + " bytes read from " + id + ", " + event.getBytesSinceBeginOfOperation()
						+ " bytes read from this stream in total");
			}
		});

		if (listener != null) {
			countingInputStream.addByteTransferListener(listener);
		}

		filesRead++;

		return countingInputStream;
	}

	/**
	 * Läd das Objekt mit der angegebenen Id aus dem Speicher.
	 * 
	 * @param id
	 *            Id, unter der das gewünschte Objekt abgelegt wurde
	 * @return InputStream des gespeichernten Objektes
	 * @throws IdNotFoundException
	 *             falls unter <code>id</code> kein Objekt abgelegt ist
	 * @throws IOException
	 */
	public InputStream retrieveStream(String id) throws IOException {
		return retrieveStream(id, null);
	}

	protected abstract InputStream retrieve(String id) throws IOException;

	/**
	 * Ermittelt die Größe eines Objektes im Speicher. Kann beispielsweise genutzt werden um einen detaillierten Fortschrittsbalken
	 * anzuzeigen.
	 * 
	 * @param id
	 *            Id, unter der das gewünschte Objekt abgelegt wurde
	 * @return Größe des Objektes in Byte
	 * @throws IdNotFoundException
	 *             falls unter <code>id</code> kein Objekt abgelegt ist
	 * @throws IOException
	 */
	public abstract long sizeInRepository(String id) throws IOException;

	/**
	 * Liefert zu der übergebenen Referenz die referenzierte Id oder <code>NULL</code>, falls die Referenz nicht existiert.
	 * 
	 * @param reference
	 *            Name, unter dem die Referenz angelegt wurde
	 * @return Referenzierte Id oder <code>NULL</code>, falls die Referenz nicht existiert.
	 * @throws IOException
	 */
	public abstract String resolveReference(String reference) throws IOException;

	/**
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public abstract boolean contains(String id) throws IOException;

	public long bytesRead() {
		return bytesRead;
	}

	public long filesRead() {
		return filesRead;
	}

	/**
	 * Gibt ggf. belegte Ressourcen wieder frei.
	 */
	public abstract void close();

	public abstract List<Reference> allReferences() throws IOException;

	public abstract List<String> allObjects();
}
