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

package net.pterodactylus.sonitus.data.source;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.Source;
import net.pterodactylus.sonitus.data.event.SourceFinishedEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * {@link Source} implementation that simply forwards another source and
 * supports changing the source without letting the {@link
 * net.pterodactylus.sonitus.data.Sink} know.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MultiSource implements Source {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(MultiSource.class.getName());

	/** The event bus. */
	private final EventBus eventBus;

	/** The current source. */
	private final AtomicReference<Source> source = new AtomicReference<Source>();

	/** Whether the source was changed. */
	private boolean sourceChanged;

	@Inject
	public MultiSource(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	//
	// ACTIONS
	//

	/**
	 * Sets the new source to use.
	 *
	 * @param source
	 * 		The new source to use
	 */
	public void setSource(Source source) {
		checkNotNull(source, "source must not be null");

		Source oldSource = this.source.getAndSet(source);
		if (oldSource != null) {
			synchronized (this.source) {
				sourceChanged = true;
				this.source.notifyAll();
			}
			logger.info(String.format("Next Source set: %s", source));
		}
	}

	//
	// CONTROLLED METHODS
	//

	@Override
	public List<Controller> controllers() {
		return Collections.emptyList();
	}

	//
	// SOURCE METHODS
	//

	@Override
	public Metadata metadata() {
		return source.get().metadata();
	}

	@Override
	public byte[] get(int bufferSize) throws EOFException, IOException {
		while (true) {
			try {
				return source.get().get(bufferSize);
			} catch (EOFException eofe1) {
				eventBus.post(new SourceFinishedEvent(source.get()));
				synchronized (source) {
					while (!sourceChanged) {
						try {
							logger.info("Waiting for next Source...");
							source.wait();
							logger.info("Was notified.");
						} catch (InterruptedException ioe1) {
							/* ignore: we’ll end up here again if we were interrupted. */
						}
					}
				}
			} finally {
				synchronized (source) {
					sourceChanged = false;
				}
			}
		}
	}

}
