/*
 * Sonitus - DataPacket.java - Copyright © 2013 David Roden
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;

/**
 * A data packet is a container for audio data and optional metadata.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DataPacket {

	/** The metadata. */
	private final Optional<Metadata> metadata;

	/** The audio data. */
	private final byte[] buffer;

	/**
	 * Creates a new data packet.
	 *
	 * @param metadata
	 * 		The metadata (may be {@code null})
	 * @param buffer
	 * 		The audio data
	 */
	public DataPacket(Metadata metadata, byte[] buffer) {
		this(Optional.fromNullable(metadata), buffer);
	}

	/**
	 * Creates a new data packet.
	 *
	 * @param metadata
	 * 		The metadata
	 * @param buffer
	 * 		The audio date
	 */
	public DataPacket(Optional<Metadata> metadata, byte[] buffer) {
		this.metadata = checkNotNull(metadata, "metadata must not be null");
		this.buffer = checkNotNull(buffer, "buffer must not be null");
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the metadata of this data packet.
	 *
	 * @return The metadata of this data packet
	 */
	public Optional<Metadata> metadata() {
		return metadata;
	}

	/**
	 * Returns the audio data of this data packet.
	 *
	 * @return The audio data of this data packet
	 */
	public byte[] buffer() {
		return buffer;
	}

	//
	// OBJECT METHODS
	//

	@Override
	public String toString() {
		return String.format("%s (%d)", metadata, buffer.length);
	}

}
