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

import com.google.common.collect.ImmutableList;

/**
 * {@link ExternalMp3Decoder} implementation that uses LAME to decode an MP3.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LameMp3Decoder extends ExternalMp3Decoder {

	/**
	 * Creates a new LAME MP3 decoder.
	 *
	 * @param binary
	 * 		The location of the binary
	 * @param swapBytes
	 * 		{@code true} to swap the decoded bytes, {@code false} to use platform
	 * 		endianness
	 */
	public LameMp3Decoder(String binary, boolean swapBytes) {
		super(binary, generateParameters(swapBytes));
	}

	//
	// STATIC METHODS
	//

	/**
	 * Generates the parameters for LAME.
	 *
	 * @param swapBytes
	 * 		{@code true} to swap the decoded bytes, {@code false} to use platform
	 * 		endianness
	 * @return The parameters for LAME
	 */
	private static Iterable<String> generateParameters(boolean swapBytes) {
		ImmutableList.Builder parameters = ImmutableList.builder();
		parameters.add("--mp3input").add("--decode").add("-t");
		if (swapBytes) {
			parameters.add("-x");
		}
		parameters.add("-").add("-");
		return parameters.build();
	}

}
