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
package de.ecclesia.kipeto.tools.blueprint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.blueprint.Directory;
import de.ecclesia.kipeto.blueprint.DirectoryItem;
import de.ecclesia.kipeto.blueprint.FileItem;
import de.ecclesia.kipeto.blueprint.Item;
import de.ecclesia.kipeto.compressor.Compressor;
import de.ecclesia.kipeto.job.UpdateJob;
import de.ecclesia.kipeto.repository.Blob;
import de.ecclesia.kipeto.repository.WritingRepository;

/**
 * Simple Factory um Blueprints zu erstellen.
 * 
 * @author Daniel Hintze
 * @since 27.01.2010
 */
public class BlueprintFactory {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final WritingRepository writingRepository;

	public BlueprintFactory(WritingRepository writingRepository) {
		this.writingRepository = writingRepository;
	}

	/**
	 * Erstellt einen Blueprint aus dem übergebenen RootDir. Blobs und Directorys werden sofort im übergebenen
	 * Repository gespeichert. Der Blueprint selbst wird <b>nicht</b> von gepeichert.<br/>
	 * <br/>
	 * Alle Dateien werden GZIP komprimiert
	 * 
	 * @param programId
	 *            Name
	 * @param description
	 *            Beschreibung
	 * @param rootDir
	 *            Einstiegsverzeichnis
	 * @return
	 */
	public Blueprint fromDir(String programId, String description, File rootDir) {
		return new Blueprint(programId, description, processDir(rootDir), null);
	}

	/**
	 * Erstellt einen Blueprint aus dem übergebenen RootDir. Blobs und Directorys werden sofort im übergebenen
	 * Repository gespeichert. Der Blueprint selbst wird <b>nicht</b> von gepeichert.<br/>
	 * <br/>
	 * Alle Dateien werden GZIP komprimiert
	 * 
	 * @param programId
	 *            Name
	 * @param description
	 *            Beschreibung
	 * @param rootDir
	 *            Einstiegsverzeichnis
	 * @param icon
	 *            Icon
	 * @return
	 */
	public Blueprint fromDir(String programId, String description, File rootDir, File icon) {
		return new Blueprint(programId, description, processDir(rootDir), processBlob(icon));
	}

	/**
	 * Verarbeitet ein Verzeichnis
	 * 
	 * @param dirFile
	 * @return
	 */
	private Directory processDir(File dirFile) {
		List<Item> items = new ArrayList<Item>();

		File[] files = dirFile.listFiles();
		logger.info("processing directory {} with {} entries", dirFile, files.length);

		for (File file : files) {
			if (file.getName().equals(UpdateJob.LOCK_FILE)) {
				logger.info("skipping lockfile {}", file);
			} else if (file.isDirectory()) {
				logger.debug("adding directory {}", file);
				items.add(new DirectoryItem(file.getName(), processDir(file)));
			} else {
				// LastModified nur sekundengenau speichern, da Windows zwar
				// millisekundengenau speichern kann, Linux Dateisysteme (ext3,
				// ReiserFS, etc.) aber nur sekundengenau. Es kommst sonst zu
				// Problemen, wenn der Blueprint auf einem Linux-System gebaut
				// und auf ein Windows-System deployd wird:
				long lastModified = (file.lastModified() / 1000) * 1000;
				logger.debug("adding blob {}, lastModified: ", file, lastModified);

				items.add(new FileItem(file.getName(), processBlob(file), lastModified));
			}
		}

		Directory dir = new Directory(items.toArray(new Item[items.size()]));
		try {
			String id = writingRepository.store(dir);
			logger.info("added directory {} -> {}", dirFile, id);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return dir;
	}

	/**
	 * Verarbeitet ein Blob (eine Datei)
	 * 
	 * @param blobFile
	 * @return
	 */
	private Blob processBlob(File blobFile) {
		logger.trace("processing blob {}", blobFile);

		try {
			Blob blob = new Blob(Compressor.GZIP, new FileInputStream(blobFile), blobFile.length());
			String id = writingRepository.store(blob);
			logger.info("added blob {} -> {}", blobFile, id);

			return blob;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
