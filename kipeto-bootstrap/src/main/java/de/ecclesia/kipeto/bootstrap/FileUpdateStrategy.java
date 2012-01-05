package de.ecclesia.kipeto.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Date;

import de.ecclesia.kipeto.common.util.Streams;

public class FileUpdateStrategy implements IUpdateStrategy {

	private File remoteJarFile;

	public FileUpdateStrategy(String repositoryUrl) {
		File repositoryDir = new File(repositoryUrl);
		remoteJarFile = new File(repositoryDir, BootstrapApp.DIST_DIR + "/" + BootstrapApp.JAR_FILENAME);
	}
	
	public boolean isUpdateAvailable(File localJarFile) throws Exception {
		return (!localJarFile.exists() || localJarFile.length() != remoteJarFile.length() || localJarFile.lastModified() != remoteJarFile.lastModified());
	}

	public String getUpdateUrl() {
		return remoteJarFile.getAbsolutePath();
	}

	public Date downloadUpdate(OutputStream destinationStream) throws Exception {
		Streams.copyStream(new FileInputStream(remoteJarFile), destinationStream, true);
		return new Date(remoteJarFile.lastModified());
	}
	
}
