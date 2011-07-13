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

import de.ecclesia.kipeto.common.util.ByteTransferListener;

/**
 * @author Daniel Hintze
 * @since 02.02.2010
 */
public class CachedReadingStrategy extends ReadingRepositoryStrategy {

	private final ReadingRepositoryStrategy repository;
	private final WritingRepositoryStrategy cache;

	public CachedReadingStrategy(ReadingRepositoryStrategy repository, WritingRepositoryStrategy cache) {
		this.repository = repository;
		this.cache = cache;
	}

	@Override
	public boolean contains(String id) throws IOException {
		return cache.contains(id) ? true : repository.contains(id);
	}

	@Override
	public String resolveReference(String reference) throws IOException {
		return repository.resolveReference(reference);
	}

	public void load(String id, ByteTransferListener listener) throws IOException {
		cache.storeStream(id, repository.retrieveStream(id, listener), null);
	}

	@Override
	protected InputStream retrieve(String id) throws IOException {
		if (!cache.contains(id)) {
			load(id, null);
		}

		return cache.retrieveStream(id);
	}

	@Override
	public long sizeInRepository(String id) throws IOException {
		return cache.contains(id) ? cache.sizeInRepository(id) : repository.sizeInRepository(id);
	}

	public ReadingRepositoryStrategy getRepository() {
		return repository;
	}

	public WritingRepositoryStrategy getCache() {
		return cache;
	}

	@Override
	public void close() {
		repository.close();
		cache.close();
	}

	@Override
	public List<Reference> allReferences() throws IOException {
		return repository.allReferences();
	}

	@Override
	public List<String> allObjects() {
		return repository.allObjects();
	}
}
