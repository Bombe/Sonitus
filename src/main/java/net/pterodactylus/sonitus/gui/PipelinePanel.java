/*
 * Sonitus - PipelinePanel.java - Copyright © 2013 David Roden
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

import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createEtchedBorder;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.EventListener;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import net.pterodactylus.sonitus.data.ControlledComponent;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.MetadataListener;
import net.pterodactylus.sonitus.data.Pipeline;
import net.pterodactylus.sonitus.data.Sink;
import net.pterodactylus.sonitus.data.Source;

/**
 * {@link JPanel} that displays all components of a {@link Pipeline}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PipelinePanel extends JPanel {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(PipelinePanel.class.getName());

	/** The pipeline being displayed. */
	private final Pipeline pipeline;

	/** The component hover listeners. */
	private final EventListenerList componentSelectionListeners = new EventListenerList();

	/**
	 * Creates a new pipeline panel displaying the given pipeline.
	 *
	 * @param pipeline
	 * 		The pipeline to display
	 */
	public PipelinePanel(Pipeline pipeline) {
		super(new GridBagLayout());
		this.pipeline = pipeline;
		updatePanel();
	}

	//
	// LISTENER MANAGEMENT
	//

	/**
	 * Adds the given component selection listener to this panel.
	 *
	 * @param componentSelectionListener
	 * 		The component selection listener to add
	 */
	public void addComponentHoverListener(ComponentSelectionListener componentSelectionListener) {
		componentSelectionListeners.add(ComponentSelectionListener.class, componentSelectionListener);
	}

	//
	// PRIVATE METHODS
	//

	/** Update the panel. Needs to be called when the pipeline is changed. */
	private void updatePanel() {
		/* clear everything. */
		removeAll();

		/* count all sinks. */
		int sinkCount = 0;
		for (ControlledComponent component : pipeline.components()) {
			if (!(component instanceof Source)) {
				logger.finest(String.format("%s is not a Source, skipping.", component.name()));
				sinkCount++;
				continue;
			}
			Collection<Sink> sinks = pipeline.sinks((Source) component);
			logger.finest(String.format("%s has %d sinks: %s", component.name(), sinks.size(), sinks));
			if (sinks.isEmpty()) {
				sinkCount++;
			}
		}

		/* get number of maximum horizontal grid cells. */
		int gridCellCount = 1;
		for (int n = 2; n <= sinkCount; ++n) {
			gridCellCount *= n;
		}

		/* paint all components recursively. */
		addControlled(pipeline.source(), 0, 0, gridCellCount, null);
	}

	/**
	 * Displays the given component.
	 *
	 * @param controlledComponent
	 * 		The component to add this panel.
	 * @param level
	 * 		The level at which to show the component (the source is level {@code 0})
	 * @param position
	 * 		The position at which to display the component
	 * @param width
	 * 		The width of the component in grid cells
	 */
	private void addControlled(final ControlledComponent controlledComponent, int level, int position, int width, ControlledComponent parentComponent) {
		/* create a GUI component that displays the component. */
		final JPanel componentPanel = createComponentPanel(controlledComponent, parentComponent);
		componentPanel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				for (ComponentSelectionListener componentSelectionListener : componentSelectionListeners.getListeners(ComponentSelectionListener.class)) {
					componentSelectionListener.componentSelected(controlledComponent);
				}
				selectedComponent = componentPanel;
			}
		});

		/* show component. */
		add(componentPanel, new GridBagConstraints(position, level, width, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		/* if the component does not have connected sinks, exit here. */
		if (!(controlledComponent instanceof Source)) {
			add(new JPanel(), new GridBagConstraints(position, 999, width, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			return;
		}

		/* iterate over the component’s sinks. */
		Collection<Sink> sinks = pipeline.sinks((Source) controlledComponent);
		if (!sinks.isEmpty()) {
			int sinkWidth = width / sinks.size();
			int sinkIndex = 0;
			for (Sink connectedSink : sinks) {
				/* distribute all sinks evenly below this source. */
				addControlled(connectedSink, level + 1, position + sinkIndex * sinkWidth, sinkWidth, controlledComponent);
				sinkIndex++;
			}
		}
	}

	/**
	 * Creates a panel displaying a single component.
	 *
	 * @param controlledComponent
	 * 		The component to display
	 * @return The created panel
	 */
	private static JPanel createComponentPanel(final ControlledComponent controlledComponent, final ControlledComponent parentComponent) {
		JPanel componentPanel = new JPanel(new BorderLayout(12, 12));
		componentPanel.setBorder(createCompoundBorder(createEtchedBorder(), createEmptyBorder(0, 4, 0, 3)));
		componentPanel.add(new JLabel(controlledComponent.name()), BorderLayout.WEST);
		final JLabel titleLabel = new JLabel(controlledComponent.metadata().fullTitle());
		titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getSize2D() * 0.8f));
		componentPanel.add(titleLabel, BorderLayout.EAST);
		if (parentComponent != null) {
			titleLabel.setVisible(!parentComponent.metadata().fullTitle().equals(controlledComponent.metadata().fullTitle()));
		}
		controlledComponent.addMetadataListener(new MetadataListener() {

			@Override
			public void metadataUpdated(ControlledComponent component, Metadata metadata) {
				titleLabel.setText(metadata.fullTitle());
				titleLabel.setVisible((parentComponent == null) || !parentComponent.metadata().fullTitle().equals(metadata.fullTitle()));
			}
		});
		return componentPanel;
	}

	/**
	 * Interface for objects that want to be notified if the user moves the mouse
	 * cursor over a controlled component.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static interface ComponentSelectionListener extends EventListener {

		/**
		 * Notifies the listener that the mouse is now over the given controlled
		 * component.
		 *
		 * @param controlledComponent
		 * 		The controlled component now under the mouse
		 */
		void componentSelected(ControlledComponent controlledComponent);

	}

}
