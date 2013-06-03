/*
 * Sonitus - StreamInfo.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.io.flac;

/**
 * Parser for a {@link BlockType#STREAMINFO} metadata block.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class StreamInfo extends Data {

	/**
	 * Creates a new STREAMINFO block from the given buffer.
	 *
	 * @param content
	 * 		The contents of the metadata block
	 */
	public StreamInfo(byte[] content) {
		super(content);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the minimum block size.
	 *
	 * @return The minimum block size (in samples)
	 */
	public int minimumBlockSize() {
		return (int) parseBits(0, 0, 16);
	}

	/**
	 * Returns the maximum block size.
	 *
	 * @return The maximum block size (in samples)
	 */
	public int maximumBlockSize() {
		return (int) parseBits(2, 0, 16);
	}

	/**
	 * Returns the minimum frame size.
	 *
	 * @return The minimum frame size (in bytes)
	 */
	public int minimumFrameSize() {
		return (int) parseBits(4, 0, 24);
	}

	/**
	 * Returns the maximum frame size.
	 *
	 * @return The maximum frame size (in bytes)
	 */
	public int maximumFrameSize() {
		return (int) parseBits(7, 0, 24);
	}

	/**
	 * Returns the sample rate.
	 *
	 * @return The sample rate (in Hertz)
	 */
	public int sampleRate() {
		return (int) parseBits(10, 0, 20);
	}

	/**
	 * Returns the number of channels.
	 *
	 * @return The number of channels
	 */
	public int numberOfChannels() {
		return (int) (parseBits(12, 4, 3) + 1);
	}

	/**
	 * Returns the number of bits per sample.
	 *
	 * @return The number of bits per sample
	 */
	public int bitsPerSample() {
		return (int) (parseBits(12, 7, 5) + 1);
	}

	/**
	 * Returns the total number of samples.
	 *
	 * @return The total number of samples
	 */
	public long totalSamples() {
		return parseBits(13, 4, 36);
	}

}
