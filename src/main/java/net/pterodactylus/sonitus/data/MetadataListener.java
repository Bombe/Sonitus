/*
 * Sonitus - MetadataListener.java - Copyright © 2013 David Roden
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
 * Interface for metadata listeners.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface MetadataListener {

	/**
	 * Notifies a listener when the metadata of the given filter was updated.
	 *
	 * @param filter
	 * 		The filter whose metadata was updated
	 * @param metadata
	 * 		The new metadata
	 */
	void metadataUpdated(Filter filter, Metadata metadata);

}
