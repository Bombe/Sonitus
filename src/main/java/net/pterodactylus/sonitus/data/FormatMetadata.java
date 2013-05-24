/*
 * Sonitus - ContentMetadata.java - Copyright © 2013 David Roden
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
 * The part of the {@link Metadata} that contains information about the format
 * of a track. It specifies the number of channels, the samplerate, and the
 * encoding of a track.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FormatMetadata {

	/** Constant for an unknown number of channels. */
	public static final int UNKNOWN_CHANNELS = -1;

	/** Constant for an unknown frequency. */
	public static final int UNKNOWN_FREQUENCY = -1;

	/** Constant for an unknown metadata. */
	public static final String UNKNOWN_ENCODING = "UNKNOWN";

	/** The number of channels of this metadata. */
	private final int channels;

	/** The sampling frequency of this metadata. */
	private final int frequency;

	/** The encoding of this metadata. */
	private final String encoding;

	/** Creates new format metadata whose parameters are all unknown. */
	public FormatMetadata() {
		this(UNKNOWN_CHANNELS, UNKNOWN_FREQUENCY, UNKNOWN_ENCODING);
	}

	/**
	 * Creates new format metadata with the given parameters.
	 *
	 * @param channels
	 * 		The number of channels
	 * @param frequency
	 * 		The sampling frequency (in Hertz)
	 * @param encoding
	 * 		The encoding (e.g. “PCM” or “MP3”)
	 */
	public FormatMetadata(int channels, int frequency, String encoding) {
		this.channels = channels;
		this.frequency = frequency;
		this.encoding = encoding;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the number of channels of this metadata.
	 *
	 * @return The number of channels of this metadata
	 */
	public int channels() {
		return channels;
	}

	/**
	 * Returns the sampling frequency of this metadata.
	 *
	 * @return The sampling frequency of this metadata
	 */
	public int frequency() {
		return frequency;
	}

	/**
	 * Returns the encoding of this metadata
	 *
	 * @return The encoding of this metadata
	 */
	public String encoding() {
		return encoding;
	}

	//
	// ACTIONS
	//

	/**
	 * Creates new format metadata that is a copy of this format metadata but with
	 * the number of channels changed to the given number of channels.
	 *
	 * @param channels
	 * 		The new number of channels
	 * @return The new format metadata
	 */
	public FormatMetadata channels(int channels) {
		return new FormatMetadata(channels, frequency(), encoding());
	}

	/**
	 * Creates new format metadata that is a copy of this format metadata but with
	 * the sampling frequency changed to the given sampling frequency.
	 *
	 * @param frequency
	 * 		The new sampling frequency
	 * @return The new format metadata
	 */
	public FormatMetadata frequency(int frequency) {
		return new FormatMetadata(channels(), frequency, encoding());
	}

	/**
	 * Creates new format metadata that is a copy of this format metadata but with
	 * the encoding changed to the given encoding.
	 *
	 * @param encoding
	 * 		The new encoding
	 * @return The new format metadata
	 */
	public FormatMetadata encoding(String encoding) {
		return new FormatMetadata(channels(), frequency(), encoding);
	}

	//
	// OBJECT METHODS
	//

	@Override
	public int hashCode() {
		return (channels() << 16) ^ frequency() ^ encoding().toUpperCase().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof FormatMetadata)) {
			return false;
		}
		FormatMetadata formatMetadata = (FormatMetadata) object;
		return (channels() == formatMetadata.channels()) && (frequency() == formatMetadata.frequency()) && (encoding().equalsIgnoreCase(formatMetadata.encoding()));
	}

	@Override
	public String toString() {
		return String.format("%d Channel%s, %d Hz, %s", channels(), channels() != 1 ? "s" : "", frequency(), encoding());
	}

}
