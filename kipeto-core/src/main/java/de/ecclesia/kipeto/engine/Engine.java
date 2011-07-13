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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.ecclesia.kipeto.common.util.Assert;
import de.ecclesia.kipeto.repository.ReadingRepository;

/**
 * @author Daniel Hintze
 * @since 03.02.2010
 */
public abstract class Engine {

	private final ReadingRepository repository;

	public Engine(ReadingRepository repository) {
		Assert.isNotNull(repository);

		this.repository = repository;
	}

	protected ReadingRepository getRepository() {
		return repository;
	}

	private List<ActionListener> listeners = new ArrayList<ActionListener>();

	protected abstract void handle(InstallFileAction action) throws IOException;

	protected abstract void handle(UpdateFileMetadataAction action);

	protected abstract void handle(UpdateFileAction action) throws IOException;

	protected abstract void handle(MakeDirAction action);

	protected abstract void handle(RemoveDirAction action);

	protected abstract void handle(RemoveFileAction action);


	public void process(Plan plan) throws IOException {
		int workedTotal = 0;

		int i = 0;
		for (RemoveFileAction action : plan.getRemoveFileActions()) {
			fireActionEvent(new ActionProgressEvent<Action>(action, 0, 0));
			handle(action);
			fireActionEvent(new ActionCompletedEvent<RemoveFileAction>(action, plan, ++i, ++workedTotal));
		}

		i = 0;
		for (RemoveDirAction action : plan.getRemoveDirActions()) {
			fireActionEvent(new ActionProgressEvent<Action>(action, 0, 0));
			handle(action);
			fireActionEvent(new ActionCompletedEvent<RemoveDirAction>(action, plan, ++i, ++workedTotal));
		}

		i = 0;
		for (MakeDirAction action : plan.getMakeDirActions()) {
			fireActionEvent(new ActionProgressEvent<Action>(action, 0, 0));
			handle(action);
			fireActionEvent(new ActionCompletedEvent<MakeDirAction>(action, plan, ++i, ++workedTotal));
		}

		i = 0;
		for (UpdateFileMetadataAction action : plan.getUpdateFileMetadataActions()) {
			fireActionEvent(new ActionProgressEvent<Action>(action, 0, 0));
			handle(action);
			fireActionEvent(new ActionCompletedEvent<UpdateFileMetadataAction>(action, plan, ++i, ++workedTotal));
		}

		i = 0;
		for (UpdateFileAction action : plan.getUpdateFileActions()) {
			fireActionEvent(new ActionProgressEvent<Action>(action, 0, 0));
			handle(action);
			fireActionEvent(new ActionCompletedEvent<UpdateFileAction>(action, plan, ++i, ++workedTotal));
		}

		i = 0;
		for (InstallFileAction action : plan.getInstallFileActions()) {
			fireActionEvent(new ActionProgressEvent<Action>(action, 0, 0));
			handle(action);
			fireActionEvent(new ActionCompletedEvent<InstallFileAction>(action, plan, ++i, ++workedTotal));
		}
	}

	public void addActionListener(ActionListener listener) {
		listeners.add(listener);
	}

	public void removeActionListener(ActionListener listener) {
		listeners.remove(listener);
	}

	protected void fireActionEvent(ActionEvent<?> completedEvent) {
		for (ActionListener listener : listeners) {
			listener.handleActionEvent(completedEvent);
		}
	}

}
