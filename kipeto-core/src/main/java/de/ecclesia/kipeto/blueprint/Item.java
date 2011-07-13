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
package de.ecclesia.kipeto.blueprint;

import java.io.DataOutputStream;
import java.io.IOException;

import de.ecclesia.kipeto.repository.Blob;

/**
 * Basisklasse für Elemente (z.B. Dateien und Unterverzeichnisse), die in einem
 * Directory auftauchen können.
 * 
 * @author Daniel Hintze
 * @since 21.01.2010
 */
public abstract class Item implements Comparable<Item> {

	/** Id des Item */
	private final String itemId;

	/** Name im Verzeichnis */
	private final String name;

	public Item(String name, Blob blob) {
		this(name, blob.id());
	}

	protected Item(String name, String itemId) {
		this.itemId = itemId;
		this.name = name;
	}

	/** Liefert die Id des Items */
	public String itemId() {
		return itemId;
	}

	/** Name des Items im Directory */
	public String name() {
		return name;
	}

	/** Typ des Items */
	public abstract String type();

	/**
	 * Serialisiert den Inhalt des Items (inklusive Name und ItemId, aber
	 * exklusive Type!) in den übergebenen OutputStream.
	 * 
	 * @param dataOutputStream Ziel der Serialisierung
	 * @throws IOException
	 */
	public abstract void writeToStream(DataOutputStream dataOutputStream) throws IOException;

	public int compareTo(Item other) {
		return other.name.compareTo(this.name);
	}
	
}
