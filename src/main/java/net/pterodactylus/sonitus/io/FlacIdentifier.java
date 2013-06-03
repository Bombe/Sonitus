/*
 * Sonitus - FlacIdentifier.java - Copyright © 2013 David Roden
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

import static net.pterodactylus.sonitus.io.flac.BlockType.STREAMINFO;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.pterodactylus.sonitus.data.ContentMetadata;
import net.pterodactylus.sonitus.data.FormatMetadata;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.io.flac.MetadataBlock;
import net.pterodactylus.sonitus.io.flac.Stream;
import net.pterodactylus.sonitus.io.flac.StreamInfo;

import com.google.common.base.Optional;

/**
 * An identifier for FLAC input streams.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FlacIdentifier {

	/**
	 * Tries to identify the FLAC file contained in the given stream.
	 *
	 * @param inputStream
	 * 		The input stream
	 * @return The identified metadata, or {@link Optional#absent()} if the
	 *         metadata can not be identified
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public static Optional<Metadata> identify(InputStream inputStream) throws IOException {
		Optional<Stream> stream = Stream.parse(inputStream);
		if (!stream.isPresent()) {
			return Optional.absent();
		}

		List<MetadataBlock> streamInfos = stream.get().metadataBlocks(STREAMINFO);
		if (streamInfos.isEmpty()) {
			/* FLAC file without STREAMINFO is invalid. */
			return Optional.absent();
		}

		MetadataBlock streamInfoBlock = streamInfos.get(0);
		StreamInfo streamInfo = (StreamInfo) streamInfoBlock.data();

		return Optional.of(new Metadata(new FormatMetadata(streamInfo.numberOfChannels(), streamInfo.sampleRate(), "FLAC"), new ContentMetadata()));
	}

}
