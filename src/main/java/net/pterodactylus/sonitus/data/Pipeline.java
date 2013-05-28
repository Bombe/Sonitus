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
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * A pipeline is responsible for streaming audio data from a {@link Source} to
 * an arbitrary number of connected {@link Filter}s and {@link Sink}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Pipeline implements Iterable<ControlledComponent> {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(Pipeline.class.getName());

	/** The source of the audio stream. */
	private final Source source;

	/** The sinks for each source. */
	private final Multimap<Source, Sink> sinks;

	/** All started connections. */
	private final List<Connection> connections = Lists.newArrayList();

	/**
	 * Creates a new pipeline.
	 *
	 * @param source
	 * 		The source of the audio stream
	 * @param sinks
	 * 		The sinks for each source
	 */
	private Pipeline(Source source, Multimap<Source, Sink> sinks) {
		this.source = Preconditions.checkNotNull(source, "source must not be null");
		this.sinks = Preconditions.checkNotNull(sinks, "sinks must not be null");
	}

	//
	// ACCESSORS
	//

	/**
	 * Expose this pipeline’s source.
	 *
	 * @return This pipeline’s source
	 */
	public Source source() {
		return source;
	}

	/**
	 * Returns all {@link Sink}s (or {@link Filter}s, really) that are connected to
	 * the given source.
	 *
	 * @param source
	 * 		The source to get the sinks for
	 * @return The sinks connected to the given source, or an empty list if the
	 *         source does not exist in this pipeline
	 */
	public Collection<Sink> sinks(Source source) {
		return sinks.get(source);
	}

	/**
	 * Returns the traffic counters of the given controlled component.
	 *
	 * @param controlledComponent
	 * 		The controlled component to get the traffic counters for
	 * @return The traffic counters for the given controlled component
	 */
	public TrafficCounter trafficCounter(ControlledComponent controlledComponent) {
		long input = -1;
		long output = -1;
		for (Connection connection : connections) {
			/* the connection where the source matches knows the output. */
			if (connection.source.equals(controlledComponent)) {
				output = connection.counter();
			} else if (connection.sinks.contains(controlledComponent)) {
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
	 * 		if any of the sinks can not be opened
	 * @throws IllegalStateException
	 * 		if the pipeline is already running
	 */
	public void start() throws IOException, IllegalStateException {
		if (!connections.isEmpty()) {
			throw new IllegalStateException("Pipeline is already running!");
		}
		List<Source> sources = Lists.newArrayList();
		sources.add(source);
		/* collect all source->sink pairs. */
		while (!sources.isEmpty()) {
			Source source = sources.remove(0);
			Collection<Sink> sinks = this.sinks.get(source);
			connections.add(new Connection(source, sinks));
			for (Sink sink : sinks) {
				sink.open(source.metadata());
				if (sink instanceof Filter) {
					sources.add((Source) sink);
				}
			}
		}
		for (Connection connection : connections) {
			logger.info(String.format("Starting Connection from %s to %s.", connection.source, connection.sinks));
			new Thread(connection).start();
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
	public Iterator<ControlledComponent> iterator() {
		return components().iterator();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Returns all components of this pipeline, listed breadth-first, starting with
	 * the source.
	 *
	 * @return All components of this pipeline
	 */
	public List<ControlledComponent> components() {
		ImmutableList.Builder<ControlledComponent> components = ImmutableList.builder();
		List<ControlledComponent> currentComponents = Lists.newArrayList();
		components.add(source);
		currentComponents.add(source);
		while (!currentComponents.isEmpty()) {
			Collection<Sink> sinks = this.sinks((Source) currentComponents.remove(0));
			for (Sink sink : sinks) {
				components.add(sink);
				if (sink instanceof Source) {
					currentComponents.add(sink);
				}
			}
		}
		return components.build();
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
	public static Builder builder(Source source) {
		return new Builder(source);
	}

	/**
	 * A builder for a {@link Pipeline}.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class Builder {

		/** The source of the pipeline. */
		private final Source source;

		/** The sinks to which each source streams. */
		private Multimap<Source, Sink> nextSinks = ArrayListMultimap.create();

		/** The last added source. */
		private Source lastSource;

		/**
		 * Creates a new builder.
		 *
		 * @param source
		 * 		The source that starts the pipeline
		 */
		private Builder(Source source) {
			this.source = source;
			lastSource = source;
		}

		/**
		 * Adds a {@link Sink} (or {@link Filter} as a recipient for the last added
		 * {@link Source}.
		 *
		 * @param sink
		 * 		The sink to add
		 * @return This builder
		 * @throws IllegalStateException
		 * 		if the last added {@link Sink} was not also a {@link Source}
		 */
		public Builder to(Sink sink) {
			Preconditions.checkState(lastSource != null, "last added Sink was not a Source");
			nextSinks.put(lastSource, sink);
			lastSource = (sink instanceof Filter) ? (Source) sink : null;
			return this;
		}

		/**
		 * Locates the given source and sets it as the last added node so that the
		 * next invocation of {@link #to(Sink)} can “fork” the pipeline.
		 *
		 * @param source
		 * 		The source to locate
		 * @return This builder
		 * @throws IllegalStateException
		 * 		if the given source was not previously added as a sink
		 */
		public Builder find(Source source) {
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
	 * A connection is responsible for streaming audio from one {@link Source} to
	 * an arbitrary number of {@link Sink}s it is connected to. A connection is
	 * started by creating a {@link Thread} wrapping it and starting said thread.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public class Connection implements Runnable {

		/** The source. */
		private final Source source;

		/** The sinks. */
		private final Collection<Sink> sinks;

		/** Whether the feeder was stopped. */
		private final AtomicBoolean stopped = new AtomicBoolean(false);

		/** The executor service. */
		private final ExecutorService executorService;

		/** The number of copied bytes. */
		private long counter;

		/**
		 * Creates a new connection.
		 *
		 * @param source
		 * 		The source of the stream
		 * @param sinks
		 * 		The sinks to which to stream
		 */
		public Connection(Source source, Collection<Sink> sinks) {
			this.source = source;
			this.sinks = sinks;
			if (sinks.size() == 1) {
				executorService = MoreExecutors.sameThreadExecutor();
			} else {
				executorService = Executors.newCachedThreadPool();
			}
		}

		//
		// ACCESSORS
		//

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
			Metadata firstMetadata = null;
			while (!stopped.get()) {
				try {
					final byte[] buffer;
					try {
						logger.finest(String.format("Getting %d bytes from %s...", 4096, source));
						buffer = source.get(4096);
						logger.finest(String.format("Got %d bytes from %s.", buffer.length, source));
					} catch (IOException ioe1) {
						throw new IOException(String.format("I/O error while reading from %s.", source), ioe1);
					}
					List<Future<Void>> futures = executorService.invokeAll(FluentIterable.from(sinks).transform(new Function<Sink, Callable<Void>>() {

						@Override
						public Callable<Void> apply(final Sink sink) {
							return new Callable<Void>() {

								@Override
								public Void call() throws Exception {
									try {
										logger.finest(String.format("Sending %d bytes to %s.", buffer.length, sink));
										sink.process(buffer);
										logger.finest(String.format("Sent %d bytes to %s.", buffer.length, sink));
									} catch (IOException ioe1) {
										throw new IOException(String.format("I/O error while writing to %s", sink), ioe1);
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
					counter += buffer.length;
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
		 *         component can not receive input
		 */
		public Optional<Long> input() {
			return (input == -1) ? Optional.<Long>absent() : Optional.of(input);
		}

		/**
		 * Returns the number of output bytes.
		 *
		 * @return The number of output bytes, or {@link Optional#absent()} if the
		 *         component can not send output
		 */
		public Optional<Long> output() {
			return (output == -1) ? Optional.<Long>absent() : Optional.of(output);
		}

	}

}
