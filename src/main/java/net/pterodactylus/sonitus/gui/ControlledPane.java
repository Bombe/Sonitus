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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.pterodactylus.sonitus.data.Controlled;
import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.controller.Fader;
import net.pterodactylus.sonitus.data.controller.Knob;
import net.pterodactylus.sonitus.data.controller.Switch;

/**
 * Panel that displays all {@link Controller}s of a {@link Controlled}
 * component.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ControlledPane extends JPanel {

	/**
	 * Creates a new controlled pane.
	 *
	 * @param controlled
	 * 		The controlled whose controllers to display
	 */
	public ControlledPane(Controlled controlled) {
		super(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(6, 12, 12, 12));

		int controllerIndex = 0;
		for (Controller controller : controlled.controllers()) {
			add(new JLabel(controller.name()), new GridBagConstraints(0, controllerIndex, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 0, 6), 0, 0));
			if (controller instanceof Fader) {
				add(new FaderPanel((Fader) controller), new GridBagConstraints(1, controllerIndex, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
			} else if (controller instanceof Switch) {
				add(new SwitchPanel((Switch) controller), new GridBagConstraints(1, controllerIndex, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
			} else if (controller instanceof Knob) {
				add(new KnobPanel((Knob) controller), new GridBagConstraints(1, controllerIndex, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
			}
			++controllerIndex;
		}
		add(new JPanel(), new GridBagConstraints(0, controllerIndex, 2, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(6, 0, 0, 0), 0, 0));
	}

}
