/*
 * Sonitus - IdentifyingStream.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.io;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.pterodactylus.sonitus.data.Metadata;

import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;

/**
 * Wrapper around an {@link InputStream} that identifies the {@link Metadata} of
 * the wrapped stream.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentifyingInputStream extends FilterInputStream {

	/** The identified metadata. */
	private final Metadata metadata;

	/**
	 * Creates a new identifying input stream.
	 *
	 * @param inputStream
	 * 		The input stream to wrap
	 * @param metadata
	 * 		The metadata of the stream
	 */
	private IdentifyingInputStream(InputStream inputStream, Metadata metadata) {
		super(inputStream);
		this.metadata = metadata;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the identified metadata.
	 *
	 * @return The identified metadata
	 */
	public Metadata metadata() {
		return metadata;
	}

	//
	// STATIC METHODS
	//

	/**
	 * Tries to identify the given input stream.
	 *
	 * @param inputStream
	 * 		The input stream to identify
	 * @return An identifying input stream that delivers the original stream and
	 *         the metadata it detected, or {@link Optional#absent()} if no
	 *         metadata could be identified
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public static Optional<IdentifyingInputStream> create(InputStream inputStream) throws IOException {

		/* remember everything we read here. */
		RememberingInputStream rememberingInputStream = new RememberingInputStream(inputStream);

		/* first, try formats with unambiguous layouts. */
		try {
			Optional<Metadata> metadata = FlacIdentifier.identify(rememberingInputStream);
			if (metadata.isPresent()) {
				return Optional.of(new IdentifyingInputStream(rememberingInputStream.remembered(), metadata.get()));
			}
		} catch (EOFException eofe1) {
			/* ignore. */
		}

		/* try Ogg Vorbis next. */
		try {
			rememberingInputStream = new RememberingInputStream(rememberingInputStream.remembered());
			Optional<Metadata> metadata = OggVorbisIdentifier.identify(rememberingInputStream);
			if (metadata.isPresent()) {
				return Optional.of(new IdentifyingInputStream(rememberingInputStream.remembered(), metadata.get()));
			}
		} catch (EOFException eofe1) {
			/* ignore. */
		}

		/* finally, try MP3. */
		try {
			rememberingInputStream = new RememberingInputStream(rememberingInputStream.remembered());
			InputStream limitedInputStream = ByteStreams.limit(rememberingInputStream, 1048576);
			Optional<Metadata> metadata = Mp3Identifier.identify(limitedInputStream);
			if (metadata.isPresent()) {
				return Optional.of(new IdentifyingInputStream(rememberingInputStream.remembered(), metadata.get()));
			}
		} catch (EOFException eofe1) {
			/* ignore. */
		}

		return Optional.absent();
	}

}
