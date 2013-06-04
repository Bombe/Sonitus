/*
 * Sonitus - Filter.java - Copyright © 2013 David Roden
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

import java.io.IOException;
import java.util.List;

/**
 * A filter is both a source and a sink for audio data. It is used to process
 * the audio date in whatever way seems appropriate.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Filter {

	/**
	 * Adds the given listener to the list of registered listeners.
	 *
	 * @param metadataListener
	 * 		The metadata listener to add
	 */
	void addMetadataListener(MetadataListener metadataListener);

	/**
	 * Removes the given listener from the list of registered listeners.
	 *
	 * @param metadataListener
	 * 		The metadata listener to remove
	 */
	void removeMetadataListener(MetadataListener metadataListener);

	/**
	 * Returns the name of this filter.
	 *
	 * @return The name of this filter
	 */
	String name();

	/**
	 * Returns the controllers offered by this filter.
	 *
	 * @return The controllers of this filter
	 */
	List<Controller<?>> controllers();

	/**
	 * Returns the metadata of the audio stream.
	 *
	 * @return The metadata of the audio stream
	 */
	Metadata metadata();

	/**
	 * Notifies the sink that the metadata of the audio stream has changed. This
	 * method should return as fast as possible, i.e. every heavy lifting should be
	 * done from another thread.
	 *
	 * @param metadata
	 * 		The new metadata
	 */
	void metadataUpdated(Metadata metadata);

	/**
	 * Retrieves data from the audio stream.
	 *
	 * @param bufferSize
	 * 		The maximum amount of bytes to retrieve from the audio stream
	 * @return A buffer filled with up to {@code bufferSize} bytes of data; the
	 *         returned buffer may contain less data than requested but will not
	 *         contain excess elements
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	byte[] get(int bufferSize) throws IOException;

	/**
	 * Opens this sink using the format parameters of the given metadata.
	 *
	 * @param metadata
	 * 		The metadata of the stream
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	void open(Metadata metadata) throws IOException;

	/** Closes this sink. */
	void close();

	/**
	 * Processes the given buffer of data.
	 *
	 * @param buffer
	 * 		The data to process
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	void process(byte[] buffer) throws IOException;

}
