/*
 * Sonitus - OggVorbisIdentifier.java - Copyright © 2013 David Roden
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

import net.pterodactylus.sonitus.data.ContentMetadata;
import net.pterodactylus.sonitus.data.FormatMetadata;
import net.pterodactylus.sonitus.data.Metadata;

import com.google.common.base.Optional;
import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.Info;

/**
 * Identifies Ogg Vorbis files. <p> All knowledge used in this class has been
 * taken from <a href="http://www.jcraft.com/jorbis/tutorial/Tutorial.html">jcraft.com/jorbis/tutorial/Tutorial.html</a>.
 * </p>
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class OggVorbisIdentifier {

	/** The default size of the read buffer. */
	private static final int BUFFER_SIZE = 4096;

	/** Suppress default constructor. */
	private OggVorbisIdentifier() {
		/* nothing here. */
	}

	/**
	 * Tries to parse the given stream as Ogg Vorbis file and returns a {@link
	 * Metadata} describing the stream.
	 *
	 * @param inputStream
	 * 		The input stream to identify as Ogg Vorbis
	 * @return The identified metadata, or {@link Optional#absent()} if the stream
	 *         could not be identified
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public static Optional<Metadata> identify(InputStream inputStream) throws IOException {

		/* stuff needed to decode Ogg. */
		Packet packet = new Packet();
		Page page = new Page();
		StreamState streamState = new StreamState();
		SyncState syncState = new SyncState();

		/* stuff needed to decode Vorbis. */
		Comment comment = new Comment();
		Info info = new Info();

		/* initialize jorbis. */
		syncState.init();
		int bufferSize = BUFFER_SIZE;
		int index = syncState.buffer(bufferSize);
		byte[] buffer = syncState.data;

		boolean streamStateInitialized = false;
		int packetsRead = 0;

		/* read until we have read the three packets required to decode the header. */
		while (packetsRead < 3) {
			int read = inputStream.read(buffer, index, bufferSize);
			syncState.wrote(read);
			switch (syncState.pageout(page)) {
				case -1:
					return Optional.absent();
				case 1:
					if (!streamStateInitialized) {
						/* init stream state. */
						streamState.init(page.serialno());
						streamState.reset();
						info.init();
						comment.init();
						streamStateInitialized = true;
					}
					if (streamState.pagein(page) == -1) {
						return Optional.absent();
					}
					switch (streamState.packetout(packet)) {
						case -1:
							return Optional.absent();
						case 1:
							info.synthesis_headerin(comment, packet);
							packetsRead++;
						default:
							/* continue. */
					}

				default:
					/* continue. */
			}
			index = syncState.buffer(bufferSize);
			buffer = syncState.data;
		}

		FormatMetadata formatMetadata = new FormatMetadata(info.channels, info.rate, "Vorbis");
		ContentMetadata contentMetadata = new ContentMetadata("");
		for (int c = 0; c < comment.comments; ++c) {
			String field = comment.getComment(c);
			Optional<String> extractedField = extractField(field, "ARTIST");
			if (extractedField.isPresent()) {
				contentMetadata = contentMetadata.artist(extractedField.get());
				continue;
			}
			extractedField = extractField(field, "TITLE");
			if (extractedField.isPresent()) {
				contentMetadata = contentMetadata.name(extractedField.get());
				continue;
			}
		}
		return Optional.of(new Metadata(formatMetadata, contentMetadata));
	}

	/**
	 * Extracts the content of the field from the comment if the comment contains
	 * the given field.
	 *
	 * @param comment
	 * 		The comment to extract the value from
	 * @param fieldName
	 * 		The name of the field to extract
	 * @return The extracted field, or {@link Optional#absent()} if the comment
	 *         does not contain the given field
	 */
	private static Optional<String> extractField(String comment, String fieldName) {
		if (comment.startsWith(fieldName + "=")) {
			return Optional.of(comment.substring(fieldName.length() + 1));
		}
		return Optional.absent();
	}

}
