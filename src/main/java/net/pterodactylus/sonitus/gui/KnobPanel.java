/*
 * Sonitus - KnobPanel.java - Copyright © 2013 David Roden
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
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.pterodactylus.sonitus.data.controller.Knob;

/**
 * A panel that displays a {@link Knob}. A knob panel is very similar to a
 * {@link FaderPanel}, it just has added tick marks at the center of the
 * slider.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class KnobPanel extends JPanel {

	/**
	 * Creates a new knob panel.
	 *
	 * @param knob
	 * 		The knob being controlled
	 */
	public KnobPanel(final Knob knob) {
		super(new GridBagLayout());

		/* create knob labels. */
		add(new JLabel("-1"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
		add(new JLabel("1"), new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));

		/* create fader. */
		JSlider slider = new JSlider(new DefaultBoundedRangeModel((int) (knob.value() * 65536), 0, -65536, 65536));
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent changeEvent) {
				knob.value(((JSlider) changeEvent.getSource()).getValue() / 65536.0);
			}
		});
		if (knob.centered()) {
			slider.setMajorTickSpacing(65536);
			slider.setPaintTicks(true);
		}
		add(slider, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
	}

}
