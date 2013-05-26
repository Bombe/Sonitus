/*
 * Sonitus - Switch.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data.controller;

/**
 * Simple switch implementation.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class Switch extends AbstractController<Boolean> {

	/** Creates a new switch that is off. */
	public Switch() {
		this(false);
	}

	/**
	 * Creates a new switch with the given state.
	 *
	 * @param active
	 * 		The state of the switch
	 */
	public Switch(boolean active) {
		super(false, true, false, active);
	}

}
