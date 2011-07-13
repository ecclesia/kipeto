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

import java.util.Collection;

public class Strings {

	private static final String CROP_STRING = "...";

	public static String crop(String source, int length) {
		if (source.length() <= length) {
			return source;
		}

		String croppedString;

		if (length > 3) {
			croppedString = source.substring(0, length - 3);
			croppedString += CROP_STRING;
		} else {
			croppedString = source.substring(0, length);
		}

		return croppedString;
	}

	public static String padLeft(String source, int paddingSize, String paddingString) {
		String target = source;
		while (target.length() < paddingSize) {
			target = paddingString + target;
		}
		return target;
	}

	public static String padRight(String source, int paddingSize, String paddingString) {
		String target = source;
		while (target.length() < paddingSize) {
			target = target + paddingString;
		}
		return target;
	}

	public static String firstNonNull(String... strings) {
		for (String string : strings) {
			if (string != null) {
				return string;
			}
		}

		return null;
	}

	public static boolean equalsAtLeastOne(String string, String... conditions) {
		for (String condition : conditions) {
			if (string.equalsIgnoreCase(condition)) {
				return true;
			}
		}
		return false;
	}

	public static String join(String delimiter, String... strings) {
		String result = "";
		for (String string : strings) {
			if (string != null && string.length() > 0) {
				if (result.length() > 0) {
					result += delimiter;
				}
				result += string;
			}
		}

		return result;
	}

	public static String capitalize(String string) {
		if (string.length() > 0) {
			char[] chars = string.toLowerCase().toCharArray();
			chars[0] = Character.toUpperCase(chars[0]);
			string = String.valueOf(chars);
		}
		return string;
	}

	public static String sanitize(String unsanitizedString) {
		if (unsanitizedString == null) {
			return null;
		}
		unsanitizedString = unsanitizedString.replace('„', '\"');
		unsanitizedString = unsanitizedString.replace('“', '\"');
		unsanitizedString = unsanitizedString.replace(String.valueOf((char) 26), " ");
		return unsanitizedString;
	}

	public static String longestStringOf(Collection<String> strings) {
		String longest = null;
		for (String string : strings) {
			if (longest == null || (string != null && string.length() > longest.length())) {
				longest = string;
			}
		}
		return longest;
	}
}
