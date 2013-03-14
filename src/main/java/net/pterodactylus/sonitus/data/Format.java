/*
 * Sonitus - Format.java - Copyright © 2013 David Roden
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

/**
 * A format is a combination of a number of channels, a sampling frequency, and
 * an encoding scheme.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Format {

	/** The number of channels of this format. */
	private final int channels;

	/** The sampling frequency of this format. */
	private final int frequency;

	/** The encoding of this format. */
	private final String encoding;

	/**
	 * Creates a new format.
	 *
	 * @param channels
	 * 		The number of channels of this format
	 * @param frequency
	 * 		The sampling frequency of this format
	 * @param encoding
	 * 		The encoding of this format
	 */
	public Format(int channels, int frequency, String encoding) {
		this.channels = channels;
		this.frequency = frequency;
		this.encoding = encoding;
	}

	/**
	 * Returns the number of channels of this format.
	 *
	 * @return The number of channels of this format
	 */
	public int channels() {
		return channels;
	}

	/**
	 * Returns the sampling frequency of this format.
	 *
	 * @return The sampling frequency of this format
	 */
	public int frequency() {
		return frequency;
	}

	/**
	 * Returns the encoding of this format
	 *
	 * @return The encoding of this format
	 */
	public String encoding() {
		return encoding;
	}

}
