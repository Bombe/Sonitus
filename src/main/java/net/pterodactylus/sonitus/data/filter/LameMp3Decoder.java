/*
 * Sonitus - LameMp3Decoder.java - Copyright © 2013 David Roden
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

import net.pterodactylus.sonitus.data.Metadata;

import com.google.common.collect.ImmutableList;

/**
 * {@link ExternalMp3Decoder} implementation that uses LAME to decode an MP3.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LameMp3Decoder extends ExternalMp3Decoder {

	/** The location of the binary. */
	private final String binary;

	/** Whether to swap bytes in the decoded output. */
	private boolean swapBytes;

	/**
	 * Creates a new LAME MP3 decoder.
	 *
	 * @param binary
	 * 		The location of the binary
	 */
	public LameMp3Decoder(String binary) {
		this.binary = binary;
	}

	/**
	 * Sets whether to swap bytes on the decoded output.
	 *
	 * @param swapBytes
	 * 		{@code true} to swap the decoded bytes, {@code false} to use platform
	 * 		endianness
	 * @return This MP3 decoder
	 */
	public LameMp3Decoder swapBytes(boolean swapBytes) {
		this.swapBytes = swapBytes;
		return this;
	}

	//
	// EXTERNALFILTER METHODS
	//

	@Override
	protected String binary(Metadata metadata) {
		return binary;
	}

	@Override
	protected Iterable<String> parameters(Metadata metadata) {
		ImmutableList.Builder parameters = ImmutableList.builder();
		parameters.add("--mp3input").add("--decode").add("-t");
		if (swapBytes) {
			parameters.add("-x");
		}
		parameters.add("-").add("-");
		return parameters.build();
	}

}
