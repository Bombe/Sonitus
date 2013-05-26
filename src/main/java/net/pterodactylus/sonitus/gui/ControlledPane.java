/*
 * Sonitus - ControlledTab.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.gui;

import javax.swing.Box;
import javax.swing.BoxLayout;

import net.pterodactylus.sonitus.data.Controlled;
import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.controller.Fader;
import net.pterodactylus.sonitus.data.controller.Switch;

/**
 * Panel that displays all {@link Controller}s of a {@link Controlled}
 * component.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ControlledPane extends Box {

	/**
	 * Creates a new controlled pane.
	 *
	 * @param controlled
	 * 		The controlled whose controllers to display
	 */
	public ControlledPane(Controlled controlled) {
		super(BoxLayout.Y_AXIS);
		for (Controller controller : controlled.controllers()) {
			if (controller instanceof Fader) {
				add(new FaderPanel((Fader) controller));
			} else if (controller instanceof Switch) {
				add(new SwitchPanel((Switch) controller));
			}
		}
		add(Box.createGlue());
	}

}
