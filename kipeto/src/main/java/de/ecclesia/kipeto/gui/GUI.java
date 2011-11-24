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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ecclesia.kipeto.KipetoApp;
import de.ecclesia.kipeto.blueprint.Blueprint;
import de.ecclesia.kipeto.common.AWTExceptionErrorDialog;
import de.ecclesia.kipeto.common.util.Streams;
import de.ecclesia.kipeto.engine.Action;
import de.ecclesia.kipeto.engine.ActionEvent;
import de.ecclesia.kipeto.engine.InstallFileAction;
import de.ecclesia.kipeto.engine.PlannerHashEvent;
import de.ecclesia.kipeto.engine.PlannerProcessEvent;
import de.ecclesia.kipeto.engine.RemoveDirAction;
import de.ecclesia.kipeto.engine.RemoveFileAction;
import de.ecclesia.kipeto.engine.UpdateFileAction;
import de.ecclesia.kipeto.engine.UpdateFileMetadataAction;
import de.ecclesia.kipeto.exception.KipetoException;
import de.ecclesia.kipeto.job.JobListener;
import de.ecclesia.kipeto.job.UpdateJob;
import de.ecclesia.kipeto.job.UpdateJob.Phase;
import de.ecclesia.kipeto.job.event.BeginPhaseEvent;
import de.ecclesia.kipeto.job.event.BlueprintReceivedEvent;
import de.ecclesia.kipeto.job.event.ByteTransferSubProgressEvent;
import de.ecclesia.kipeto.job.event.EndPhaseEvent;
import de.ecclesia.kipeto.job.event.JobEvent;
import de.ecclesia.kipeto.job.event.JobProgressEvent;
import de.ecclesia.kipeto.job.event.PhaseProgressEvent;
import de.ecclesia.kipeto.job.event.SubProgressEvent;
import de.ecclesia.kipeto.repository.Blob;
import de.ecclesia.kipeto.repository.ReadingRepositoryStrategy;
import de.ecclesia.kipeto.repository.WritingRepositoryStrategy;

public class GUI implements JobListener<JobEvent> {

	private static final Logger logger = LoggerFactory.getLogger(KipetoApp.class);

	private final String bluePrintRef;

	private final ReadingRepositoryStrategy repositry;

	private final WritingRepositoryStrategy cache;

	private final File target;

	private Formatter mainProgressLabelFormatter;

	private ProgressModel progressModel = new ProgressModel(null, 0, 0, null, 0, 0);

	private ProgressModel lastProgressModel;

	ProgressWindow window;

	UpdateJob job;

	boolean suppressOfflineErrorMsg;

	private boolean launchTarget = true;

	public GUI(ReadingRepositoryStrategy repositry, WritingRepositoryStrategy cache, String bluePrintRef, File target, boolean suppressOfflineErrorMsg) throws IOException {
		this.repositry = repositry;
		this.cache = cache;
		this.bluePrintRef = bluePrintRef;
		this.target = target;
		this.suppressOfflineErrorMsg = suppressOfflineErrorMsg;

		setLookAndFeel();
	}

