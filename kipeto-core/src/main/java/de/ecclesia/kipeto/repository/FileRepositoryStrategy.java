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
package de.ecclesia.kipeto.repository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.common.util.Assert;
import de.ecclesia.kipeto.common.util.Streams;

/**
 * RepositoryStrategy zur Nutzung eines lokalen Dateisystems zur Speicherung von
 * Objekten.
 * 
 * @author Daniel Hintze
 * @since 21.01.2010
 */
public class FileRepositoryStrategy extends WritingRepositoryStrategy {

	private final Logger logger = LoggerFactory.getLogger(FileRepositoryStrategy.class);

	/** Wurzelverzeichnis des Reposiorys */
	private final File rootDir;

	/** Verzeichnis zur Speicherung der Objekte */
	private File objs;

	/** Verzeichnis zur Speicherung von Referenzen */
	private File refs;

	private final File tempDir;

	/**
	 * Erzeugt ein neues FileSystemStorageStrategy mit dem übergebenen
	 * Wurzelverzeichnis.
	 * 
	 * @param rootDir
	 *            Wurzelverzeichnis des Repositorys.
	 */
	public FileRepositoryStrategy(File rootDir, File tempDir) {
		this.tempDir = tempDir;
		Assert.isNotNull(rootDir, "No root-directory supplied");
		Assert.isTrue(rootDir.exists(), "'" + rootDir.getAbsolutePath() + "' does not exist");

		Assert.isTrue(rootDir.isDirectory(), "'" + rootDir.getAbsolutePath() + "' is not a directory");

		this.rootDir = rootDir;

		createRepositoryDirectoryStructure(this.rootDir);
	}

	@Override
	protected void store(String id, InputStream inputStream) throws IOException {
		// UPD-17: Da sonst die Datei-Rechte des Temp-Verzeichnis verhindern,
		// dass andere Benutzer das Repository lesen können, muss hier das
		// eingene Temp-Verzeichnis verwendet werden.
		File file = File.createTempFile(getClass().getName(), null, tempDir);
		
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
		Streams.copyStream(inputStream, outputStream, true);

		storeFileByRename(id, file);
	}

	public void storeFileByRename(String id, File file) {
		Assert.isNotNull(id);
		Assert.isNotNull(file);

		File item = fileForItem(id);

		if (item.exists()) {
			// Eine Item mit dem gleichen Hash ist bereits im Repository
			// gespeichert. Aus Paranoia wird hier überprüft, ob diese
			// Dateien auch gleich groß sind. Sollten Sie dies tatsächlich
			// jemals nicht sein, dann sollten wir uns was besseres
			// einfallen lassen.
			if (file.length() != item.length()) {
				throw new RuntimeException(file.getAbsolutePath() + " und " + item.getAbsolutePath()
						+ " sind unterschiedlich (groß), haben aber die gleiche Id!");
			}

			// Temp-Datei löschen, da sie nicht weiter benötigt wird.
			file.delete();
		} else {
			// Das Item gibt es bis jetzt noch nicht im Repository, also
			// jetzt anlegen.
			boolean success = file.renameTo(item);

			// Theoretisch könnte es bei konkurrierende Updates sein, dass zwei
			// Instanzen versuchen, gleichzeitig eine Datei zu schreiben. Wenn
			// das rename also fehlschlägt, muss überprüft werden, ob die Datei
			// nun existiert.
			if (!success) {
				Assert.isTrue(success, "Rename from " + file + " to " + item + " failed");
			}
		}
	}

	@Override
	public void createReference(String reference, String id) throws IOException {
		File referenceFile = fileForReference(reference);

		if (!referenceFile.exists()) {
			new File(referenceFile.getParent()).mkdirs();
			referenceFile.createNewFile();
		}

		FileWriter fileWriter = new FileWriter(referenceFile);
		fileWriter.write(id);
		fileWriter.flush();
		fileWriter.close();
	}

	@Override
	public String resolveReference(String reference) throws IOException {
		File referenceFile = new File(refs, reference);

		if (!referenceFile.exists()) {
			return null;
		}

		FileReader fileReader = new FileReader(referenceFile);
		BufferedReader reader = new BufferedReader(fileReader);

		String id = reader.readLine();

		fileReader.close();

		return id;
	}

