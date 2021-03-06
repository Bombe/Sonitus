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
import javax.swing.event.EventListenerList;

import net.pterodactylus.sonitus.data.AbstractFilter;
import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.DataPacket;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Metadata;

import com.google.inject.Inject;

/**
 * {@link Filter} implementation that simply forwards data from another filter
 * and supports changing the source without letting downstream filters know.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MultiSource extends AbstractFilter {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(MultiSource.class.getName());

	/** The source finished listeners. */
	private final EventListenerList sourceFinishedListeners = new EventListenerList();

	/** The current source. */
	private final AtomicReference<Filter> source = new AtomicReference<Filter>();

	/** Whether the source was changed. */
	private boolean sourceChanged;

	/** Creates a new multi source. */
	@Inject
	public MultiSource() {
		super("Multisource");
	}

	//
	// LISTENER MANAGEMENT
	//

	/**
	 * Adds a source finished listener to the list of registered listeners.
	 *
	 * @param sourceFinishedListener
	 * 		The source finished listener to add
	 */
	public void addSourceFinishedListener(SourceFinishedListener sourceFinishedListener) {
		sourceFinishedListeners.add(SourceFinishedListener.class, sourceFinishedListener);
	}

	/**
	 * Removes a source finished listener from the list of registered listeners.
	 *
	 * @param sourceFinishedListener
	 * 		The source finished listener to remove
	 */
	public void removeSourceFinishedListener(SourceFinishedListener sourceFinishedListener) {
		sourceFinishedListeners.remove(SourceFinishedListener.class, sourceFinishedListener);
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
	public void setSource(Filter source) {
		checkNotNull(source, "source must not be null");

		Filter oldSource = this.source.getAndSet(source);
		if (!source.equals(oldSource)) {
			synchronized (this.source) {
				sourceChanged = true;
				this.source.notifyAll();
			}
			metadataUpdated(source.metadata());
			logger.info(String.format("Next Source set: %s", source.name()));
		}
	}

	//
	// EVENT METHODS
	//

	/**
	 * Notifies all registered listeners that the current source finished playing
	 * and that a new source should be {@link #setSource(Filter) set}.
	 *
	 * @see SourceFinishedListener
	 */
	private void fireSourceFinished() {
		for (SourceFinishedListener sourceFinishedListener : sourceFinishedListeners.getListeners(SourceFinishedListener.class)) {
			sourceFinishedListener.sourceFinished(this);
		}
	}

	//
	// FILTER METHODS
	//

	@Override
	public List<Controller<?>> controllers() {
		return Collections.emptyList();
	}

	@Override
	public Metadata metadata() {
		if (super.metadata() == Metadata.UNKNOWN) {
			/* no metadata yet, wait for it. */
			waitForNewSource();
			sourceChanged = false;
		}
		return super.metadata();
	}

	@Override
	public DataPacket get(int bufferSize) throws EOFException, IOException {
		while (true) {
			try {
				return source.get().get(bufferSize);
			} catch (EOFException eofe1) {
				logger.info(String.format("Got EOF from %s.", source.get().name()));
				waitForNewSource();
			} finally {
				synchronized (source) {
					sourceChanged = false;
				}
			}
		}
	}

	//
	// PRIVATE METHODS
	//

	/** Waits for a new source to be {@link #setSource(Filter) set}. */
	private void waitForNewSource() {
		fireSourceFinished();
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
	}

}
