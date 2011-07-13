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
package de.ecclesia.kipeto.compressor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface für Kompressions-Algorithmen.
 * 
 * @author Daniel Hintze
 * @since 27.01.2010
 */
public interface Compressor {

	/** Keine Kompression */
	public static final String NONE = "NONE";

	/** GZIP Kompression */
	public static final String GZIP = "GZIP";

	/**
	 * Gibt einen Stream zurück, der die Daten komprimiert, die in ihn
	 * geschrieben werden.
	 * 
	 * @param outputStream
	 * @return Komprimierenden Stream
	 * @throws IOException
	 */
	public abstract OutputStream compress(OutputStream outputStream) throws IOException;

	/**
	 * Gibt einen Stream zurück, von dem die Daten aus
	 * <code>compressedStream</code> dekomprimiert gelesen werden können.
	 * 
	 * @param compressedStream
	 *            Komprimierte Daten
	 * @return Unkompremierte Daten
	 * @throws IOException
	 */
	public abstract InputStream decompress(InputStream compressedStream) throws IOException;
}
