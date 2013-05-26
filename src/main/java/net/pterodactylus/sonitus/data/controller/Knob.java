/*
 * Sonitus - Knob.java - Copyright © 2013 David Roden
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
 * A know is a controller that usually starts out in a center position and can
 * be de- or increased from there.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Knob extends AbstractController<Double> {

	/**
	 * Creates a new knob with the given name.
	 *
	 * @param name
	 * 		The name of the knob
	 */
	public Knob(String name) {
		this(name, 0.0);
	}

	/**
	 * Creates a new knob with the given name and value.
	 *
	 * @param name
	 * 		The name of the knob
	 * @param currentValue
	 * 		The current value of the knob
	 */
	public Knob(String name, double currentValue) {
		super(name, -1.0, 1.0, true, currentValue);
	}

}
