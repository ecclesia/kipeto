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
package de.ecclesia.kipeto.job.event;

import java.io.InputStream;

import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.job.UpdateJob.Phase;

public class BlueprintReceivedEvent extends JobEvent {

	private final Blueprint bluePrint;
	
	private final InputStream iconInputStream;

	public BlueprintReceivedEvent(Phase phase, Blueprint bluePrint, InputStream iconInputStream) {
		super(phase);
		this.bluePrint = bluePrint;
		this.iconInputStream = iconInputStream;
	}

	public Blueprint getBlueprint() {
		return bluePrint;
	}
	
	/**
	 * Liefert den InputStream zum Icon des Blueprints zurück
	 * 
	 * @return null, falls der Blueprint kein Icon hat.
	 */
	public InputStream getIconInputStream() {
		return iconInputStream;
	}
}
