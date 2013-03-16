/*
 * Sonitus - DelayFilter.java - Copyright © 2013 David Roden
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

import static com.google.common.io.Closeables.close;

import java.io.EOFException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.ConnectException;
import net.pterodactylus.sonitus.data.Connection;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Format;
import net.pterodactylus.sonitus.data.Source;

import com.google.common.base.Preconditions;

/**
 * Rate limiting filter that only passes a specified amount of data per second
 * from its {@link Source} to its {@link net.pterodactylus.sonitus.data.Sink}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class RateLimitingFilter implements Filter {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(RateLimitingFilter.class.getName());

	/** The limiting rate in bytes/second. */
	private final int rate;

	/** The source’s format. */
	private Format format;

	/** The input stream to read from. */
	private PipedInputStream pipedInputStream = new PipedInputStream();

	/**
	 * Creates a new rate limiting filter.
	 *
	 * @param rate
	 * 		The limiting rate (in bytes/second)
	 */
	public RateLimitingFilter(int rate) {
		this.rate = rate;
	}

	//
	// FILTER METHODS
	//

	@Override
	public Format format() {
		return format;
	}

	@Override
	public byte[] get(int bufferSize) throws EOFException, IOException {
		byte[] buffer = new byte[bufferSize];
		int read = pipedInputStream.read(buffer);
		if (read == -1) {
			throw new EOFException();
		}
		return Arrays.copyOf(buffer, read);
	}

	@Override
	public void connect(Source source) throws ConnectException {
		Preconditions.checkNotNull(source, "source must not be null");

		format = source.format();
		final long start = System.currentTimeMillis();
		try {
			pipedInputStream = new PipedInputStream();
			final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
			new Thread(new Connection(source) {

				@Override
				protected int bufferSize() {
					return rate;
				}

				@Override
				protected void feed(byte[] buffer) throws IOException {
					long waitTime = 1000 * buffer.length / rate;
					long now = System.currentTimeMillis();
					pipedOutputStream.write(buffer);
					pipedOutputStream.flush();
					while ((System.currentTimeMillis() - now) < waitTime) {
						try {
							long limitDelay = waitTime - (System.currentTimeMillis() - now);
							logger.finest(String.format("Waiting %d ms...", limitDelay));
							Thread.sleep(limitDelay);
						} catch (InterruptedException ie1) {
							/* ignore, keep looping. */
						}
					}
				}

				@Override
				protected void finish() throws IOException {
					close(pipedInputStream, true);
					close(pipedOutputStream, true);
				}
			}).start();
		} catch (IOException ioe1) {
			throw new ConnectException(ioe1);
		}
	}

}
