/*
 * Sonitus - Controller.java - Copyright © 2013 David Roden
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
 * A single controllable element.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Controller {

	/**
	 * Returns the minimum value of this controller.
	 *
	 * @return The minimum value of this controller
	 */
	int minimum();

	/**
	 * Returns the maximum value of this controller.
	 *
	 * @return The maximum value of this controller
	 */
	int maximum();

	/**
	 * Returns whether this control has a “center” position.
	 *
	 * @return {@code true} if this controller has a “center” position, {@code
	 *         false} otherwise
	 */
	boolean centered();

	/**
	 * Returns the current value of this controller.
	 *
	 * @return The current value of this controller
	 */
	int value();

	/**
	 * Sets the current value of this controller.
	 *
	 * @param value
	 * 		The current value of this controller
	 */
	void value(int value);

}
