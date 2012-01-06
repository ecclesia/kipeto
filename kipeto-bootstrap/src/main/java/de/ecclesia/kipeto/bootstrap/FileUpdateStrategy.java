package de.ecclesia.kipeto.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.common.util.Streams;

public class FileUpdateStrategy implements IUpdateStrategy {

	private static final Logger log = LoggerFactory.getLogger(FileUpdateStrategy.class);
	private File remoteJarFile;

	public FileUpdateStrategy(String repositoryUrl) {
		File repositoryDir = new File(repositoryUrl);
		remoteJarFile = new File(repositoryDir, BootstrapApp.DIST_DIR + "/" + BootstrapApp.JAR_FILENAME);
	}
	
	public boolean isUpdateAvailable(File localJarFile) throws Exception {
		if (!localJarFile.exists()) {
			log.debug("Local jar file is missing");
			return remoteJarFile.exists();
		}
		
		long localLength = localJarFile.length();
		long remoteLength = remoteJarFile.length();
		long localLastModified = localJarFile.lastModified();
		long remoteLastModified = remoteJarFile.lastModified();
		
		log.debug("Local jar length: {}; lastModified: {}", new Object[]{localLength, localLastModified});
		log.debug("Remote jar length: {}; lastModified: {}", new Object[]{remoteLength, remoteLastModified});
		if (localLength != remoteLength || localLastModified != remoteLastModified) {
			log.debug("Local jar file is outdated");
			return true;
		}
		
		return false;
	}

	public String getUpdateUrl() {
		return remoteJarFile.getAbsolutePath();
	}
	
	public long getUpdateSize() {
		return remoteJarFile.length();
	}

	public Date downloadUpdate(OutputStream destinationStream) throws Exception {
		Streams.copyStream(new FileInputStream(remoteJarFile), destinationStream, true);
		return new Date(remoteJarFile.lastModified());
	}
	
}
