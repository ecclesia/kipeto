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

import java.io.File;

import de.ecclesia.kipeto.blueprint.FileItem;
import de.ecclesia.kipeto.common.util.Assert;

/**
 * @author Daniel Hintze
 * @since 02.02.2010
 */
public class InstallFileAction implements Action {

	private final FileItem fileItem;
	private final File target;

	public InstallFileAction(FileItem fileItem, File target) {
		this(fileItem, target, true);
	}

	public InstallFileAction(FileItem fileItem, File target, boolean checkExists) {
		Assert.isNotNull(fileItem);

		if (checkExists) {
			Assert.isFalse(target.exists(), "Target File already exists: " + target.getAbsolutePath());
		}

		this.target = target;
		this.fileItem = fileItem;
	}

	public File getTarget() {
		return target;
	}

	public FileItem getFileItem() {
		return fileItem;
	}
	
	@Override
	public String toString() {
		return "Install blob " + fileItem.itemId() + " to " + target.getAbsolutePath();
	}

}
