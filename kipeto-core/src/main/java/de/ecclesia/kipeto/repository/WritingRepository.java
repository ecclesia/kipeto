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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.ecclesia.kipeto.common.util.Assert;

/**
 * Repository für lesenden und schreibenden Zugriff.
 * 
 * @author Daniel Hintze
 * @since 02.02.2010
 */
public class WritingRepository extends ReadingRepository {

	private final WritingRepositoryStrategy strategy;

	/**
	 * Erzeugt ein neues Repository, welches auf der übergebenen
	 * Repository-Strategie arbeitet.
	 * 
	 * @param repositoryStorage
	 *            zu verwendende Repository-Strategie
	 */
	public WritingRepository(WritingRepositoryStrategy repositoryStorage) {
		super(repositoryStorage);

		this.strategy = repositoryStorage;
	}

	/**
	 * Speichert ein Blob im Repository.
	 * 
	 * @param blob
	 *            zu speicherndes Blob
	 * @return Id, unter der das Blob abgelegt wurde
	 * @throws IOException
	 */
	public String store(Blob blob) throws IOException {
		File blobFile = File.createTempFile(getClass().getName(), null, strategy.tempDir());

		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(blobFile));
		blob.writeToStream(outputStream);

		if (strategy instanceof FileRepositoryStrategy) {
			((FileRepositoryStrategy) strategy).storeFileByRename(blob.id(), blobFile);
		} else {
			strategy.store(blob.id(), new FileInputStream(blobFile));
		}

		return blob.id();
	}

	/**
	 * Erzeugt eine Referenz (d.h. eine Art Link) unter dem Name
	 * <code>reference</code>, welche auf das Blob mit der Id <code>id</code>
	 * verweist.
	 * 
	 * @param reference
	 *            Name der Referenz
	 * @param id
	 *            Id des Blobs welches referenziert werden soll
	 * @throws IOException
	 * @throws IdNotFoundException
	 *             falls unter <code>id</code> kein Blob abgelegt ist
	 */
	public void createReference(String reference, String id) throws IOException {
		Assert.isNotNull(reference);
		Assert.isFalse(reference.length() == 0, "Die Referenz darf nicht leer sein");
		Assert.isNotNull(id, "Es muss die Id des Items übergeben werden");
		Assert.isTrue(id.length() > WritingRepositoryStrategy.SUBDIR_POLICY, "Die Id '" + id
				+ "' ist zu kurz, um im Repository abgelegt zu werden");

		if (!contains(id)) {
			throw new IdNotFoundException(id);
		}

		strategy.createReference(reference, id);
	}

	/**
	 * Löscht die übergebene Referenz.
	 * 
	 * @param reference
	 *            Name, unter dem die Referenz angelegt wurde
	 * @return TRUE, wenn die Referenz gelöscht wurde
	 */
	public boolean removeReference(String reference) {
		return strategy.removeReference(reference);
	}

	/**
	 * Entfernt das unter <code>id</code> abgelegte Blob aus dem Speicher.
	 * 
	 * @param Id
	 *            , unter der das zu löschende Blob abgelegt wurde
	 * @return TRUE, wenn ein Objekt gelöscht wurde
	 */
	public boolean remove(String id) {
		return strategy.remove(id);
	}

	@Override
	public WritingRepositoryStrategy strategy() {
		return strategy;
	}

	public long bytesWritten() {
		return strategy.bytesWritten();
	}

}
