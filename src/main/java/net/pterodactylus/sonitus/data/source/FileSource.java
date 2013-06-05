/*
 * Sonitus - FileSource.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data.source;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sonitus.data.AbstractFilter;
import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.DataPacket;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.io.IdentifyingInputStream;

import com.google.common.base.Optional;

/**
 * A {@link Filter} that reads a file from the local file system and does not
 * expect any input.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FileSource extends AbstractFilter {

	/** The path of the file. */
	private final String path;

	/** The input stream. */
	private InputStream fileInputStream;

	/**
	 * Creates a new file source.
	 *
	 * @param path
	 * 		The path of the file
	 * @throws java.io.IOException
	 * 		if the file can not be found, or an I/O error occurs
	 */
	public FileSource(String path) throws IOException {
		super(path);
		this.path = checkNotNull(path, "path must not be null");
		fileInputStream = new FileInputStream(path);

		/* identify file type. */
		Optional<IdentifyingInputStream> identifyingInputStream = IdentifyingInputStream.create(new FileInputStream(path));
		if (identifyingInputStream.isPresent()) {
			metadataUpdated(identifyingInputStream.get().metadata());
		} else {
			/* fallback. */
			metadataUpdated(new Metadata().name(path));
		}
	}

	//
	// FILTER METHODS
	//

	@Override
	public List<Controller<?>> controllers() {
		return Collections.emptyList();
	}

	@Override
	public DataPacket get(int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int read = fileInputStream.read(buffer);
		if (read == -1) {
			throw new EOFException();
		}
		return new DataPacket(metadata(), Arrays.copyOf(buffer, read));
	}

	//
	// OBJECT METHODS
	//

	@Override
	public String toString() {
		return String.format("%s (%s)", path, metadata());
	}

}
