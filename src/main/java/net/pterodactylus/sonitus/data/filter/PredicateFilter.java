/*
 * Sonitus - PredicateFilter.java - Copyright © 2013 David Roden
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Metadata;

import com.google.common.base.Predicate;

/**
 * {@link Filter} implementation that uses a {@link Predicate} to determine
 * whether a filter will be used or the data will only be passed through.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PredicateFilter extends DummyFilter {

	/** The predicate. */
	private final Predicate<Metadata> metadataPredicate;

	/** The filter to use if the predicate matches. */
	private final Filter filter;

	/** Whether the predicate currently matches. */
	private final AtomicBoolean metadataMatches = new AtomicBoolean(false);

	/**
	 * Creates a new predicate filter.
	 *
	 * @param metadataPredicate
	 * 		The predicate to evaluate every time the metadata changes
	 * @param filter
	 * 		The filter to use if the predicate matches the metadata
	 */
	public PredicateFilter(Predicate<Metadata> metadataPredicate, Filter filter) {
		this.metadataPredicate = metadataPredicate;
		this.filter = filter;
	}

	//
	// FILTER METHODS
	//

	@Override
	public void open(Metadata metadata) throws IOException {
		checkNotNull(metadata, "metadata must not be null");

		metadataMatches.set(metadataPredicate.apply(metadata));
		if (metadataMatches.get()) {
			filter.open(metadata);
		} else {
			super.open(metadata);
		}
	}

	@Override
	public void close() {
		if (metadataMatches.get()) {
			filter.close();
		} else {
			super.close();
		}
	}

	@Override
	public void metadataUpdated(Metadata metadata) {
		metadataMatches.set(metadataPredicate.apply(metadata));
		if (metadataMatches.get()) {
			filter.metadataUpdated(metadata);
		} else {
			super.metadataUpdated(metadata);
		}
	}

	@Override
	public void process(byte[] buffer) throws IOException {
		if (metadataMatches.get()) {
			filter.process(buffer);
		} else {
			super.process(buffer);
		}
	}

	@Override
	public Metadata metadata() {
		if (metadataMatches.get()) {
			return filter.metadata();
		} else {
			return super.metadata();
		}
	}

	@Override
	public byte[] get(int bufferSize) throws IOException {
		if (metadataMatches.get()) {
			return filter.get(bufferSize);
		} else {
			return super.get(bufferSize);
		}
	}

}
