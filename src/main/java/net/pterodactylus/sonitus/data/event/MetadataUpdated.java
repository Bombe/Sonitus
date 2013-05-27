/*
 * Sonitus - MetadataUpdated.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data.event;

import net.pterodactylus.sonitus.data.Controlled;
import net.pterodactylus.sonitus.data.Metadata;

/**
 * Event that notifies all listeners that the {@link Metadata} of a {@link
 * Controlled} component was changed.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MetadataUpdated {

	/** The controlled component. */
	private final Controlled controlled;

	/** The new metadata. */
	private final Metadata metadata;

	/**
	 * Creates a new metadata updated event.
	 *
	 * @param controlled
	 * 		The controlled component
	 * @param metadata
	 * 		The new metadata
	 */
	public MetadataUpdated(Controlled controlled, Metadata metadata) {
		this.controlled = controlled;
		this.metadata = metadata;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the controlled component.
	 *
	 * @return The controlled component
	 */
	public Controlled controlled() {
		return controlled;
	}

	/**
	 * Returns the new metadata.
	 *
	 * @return The new metadata
	 */
	public Metadata metadata() {
		return metadata;
	}

}