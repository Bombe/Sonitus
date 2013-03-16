/*
 * Sonitus - LameMp3Encoder.java - Copyright © 2013 David Roden
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

import java.util.Arrays;

import net.pterodactylus.sonitus.data.Format;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * {@link ExternalMp3Encoder} implementation that uses LAME to encode MP3s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LameMp3Encoder extends ExternalMp3Encoder {

	/** Preset for LAME. */
	public enum Preset {

		/** “medium” preset. */
		MEDIUM,

		/** “standard” preset. */
		STANDARD,

		/** “extreme” preset. */
		EXTREME,

		/** “insane” preset. */
		INSANE

	}

	/** The location of the binary. */
	private final String binary;

	/** Whether to swap bytes in the input. */
	private final boolean swapBytes;

	/** The preset to use. */
	private final Optional<Preset> preset;

	/** The bitrate to encode to. */
	private final Optional<Integer> bitrate;

	/**
	 * Creates a new LAME MP3 encoder.
	 *
	 * @param binary
	 * 		The location of the binary
	 * @param swapBytes
	 * 		{@code true} to swap bytes in the input, {@code false} to use platform
	 * 		endianness
	 * @param preset
	 * 		The preset to use
	 */
	public LameMp3Encoder(String binary, boolean swapBytes, Preset preset) {
		this(binary, swapBytes, preset, -1);
	}

	/**
	 * Creates a new LAME MP3 encoder.
	 *
	 * @param binary
	 * 		The location of the binary
	 * @param swapBytes
	 * 		{@code true} to swap bytes in the input, {@code false} to use platform
	 * 		endianness
	 * @param bitrate
	 * 		The bitrate to encode to (in kbps)
	 */
	public LameMp3Encoder(String binary, boolean swapBytes, int bitrate) {
		this(binary, swapBytes, null, bitrate);
	}

	/**
	 * Creates a new LAME MP3 encoder.
	 *
	 * @param binary
	 * 		The location of the binary
	 * @param swapBytes
	 * 		{@code true} to swap bytes in the input, {@code false} to use platform
	 * 		endianness
	 * @param preset
	 * 		The preset to use
	 * @param bitrate
	 * 		The bitrate to encode to (in kbps)
	 */
	private LameMp3Encoder(String binary, boolean swapBytes, Preset preset, int bitrate) {
		this.binary = binary;
		this.swapBytes = swapBytes;
		this.preset = Optional.fromNullable(preset);
		this.bitrate = (bitrate < 0) ? Optional.<Integer>absent() : Optional.<Integer>of(bitrate);
	}

	//
	// EXTERNALFILTER METHODS
	//

	@Override
	protected String binary(Format format) {
		return binary;
	}

	@Override
	protected Iterable<String> parameters(Format format) {
		ImmutableList.Builder parameters = ImmutableList.builder();
		parameters.add("-r");
		parameters.add("-s").add(String.valueOf(format.frequency() / 1000.0));
		if (swapBytes) {
			parameters.add("-x");
		}
		parameters.add("--preset");
		if (preset.isPresent()) {
			parameters.add(preset.get().name().toLowerCase());
		}
		if (bitrate.isPresent()) {
			if (isSignificant(bitrate.get())) {
				parameters.add("cbr");
			}
			parameters.add(String.valueOf(bitrate.get()));
		}
		parameters.add("-p");
		parameters.add("-q").add("0");
		parameters.add("-").add("-");
		return parameters.build();
	}

	//
	// STATIC METHODS
	//

	/**
	 * Returns whether the given bitrate is one of the significant bitrates of
	 * MP3.
	 *
	 * @param bitrate
	 * 		The bitrate to check (in kbps)
	 * @return {@code true} if the given bitrate is one of the significant MP3
	 *         bitrates, {@code false} otherwise
	 */
	private static boolean isSignificant(int bitrate) {
		return Arrays.asList(80, 96, 112, 128, 160, 192, 224, 256, 320).contains(bitrate);
	}

}
