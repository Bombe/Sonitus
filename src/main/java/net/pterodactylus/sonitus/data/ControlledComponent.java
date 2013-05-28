/*
 * Sonitus - Controlled.java - Copyright © 2013 David Roden
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

import java.util.List;

/**
 * Interface for components that can be controlled externally in some way.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ControlledComponent {

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
	 * Returns the name of this controlled component.
	 *
	 * @return The name of this controlled component
	 */
	public String name();

	/**
	 * Returns the current metadata of this component.
	 *
	 * @return The current metadata of this component
	 */
	public Metadata metadata();

	/**
	 * Returns the controllers offered by this component.
	 *
	 * @return The controllers of this component
	 */
	public List<Controller<?>> controllers();

	/**
	 * Notifies the sink that the metadata of the audio stream has changed. This
	 * method should return as fast as possible, i.e. every heavy lifting should be
	 * done from another thread.
	 *
	 * @param metadata
	 * 		The new metadata
	 */
	void metadataUpdated(Metadata metadata);

}
