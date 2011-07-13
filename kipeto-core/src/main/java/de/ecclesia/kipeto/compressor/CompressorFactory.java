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

/**
 * @author Daniel Hintze
 * @since 27.01.2010
 */
public class CompressorFactory {

	/**
	 * Liefert anhand von <code>compression</code> einen geeigneten Compressor.
	 * 
	 * @param compression Kompressions-Algorithmus
	 * @return Compressor für <code>compression</code>
	 * @throws UnsupportedOperationException falls die übergebene Kompression
	 *             nicht unterstützt wird
	 */
	public static Compressor getCompressor(String compression) {
		if (Compressor.GZIP.equals(compression)) {
			return new GZIPCompressor();
		} else if (Compressor.NONE.equals(compression)) {
			return new NoneCompressor();
		} else {
			throw new UnsupportedOperationException("Kompression nicht unterstützt: " + compression);
		}
	}
}
