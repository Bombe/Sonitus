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

import static javax.swing.BorderFactory.createEtchedBorder;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.pterodactylus.sonitus.data.Controlled;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Pipeline;
import net.pterodactylus.sonitus.data.Sink;
import net.pterodactylus.sonitus.data.Source;

import com.google.common.collect.Lists;

/**
 * {@link JPanel} that displays all components of a {@link Pipeline}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PipelinePanel extends JPanel {

	/** The pipeline being displayed. */
	private final Pipeline pipeline;

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
	// PRIVATE METHODS
	//

	/** Update the panel. Needs to be called when the pipeline is changed. */
	private void updatePanel() {
		/* clear everything. */
		removeAll();

		/* count all sinks. */
		int sinkCount = 0;
		List<Source> sources = Lists.newArrayList(pipeline.source());
		while (!sources.isEmpty()) {
			Collection<Sink> sinks = pipeline.sinks(sources.remove(0));
			for (Sink sink : sinks) {
				/* only count real sinks, everything else is filter. */
				if (sink instanceof Filter) {
					sources.add((Filter) sink);
				} else {
					sinkCount++;
				}
			}
		}

		/* get number of maximum horizontal grid cells. */
		int gridCellCount = 1;
		for (int n = 2; n <= sinkCount; ++n) {
			gridCellCount *= n;
		}

		/* paint all components recursively. */
		addControlled(pipeline.source(), 0, 0, gridCellCount);
	}

	/**
	 * Displays the given component.
	 *
	 * @param controlled
	 * 		The component to add this panel.
	 * @param level
	 * 		The level at which to show the component (the source is level {@code 0})
	 * @param position
	 * 		The position at which to display the component
	 * @param width
	 * 		The width of the component in grid cells
	 */
	private void addControlled(Controlled controlled, int level, int position, int width) {
		/* create a GUI component that displays the component. */
		JLabel sourceLabel = new JLabel(controlled.name());
		sourceLabel.setBorder(createEtchedBorder());

		/* show component. */
		add(sourceLabel, new GridBagConstraints(position, level, width, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		/* if the component does not have connected sinks, exit here. */
		if (!(controlled instanceof Source)) {
			return;
		}

		/* iterate over the component’s sinks. */
		Collection<Sink> sinks = pipeline.sinks((Source) controlled);
		int sinkWidth = width / sinks.size();
		int sinkIndex = 0;
		for (Sink connectedSink : sinks) {
			/* distribute all sinks evenly below this source. */
			addControlled(connectedSink, level + 1, position + sinkIndex * sinkWidth, sinkWidth);
			sinkIndex++;
		}
	}

}
