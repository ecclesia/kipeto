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
package de.ecclesia.kipeto.tui;

import java.io.File;
import java.io.IOException;

import de.ecclesia.kipeto.KipetoApp;
import de.ecclesia.kipeto.job.JobListener;
import de.ecclesia.kipeto.job.UpdateJob;
import de.ecclesia.kipeto.job.UpdateJob.Phase;
import de.ecclesia.kipeto.job.event.BeginPhaseEvent;
import de.ecclesia.kipeto.job.event.EndPhaseEvent;
import de.ecclesia.kipeto.job.event.JobEvent;
import de.ecclesia.kipeto.job.event.PhaseProgressEvent;
import de.ecclesia.kipeto.repository.ReadingRepositoryStrategy;
import de.ecclesia.kipeto.repository.WritingRepositoryStrategy;
import de.ecclesia.kipeto.tui.TUI.Task.Status;

/**
 * @author Daniel Hintze
 * @since 08.02.2010
 */
public class TUI implements JobListener<JobEvent> {

	private final ReadingRepositoryStrategy repositry;
	private final WritingRepositoryStrategy cache;
	private final String bluePrintRef;
	private final File target;
	private Task currentTask;

	public TUI(ReadingRepositoryStrategy repositry, WritingRepositoryStrategy cache, String bluePrintRef, File target) {
		this.repositry = repositry;
		this.cache = cache;
		this.bluePrintRef = bluePrintRef;
		this.target = target;
	}

	public void run() throws IOException {
		UpdateJob job = new UpdateJob(repositry, cache, bluePrintRef, target);
		
		System.out.println(KipetoApp.TITLE + " " + KipetoApp.VERSION);
		
		job.addJobListener(this);
		job.run();

		System.out.println();
	}

	public void handleJobEvent(JobEvent event) {
		if (event instanceof BeginPhaseEvent) {
			Phase phase = event.phase();

			if (phase == Phase.LOOKUP) {
				currentTask = new Task("Connecting to repository", 1);
			} else if (phase == Phase.PLAN) {
				currentTask = new Task("Calculating changes\t", 1);
			} else if (phase == Phase.FILL_CACHE) {
				currentTask = new Task("Updating cache\t");
			} else if (phase == Phase.PROCESS) {
				currentTask = new Task("Applying changes\t");
			}

		} else if (event instanceof EndPhaseEvent) {
			currentTask.status = Status.OK;
			currentTask.worked = currentTask.totalSteps;
		} else if (event instanceof PhaseProgressEvent) {
			PhaseProgressEvent workEvent = (PhaseProgressEvent) event;
			
			currentTask.totalSteps = workEvent.totalSteps();
			currentTask.worked = workEvent.workedSteps();
		}

		currentTask.print();
	}

	static class Task {

		long totalSteps;

		long worked;

		enum Status {
			ACTIVE, OK
		}

		private final String desc;

		private Status status;

		public Task(String desc, int totalSteps) {
			this.desc = desc;
			this.totalSteps = totalSteps;
			
			status = Status.ACTIVE;

			System.out.print("\n");
		}
		
		public Task(String desc) {
			this(desc, 0);
		}

		private int percentage() {
			if(totalSteps == 0) {
				return 100;
			} else {
				return (int) Math.ceil((100. / totalSteps) * worked);
			}
		}

		private void print() {
			int percentage = percentage();

			int fak = 25;

			String prgBar = "=========================>                         ";
			int end = prgBar.length() - (percentage / 4) - 1;
			int begin = end - fak;

			String prg = prgBar.substring(begin, end);

			System.out.print(String.format("\r  %s \t\t %5d/%-5d\t%s%% \t[%s]\t%s                                        ", desc,  worked, totalSteps, percentage, prg, status));
		}
	}

}
