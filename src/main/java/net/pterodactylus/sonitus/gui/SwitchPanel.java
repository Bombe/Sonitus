/*
 * Sonitus - SwitchPanel.java - Copyright © 2013 David Roden
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.pterodactylus.sonitus.data.controller.Switch;

/**
 * Displays a {@link Switch}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SwitchPanel extends JPanel {

	/**
	 * Creates a new fader panel.
	 *
	 * @param fader
	 * 		The fader being controlled
	 */
	public SwitchPanel(final Switch switchController) {
		super(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		/* create label. */
		JLabel label = new JLabel("Mute");
		add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		/* create checkbox. */
		JCheckBox checkBox = new JCheckBox();
		checkBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				switchController.value(((JCheckBox) actionEvent.getSource()).isSelected());
			}
		});
		add(checkBox, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
	}

}