	private void setLookAndFeel() {
		String nativeLF = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(nativeLF);
		} catch (Exception e) {
		}
	}

	public void run() throws Exception {
		job = new UpdateJob(repositry, cache, bluePrintRef, target);
		job.addJobListener(this);

		window = new ProgressWindow();
		window.setVisible(true);

		try {
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					try {
						updateWindow();
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					} catch (InvocationTargetException e) {
						logger.error(e.getMessage(), e);
					}
				}

			}, 0, 100);

			job.run();
			updateWindow();

			window.dispose();
		} catch (KipetoException e) {
			logger.error(e.getMessage(), e);
			try {
				SwingUtilities.invokeAndWait(new ExceptionHandler(GUI.this, e));
			} catch (InterruptedException e1) {
				logger.error(e.getMessage(), e1);
			} catch (InvocationTargetException e1) {
				logger.error(e.getMessage(), e1);
			}
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					public void run() {
						AWTExceptionErrorDialog errorDialog = new AWTExceptionErrorDialog(window, e);
						errorDialog.setVisible(true);

						window.dispose();
					}
				});
			} catch (InterruptedException e2) {
				logger.error(e.getMessage(), e);
			} catch (InvocationTargetException e2) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void handleJobEvent(final JobEvent event) {
		if (event instanceof BeginPhaseEvent) {
			handleBeginPhaseEvent((BeginPhaseEvent) event);
		} else if (event instanceof EndPhaseEvent) {
			handleEndphaseEvent((EndPhaseEvent) event);
		} else if (event instanceof PhaseProgressEvent) {
			handlePhaseProgressEvent((PhaseProgressEvent) event);
		} else if (event instanceof JobProgressEvent) {
			handleJobProgressEvent((JobProgressEvent) event);
		} else if (event instanceof SubProgressEvent) {
			handleSubProgressEvent((SubProgressEvent) event);
		} else if (event instanceof BlueprintReceivedEvent) {
			handleBlueprintReceivedEvent((BlueprintReceivedEvent) event);
		}

	}

	private void handleBlueprintReceivedEvent(final BlueprintReceivedEvent event) {
		Blueprint bluePrint = event.getBlueprint();

		final String app = bluePrint.getDescription() != null ? bluePrint.getDescription() : bluePrintRef;

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					window.setTitle(String.format("%s - %s %s", app, KipetoApp.TITLE, KipetoApp.VERSION));

					InputStream iconInputStream = event.getIconInputStream();
					//URL overlayURL = GUI.class.getClassLoader().getResource("overlay.png");

					if (iconInputStream != null) {// && overlayURL != null) {
						try {
							ImageIcon icon;
							icon = new ImageIcon(Streams.getBytes(iconInputStream, true));
							//ImageIcon arrow = new ImageIcon(overlayURL);

							window.setIconImage(icon.getImage());
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	private void handleSubProgressEvent(final SubProgressEvent event) {
		String text = null;

		Object source = event.source();
		if (event instanceof ByteTransferSubProgressEvent) {
			ByteTransferSubProgressEvent transferEvent = (ByteTransferSubProgressEvent) event;
			text = "Herunterladen von " + transferEvent.id();
		} else if (source instanceof ActionEvent<?>) {
			text = (actionEventDesc((ActionEvent<?>) source));
		} else if (source instanceof PlannerProcessEvent) {
			PlannerProcessEvent e = (PlannerProcessEvent) source;
			text = "Untersuche " + e.getTarget().getAbsolutePath();
		} else if (source instanceof PlannerHashEvent) {
			PlannerHashEvent e = (PlannerHashEvent) source;
			text = "Ermittle " + Blob.HASH_ALGORITHM + "-Hash für " + e.getTarget().getAbsolutePath();
		}

		progressModel = new ProgressModel(progressModel, null, null, null, text, event.totalSteps(), event.workedSteps());
	}

	private void handleJobProgressEvent(final JobProgressEvent event) {
		progressModel = new ProgressModel(progressModel, null, event.totalSteps(), event.workedSteps(), null, null, null);
	}

	private void handlePhaseProgressEvent(final PhaseProgressEvent event) {
		String text = mainProgressLabelFormatter.format(event.workedSteps(), event.totalSteps());
		progressModel = new ProgressModel(progressModel, text, null, null, null, null, null);
	}

	private void handleBeginPhaseEvent(final BeginPhaseEvent event) {
		switch (event.phase()) {
		case LOOKUP:
			mainProgressLabelFormatter = new LookupFormatter();
			break;
		case PLAN:
			mainProgressLabelFormatter = new PlanFormatter();
			break;
		case FILL_CACHE:
			mainProgressLabelFormatter = new FillCacheFormatter();
			break;
		case PROCESS:
			mainProgressLabelFormatter = new ProcessFormatter();
			break;
		}

		String text = mainProgressLabelFormatter.format(0, 0);
		progressModel = new ProgressModel(progressModel, text, null, null, null, null, null);

		if (event.phase() == Phase.FILL_CACHE) {
			window.subProgress.setIndeterminate(false);
		}
	}

	private void handleEndphaseEvent(final EndPhaseEvent event) {
		switch (event.phase()) {
		case LOOKUP:
			progressModel = new ProgressModel(progressModel, null, null, (int) (0.05 * progressModel.getProgressTotal()), null, null, null);
			break;
		case PLAN:
			progressModel = new ProgressModel(progressModel, null, null, (int) (0.10 * progressModel.getProgressTotal()), null, null, 0);
			break;
		case FILL_CACHE:
			mainProgressLabelFormatter = new FillCacheFormatter();
			progressModel = new ProgressModel(progressModel, null, null, null, null, null, 0);
			break;
		case PROCESS:
			progressModel = new ProgressModel(progressModel, "", null, progressModel.getProgressTotal(), "", null, progressModel.getSubProgressTotal());
			break;
		}
	}

	private String actionEventDesc(ActionEvent<? extends Action> event) {
		Action action = event.action();

		if (action instanceof RemoveDirAction) {
			return "Entferne " + ((RemoveDirAction) action).getTarget();
		} else if (action instanceof RemoveFileAction) {
			return "Entferne " + ((RemoveFileAction) action).getTarget();
		} else if (action instanceof InstallFileAction) {
			return "Installiere " + ((InstallFileAction) action).getTarget();
		} else if (action instanceof UpdateFileAction) {
			return "Aktualisiere " + ((UpdateFileAction) action).getInstallFileAction().getFileItem();
		} else if (action instanceof UpdateFileMetadataAction) {
			return "Aktualisiere Metadaten von " + ((UpdateFileMetadataAction) action).getTarget();
		} else {
			return "";
		}

	}

	private void updateWindow() throws InterruptedException, InvocationTargetException {
		ProgressModel currentProgressModel = progressModel;
		final ProgressModel toUpdate = currentProgressModel.getDeltaTo(lastProgressModel);

		if (!toUpdate.isEmpty()) {

			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					if (toUpdate.getProgressLabel() != null) {
						window.mainProgressLabel.setText(toUpdate.getProgressLabel());
					}
					if (toUpdate.getSubProgressLabel() != null) {
						window.subProgressLabel.setText(toUpdate.getSubProgressLabel());
					}
					if (toUpdate.getProgressTotal() != null) {
						window.mainProgress.setMaximum(toUpdate.getProgressTotal());
					}
					if (toUpdate.getSubProgressTotal() != null) {
						window.subProgress.setMaximum(toUpdate.getSubProgressTotal());
					}
					if (toUpdate.getSubProgressDone() != null) {
						window.subProgress.setValue(toUpdate.getSubProgressDone());
					}
					if (toUpdate.getProgressDone() != null) {
						window.mainProgress.setValue(toUpdate.getProgressDone());
					}
				}

			});
		}
		lastProgressModel = currentProgressModel;
	}

	private abstract class Formatter {
		String format(long done, long total) {
			return initText();
		}

		abstract String initText();
	}

	private final class LookupFormatter extends Formatter {
		@Override
		public String initText() {
			return "Verbindung zum Repository wird hergestellt...";
		}
	}

	private final class PlanFormatter extends Formatter {
		@Override
		public String initText() {
			return "Änderungen werden ermittelt...";
		}
	}

	private final class ProcessFormatter extends Formatter {
		@Override
		public String format(long done, long total) {
			return String.format("Änderungen werden durchgeführt (%d von %d)", done, total);
		}

		@Override
		String initText() {
			return String.format("Änderungen werden vorbereitet");
		}
	}

	private class FillCacheFormatter extends Formatter {
		@Override
		public String format(long done, long total) {
			return String.format("Dateien werden heruntergeladen (%d von %d)", done, total);
		}

		@Override
		String initText() {
			return String.format("Herunterladen von Dateien wird vorbereitet");
		}
	}

	public void setLaunchTarget(boolean launchTarget) {
		this.launchTarget = launchTarget;
	}

	public boolean isLaunchTarget() {
		return launchTarget;
	}

}
