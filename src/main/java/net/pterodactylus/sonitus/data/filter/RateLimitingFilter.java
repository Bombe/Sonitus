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

import java.io.IOException;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.AbstractFilter;
import net.pterodactylus.sonitus.data.DataPacket;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Metadata;

/**
 * Rate limiting filter that only passes a specified amount of data per second
 * from its {@link net.pterodactylus.sonitus.data.Source} to its {@link
 * net.pterodactylus.sonitus.data.Sink}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class RateLimitingFilter extends AbstractFilter implements Filter {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(RateLimitingFilter.class.getName());

	/** The limiting rate in bytes/second. */
	private final int rate;

	/** The start time. */
	private long startTime;

	/** The number of bytes. */
	private long counter;

	/**
	 * Creates a new rate limiting filter.
	 *
	 * @param name
	 * 		The name of the filter
	 * @param rate
	 */
	public RateLimitingFilter(String name, int rate) {
		this(name, rate, 0);
	}

	/**
	 * Creates a new rate limiting filter.
	 *
	 * @param name
	 * 		The name of the filter
	 * @param rate
	 * 		The limiting rate (in bytes/second)
	 * @param fastStartTime
	 * 		The amount of time at the start of the filtering during which no delay
	 */
	public RateLimitingFilter(String name, int rate, long fastStartTime) {
		super(name);
		this.rate = rate;
		this.counter = (long) (-rate * (fastStartTime / 1000.0));
	}

	//
	// FILTER METHODS
	//

	@Override
	public void open(Metadata metadata) throws IOException {
		super.open(metadata);
		startTime = System.currentTimeMillis();
	}

	@Override
	public void process(DataPacket dataPacket) throws IOException {
		super.process(dataPacket);
		/* delay. */
		counter += dataPacket.buffer().length;
		long waitTime = (long) (counter / (rate / 1000.0));
		while ((System.currentTimeMillis() - startTime) < waitTime) {
			try {
				long limitDelay = waitTime - (System.currentTimeMillis() - startTime);
				logger.finest(String.format("Waiting %d ms...", limitDelay));
				Thread.sleep(limitDelay);
			} catch (InterruptedException ie1) {
				/* ignore, keep looping. */
			}
		}
		logger.finest(String.format("Processed %d Bytes during %d ms, that’s %.1f bytes/s.", counter, System.currentTimeMillis() - startTime, counter / ((System.currentTimeMillis() - startTime) / 1000.0)));
	}

}
