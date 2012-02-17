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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CountingOutputStream extends FilterOutputStream {

	private static final int DEFAULT_THRESHOLD = 20480;

	private List<ByteTransferListener> byteEventListeners = new ArrayList<ByteTransferListener>();

	private long count;

	private long countAtLastEvent;

	private int threshold;

	public CountingOutputStream(OutputStream out) {
		this(out, DEFAULT_THRESHOLD);
	}

	public CountingOutputStream(OutputStream out, int threshold) {
		super(out);
		this.threshold = threshold;
	}

	@Override
	public void write(int b) throws IOException {
		super.write(b);
		updateCount(1);
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		updateCount(-1);
	}
	
	public long getCount() {
		return this.count;
	}
	
	private void updateCount(int bytes) {
		if (bytes == -1) {
			ByteTransferEvent event = new ByteTransferEvent(count, count - countAtLastEvent, threshold, true);
			fireByteTransferEvent(event);
		} else {
			count += bytes;

			if (count - countAtLastEvent >= threshold) {
				ByteTransferEvent event = new ByteTransferEvent(count, count - countAtLastEvent, threshold, false);
				fireByteTransferEvent(event);

				countAtLastEvent = count;
			}
		}
	}

	public void addByteTransferListener(ByteTransferListener listener) {
		byteEventListeners.add(listener);
	}

	public void removeByteTransferListener(ByteTransferListener listener) {
		byteEventListeners.remove(listener);
	}

	protected void fireByteTransferEvent(ByteTransferEvent byteTransferEvent) {
		for (ByteTransferListener listener : byteEventListeners) {
			listener.handleByteTransfer(byteTransferEvent);
		}
	}

	public long getByteCount() {
		return count;
	}

}
