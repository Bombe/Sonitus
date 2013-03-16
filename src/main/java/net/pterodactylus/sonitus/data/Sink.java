/*
 * Sonitus - Sink.java - Copyright © 2013 David Roden
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

/**
 * A sink is an endpoint for an audio stream. It might be a file on disk, it can
 * be an audio output in the current system, or it can be sent to a remote
 * streaming server for broadcasting.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Sink {

	/**
	 * Connects this sink to the given source.
	 *
	 * @param source
	 * 		The source to connect to
	 * @throws ConnectException
	 * 		if the source can not be connected, e.g. due to a {@link Format} mismatch
	 */
	void connect(Source source) throws ConnectException;

	/** Notifies the sink that a source has updated its metadata. */
	void metadataUpdated();

}
