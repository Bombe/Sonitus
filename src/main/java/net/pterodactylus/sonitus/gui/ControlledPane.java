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

import static javax.swing.BorderFactory.createEtchedBorder;
import static javax.swing.BorderFactory.createTitledBorder;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.pterodactylus.sonitus.data.Controlled;
import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.controller.Fader;
import net.pterodactylus.sonitus.data.controller.Knob;
import net.pterodactylus.sonitus.data.controller.Switch;
import net.pterodactylus.sonitus.data.event.MetadataUpdated;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

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
	 * @param eventBus
	 * 		The event bus
	 * @param controlled
	 * 		The controlled whose controllers to display
	 */
	public ControlledPane(EventBus eventBus, Controlled controlled) {
		super(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(6, 12, 12, 12));

		MetadataPanel metadataPanel = new MetadataPanel(controlled);
		eventBus.register(metadataPanel);
		add(metadataPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
		add(createControllerPanel(controlled), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
		add(Box.createVerticalGlue(), new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(6, 0, 0, 0), 0, 0));
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates the controller panel for the given component.
	 *
	 * @param controlled
	 * 		The component whose controllers to display
	 * @return The created controller panel
	 */
	private JComponent createControllerPanel(Controlled controlled) {
		JPanel controllerPanel = new JPanel(new GridBagLayout());
		controllerPanel.setBorder(createTitledBorder(createEtchedBorder(), "Controller"));

		int controllerIndex = 0;
		for (Controller controller : controlled.controllers()) {
			controllerPanel.add(new JLabel(controller.name()), new GridBagConstraints(0, controllerIndex, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 0, 6), 0, 0));
			if (controller instanceof Fader) {
				controllerPanel.add(new FaderPanel((Fader) controller), new GridBagConstraints(1, controllerIndex, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
			} else if (controller instanceof Switch) {
				controllerPanel.add(new SwitchPanel((Switch) controller), new GridBagConstraints(1, controllerIndex, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
			} else if (controller instanceof Knob) {
				controllerPanel.add(new KnobPanel((Knob) controller), new GridBagConstraints(1, controllerIndex, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 0, 0), 0, 0));
			}
			++controllerIndex;
		}

		return controllerPanel;
	}

	/**
	 * A {@link JPanel} that displays {@link Metadata} information about a {@link
	 * Controlled} component.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class MetadataPanel extends JPanel {

		/** The controlled component. */
		private final Controlled controlled;

		/** The format label. */
		private JLabel format;

		/** The title label. */
		private JLabel title;

		/**
		 * Creates a new metadata panel.
		 *
		 * @param controlled
		 * 		The controlled component
		 */
		public MetadataPanel(Controlled controlled) {
			this.controlled = controlled;

			setBorder(createTitledBorder(createEtchedBorder(), "Metadata"));
			setLayout(new GridBagLayout());

			format = new JLabel();
			title = new JLabel();

			JLabel formatLabel = new JLabel("Format");
			formatLabel.setFont(formatLabel.getFont().deriveFont(Font.BOLD));
			JLabel titleLabel = new JLabel("Title");
			titleLabel.setFont(formatLabel.getFont().deriveFont(Font.BOLD));

			add(formatLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));
			add(format, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));
			add(titleLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));
			add(title, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 0, 0), 0, 0));

			metadataUpdated(new MetadataUpdated(controlled, controlled.metadata()));
		}

		/**
		 * Called by the {@link EventBus} when a {@link Controlled} component updates
		 * its metadata.
		 *
		 * @param metadataUpdated
		 * 		The metadata updated event
		 */
		@Subscribe
		public void metadataUpdated(MetadataUpdated metadataUpdated) {
			/* do we care about the event’s component? */
			if (!controlled.equals(metadataUpdated.controlled())) {
				return;
			}

			/* did we actually get any metadata? */
			Metadata metadata = metadataUpdated.metadata();
			if (metadata == null) {
				format.setText("");
				title.setText("");
				return;
			}

			/* set the texts. */
			format.setText(String.format("%s kHz, %d channel%s, %s", metadata.frequency() / 1000.0, metadata.channels(), metadata.channels() != 1 ? "s" : "", metadata.encoding()));
			title.setText(metadata.title());
		}

	}

}