	@Override
	public long sizeInRepository(String id) {
		File item = fileForItem(id);

		if (!item.exists()) {
			throw new IdNotFoundException(id);
		}

		return item.length();
	}

	@Override
	protected InputStream retrieve(String id) {
		File item = fileForItem(id);

		logger.debug("Retrieve Stream {} from {}", id, item);

		if (!item.exists()) {
			throw new IdNotFoundException(id);
		}

		try {
			return new BufferedInputStream(new FileInputStream(item));
		} catch (FileNotFoundException e) {
			throw new IdNotFoundException(id);
		}
	}

	@Override
	public boolean remove(String id) {
		return fileForItem(id).delete();
	}

	@Override
	public boolean removeReference(String reference) {
		return fileForReference(reference).delete();
	}

	@Override
	public boolean contains(String id) throws IOException {
		return id != null && fileForItem(id).exists();
	}

	/**
	 * Ermittelt anhand der <code>SUBDIR_POLICY</code> das Unterverzeichnis, in
	 * dem das Item zu der übergebenen Id gepeichert ist bzw. werden muss.
	 * 
	 * } Das Verzeichnis wird angelegt, falls es nicht existiert.
	 * 
	 * @param id
	 *            des Items
	 * @return Verzeichnis, in dem das Item gespeichert ist bzw. werden soll.
	 */
	private File subDirForId(String id) {
		File subDir = new File(objs, id.substring(0, SUBDIR_POLICY));

		if (!subDir.exists()) {
			subDir.mkdirs();
		}

		return subDir;
	}

	/**
	 * Gibt das mit der Id korrespondierende File-Objekt zurück. (Die Datei muss
	 * nicht existieren)
	 * 
	 * @param id
	 * @return
	 */
	private File fileForItem(String id) {
		File subDir = subDirForId(id);
		String fileName = id.substring(SUBDIR_POLICY);
		return new File(subDir, fileName);
	}

	private File fileForReference(String reference) {
		String[] referenceParts = reference.split("/");
		if (referenceParts.length == 1) {
			return new File(refs, reference);
		} else {
			int lastSlash = reference.lastIndexOf("/");

			String subDirPath = reference.substring(0, lastSlash);
			File subDir = new File(refs, subDirPath);
			if (!subDir.exists()) {
				subDir.mkdirs();
			}

			return new File(subDir, referenceParts[referenceParts.length - 1]);
		}
	}

	/**
	 * Erzeugt die Verzeichnis-Struktur des Repositorys, falls die Verzeichinsse
	 * noch nicht existieren.
	 * 
	 * @param rootDir
	 *            Wurzelverzeichnis zur Anlage der Verzeichnis-Struktur
	 */
	private void createRepositoryDirectoryStructure(File rootDir) {
		this.objs = new File(rootDir, OBJECT_DIR);
		if (!objs.exists()) {
			if (!objs.mkdirs()) {
				throw new RuntimeException("Could not create dir: " + objs.getAbsolutePath());
			}
		}

		this.refs = new File(rootDir, REFERENCE_DIR);
		if (!refs.exists()) {
			if (!refs.mkdirs()) {
				throw new RuntimeException("Could not create dir: " + refs.getAbsolutePath());
			}
		}
	}

	@Override
	public void close() {
		// gibt nichts zu schliessen
	}

	@Override
	public List<Reference> allReferences() throws IOException {
		List<Reference> list = new ArrayList<Reference>();

		for (File file : refs.listFiles()) {
			String name = file.getName();
			String id = resolveReference(file.getName());

			list.add(new Reference(id, name));
		}

		return list;
	}

	@Override
	public List<String> allObjects() {
		List<String> list = new ArrayList<String>();

		for (File dir : objs.listFiles()) {
			for (File object : dir.listFiles()) {
				list.add(dir.getName() + object.getName());
			}
		}

		return list;
	}
	
	@Override
	public File tempDir() {
		return tempDir;
	}
}
