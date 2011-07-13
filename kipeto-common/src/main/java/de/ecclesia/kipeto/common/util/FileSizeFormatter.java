/*
 * #%L
 * Kipeto Common
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
package de.ecclesia.kipeto.common.util;

import java.text.NumberFormat;

public abstract class FileSizeFormatter {
	
	private FileSizeFormatter() {
	}

	public static String formateBytes(long bytes, int decimalPos) {
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		double size = bytes;

		if (decimalPos >= 0) {
			numberFormat.setMaximumFractionDigits(decimalPos);
		}

		if (bytes > (8L * 1024 * 1024 * 1024)) {
			return numberFormat.format(size / (1024 * 1024 * 1024)).concat(" GB");
		}

		if (bytes > (8L * 1024 * 1024)) {
			return numberFormat.format(size / (1024 * 1024)).concat(" MB");
		}

		if (bytes > (8 * 1024)) {
			return numberFormat.format(size / 1024).concat(" KB");
		}

		return numberFormat.format(bytes).concat(" bytes");
	}

}
