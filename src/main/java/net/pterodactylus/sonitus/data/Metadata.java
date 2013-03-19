/*
 * Sonitus - Metainfo.java - Copyright © 2013 David Roden
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

import com.google.common.base.Optional;

/**
 * Metadata contains information about a source, e.g. the number of channels,
 * the frequency, the encoding, the name of the content, the artist performing
 * it, dates, comments, URLs, etc.
 * <p/>
 * Metadata, once created, is immutable.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Metadata {

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

	/** The artist performing the content. */
	private final Optional<String> artist;

	/** The name of the content. */
	private final Optional<String> name;

	/** Creates empty metadata. */
	public Metadata(int channels, int frequency, String encoding) {
		this(channels, frequency, encoding, null, null);
	}

	/**
	 * Creates metadata with the given attributes.
	 *
	 * @param artist
	 * 		The artist performing the content (may be {@code null})
	 * @param name
	 * 		The name of the content (may be {@code null})
	 */
	private Metadata(int channels, int frequency, String encoding, String artist, String name) {
		this.channels = channels;
		this.frequency = frequency;
		this.encoding = encoding;
		this.artist = Optional.fromNullable(artist);
		this.name = Optional.fromNullable(name);
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
	 * Returns a metadata with the same parameters as this metadata and the given
	 * number of channels.
	 *
	 * @param channels
	 * 		The new number of channels
	 * @return A new metadata with the given number of channels
	 */
	public Metadata channels(int channels) {
		return new Metadata(channels, frequency, encoding, artist.orNull(), name.orNull());
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
	 * Returns a new metadata with the same parameters as this metadata and the
	 * given frequency.
	 *
	 * @param frequency
	 * 		The new frequency
	 * @return A new metadata with the given frequency
	 */
	public Metadata frequency(int frequency) {
		return new Metadata(channels, frequency, encoding, artist.orNull(), name.orNull());
	}

	/**
	 * Returns the encoding of this metadata
	 *
	 * @return The encoding of this metadata
	 */
	public String encoding() {
		return encoding;
	}

	/**
	 * Returns a new metadata with the same parameters as this metadata and the
	 * given encoding.
	 *
	 * @param encoding
	 * 		The new encoding
	 * @return A new metadata with the given encoding
	 */
	public Metadata encoding(String encoding) {
		return new Metadata(channels, frequency, encoding, artist.orNull(), name.orNull());
	}

	/**
	 * Returns the artist, if any.
	 *
	 * @return The artist, or {@link Optional#absent()}
	 */
	public Optional<String> artist() {
		return artist;
	}

	/**
	 * Returns new metadata with the same attributes as this metadata, except for
	 * the artist.
	 *
	 * @param artist
	 * 		The new artist
	 * @return New metadata with a changed artist
	 */
	public Metadata artist(String artist) {
		return new Metadata(channels, frequency, encoding, (artist != null) ? artist.trim() : artist, name.orNull());
	}

	/**
	 * Returns the name of the content, if any.
	 *
	 * @return The name, or {@link Optional#absent()}
	 */
	public Optional<String> name() {
		return name;
	}

	/**
	 * Returns new metadata with the same attributes as this metadata, except for
	 * the name.
	 *
	 * @param name
	 * 		The new name
	 * @return New metadata with a changed name
	 */
	public Metadata name(String name) {
		return new Metadata(channels, frequency, encoding, artist.orNull(), (name != null) ? name.trim() : name);
	}

	//
	// OBJECT METHODS
	//

	@Override
	public int hashCode() {
		int hashCode = (channels << 16) ^ frequency ^ encoding.toUpperCase().hashCode();
		if (artist.isPresent()) {
			hashCode ^= artist.get().hashCode();
		}
		if (name.isPresent()) {
			hashCode ^= name.get().hashCode();
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object object) {
		if ((object == null) || (getClass() != object.getClass())) {
			return false;
		}
		Metadata metadata = (Metadata) object;
		if ((metadata.channels != channels) || (metadata.frequency != frequency) || !metadata.encoding.equalsIgnoreCase(encoding)) {
			return false;
		}
		if (!artist.equals(metadata.artist)) {
			return false;
		}
		if (!name.equals(metadata.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append(String.format("%d Channel%s, %d Hz, %s:", channels, channels != 1 ? "s" : "", frequency, encoding));
		if (artist.isPresent()) {
			string.append(" Artist(").append(artist.get()).append(")");
		}
		if (name.isPresent()) {
			string.append(" Name(").append(name.get()).append(")");
		}
		return string.toString();
	}

}
