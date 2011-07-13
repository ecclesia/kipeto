/*
 * #%L
 * Kipeto Tools
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
package de.ecclesia.kipeto.tools;

import java.io.IOException;

import de.ecclesia.kipeto.tools.blueprint.CreateBlueprintApp;
import de.ecclesia.kipeto.tools.blueprint.CreateBlueprintOptions;
import de.ecclesia.kipeto.tools.cpref.CopyReferenceApp;
import de.ecclesia.kipeto.tools.cpref.CopyReferenceOptions;
import de.ecclesia.kipeto.tools.deploy.DeployBlueprintApp;
import de.ecclesia.kipeto.tools.deploy.DeployBlueprintOptions;
import de.ecclesia.kipeto.tools.gbc.CollectGarbageApp;
import de.ecclesia.kipeto.tools.gbc.CollectGarbageOptions;
import de.ecclesia.kipeto.tools.lsref.ListReferencesApp;
import de.ecclesia.kipeto.tools.lsref.ListReferencesOptions;
import de.ecclesia.kipeto.tools.meta.UpdateMetadataApp;
import de.ecclesia.kipeto.tools.meta.UpdateMetadataOptions;
import de.ecclesia.kipeto.tools.rmref.RemoveReferenceApp;
import de.ecclesia.kipeto.tools.rmref.RemoveReferenceOptions;

public class RepositoryToolApp {

	private enum App {
		LIST_REFERENCES, UPDATE_METADATA, DEPLOY_BLUEPRINT, COLLECT_GARBAGE, COPY_REFERENCE, REMOVE_REFERENCE, CREATE_BLUEPRINT
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			printUsage();
			System.exit(1);
		}

		try {
			App tool = App.valueOf(args[0].toUpperCase());
			String[] options = new String[args.length - 1];
			System.arraycopy(args, 1, options, 0, args.length - 1);

			run(tool, options);
		} catch (IllegalStateException e) {
			printUsage();
			System.exit(1);
		}
	}

	private static void run(App app, String[] args) throws IOException {
		if (app == App.LIST_REFERENCES) {
			new ListReferencesApp(new ListReferencesOptions(args)).run();
		} else if (app == App.UPDATE_METADATA) {
			new UpdateMetadataApp(new UpdateMetadataOptions(args)).run();
		} else if (app == App.DEPLOY_BLUEPRINT) {
			new DeployBlueprintApp(new DeployBlueprintOptions(args)).run();
		} else if (app == App.COLLECT_GARBAGE) {
			new CollectGarbageApp(new CollectGarbageOptions(args)).run();
		} else if (app == App.COPY_REFERENCE) {
			new CopyReferenceApp(new CopyReferenceOptions(args)).run();
		} else if (app == App.REMOVE_REFERENCE) {
			new RemoveReferenceApp(new RemoveReferenceOptions(args)).run();
		} else if (app == App.CREATE_BLUEPRINT) {
			new CreateBlueprintApp(new CreateBlueprintOptions(args)).run();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private static void printUsage() {
		StringBuilder builder = new StringBuilder();
		builder.append("Usage: repository_tool [");
		App[] values = App.values();
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				builder.append(" | ");
			}
			builder.append(values[i].toString().toLowerCase());
		}
		builder.append("] [options...]");

		System.err.println(builder.toString());
	}
}
