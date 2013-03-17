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

import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.ConnectException;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Metadata;
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

	/** The source. */
	private Source source;

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
	public Metadata metadata() {
		return source.metadata();
	}

	@Override
	public byte[] get(int bufferSize) throws EOFException, IOException {
		long now = System.currentTimeMillis();
		byte[] buffer = source.get(bufferSize);
		/* delay. */
		long waitTime = 1000 * buffer.length / rate;
		while ((System.currentTimeMillis() - now) < waitTime) {
			try {
				long limitDelay = waitTime - (System.currentTimeMillis() - now);
				logger.finest(String.format("Waiting %d ms...", limitDelay));
				Thread.sleep(limitDelay);
			} catch (InterruptedException ie1) {
				/* ignore, keep looping. */
			}
		}
		return buffer;
	}

	@Override
	public void connect(Source source) throws ConnectException {
		Preconditions.checkNotNull(source, "source must not be null");

		this.source = source;
	}

	@Override
	public void metadataUpdated() {
		/* ignore. */
	}

}
