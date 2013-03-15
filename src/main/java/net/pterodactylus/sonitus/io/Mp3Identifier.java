/*
 * Sonitus - Mp3Identifier.java - Copyright © 2013 David Roden
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

import java.io.IOException;
import java.io.InputStream;

import net.pterodactylus.sonitus.data.Format;

import com.google.common.base.Optional;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;

/**
 * Identifies MP3 files.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Mp3Identifier {

	/**
	 * Tries to identify the MP3 file contained in the given stream.
	 *
	 * @param inputStream
	 * 		The input stream
	 * @return The identified format, or {@link com.google.common.base.Optional#absent()}
	 *         if the format can not be identified
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public static Optional<Format> identify(InputStream inputStream) throws IOException {
		Bitstream bitstream = new Bitstream(inputStream);
		try {
			Header frame = bitstream.readFrame();
			if (frame == null) {
				return Optional.absent();
			}
			return Optional.of(new Format(frame.mode() == Header.SINGLE_CHANNEL ? 1 : 2, frame.frequency(), "MP3"));
		} catch (BitstreamException be1) {
			return Optional.absent();
		}
	}

}
