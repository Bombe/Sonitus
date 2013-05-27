/*
 * Sonitus - ComponentInfoPanel.java - Copyright © 2013 David Roden
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.pterodactylus.sonitus.data.Controlled;
import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.FormatMetadata;
import net.pterodactylus.sonitus.data.controller.Fader;
import net.pterodactylus.sonitus.data.controller.Knob;
import net.pterodactylus.sonitus.data.controller.Switch;

import com.google.common.base.Optional;

/**
 * Panel that shows information about a {@link Controlled} component.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ComponentInfoPanel extends JPanel {

	/** The name of the component. */
	private final JLabel headerLabel = new JLabel();

	/** The number of received input bytes. */
	private final JLabel inputLabel = new JLabel();

	/** The number of sent output bytes. */
	private final JLabel outputLabel = new JLabel();

	/** The current format metadata. */
	private final JLabel formatLabel = new JLabel();

	/**
	 * Creates a new component info panel.
	 *
	 * @param controlled
	 * 		The component to display
	 */
	public ComponentInfoPanel(Controlled controlled) {
		super(new GridBagLayout());

		setPreferredSize(new Dimension(400, 0));
		createPanel(controlled);
	}

	//
	// ACTIONS
	//

	/**
	 * Sets the number of received input bytes.
	 *
	 * @param input
	 * 		The number of received input bytes
	 * @return This panel
	 */
	public ComponentInfoPanel input(Optional<Long> input) {
		if (input.isPresent()) {
			inputLabel.setText(format(input.get()));
		} else {
			inputLabel.setText("");
		}
		return this;
	}

	/**
	 * Sets the number of sent output bytes.
	 *
	 * @param output
	 * 		The number of sent output input bytes
	 * @return This panel
	 */
	public ComponentInfoPanel output(Optional<Long> output) {
		if (output.isPresent()) {
			outputLabel.setText(format(output.get()));
		} else {
			outputLabel.setText("");
		}
		return this;
	}

	/**
	 * Sets the current format metadata.
	 *
	 * @param metadata
	 * 		The format metadata
	 * @return This panel
	 */
	public ComponentInfoPanel format(Optional<FormatMetadata> metadata) {
		if (metadata.isPresent()) {
			formatLabel.setText(metadata.get().toString());
		} else {
			formatLabel.setText("");
		}
		return this;
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates the panel for the given controlled component.
	 *
	 * @param controlled
	 * 		The controlled component
	 */
	private void createPanel(Controlled controlled) {
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		headerLabel.setText(controlled.name());
		headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));

		int line = 0;
		add(headerLabel, new GridBagConstraints(0, line++, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(new JLabel("Input"), new GridBagConstraints(0, line, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(18, 0, 0, 0), 0, 0));
		add(inputLabel, new GridBagConstraints(1, line++, 1, 1, 1.0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(18, 6, 0, 0), 0, 0));
		add(new JLabel("Output"), new GridBagConstraints(0, line, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));
		add(outputLabel, new GridBagConstraints(1, line++, 1, 1, 1.0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
		add(new JLabel("Format"), new GridBagConstraints(0, line, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));
		add(formatLabel, new GridBagConstraints(1, line++, 1, 1, 1.0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

		/* add the controllers. */
		for (Controller<?> controller : controlled.controllers()) {
			add(new JLabel(controller.name()), new GridBagConstraints(0, line, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 0, 6), 0, 0));
			if (controller instanceof Fader) {
				add(new FaderPanel((Fader) controller), new GridBagConstraints(1, line++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
			} else if (controller instanceof Switch) {
				add(new SwitchPanel((Switch) controller), new GridBagConstraints(1, line++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
			} else if (controller instanceof Knob) {
				add(new KnobPanel((Knob) controller), new GridBagConstraints(1, line++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
			}
		}

		add(Box.createVerticalGlue(), new GridBagConstraints(1, line++, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(6, 6, 0, 0), 0, 0));
	}

	/**
	 * Formats the number using SI prefixes so that a maximum of 3 digits are
	 * shown.
	 *
	 * @param number
	 * 		The number to format
	 * @return The formatted number
	 */
	private static String format(long number) {
		String[] prefixes = { "", "Ki", "Mi", "Gi", "Ti", "Pi", "Ei" };
		double shortenedNumber = number;
		for (String prefix : prefixes) {
			if (shortenedNumber < 1000) {
				return String.format("%.1f %sB", shortenedNumber, prefix);
			}
			shortenedNumber /= 1024;
		}
		return String.format("%.1e B", (double) number);
	}

}
