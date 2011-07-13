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

import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Hintze
 * @since 03.02.2010
 */
public class Plan {

	private List<RemoveDirAction> removeDirActions;
	private List<RemoveFileAction> removeFileActions;
	private List<InstallFileAction> installFileActions;
	private List<UpdateFileMetadataAction> updateFileMetadataActions;
	private List<UpdateFileAction> updateFileActions;
	private final List<MakeDirAction> makeDirActions;

	public Plan(List<MakeDirAction> mkDir, List<RemoveDirAction> rmDir, List<RemoveFileAction> rmFile, List<InstallFileAction> install,
			List<UpdateFileMetadataAction> updateMetadata, List<UpdateFileAction> update) {
		makeDirActions = mkDir;
		removeDirActions = rmDir;
		removeFileActions = rmFile;
		installFileActions = install;
		updateFileMetadataActions = updateMetadata;
		updateFileActions = update;
	}

	public List<MakeDirAction> getMakeDirActions() {
		return Collections.unmodifiableList(makeDirActions);
	}

	public List<InstallFileAction> getInstallFileActions() {
		return Collections.unmodifiableList(installFileActions);
	}

	public List<RemoveDirAction> getRemoveDirActions() {
		return Collections.unmodifiableList(removeDirActions);
	}

	public List<RemoveFileAction> getRemoveFileActions() {
		return Collections.unmodifiableList(removeFileActions);
	}

	public List<UpdateFileAction> getUpdateFileActions() {
		return Collections.unmodifiableList(updateFileActions);
	}

	public List<UpdateFileMetadataAction> getUpdateFileMetadataActions() {
		return Collections.unmodifiableList(updateFileMetadataActions);
	}

	public int getTotalNumberOfActions() {
		return makeDirActions.size() + removeDirActions.size() + removeFileActions.size() + installFileActions.size()
				+ updateFileActions.size() + updateFileMetadataActions.size();
	}

}
