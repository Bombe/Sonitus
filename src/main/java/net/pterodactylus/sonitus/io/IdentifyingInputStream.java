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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.pterodactylus.sonitus.data.Format;

import com.google.common.base.Optional;

/**
 * Wrapper around an {@link InputStream} that identifies the {@link Format} of
 * the wrapped stream.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentifyingInputStream extends FilterInputStream {

	/** The identified format. */
	private final Format format;

	/**
	 * Creates a new identifying input stream.
	 *
	 * @param inputStream
	 * 		The input stream to wrap
	 * @param format
	 * 		The format of the stream
	 */
	private IdentifyingInputStream(InputStream inputStream, Format format) {
		super(inputStream);
		this.format = format;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the identified format.
	 *
	 * @return The identified format
	 */
	public Format format() {
		return format;
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
	 *         the format it detected, or {@link com.google.common.base.Optional#absent()}
	 *         if no format could be identified
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public static Optional<IdentifyingInputStream> create(InputStream inputStream) throws IOException {

		/* remember everything we read here. */
		RememberingInputStream rememberingInputStream = new RememberingInputStream(inputStream);

		/* try Ogg Vorbis first. */
		Optional<Format> format = OggVorbisIdentifier.identify(rememberingInputStream);
		if (format.isPresent()) {
			return Optional.of(new IdentifyingInputStream(rememberingInputStream.remembered(), format.get()));
		}

		return Optional.absent();
	}

}
