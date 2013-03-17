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

import net.pterodactylus.sonitus.data.Metadata;

import com.google.common.base.Optional;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.v2.ID3V2Tag;

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
	 * @return The identified metadata, or {@link Optional#absent()} if the
	 *         metadata can not be identified
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public static Optional<Metadata> identify(InputStream inputStream) throws IOException {
		Bitstream bitstream = new Bitstream(inputStream);
		Optional<ID3V2Tag> id3v2Tag = Optional.absent();
		try {
			InputStream id3v2Stream = bitstream.getRawID3v2();
			id3v2Stream.read(new byte[3]);
			id3v2Tag = Optional.fromNullable(ID3V2Tag.read(id3v2Stream));
		} catch (ID3Exception id3e1) {
			/* ID3v2 tag could not be parsed, don’t cry about it. */
		}
		try {
			Header frame = bitstream.readFrame();
			if (frame == null) {
				return Optional.absent();
			}
			Metadata metadata = new Metadata(frame.mode() == Header.SINGLE_CHANNEL ? 1 : 2, frame.frequency(), "MP3");
			if (id3v2Tag.isPresent()) {
				metadata = metadata.artist(id3v2Tag.get().getArtist());
				metadata = metadata.name(id3v2Tag.get().getTitle());
			}
			return Optional.of(metadata);
		} catch (BitstreamException be1) {
			return Optional.absent();
		}
	}

}
