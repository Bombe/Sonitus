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

import net.pterodactylus.sonitus.data.Metadata;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

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
	private boolean swapBytes;

	/** The preset to use. */
	private final Optional<Preset> preset;

	/** The bitrate to encode to. */
	private final Optional<Integer> bitrate;

	/** Whether to use highest quality encoding. */
	private boolean hq = false;

	/**
	 * Creates a new LAME MP3 encoder.
	 *
	 * @param eventBus
	 * 		The event bus
	 * @param binary
	 * 		The location of the binary
	 * @param preset
	 * 		The preset to use
	 */
	public LameMp3Encoder(EventBus eventBus, String binary, Preset preset) {
		this(eventBus, binary, preset, -1);
	}

	/**
	 * Creates a new LAME MP3 encoder.
	 *
	 * @param eventBus
	 * 		The event bus
	 * @param binary
	 * 		The location of the binary
	 * @param bitrate
	 * 		The bitrate to encode to (in kbps)
	 */
	public LameMp3Encoder(EventBus eventBus, String binary, int bitrate) {
		this(eventBus, binary, null, bitrate);
	}

	/**
	 * Creates a new LAME MP3 encoder.
	 *
	 * @param eventBus
	 * 		The event bus
	 * @param binary
	 * 		The location of the binary
	 * @param preset
	 * 		The preset to use
	 * @param bitrate
	 * 		The bitrate to encode to (in kbps)
	 */
	private LameMp3Encoder(EventBus eventBus, String binary, Preset preset, int bitrate) {
		super(eventBus, "LAME Encoder");
		this.binary = binary;
		this.preset = Optional.fromNullable(preset);
		this.bitrate = (bitrate < 0) ? Optional.<Integer>absent() : Optional.<Integer>of(bitrate);
	}

	/**
	 * Sets whether to swap bytes on the input to encode
	 *
	 * @param swapBytes
	 * 		{@code true} to swap the input bytes, {@code false} to use platform
	 * 		endianness
	 * @return This MP3 encoder
	 */
	public LameMp3Encoder swapBytes(boolean swapBytes) {
		this.swapBytes = swapBytes;
		return this;
	}

	/**
	 * Sets whether to use highest quality encoding.
	 *
	 * @param hq
	 * 		{@code true} to use highest quality encoding, {@code false} otherwise
	 * @return This MP3 encoder
	 */
	public LameMp3Encoder hq(boolean hq) {
		this.hq = hq;
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
		ImmutableList.Builder<String> parameters = ImmutableList.builder();
		parameters.add("-r");
		parameters.add("-s").add(String.valueOf(metadata.frequency() / 1000.0));
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
		if (hq) {
			parameters.add("-q").add("0");
		}
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
