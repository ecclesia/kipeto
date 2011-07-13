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

import de.ecclesia.kipeto.common.util.Assert;

/**
 * Repository für lesenden Zugriff.
 * 
 * @author Daniel Hintze
 * @since 02.02.2010
 */
public class ReadingRepository {

	/** Strategie zum Laden und Speichern von Objekten */
	private final ReadingRepositoryStrategy strategy;

	/**
	 * Erzeugt ein neues Repository, welches auf der übergebenen
	 * Repository-Strategie arbeitet.
	 * 
	 * @param repositoryStorage
	 *            zu verwendende Repository-Strategie
	 */
	public ReadingRepository(ReadingRepositoryStrategy repositoryStorage) {
		Assert.isNotNull(repositoryStorage);

		this.strategy = repositoryStorage;
	}

	/**
	 * Ermittelt die Größe eines Blobs im Repository (d.h. ggf. komprimiert).
	 * Kann beispielsweise genutzt werden um einen detaillierten
	 * Fortschrittsbalken anzuzeigen.
	 * 
	 * @param id
	 *            Id, unter der das gewünschte Blob abgelegt wurde
	 * @return Größe des Blobs in Byte
	 * @throws IdNotFoundException
	 *             falls unter <code>id</code> kein Blob abgelegt ist
	 * @throws IOException
	 */
	public long sizeInRepository(String id) throws IOException {
		return strategy.sizeInRepository(id);
	}

	/**
	 * Liefert zu der übergebenen Referenz die referenzierte Id oder
	 * <code>NULL</code>, falls die Referenz nicht existiert.
	 * 
	 * @param reference
	 *            Name, unter dem die Referenz angelegt wurde
	 * @return Referenzierte Id oder <code>NULL</code>, falls die Referenz nicht
	 *         existiert.
	 * @throws IOException
	 */
	public String resolveReference(String reference) throws IOException {
		return strategy.resolveReference(reference);
	}

	public <T extends Blob> T retrieve(String id, Class<T> clazz) throws IOException {
		InputStream stream = strategy.retrieveStream(id);

		return Blob.retrieveFromStream(id, stream, clazz);
	}

	/**
	 * Ermittelt, ob ein Blob zu der übergebenen Id im Repository existiert.
	 * 
	 * @param id
	 *            Id, unter der das gewünschte Blob abgelegt wurde
	 * @return TRUE, falls das Blob im Repository existiert
	 * @throws IOException
	 */
	public boolean contains(String id) throws IOException {
		return strategy.contains(id);
	}

	/**
	 * Liefert die verwendete RepositoryStrategy.
	 * 
	 * @return RepositoryStrategy
	 */
	public ReadingRepositoryStrategy strategy() {
		return strategy;
	}

	public long bytesRead() {
		return strategy.bytesRead();
	}

	public void close() {
		strategy.close();
	}


}
