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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Hintze
 * @since 05.02.2010
 */
public class CountingInputStream extends FilterInputStream {

	private static final int DEFAULT_THRESHOLD = 20480;

	private List<ByteTransferListener> byteEventListeners = new ArrayList<ByteTransferListener>();

	private long count;

	private long countAtLastEvent;

	private int threshold;

	public CountingInputStream(InputStream in) {
		this(in, DEFAULT_THRESHOLD);
	}

	public CountingInputStream(InputStream in, int threshold) {
		super(in);
		this.threshold = threshold;
	}

	public int read(byte[] b) throws IOException {
		int found = super.read(b);
		updateCount(found);
		return found;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int found = super.read(b, off, len);
		updateCount(found);
		return found;
	}

	public int read() throws IOException {
		updateCount(1);
		return super.read();
	}

	public long getCount() {
		return this.count;
	}

	@Override
	public void close() throws IOException {
		updateCount(-1);
		super.close();
	}

	private void updateCount(int read) {
		if (read == -1) {
			ByteTransferEvent event = new ByteTransferEvent(count, count - countAtLastEvent, threshold, true);
			fireByteTransferEvent(event);
		} else {
			count += read;

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
