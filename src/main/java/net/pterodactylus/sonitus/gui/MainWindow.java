/*
 * Sonitus - MainWindow.java - Copyright © 2013 David Roden
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Pipeline;
import net.pterodactylus.sonitus.gui.PipelinePanel.FilterSelectionListener;
import net.pterodactylus.sonitus.main.Version;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * Sonitus main window.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MainWindow extends JFrame {

	/** The pipeline to display. */
	private final Pipeline pipeline;

	/** The tabbed pane displaying all pipelines. */
	private final JTabbedPane tabbedPane = new JTabbedPane();

	/** The info panel card layout. */
	private final CardLayout infoPanelCardLayout = new CardLayout();

	/** The info panel. */
	private final JPanel infoPanel = new JPanel(infoPanelCardLayout);

	/** The mapping from filters to info panels. */
	private final Map<Filter, FilterInfoPanel> filterInfoPanels = Maps.newHashMap();

	/**
	 * Creates a new main window.
	 *
	 * @param pipeline
	 * 		The pipeline to display
	 */
	public MainWindow(Pipeline pipeline) {
		super(String.format("Sonitus %s", Version.version()));
		this.pipeline = pipeline;
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		final JPanel pipelineInfoPanel = new JPanel(new BorderLayout(12, 12));
		PipelinePanel pipelinePanel = new PipelinePanel(pipeline);
		pipelinePanel.addFilterSelectionListener(new FilterSelectionListener() {

			@Override
			public void filterSelected(Filter filter) {
				infoPanelCardLayout.show(infoPanel, filter.name());
			}
		});
		pipelineInfoPanel.add(pipelinePanel, BorderLayout.CENTER);
		pipelineInfoPanel.add(infoPanel, BorderLayout.EAST);
		tabbedPane.add("Pipeline", pipelineInfoPanel);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		setSize(new Dimension(800, 450));

		/* create info panels for all filters. */
		for (Filter fliter : pipeline) {
			FilterInfoPanel filterInfoPanel = new FilterInfoPanel(fliter);
			infoPanel.add(filterInfoPanel, fliter.name());
			filterInfoPanels.put(fliter, filterInfoPanel);
		}

		Timer timer = new Timer(250, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				/* update all info panels. */
				for (Filter filter : MainWindow.this.pipeline) {
					FilterInfoPanel filterInfoPanel = filterInfoPanels.get(filter);
					filterInfoPanel.input(MainWindow.this.pipeline.trafficCounter(filter).input());
					filterInfoPanel.output(MainWindow.this.pipeline.trafficCounter(filter).output());
					filterInfoPanel.format(Optional.of(filter.metadata().format()));
				}
			}
		});
		timer.start();

		/* FIXME - shut everything down properly. */
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

}
