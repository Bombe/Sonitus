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

import static com.google.common.io.Closeables.close;
import static net.pterodactylus.sonitus.io.mp3.Frame.ChannelMode.SINGLE_CHANNEL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import net.pterodactylus.sonitus.data.ContentMetadata;
import net.pterodactylus.sonitus.data.FormatMetadata;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.io.mp3.Frame;
import net.pterodactylus.sonitus.io.mp3.Parser;

import com.google.common.base.Optional;
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
		Parser mp3Parser = new Parser(inputStream);
		Frame frame = mp3Parser.nextFrame();
		FormatMetadata formatMetadata = new FormatMetadata((frame.channelMode() == SINGLE_CHANNEL) ? 1 : 2, frame.samplingRate(), "MP3");
		ContentMetadata contentMetadata = new ContentMetadata("");
		/* check for ID3v2 tag. */
		Optional<byte[]> id3v2TagBuffer = mp3Parser.getId3Tag();
		if (id3v2TagBuffer.isPresent()) {
			byte[] buffer = id3v2TagBuffer.get();
			ByteArrayInputStream tagInputStream = new ByteArrayInputStream(Arrays.copyOfRange(buffer, 3, buffer.length));
			try {
				/* skip “ID3” header tag. */
				ID3V2Tag id3v2Tag = ID3V2Tag.read(tagInputStream);
				if (id3v2Tag != null) {
					contentMetadata = contentMetadata.artist(id3v2Tag.getArtist()).name(id3v2Tag.getTitle());
				}
			} catch (ID3Exception id3e1) {
				id3e1.printStackTrace();
			} finally {
				close(tagInputStream, true);
			}
		}
		return Optional.of(new Metadata(formatMetadata, contentMetadata));
	}

}
