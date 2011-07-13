/*
 * #%L
 * Kipeto Common
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
package de.ecclesia.kipeto.common.util;

/**
 * @author Daniel Hintze
 * @since 05.02.2010
 */
public class ByteTransferEvent {

	private final long bytesSinceBeginOfOperation;
	private final int threshold;
	private final long bytesSinceLastEvent;
	private final boolean endOfOperation;
	
	public ByteTransferEvent(long bytesSinceBeginOfOperation, long bytesSinceLastEvent, int threshold, boolean endOfOperation) {
		this.bytesSinceBeginOfOperation = bytesSinceBeginOfOperation;
		this.bytesSinceLastEvent = bytesSinceLastEvent;
		this.threshold = threshold;
		this.endOfOperation = endOfOperation;
	}

	public long getBytesSinceBeginOfOperation() {
		return bytesSinceBeginOfOperation;
	}
	
	public long getBytesSinceLastEvent() {
		return bytesSinceLastEvent;
	}
	
	public int getThreshold() {
		return threshold;
	}

	public boolean isEndOfOperation() {
		return endOfOperation;
	}
}
