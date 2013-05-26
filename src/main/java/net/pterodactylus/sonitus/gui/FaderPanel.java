/*
 * Sonitus - FaderPanel.java - Copyright © 2013 David Roden
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
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.pterodactylus.sonitus.data.controller.Fader;

/**
 * Displays a {@link Fader}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FaderPanel extends JPanel {

	/**
	 * Creates a new fader panel.
	 *
	 * @param fader
	 * 		The fader being controlled
	 */
	public FaderPanel(final Fader fader) {
		super(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		/* create label. */
		JLabel label = new JLabel("Volume");
		add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		/* create fader labels. */
		add(new JLabel("-∞"), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
		add(new JLabel("0"), new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));

		/* create fader. */
		JSlider slider = new JSlider(new DefaultBoundedRangeModel(fader.value(), 0, fader.minimum(), fader.maximum()));
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent changeEvent) {
				fader.value(((JSlider) changeEvent.getSource()).getValue());
			}
		});
		add(slider, new GridBagConstraints(2, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
	}

}
