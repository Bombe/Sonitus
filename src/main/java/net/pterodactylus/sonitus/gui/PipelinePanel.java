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
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.EventListener;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.MetadataListener;
import net.pterodactylus.sonitus.data.Pipeline;

/**
 * {@link JPanel} that displays all filters of a {@link Pipeline}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PipelinePanel extends JPanel {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(PipelinePanel.class.getName());

	/** The pipeline being displayed. */
	private final Pipeline pipeline;

	/** The filter selection listeners. */
	private final EventListenerList filterSelectionListeners = new EventListenerList();

	/** The currently selected filter. */
	private JComponent selectedFilter;

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
	 * Adds the given filter selection listener to this panel.
	 *
	 * @param filterSelectionListener
	 * 		The filter selection listener to add
	 */
	public void addFilterSelectionListener(FilterSelectionListener filterSelectionListener) {
		filterSelectionListeners.add(FilterSelectionListener.class, filterSelectionListener);
	}

	//
	// PRIVATE METHODS
	//

	/** Update the panel. Needs to be called when the pipeline is changed. */
	private void updatePanel() {
		/* clear everything. */
		removeAll();

		/* count all filters. */
		int sinkCount = 0;
		for (Filter filter : pipeline.filters()) {
			Collection<Filter> sinks = pipeline.filters(filter);
			logger.finest(String.format("%s has %d filters: %s", filter.name(), sinks.size(), sinks));
			if (sinks.isEmpty()) {
				sinkCount++;
			}
		}

		/* get number of maximum horizontal grid cells. */
		int gridCellCount = 1;
		for (int n = 2; n <= sinkCount; ++n) {
			gridCellCount *= n;
		}

		/* paint all filters recursively. */
		addFilter(pipeline.source(), 0, 0, gridCellCount, null);
	}

	/**
	 * Displays the given filter.
	 *
	 * @param filter
	 * 		The filter to add this panel.
	 * @param level
	 * 		The level at which to show the filter (the source is level {@code 0})
	 * @param position
	 * 		The position at which to display the filter
	 * @param width
	 * 		The width of the filter in grid cells
	 */
	private void addFilter(final Filter filter, int level, int position, int width, Filter parentFilter) {
		/* create a GUI component that displays the filter. */
		final JPanel filterPanel = createFilterPanel(filter, parentFilter);
		filterPanel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				for (Component component : getComponents()) {
					component.setBackground(UIManager.getColor("Panel.background"));
				}
				for (FilterSelectionListener filterSelectionListener : filterSelectionListeners.getListeners(FilterSelectionListener.class)) {
					filterPanel.setBackground(Color.LIGHT_GRAY);
					filterSelectionListener.filterSelected(filter);
				}
				selectedFilter = filterPanel;
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent) {
				if (filterPanel != selectedFilter) {
					filterPanel.setBackground(Color.white);
				}
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent) {
				if (filterPanel != selectedFilter) {
					filterPanel.setBackground(UIManager.getColor("Panel.background"));
				}
			}
		});

		/* show filter. */
		add(filterPanel, new GridBagConstraints(position, level, width, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		/* if the filter does not have connected filters, exit here. */
		Collection<Filter> sinks = pipeline.filters(filter);
		if (sinks.isEmpty()) {
			add(new JPanel(), new GridBagConstraints(position, 999, width, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			return;
		}

		/* iterate over the filter’s connected filters. */
		if (!sinks.isEmpty()) {
			int sinkWidth = width / sinks.size();
			int sinkIndex = 0;
			for (Filter connectedSink : sinks) {
				/* distribute all filters evenly below this source. */
				addFilter(connectedSink, level + 1, position + sinkIndex * sinkWidth, sinkWidth, filter);
				sinkIndex++;
			}
		}
	}

	/**
	 * Creates a panel displaying a single filter.
	 *
	 * @param filter
	 * 		The filter to display
	 * @return The created panel
	 */
	private static JPanel createFilterPanel(final Filter filter, final Filter parentFilter) {
		JPanel filterPanel = new JPanel(new BorderLayout(12, 12));
		filterPanel.setBorder(createCompoundBorder(createEtchedBorder(), createEmptyBorder(0, 4, 0, 3)));
		filterPanel.add(new JLabel(filter.name()), BorderLayout.WEST);
		final JLabel titleLabel = new JLabel(filter.metadata().fullTitle());
		titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getSize2D() * 0.8f));
		filterPanel.add(titleLabel, BorderLayout.EAST);
		if (parentFilter != null) {
			titleLabel.setVisible(!parentFilter.metadata().fullTitle().equals(filter.metadata().fullTitle()));
		}
		filter.addMetadataListener(new MetadataListener() {

			@Override
			public void metadataUpdated(Filter filter, Metadata metadata) {
				titleLabel.setText(metadata.fullTitle());
				titleLabel.setVisible((parentFilter == null) || !parentFilter.metadata().fullTitle().equals(metadata.fullTitle()));
			}
		});
		return filterPanel;
	}

	/**
	 * Interface for objects that want to be notified if the user moves the mouse
	 * cursor over a filter.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static interface FilterSelectionListener extends EventListener {

		/**
		 * Notifies the listener that the mouse is now over the given filter.
		 *
		 * @param filter
		 * 		The filter now under the mouse
		 */
		void filterSelected(Filter filter);

	}

}
