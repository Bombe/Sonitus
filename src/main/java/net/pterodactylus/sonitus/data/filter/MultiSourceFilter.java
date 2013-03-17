/*
 * Sonitus - MultiSource.java - Copyright © 2013 David Roden
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

import static com.google.common.base.Preconditions.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.ConnectException;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.ReusableSink;
import net.pterodactylus.sonitus.data.Source;
import net.pterodactylus.sonitus.data.event.SourceFinishedEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * {@link ReusableSink} implementation that supports changing the source without
 * letting the {@link net.pterodactylus.sonitus.data.Sink} know.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MultiSourceFilter implements Filter, ReusableSink {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(MultiSourceFilter.class.getName());

	/** The event bus. */
	private final EventBus eventBus;

	/** The current source. */
	private final AtomicReference<Source> source = new AtomicReference<Source>();

	@Inject
	public MultiSourceFilter(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public Metadata metadata() {
		return source.get().metadata();
	}

	@Override
	public byte[] get(int bufferSize) throws EOFException, IOException {
		return source.get().get(bufferSize);
	}

	@Override
	public void connect(Source source) throws ConnectException {
		checkNotNull(source, "source must not be null");

		Source oldSource = this.source.getAndSet(source);
		if (oldSource != null) {
			checkArgument(oldSource.metadata().channels() == source.metadata().channels(), "source’s channel count must equal existing source’s channel count");
			checkArgument(oldSource.metadata().frequency() == source.metadata().frequency(), "source’s frequency must equal existing source’s frequency");
			checkArgument(oldSource.metadata().encoding().equalsIgnoreCase(source.metadata().encoding()), "source’s encoding must equal existing source’s encoding");

			eventBus.post(new SourceFinishedEvent(oldSource));
		}
	}

	@Override
	public void metadataUpdated() {
		/* ignore. */
	}

}
