/*
 * Sonitus - PipelineFilter.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.AbstractFilter;
import net.pterodactylus.sonitus.data.DataPacket;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.Pipeline.Connection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * {@link Filter} that combines several filters into one.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PipelineFilter extends AbstractFilter implements Filter {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(PipelineFilter.class.getName());

	/** The first filter. */
	private final Filter source;

	/** All following filters. */
	private final List<Filter> filters = Lists.newArrayList();

	/** The last filter (for convenience). */
	private final Filter lastFilter;

	/** The connections for each filter. */
	private final Map<Filter, Connection> filterConnections = Maps.newHashMap();

	/**
	 * Creates a new pipeline filter.
	 *
	 * @param name
	 * 		The name of the filter
	 * @param source
	 * 		The first source of the filter
	 * @param filters
	 * 		All other filters in correct order
	 */
	private PipelineFilter(String name, Filter source, Collection<Filter> filters) {
		super(name);
		this.source = source;
		this.filters.addAll(filters);
		this.lastFilter = this.filters.get(filters.size() - 1);
	}

	//
	// FILTER METHODS
	//

	@Override
	public Metadata metadata() {
		return lastFilter.metadata();
	}

	@Override
	public void open(Metadata metadata) throws IOException {
		/* open the source and all filters in the correct order. */
		source.open(metadata);
		Metadata currentMetadata = source.metadata();
		Filter currentSource = source;
		for (Filter filter : filters) {
			filter.open(currentMetadata);
			currentMetadata = filter.metadata();
			Connection connection = new Connection(currentSource, Arrays.asList(filter));
			filterConnections.put(filter, connection);
			String threadName = String.format("%s → %s", connection.source().name(), filter.name());
			logger.info(String.format("Starting Thread: %s.", threadName));
			new Thread(connection, threadName).start();
			currentSource = filter;
		}
		metadataUpdated(currentMetadata);
	}

	@Override
	public DataPacket get(int bufferSize) throws IOException {
		if (filterConnections.get(lastFilter).ioException().isPresent()) {
			logger.info(String.format("Rethrowing exception from %s: %s", lastFilter.name(), filterConnections.get(lastFilter).ioException().get().getMessage()));
			throw filterConnections.get(lastFilter).ioException().get();
		}
		logger.info(String.format("Requesting %d bytes from %s...", bufferSize, lastFilter.name()));
		return lastFilter.get(bufferSize);
	}

	@Override
	public void process(DataPacket dataPacket) throws IOException {
		source.process(dataPacket);
	}

	//
	// STATIC METHODS
	//

	/**
	 * Returns a builder that can create pipeline filters.
	 *
	 * @param source
	 * 		The source filter of the pipeline
	 * @return The pipeline filter builder
	 */
	public static Builder builder(Filter source) {
		return new Builder(source);
	}

	/**
	 * Builder for a {@link PipelineFilter}.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class Builder {

		/** The source of the pipeline. */
		private final Filter source;

		/** All other filters of the pipeline. */
		private final List<Filter> filters = Lists.newArrayList();

		/**
		 * Creates a new builder with the given source.
		 *
		 * @param source
		 * 		The source of the pipeline filter
		 */
		private Builder(Filter source) {
			this.source = source;
		}

		/**
		 * Connects the given filter at the end of the pipeline being build.
		 *
		 * @param filter
		 * 		The filter to add
		 * @return This builder
		 */
		public Builder to(Filter filter) {
			filters.add(filter);
			return this;
		}

		/**
		 * Builds a filter using the given name. If no filters other than the source
		 * have been added, only the source filter is being returned.
		 *
		 * @param name
		 * 		The name of the pipeline filter to build
		 * @return The created filter, or the source filter
		 */
		public Filter build(String name) {
			if (filters.isEmpty()) {
				return source;
			}
			return new PipelineFilter(name, source, filters);
		}

	}

}
