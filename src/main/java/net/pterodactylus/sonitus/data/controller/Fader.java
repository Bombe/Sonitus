/*
 * Sonitus - Fader.java - Copyright © 2013 David Roden
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

import net.pterodactylus.sonitus.data.Controller;

/**
 * A {@link Controller} that implements a simple fader.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class Fader extends AbstractController<Double> {

	/**
	 * Creates a new fader that is at maximum position.
	 *
	 * @param name
	 * 		The name of the fader
	 */
	public Fader(String name) {
		this(name, 1.0);
	}

	/**
	 * Creates a new fader that is at the given position.
	 *
	 * @param name
	 * 		The name of the fader
	 * @param currentValue
	 * 		The current value of the fader (from {@code 0.0} to {@code 1.0})
	 */
	public Fader(String name, Double currentValue) {
		super(name, 0.0, 1.0, false, currentValue);
	}

}
