/*
 * Sonitus - TimeCounterFilter.java - Copyright © 2013 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sonitus.data.filter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.sonitus.data.AbstractFilter;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Metadata;

/**
 * {@link Filter} implementation that uses the number of bytes that have been
 * {@link #process(byte[]) processed} together with the {@link Metadata} to
 * calculate how long a source is already playing.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TimeCounterFilter extends AbstractFilter implements Filter {

	/** The byte counter. */
	private final AtomicLong counter = new AtomicLong();

	/** The parent’s metdata. */
	private final AtomicReference<Metadata> parentMetadata = new AtomicReference<Metadata>();

	/** Whether to reset the counter on a metadata update. */
	private final boolean resetOnMetadataUpdate;

	/** The last displayed timestamp. */
	private final AtomicLong lastTimestamp = new AtomicLong(-1);

	/**
	 * Creates a new time counter filter that automatically resets the counter when
	 * the metadata is {@link #metadataUpdated(Metadata) updated}.
	 *
	 * @param name
	 * 		The name of the filter
	 */
	public TimeCounterFilter(String name) {
		this(name, true);
	}

	/**
	 * Creates a new time counter filter.
	 *
	 * @param name
	 * 		The name of the filter
	 * @param resetOnMetadataUpdate
	 * 		{@code true} if the counter should automatically be reset if the metadata
	 * 		is updated, {@code false} otherwise
	 */
	public TimeCounterFilter(String name, boolean resetOnMetadataUpdate) {
		super(name);
		this.resetOnMetadataUpdate = resetOnMetadataUpdate;
	}

	//
	// ACTIONS
	//

	/**
	 * Returns the number of milliseconds worth of data that has been passed into
	 * {@link #process(byte[])}. If no metadata has yet been set, {@code 0} is
	 * returned.
	 *
	 * @return The number of milliseconds the current source is already playing
	 */
	public long getMillis() {
		Metadata metadata = super.metadata();
		if (metadata == null) {
			return 0;
		}
		return 1000 * counter.get() / (metadata.channels() * metadata.frequency() * 2);
	}

	/** Resets the counter to 0. */
	public void reset() {
		counter.set(0);
	}

	//
	// FILTER METHODS
	//

	@Override
	public void metadataUpdated(Metadata metadata) {
		parentMetadata.set(metadata);
		if (resetOnMetadataUpdate) {
			reset();
		}
		updateTimestamp(true);
	}

	@Override
	public void process(byte[] buffer) throws IOException {
		super.process(buffer);
		counter.getAndAdd(buffer.length);
		updateTimestamp(false);
	}

	//
	// PRIVATE METHODS
	//

	/** Updates the timestamp in the metadata. */
	private void updateTimestamp(boolean now) {
		long timestamp = getMillis() / 1000;
		if (now || (lastTimestamp.get() != timestamp)) {
			super.metadataUpdated(parentMetadata.get().comment(String.format("%s%02d:%02d", (timestamp >= 3600) ? String.format("%d:", timestamp / 3600) : "" , (timestamp % 3600) / 60, timestamp % 60)));
			lastTimestamp.set(timestamp);
		}
	}

}
