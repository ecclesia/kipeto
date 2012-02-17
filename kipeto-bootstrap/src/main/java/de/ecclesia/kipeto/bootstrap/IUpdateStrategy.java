package de.ecclesia.kipeto.bootstrap;

import java.io.File;
import java.io.OutputStream;
import java.util.Date;

public interface IUpdateStrategy {

	boolean isUpdateAvailable(File localJarFile) throws Exception;

	String getUpdateUrl();

	Date downloadUpdate(OutputStream destinationStream) throws Exception;

	long getUpdateSize();

	
}
