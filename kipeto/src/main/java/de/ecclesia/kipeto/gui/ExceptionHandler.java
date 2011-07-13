/*
 * #%L
 * Kipeto
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
package de.ecclesia.kipeto.gui;

import java.text.MessageFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import de.ecclesia.kipeto.exception.ConnectException;
import de.ecclesia.kipeto.exception.FileDeleteException;
import de.ecclesia.kipeto.exception.FileLockException;
import de.ecclesia.kipeto.exception.KipetoException;
import de.ecclesia.kipeto.job.UpdateJob;

public class ExceptionHandler implements Runnable {

	private static final String NOT_UPDATED = "Die Anwendung konnte nicht aktualisiert werden.\n";
	private static final String SEEK_HELP = "Bitte wenden Sie sich an den EDV-Support, Durchwahl 555.\n";
	private static final String LAST_SUCCESSFUL_UPDATE = "Zeitpunkt der letzten erfolgreichen Aktualisierung: ";

	private final Exception e;
	private final ProgressWindow window;
	private UpdateJob job;
	private String lastSuccessfullUpdate;
	private boolean suppressOfflineErrorMsg;
	private final GUI gui;

	public ExceptionHandler(GUI gui, KipetoException e) {
		this.gui = gui;
		this.suppressOfflineErrorMsg = gui.suppressOfflineErrorMsg;
		this.window = gui.window;
		this.job = gui.job;
		this.e = e;
	}

	public void run() {
		lastSuccessfullUpdate = LAST_SUCCESSFUL_UPDATE + lastSuccessfullUpdate();

		if (e instanceof ConnectException) {
			handelConnectException((ConnectException) e);
		} else if (e instanceof FileLockException) {
			handelFileLockException((FileLockException) e);
		} else if (e instanceof FileDeleteException) {
			handelFileDeleteException((FileDeleteException) e);
		} else {
			throw new RuntimeException(e);
		}

		window.dispose();
	}

	private void handelConnectException(ConnectException e) {
		if (suppressOfflineErrorMsg) {
			return;
		}

		StringBuilder msg = new StringBuilder();

		msg.append("Der Update-Server <" + e.getRepository() + "> antwortet nicht.");
		msg.append("\n\n");
		msg.append(NOT_UPDATED);
		msg.append(lastSuccessfullUpdate);
		msg.append("\n\n");
		msg.append(SEEK_HELP);

		openWarning("Verbindung zum Update-Server fehlgeschlagen", msg.toString());
	}

	private void handelFileLockException(FileLockException e) {
		gui.setLaunchTarget(false);
		
		StringBuilder msg = new StringBuilder();

		msg.append("Die Datei <" + e.getFile().getAbsolutePath());
		msg.append("> kann nicht ausgetauscht werden, da sie noch von einem anderen Programm verwendet wird. ");
		msg.append("\n\n");
		msg.append("Bitte beenden Sie das entsprechende Programm und versuchen Sie es erneut.");

		openError("Eine Datei kann nicht ausgetauscht werden", msg.toString());
	}

	private void handelFileDeleteException(FileDeleteException e) {
		StringBuilder msg = new StringBuilder();

		if (e.getFile().isDirectory()) {
			msg.append("Das Verzeichnis <");
		} else {
			msg.append("Die Datei <");
		}

		msg.append(e.getFile().getAbsolutePath());
		msg.append("> kann nicht gelöscht werden werden.");
		msg.append("\n\n");
		msg.append(SEEK_HELP);

		openError("Eine Datei kann nicht gelöscht werden", msg.toString());
	}

	private void openWarning(String title, String msg) {
		JOptionPane.showMessageDialog(gui.window, msg, title, JOptionPane.WARNING_MESSAGE);
	}

	private void openError(String title, String msg) {
		JOptionPane.showMessageDialog(gui.window, msg, title, JOptionPane.ERROR_MESSAGE);
	}

	private String lastSuccessfullUpdate() {
		Long lastUpdateTS = job.getLastSuccessfullUpdate();
		if (lastUpdateTS != null) {
			return MessageFormat.format("{0,date} um {0,time}", new Date(lastUpdateTS));
		} else {
			return "nie";
		}
	}
}
