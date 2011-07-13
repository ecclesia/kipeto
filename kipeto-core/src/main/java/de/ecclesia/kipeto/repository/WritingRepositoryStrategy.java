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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.ecclesia.kipeto.common.util.Assert;
import de.ecclesia.kipeto.common.util.ByteTransferEvent;
import de.ecclesia.kipeto.common.util.ByteTransferListener;
import de.ecclesia.kipeto.common.util.CountingInputStream;

/**
 * @author Daniel Hintze
 * @since 21.01.2010
 */
public abstract class WritingRepositoryStrategy extends ReadingRepositoryStrategy {

	private long bytesWritten;

	/**
	 * Speichert den übergebenen InputStream unter der angegebenen Id. Per
	 * Definition ist die Id der Hash des InputStreams. Falls unter der
	 * angegebenen Id also bereits ein Objekt anderer Länge gepeichert ist,
	 * schlägt das Speichern fehl.
	 * 
	 * @param id
	 *            Id des Objektes, unter der es abgelegt wird
	 * @param inputStream
	 *            des zu speichernden Objektes
	 * @throws IOException
	 */
	public void storeStream(String id, InputStream inputStream, ByteTransferListener listener) throws IOException {
		Assert.isNotNull(id);
		Assert.isNotNull(inputStream);

		CountingInputStream countingInputStream = new CountingInputStream(inputStream);
		countingInputStream.addByteTransferListener(new ByteTransferListener() {

			public void handleByteTransfer(ByteTransferEvent event) {
				bytesWritten += event.getBytesSinceLastEvent();
			}
		});
		
		if (listener != null) {
			countingInputStream.addByteTransferListener(listener);
		}
		
		store(id, countingInputStream);
	}

	/**
	 * Speichert den übergebenen InputStream unter der angegebenen Id. Per
	 * Definition ist die Id der Hash des InputStreams. Falls unter der
	 * angegebenen Id also bereits ein Objekt anderer Länge gepeichert ist,
	 * schlägt das Speichern fehl.
	 * 
	 * @param id
	 *            Id des Objektes, unter der es abgelegt wird
	 * @param inputStream
	 *            des zu speichernden Objektes
	 * @throws IOException
	 */
	public void storeStream(String id, InputStream inputStream) throws IOException {
		storeStream(id, inputStream, null);
	}
	
	protected abstract void store(String id, InputStream inputStream) throws IOException;

	/**
	 * Erzeugt eine Referenz (d.h. eine Art Link) unter dem Name
	 * <code>reference</code>, welche auf das Objekt mit der Id <code>id</code>
	 * verweist.
	 * 
	 * @param reference
	 *            Name der Referenz
	 * @param id
	 *            Id des Objektes welches referenziert werden soll
	 * @throws IOException
	 * @throws IdNotFoundException
	 *             falls unter <code>id</code> kein Objekt abgelegt ist
	 */
	public abstract void createReference(String reference, String id) throws IOException;

	/**
	 * Löscht die übergebene Referenz.
	 * 
	 * @param reference
	 *            Name, unter dem die Referenz angelegt wurde
	 * @return TRUE, wenn die Referenz gelöscht wurde
	 */
	public abstract boolean removeReference(String reference);

	/**
	 * Entfernt das unter <code>id</code> abgelegte Objekt aus dem Speicher.
	 * 
	 * @param Id
	 *            , unter der das zu löschende Objekt abgelegt wurde
	 * @return TRUE, wenn ein Objekt gelöscht wurde
	 */
	public abstract boolean remove(String id);

	public long bytesWritten() {
		return bytesWritten;
	}
	
	public abstract File tempDir();
}
