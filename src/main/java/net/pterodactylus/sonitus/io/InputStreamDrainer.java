/*
 * Sonitus - Drainer.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * {@link Runnable} implementation that simply reads an {@link InputStream}
 * until an {@link IOException} is thrown, or the stream reaches EOF.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class InputStreamDrainer implements Runnable {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(InputStreamDrainer.class.getName());

	/** The input stream to drain. */
	private final InputStream inputStream;

	/**
	 * Creates a new input stream drainer.
	 *
	 * @param inputStream
	 * 		The input stream to drain
	 */
	public InputStreamDrainer(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		int read;
		try {
			while ((read = inputStream.read(buffer)) != -1) {
				logger.finest(String.format("Drained %d Bytes: %s", read, filteredBuffer(Arrays.copyOf(buffer, read))));
			}
		} catch (IOException ioe1) {
			/* ignore, just exit. */
		}
	}

	/**
	 * Creates a string from all ASCII characters contained in the buffer.
	 *
	 * @param buffer
	 * 		The buffer to create a string from
	 * @return The pure-ASCII string representation of the buffer’s content
	 */
	private String filteredBuffer(byte[] buffer) {
		StringBuilder filteredBuffer = new StringBuilder();
		for (byte b : buffer) {
			if (b == 13) {
				filteredBuffer.append("\\n");
			} else if ((b == 9) || ((b >= 32) && (b <= 127))) {
				filteredBuffer.append((char) b);
			}
		}
		return filteredBuffer.toString();
	}

}
