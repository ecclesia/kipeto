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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPRepositoryStrategy extends WritingRepositoryStrategy {

	private final Logger logger = LoggerFactory.getLogger(SFTPRepositoryStrategy.class);

	private static final String UNIX_PATH_SEPERATOR = "/";

	/** SFTP-Channel für die Operationen */
	private ChannelSftp channel;

	private Session session;

	/** Verzeichnis zur Speicherung der Objekte */
	private final String objs;

	/** Verzeichnis zur Speicherung von Referenzen */
	private final String refs;

	private final AuthenticationProvider authProvider;

	private String server;

	private final File tempDir;

	/**
	 * @param url
	 *            user@server:pfad
	 * @throws SftpException
	 * @throws JSchException
	 */
	public SFTPRepositoryStrategy(String url, AuthenticationProvider authenticationProvider, File tempDir) {
		this.authProvider = authenticationProvider;
		this.tempDir = tempDir;

		String path = null;

		try {
			server = getServer(url);
			connect();

			path = getPath(url);
			this.objs = path + UNIX_PATH_SEPERATOR + OBJECT_DIR;
			this.refs = path + UNIX_PATH_SEPERATOR + REFERENCE_DIR;

			this.channel.cd(path);

		} catch (JSchException e) {
			throw new RuntimeException(e);
		} catch (SftpException e) {
			if (e.getMessage().endsWith("No such file")) {
				throw new RuntimeException("Invalid repository location: " + path);
			}
			throw new RuntimeException(e);
		}
	}

	private String knownHosts() {
		return System.getProperty("user.home") + "/" + ".ssh" + "/" + "known_hosts";
	}

	private void connect() throws JSchException {
		JSch jSch = new JSch();
		jSch.setKnownHosts(knownHosts());

		if (authProvider.isPrivateKey()) {
			jSch.addIdentity(authProvider.getPrivateKey().getAbsolutePath());
		}

		session = jSch.getSession(authProvider.getUsername(), server);
		session.setConfig("StrictHostKeyChecking", "no");

		if (authProvider.isPassword()) {
			session.setPassword(authProvider.getPassword());
		} else if (authProvider.isUserInfo()) {
			session.setUserInfo(authProvider.getUserInfo());
		}

		session.connect();

		channel = (ChannelSftp) session.openChannel("sftp");
		channel.connect();
	}

	@Override
	public void close() {
		if (channel != null) {
			channel.quit();
			channel.exit();
			channel.disconnect();
		}

		if (session != null) {
			session.disconnect();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Reference> allReferences() throws IOException {
		List<Reference> list = new ArrayList<Reference>();

		try {
			Vector<LsEntry> ls = channel.ls(refs);
			for (LsEntry entry : ls) {
				String name = entry.getFilename();

				if (isSpecialFile(name)) {
					continue;
				} else {
					String id = resolveReference(name);
					list.add(new Reference(id, name));
				}
			}

			return list;
		} catch (SftpException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> allObjects() {
		List<String> list = new ArrayList<String>();

		try {
			Vector<LsEntry> ls = channel.ls(objs);
			for (LsEntry entry : ls) {
				String prae = entry.getFilename();

				if (!isSpecialFile(prae)) {
					Vector<LsEntry> subls = channel.ls(objs + UNIX_PATH_SEPERATOR + prae);

					for (LsEntry subEntry : subls) {
						if (!isSpecialFile(subEntry.getFilename())) {
							list.add(prae + subEntry.getFilename());
						}
					}
				}
			}

			return list;
		} catch (SftpException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isSpecialFile(String filename) {
		return filename.equals(".") || filename.equals("..");
	}

	private String getServer(String url) {
		// ssh://user@server:path
		int left = url.indexOf("@");
		int right = url.lastIndexOf(":");

		if (left == -1) {
			left = url.indexOf("//") + 1;
		}

		if (right == -1) {
			right = url.length();
		}

		String server = url.substring(left + 1, right);

		logger.debug("Server: {}", server);

		return server;
	}

	private String getPath(String url) {
		// ssh://user@server:path
		int right = url.lastIndexOf(":");

		if (right == -1) {
			throw new IllegalStateException("Malformed Url '" + url + "' expedetd ssh://user@server:path");
		}

		String path = url.substring(right + 1);

		logger.debug("Path: {}", path);

		return path;
	}

	@Override
	public void createReference(String reference, String id) throws IOException {
		String referenceFile = pathToReference(reference);

		File tempFile = File.createTempFile(getClass().getName(), null);
		tempFile.deleteOnExit();

		FileWriter fileWriter = new FileWriter(tempFile);
		fileWriter.write(id);
		fileWriter.flush();
		fileWriter.close();

		try {
			channel.put(new FileInputStream(tempFile), referenceFile);
		} catch (SftpException e) {
			throw new RuntimeException(e);
		} finally {
			tempFile.delete();
		}

	}

	public void symlinkReference(String name, String target) {
		try {
			String pathToTarget = pathToReference(target);
			String pathToLink = pathToReference(name);

			channel.symlink(pathToTarget, pathToLink);
		} catch (SftpException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean remove(String id) {
		return safeRm(pathToItem(id));
	}

	@Override
	public boolean removeReference(String reference) {
		return safeRm(pathToReference(reference));
	}

	@Override
	protected void store(String id, InputStream inputStream) throws IOException {
		try {
			String path = pathToItem(id);
			channel.put(inputStream, path);
		} catch (SftpException e) {
			throw new RuntimeException(e);
		} finally {
			inputStream.close();
		}
	}

	@Override
	public boolean contains(String id) throws IOException {
		try {
			channel.ls(pathToItem(id));
			return true;
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return false;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public String resolveReference(String reference) throws IOException {
		String referenceFile = refs + UNIX_PATH_SEPERATOR + reference;

		try {
			InputStream inputStream = channel.get(referenceFile);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader reader = new BufferedReader(inputStreamReader);

			String id = reader.readLine();
			reader.close();

			return id;
		} catch (SftpException e) {
			return null;
		}
	}

	@Override
	protected InputStream retrieve(String id) throws IOException {
		try {
			return channel.get(pathToItem(id));
		} catch (SftpException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long sizeInRepository(String id) throws IOException {
		try {
			Vector<?> result = channel.ls(pathToItem(id));
			LsEntry entry = (LsEntry) result.firstElement();

			return entry.getAttrs().getSize();
		} catch (SftpException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Ermittelt anhand der <code>SUBDIR_POLICY</code> das Unterverzeichnis, in dem das Item zu der übergebenen Id
	 * gepeichert ist bzw. werden muss.
	 * 
	 * Das Verzeichnis wird angelegt, falls es nicht existiert.
	 * 
	 * @param id
	 *            des Items
	 * @return Verzeichnis, in dem das Item gespeichert ist bzw. werden soll.
	 */
	private String subDirForId(String id) {
		String path = objs + UNIX_PATH_SEPERATOR + id.substring(0, SUBDIR_POLICY);

		safeMkdir(path);

		return path;
	}

	/**
	 * Gibt das mit der Id korrespondierende File-Objekt zurück. (Die Datei muss nicht existieren)
	 * 
	 * @param id
	 * @return
	 */
	private String pathToItem(String id) {
		return subDirForId(id) + UNIX_PATH_SEPERATOR + id.substring(SUBDIR_POLICY);
	}

	private String pathToReference(String reference) {
		String[] referenceParts = reference.split("/");
		if (referenceParts.length == 1) {
			return refs + UNIX_PATH_SEPERATOR + reference;
		} else {
			int lastSlash = reference.lastIndexOf("/");

			String subDirPath = reference.substring(0, lastSlash);
			String subDir = refs + UNIX_PATH_SEPERATOR + subDirPath;

			return subDir + UNIX_PATH_SEPERATOR + referenceParts[referenceParts.length - 1];
		}
	}

	private boolean safeMkdir(String path) {
		try {
			logger.debug("mkdir {}", path);
			channel.mkdir(path);
			return true;
		} catch (SftpException e) {
			return false;
		}
	}

	private boolean safeRm(String path) {
		try {
			logger.debug("rm {}", path);
			channel.rm(path);
			return true;
		} catch (SftpException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public File tempDir() {
		return tempDir;
	}

}
