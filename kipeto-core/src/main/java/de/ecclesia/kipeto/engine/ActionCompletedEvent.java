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
package de.ecclesia.kipeto.engine;

/**
 * @author Daniel Hintze
 * @since 08.02.2010
 */
public class ActionCompletedEvent<E extends Action> extends ActionEvent<E> {

	private final int actionsWorkedSameKind;
	private final int actionsWorkedTotal;
	private final Plan plan;

	public ActionCompletedEvent(E action, Plan plan, int actionsWorkedSameKind, int actionsWorkedTotal) {
		super(action);
		
		this.plan = plan;
		this.actionsWorkedSameKind = actionsWorkedSameKind;
		this.actionsWorkedTotal = actionsWorkedTotal;
	}


	public Plan plan() {
		return plan;
	}
	
	public int actionsWorkedSameKind() {
		return actionsWorkedSameKind;
	}

	public int actionsWorkedTotal() {
		return actionsWorkedTotal;
	}

}
