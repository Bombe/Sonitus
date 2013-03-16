/*
 * Sonitus - Source.java - Copyright © 2013 David Roden
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

import java.io.EOFException;
import java.io.IOException;

/**
 * Defines an arbitrary media source. This can be almost anything; an MP3 file,
 * a FastTracker module, or a decoded WAVE file.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Source {

	/**
	 * Returns the format of this source.
	 *
	 * @return The format of this source
	 */
	Format format();

	/**
	 * Returns the metadata of this source.
	 *
	 * @return The metadata of this source
	 */
	Metadata metadata();

	/**
	 * Retrieves the given name of bytes from this source. The source should always
	 * try to read as much data as was requested but is free to return a byte array
	 * with less elements that requested. However, the byte array will always be
	 * the same size as the data that was actually read, i.e. there are no excess
	 * elements in the returned array.
	 *
	 * @param bufferSize
	 * 		The size of the buffer
	 * @return A buffer that contains the read data
	 * @throws EOFException
	 * 		if the end of the source was reached
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	byte[] get(int bufferSize) throws EOFException, IOException;

}
