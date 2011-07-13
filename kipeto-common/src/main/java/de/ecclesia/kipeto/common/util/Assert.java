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

/**
 * Hilfsklasse zum Überprüfen von Annahmen, die immer zutreffen sollten.
 * <p>
 * Alle Methoden werfen eine {@link AssertionFailedException} wenn die Annahme nicht zutrifft. Dies ist eine
 * {@link RuntimeException} und sollte auch nie gefangen werden, da es sich ja um sichere Annahmen handelt.
 * 
 * Es gibt immer eine Version mit und ohne <code>message</code>. Diese Nachricht sollte gesetzt werden, wenn dem
 * EmpfÃ¤nger der Exception damit eine Hilfestellung geliefert wird.
 * 
 * @version 1.0
 * @author Dominik Mähl <dmaehl@ecclesia.de>
 */
public class Assert {

	public static void isTrue(boolean expression) {
		isTrue(expression, "Ausdruck ist false");
	}

	public static void isTrue(boolean expression, String message) {
		if (!expression) {
			throw new RuntimeException(message);
		}
	}

	public static void isFalse(boolean expression) {
		isFalse(expression, "Ausdruck ist true");
	}

	public static void isFalse(boolean expression, String message) {
		if (expression) {
			throw new RuntimeException(message);
		}
	}

	public static void isNotNull(Object object) {
		isNotNull(object, "Objekt ist null");
	}

	public static void isNotNull(Object object, String message) {
		if (object == null) {
			throw new RuntimeException(message);
		}
	}

	public static void isNull(Object object) {
		isNull(object, "Objekt ist nicht null");
	}

	public static void isNull(Object object, String message) {
		if (object != null) {
			throw new RuntimeException(message);
		}
	}

}
