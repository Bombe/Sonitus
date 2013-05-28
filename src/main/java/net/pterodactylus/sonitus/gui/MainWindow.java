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

import net.pterodactylus.sonitus.data.ControlledComponent;
import net.pterodactylus.sonitus.data.Pipeline;
import net.pterodactylus.sonitus.gui.PipelinePanel.ComponentHoverListener;
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

	/** The tabbed pane displaying all controlled components. */
	private final JTabbedPane tabbedPane = new JTabbedPane();

	/** The info panel card layout. */
	private final CardLayout infoPanelCardLayout = new CardLayout();

	/** The info panel. */
	private final JPanel infoPanel = new JPanel(infoPanelCardLayout);

	/** The mapping from controlled components to info panels. */
	private final Map<ControlledComponent, ComponentInfoPanel> controlledInfoPanels = Maps.newHashMap();

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
		pipelinePanel.addComponentHoverListener(new ComponentHoverListener() {

			@Override
			public void componentEntered(ControlledComponent controlledComponent) {
				infoPanelCardLayout.show(infoPanel, controlledComponent.name());
			}
		});
		pipelineInfoPanel.add(pipelinePanel, BorderLayout.CENTER);
		pipelineInfoPanel.add(infoPanel, BorderLayout.EAST);
		tabbedPane.add("Pipeline", pipelineInfoPanel);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		setSize(new Dimension(800, 450));

		/* create info panels for all components. */
		for (ControlledComponent controlledComponent : pipeline) {
			ComponentInfoPanel componentInfoPanel = new ComponentInfoPanel(controlledComponent);
			infoPanel.add(componentInfoPanel, controlledComponent.name());
			controlledInfoPanels.put(controlledComponent, componentInfoPanel);
		}

		Timer timer = new Timer(250, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				/* update all info panels. */
				for (ControlledComponent controlled : MainWindow.this.pipeline) {
					ComponentInfoPanel componentInfoPanel = controlledInfoPanels.get(controlled);
					componentInfoPanel.input(MainWindow.this.pipeline.trafficCounter(controlled).input());
					componentInfoPanel.output(MainWindow.this.pipeline.trafficCounter(controlled).output());
					componentInfoPanel.format(Optional.of(controlled.metadata().format()));
				}
			}
		});
		timer.start();

		/* FIXME - shut everything down properly. */
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

}
