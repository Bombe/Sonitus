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
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import net.pterodactylus.sonitus.data.Controlled;
import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.Pipeline;
import net.pterodactylus.sonitus.main.Version;

import com.google.common.eventbus.EventBus;

/**
 * Sonitus main window.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MainWindow extends JFrame {

	/** The event bus. */
	private final EventBus eventBus;

	/** The pipeline to display. */
	private final Pipeline pipeline;

	/** The tabbed pane displaying all controlled components. */
	private final JTabbedPane tabbedPane = new JTabbedPane();

	/**
	 * Creates a new main window.
	 *
	 * @param eventBus
	 * 		The event bus
	 * @param pipeline
	 * 		The pipeline to display
	 */
	public MainWindow(EventBus eventBus, Pipeline pipeline) {
		super(String.format("Sonitus %s", Version.version()));
		this.eventBus = eventBus;
		this.pipeline = pipeline;
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		tabbedPane.add("Pipeline", new PipelinePanel(pipeline));
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		setSize(new Dimension(800, 450));

		/* FIXME - shut everything down properly. */
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	//
	// ACTIONS
	//

	/**
	 * Adds the given controlled to this main window.
	 *
	 * @param controlled
	 * 		The controlled to add
	 */
	public void addControllers(Controlled controlled) {
		List<Controller<?>> controllers = controlled.controllers();
		ControlledPane controlledPane = new ControlledPane(eventBus, controlled);
		tabbedPane.addTab(controlled.name(), controlledPane);
	}

}
