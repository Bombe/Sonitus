/*
 * Sonitus - AbstractControlledComponent.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;

/**
 * Abstract {@link ControlledComponent} implementation that takes care of
 * managing {@link MetadataListener}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class AbstractControlledComponent implements ControlledComponent {

	/** The name of this filter. */
	private final String name;

	/** The list of metadata listeners. */
	private final List<MetadataListener> metadataListeners = Lists.newCopyOnWriteArrayList();

	/** The current metadata. */
	private final AtomicReference<Metadata> metadata = new AtomicReference<Metadata>();

	/**
	 * Creates a new abstract controlled component.
	 *
	 * @param name
	 * 		The name of the component
	 */
	protected AbstractControlledComponent(String name) {
		this.name = name;
	}

	//
	// LISTENER MANAGEMENT
	//

	@Override
	public void addMetadataListener(MetadataListener metadataListener) {
		metadataListeners.add(metadataListener);
	}

	@Override
	public void removeMetadataListener(MetadataListener metadataListener) {
		metadataListeners.remove(metadataListener);
	}

	//
	// CONTROLLEDCOMPONENT METHODS
	//

	@Override
	public String name() {
		return name;
	}

	@Override
	public Metadata metadata() {
		return metadata.get();
	}

	@Override
	public void metadataUpdated(Metadata metadata) {
		if (metadata.equals(this.metadata.get())) {
			return;
		}
		this.metadata.set(metadata);
		fireMetadataUpdated(metadata);
	}

	//
	// EVENT METHODS
	//

	/**
	 * Notifies all registered metadata listeners that the metadata has changed.
	 *
	 * @param metadata
	 * 		The new metadata
	 */
	protected void fireMetadataUpdated(Metadata metadata) {
		for (MetadataListener metadataListener : metadataListeners) {
			metadataListener.metadataUpdated(this, metadata);
		}
	}

}
