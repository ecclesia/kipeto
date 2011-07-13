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

import de.ecclesia.kipeto.job.UpdateJob.Phase;

/**
 * @author Daniel Hintze
 * @since 08.02.2010
 */
public class SubProgressEvent extends JobEvent {

	private int workedSteps;

	private int totalSteps;

	private final Object source;

	public SubProgressEvent(Phase phase, Object source, int workedSteps, int totalSteps) {
		super(phase);

		this.source = source;
		this.workedSteps = workedSteps;
		this.totalSteps = totalSteps;
	}

	public Object source() {
		return source;
	}

	public int totalSteps() {
		return totalSteps;
	}

	public int workedSteps() {
		return workedSteps;
	}

}
