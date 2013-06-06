/*
 * Sonitus - Pipeline.java - Copyright © 2013 David Roden
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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * A pipeline is responsible for streaming audio data from a {@link Filter} to
 * an arbitrary number of connected {@link Filter}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Pipeline implements Iterable<Filter> {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(Pipeline.class.getName());

	/** The source of the audio stream. */
	private final Filter source;

	/** The filters for each source. */
	private final ListMultimap<Filter, Filter> filters;

	/** All started connections. */
	private final List<Connection> connections = Lists.newArrayList();

	/**
	 * Creates a new pipeline.
	 *
	 * @param source
	 * 		The source of the audio stream
	 * @param filters
	 * 		The filters for each source
	 */
	private Pipeline(Filter source, Multimap<Filter, Filter> filters) {
		this.source = Preconditions.checkNotNull(source, "source must not be null");
		this.filters = ArrayListMultimap.create(Preconditions.checkNotNull(filters, "filters must not be null"));
	}

	//
	// ACCESSORS
	//

	/**
	 * Expose this pipeline’s source.
	 *
	 * @return This pipeline’s source
	 */
	public Filter source() {
		return source;
	}

	/**
	 * Returns all {@link Filter}s that are connected to the given filter.
	 *
	 * @param filter
	 * 		The filter to get the connected filters for
	 * @return The filters connected to the given filter, or an empty list if the
	 *         filter does not exist in this pipeline, or is not connected to any filters
	 */
	public List<Filter> filters(Filter filter) {
		return filters.get(filter);
	}

	/**
	 * Returns the traffic counters of the given filter.
	 *
	 * @param filter
	 * 		The filter to get the traffic counters for
	 * @return The traffic counters for the given filter
	 */
	public TrafficCounter trafficCounter(Filter filter) {
		long input = -1;
		long output = -1;
		for (Connection connection : connections) {
			/* the connection where the source matches knows the output. */
			if (connection.source.equals(filter)) {
				output = connection.counter();
			} else if (connection.sinks.contains(filter)) {
				input = connection.counter();
			}
		}
		return new TrafficCounter(input, output);
	}

	//
	// ACTIONS
	//

	/**
	 * Starts the pipeline.
	 *
	 * @throws IOException
	 * 		if any of the filters can not be opened
	 * @throws IllegalStateException
	 * 		if the pipeline is already running
	 */
	public void start() throws IOException, IllegalStateException {
		if (!connections.isEmpty()) {
			throw new IllegalStateException("Pipeline is already running!");
		}
		List<Filter> filters = Lists.newArrayList();
		filters.add(source);
		Metadata currentMetadata = Metadata.UNKNOWN;
		/* collect all source->sink pairs. */
		while (!filters.isEmpty()) {
			Filter filter = filters.remove(0);
			logger.info(String.format("Opening %s with %s...", filter.name(), currentMetadata));
			filter.open(currentMetadata);
			currentMetadata = filter.metadata();
			Collection<Filter> sinks = this.filters.get(filter);
			connections.add(new Connection(filter, sinks));
			for (Filter sink : sinks) {
				filters.add(sink);
			}
		}
		for (Connection connection : connections) {
			String threadName = String.format("%s → %s.", connection.source.name(), FluentIterable.from(connection.sinks).transform(new Function<Filter, String>() {

				@Override
				public String apply(Filter sink) {
					return sink.name();
				}
			}));
			logger.info(String.format("Starting Thread: %s", threadName));
			new Thread(connection, threadName).start();
		}
	}

	public void stop() {
		if (!connections.isEmpty()) {
			/* pipeline is not running. */
			return;
		}
		for (Connection connection : connections) {
			connection.stop();
		}
	}

	//
	// ITERABLE METHODS
	//

	@Override
	public Iterator<Filter> iterator() {
		return filters().iterator();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Returns all filters of this pipeline, listed breadth-first, starting with
	 * the source.
	 *
	 * @return All filters of this pipeline
	 */
	public List<Filter> filters() {
		ImmutableList.Builder<Filter> filters = ImmutableList.builder();
		List<Filter> remainingFilters = Lists.newArrayList();
		filters.add(source);
		remainingFilters.add(source);
		while (!remainingFilters.isEmpty()) {
			Collection<Filter> sinks = this.filters(remainingFilters.remove(0));
			for (Filter sink : sinks) {
				filters.add(sink);
				remainingFilters.add(sink);
			}
		}
		return filters.build();
	}

	//
	// STATIC METHODS
	//

	/**
	 * Returns a new pipeline builder.
	 *
	 * @param source
	 * 		The source at which to start
	 * @return A builder for a new pipeline
	 */
	public static Builder builder(Filter source) {
		return new Builder(source);
	}

	/**
	 * A builder for a {@link Pipeline}.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class Builder {

		/** The source of the pipeline. */
		private final Filter source;

		/** The filters to which each source streams. */
		private Multimap<Filter, Filter> nextSinks = ArrayListMultimap.create();

		/** The last added source. */
		private Filter lastSource;

		/**
		 * Creates a new builder.
		 *
		 * @param source
		 * 		The source that starts the pipeline
		 */
		private Builder(Filter source) {
			this.source = source;
			lastSource = source;
		}

		/**
		 * Adds a {@link Filter} as a recipient for the last added source.
		 *
		 * @param sink
		 * 		The sink to add
		 * @return This builder
		 */
		public Builder to(Filter sink) {
			nextSinks.put(lastSource, sink);
			lastSource = sink;
			return this;
		}

		/**
		 * Locates the given source and sets it as the last added node so that the
		 * next invocation of {@link #to(Filter)} can “fork” the pipeline.
		 *
		 * @param source
		 * 		The source to locate
		 * @return This builder
		 * @throws IllegalStateException
		 * 		if the given source was not previously added as a sink
		 */
		public Builder find(Filter source) {
			Preconditions.checkState(nextSinks.containsValue(source));
			lastSource = source;
			return this;
		}

		/**
		 * Builds the pipeline.
		 *
		 * @return The created pipeline
		 */
		public Pipeline build() {
			return new Pipeline(source, ImmutableMultimap.copyOf(nextSinks));
		}

	}

	/**
	 * A connection is responsible for streaming audio from one {@link Filter} to
	 * an arbitrary number of {@link Filter}s it is connected to. A connection is
	 * started by creating a {@link Thread} wrapping it and starting said thread.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class Connection implements Runnable {

		/** The source. */
		private final Filter source;

		/** The filters. */
		private final Collection<Filter> sinks;

		/** Whether the feeder was stopped. */
		private final AtomicBoolean stopped = new AtomicBoolean(false);

		/** The executor service. */
		private final ExecutorService executorService;

		/** The time the connection was started. */
		private long startTime;

		/** The number of copied bytes. */
		private long counter;

		/**
		 * Creates a new connection.
		 *
		 * @param source
		 * 		The source of the stream
		 * @param sinks
		 * 		The filters to which to stream
		 */
		public Connection(Filter source, Collection<Filter> sinks) {
			this.source = source;
			this.sinks = sinks;
			if (sinks.size() < 2) {
				executorService = MoreExecutors.sameThreadExecutor();
			} else {
				executorService = Executors.newCachedThreadPool();
			}
		}

		//
		// ACCESSORS
		//

		/**
		 * Returns the source of this connection.
		 *
		 * @return The source of this connection
		 */
		public Filter source() {
			return source;
		}

		/**
		 * Returns the sinks of this connection.
		 *
		 * @return The sinks of this connection
		 */
		public Collection<Filter> sinks() {
			return sinks;
		}

		/**
		 * Returns the time this connection was started.
		 *
		 * @return The time this connection was started (in milliseconds since Jan 1,
		 *         1970 UTC)
		 */
		public long startTime() {
			return startTime;
		}

		/**
		 * Returns the number of bytes that this connection has received from its
		 * source during its lifetime.
		 *
		 * @return The number of processed input bytes
		 */
		public long counter() {
			return counter;
		}

		//
		// ACTIONS
		//

		/** Stops this connection. */
		public void stop() {
			stopped.set(true);
		}

		//
		// RUNNABLE METHODS
		//

		@Override
		public void run() {
			startTime = System.currentTimeMillis();
			while (!stopped.get()) {
				try {
					final DataPacket dataPacket;
					try {
						logger.finest(String.format("Getting %d bytes from %s...", 4096, source.name()));
						dataPacket = source.get(4096);
						logger.finest(String.format("Got %d bytes from %s.", dataPacket.buffer().length, source.name()));
					} catch (IOException ioe1) {
						throw new IOException(String.format("I/O error while reading from %s.", source.name()), ioe1);
					}
					List<Future<Void>> futures = executorService.invokeAll(FluentIterable.from(sinks).transform(new Function<Filter, Callable<Void>>() {

						@Override
						public Callable<Void> apply(final Filter sink) {
							return new Callable<Void>() {

								@Override
								public Void call() throws Exception {
									try {
										logger.finest(String.format("Sending %d bytes to %s.", dataPacket.buffer().length, sink.name()));
										sink.process(dataPacket);
										logger.finest(String.format("Sent %d bytes to %s.", dataPacket.buffer().length, sink.name()));
									} catch (IOException ioe1) {
										throw new IOException(String.format("I/O error while writing to %s", sink.name()), ioe1);
									}
									return null;
								}
							};
						}
					}).toList());
					/* check all threads for exceptions. */
					for (Future<Void> future : futures) {
						future.get();
					}
					counter += dataPacket.buffer().length;
				} catch (IOException e) {
					/* TODO */
					e.printStackTrace();
					break;
				} catch (InterruptedException e) {
					/* TODO */
					e.printStackTrace();
					break;
				} catch (ExecutionException e) {
					/* TODO */
					e.printStackTrace();
					break;
				}
			}
		}

	}

	/**
	 * Container for input and output counters.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class TrafficCounter {

		/** The number of input bytes. */
		private final long input;

		/** The number of output bytes. */
		private final long output;

		/**
		 * Creates a new traffic counter.
		 *
		 * @param input
		 * 		The number of input bytes (may be {@code -1} to signify non-available
		 * 		input)
		 * @param output
		 * 		The number of output bytes (may be {@code -1} to signify non-available
		 * 		output)
		 */
		public TrafficCounter(long input, long output) {
			this.input = input;
			this.output = output;
		}

		//
		// ACCESSORS
		//

		/**
		 * Returns the number of input bytes.
		 *
		 * @return The number of input bytes, or {@link Optional#absent()} if the
		 *         filter did not receive input
		 */
		public Optional<Long> input() {
			return (input == -1) ? Optional.<Long>absent() : Optional.of(input);
		}

		/**
		 * Returns the number of output bytes.
		 *
		 * @return The number of output bytes, or {@link Optional#absent()} if the
		 *         filter did not send output
		 */
		public Optional<Long> output() {
			return (output == -1) ? Optional.<Long>absent() : Optional.of(output);
		}

	}

}
