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

public class ProgressModel {

	private String progressLabel;
	private Integer progressTotal;
	private Integer progressDone;
	private String subProgressLabel;
	private Integer subProgressTotal;
	private Integer subProgressDone;

	public ProgressModel(String progessLabel, Integer progressTotal, Integer progressDone, String subProgressLabel, Integer subProgressTotal, Integer subProgressDone) {
		this.progressLabel = progessLabel;
		this.progressTotal = progressTotal;
		this.progressDone = progressDone;
		this.subProgressLabel = subProgressLabel;
		this.subProgressTotal = subProgressTotal;
		this.subProgressDone = subProgressDone;
	}

	public ProgressModel(ProgressModel original, String progessLabel, Integer progressTotal, Integer progressDone, String subProgressLabel, Integer subProgressTotal, Integer subProgressDone) {
		this.progressLabel = (progessLabel != null) ? progessLabel : original.progressLabel;
		this.progressTotal = (progressTotal != null) ? progressTotal : original.progressTotal;
		this.progressDone = (progressDone != null) ? progressDone : original.progressDone;
		this.subProgressLabel = (subProgressLabel != null) ? subProgressLabel : original.subProgressLabel;
		this.subProgressTotal = (subProgressTotal != null) ? subProgressTotal : original.subProgressTotal;
		this.subProgressDone = (subProgressDone != null) ? subProgressDone : original.subProgressDone;
	}

	public String getProgressLabel() {
		return progressLabel;
	}

	public Integer getProgressTotal() {
		return progressTotal;
	}

	public Integer getProgressDone() {
		return progressDone;
	}

	public String getSubProgressLabel() {
		return subProgressLabel;
	}

	public Integer getSubProgressTotal() {
		return subProgressTotal;
	}

	public Integer getSubProgressDone() {
		return subProgressDone;
	}

	@Override
	public String toString() {
		return "ProgressModel [" + (progressLabel != null ? "progressLabel=" + progressLabel + ", " : "") + (progressTotal != null ? "progressTotal=" + progressTotal + ", " : "") + (progressDone != null ? "progressDone=" + progressDone + ", " : "")
				+ (subProgressLabel != null ? "subProgressLabel=" + subProgressLabel + ", " : "") + (subProgressTotal != null ? "subProgressTotal=" + subProgressTotal + ", " : "") + (subProgressDone != null ? "subProgressDone=" + subProgressDone : "") + "]";
	}

	public ProgressModel getDeltaTo(ProgressModel reference) {
		if (reference == null) {
			return this;
		}

		return new ProgressModel(
				(nullsafeEquals(progressLabel, reference.getProgressLabel()) ? null : progressLabel),
				(nullsafeEquals(progressTotal, reference.getProgressTotal()) ? null : progressTotal),
				(nullsafeEquals(progressDone, reference.getProgressDone()) ? null : progressDone),
				(nullsafeEquals(subProgressLabel, reference.getSubProgressLabel()) ? null : subProgressLabel),
				(nullsafeEquals(subProgressTotal, reference.getSubProgressTotal()) ? null : subProgressTotal),
				(nullsafeEquals(subProgressDone, reference.getSubProgressDone()) ? null : subProgressDone));

	}

	public boolean isEmpty() {
		return (progressLabel == null && progressTotal == null && progressLabel == null && subProgressLabel == null && subProgressTotal == null && subProgressDone == null);
	}

	private static boolean nullsafeEquals(Object a, Object b) {
		if (a == null && b == null) {
			return true;
		} else if ((a == null && b != null) || (a != null && b == null)) {
			return false;
		} else {
			return a.equals(b);
		}
	}
}
