/*
 * Sonitus - Connection.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data;

import java.io.IOException;

/**
 * A connection reads bytes from a {@link Source} and feeds it to a sink. This
 * class is meant to be subclassed by each {@link Sink}, overriding the {@link
 * #feed(byte[])} method to actually feed the data into the sink. The {@link
 * #feed(byte[])} method is also responsible for blocking for an appropriate
 * amount of time; this method determines how fast a {@link Source} is
 * consumed.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class Connection implements Runnable {

	/** The source to consume. */
	private final Source source;

	/**
	 * Creates a new connection that will read from the given source.
	 *
	 * @param source
	 * 		The source to read
	 */
	public Connection(Source source) {
		this.source = source;
	}

	//
	// RUNNABLE METHODS
	//

	@Override
	public void run() {
		while (true) {
			try {
				byte[] buffer = source.get(bufferSize());
				feed(buffer);
			} catch (IOException e) {
				return;
			}
		}
	}

	//
	// SUBCLASS METHODS
	//

	/**
	 * Returns the number of bytes that will be requested from the source.
	 *
	 * @return The number of bytes that will be requested from the source
	 */
	protected abstract int bufferSize();

	/**
	 * Feeds the read data into the sink. The given buffer is always filled and
	 * never contains excess elements.
	 *
	 * @param buffer
	 * 		The data
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	protected abstract void feed(byte[] buffer) throws IOException;

	/**
	 * Notifies the sink that the source does not deliver any more data.
	 *
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	protected abstract void finish() throws IOException;

}
