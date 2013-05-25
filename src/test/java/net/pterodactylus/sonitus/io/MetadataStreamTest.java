/*
 * Sonitus - MetadataStreamTest.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import com.google.common.collect.FluentIterable;
import com.google.common.io.ByteStreams;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test for {@link MetadataStream}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MetadataStreamTest {

	/**
	 * Returns test data for {@link #testMetadataStream(int, int, String[])}.
	 *
	 * @return Test data for {@link #testMetadataStream(int, int, String[])}
	 */
	@DataProvider(name = "testData")
	public Object[][] getStreamTestParameters() {
		return new Object[][] {
									  { 5, 18, new String[] { "Test 1", "Test 2", "Test 3" } },
									  { 1024, 10240, new String[] { "Test 1", "Test 2" } },
									  { 1024, 10240, new String[] { "Test 1", "", "", "", "Test 2" } },
									  { 8192, 10240, new String[] { "Test 1" } },
									  { 8192, 262144, new String[] { "Test 1", "Test 2" } }
		};
	}

	/**
	 * Returns test data for {@link #testMetadataEncoding(String, String)}.
	 *
	 * @return Test data for {@link #testMetadataEncoding(String, String)}
	 */
	@DataProvider(name = "encodingTestData")
	public Object[][] getEncodingTestParameters() {
		return new Object[][] {
									  { "Metadata mit Ümläute!", "UTF-8" },
									  { "Metadata mit Ümläute!", "ISO8859-1" }
		};
	}

	/**
	 * Tests that the {@link MetadataStream} can successfully separate the payload
	 * from the metadata, and that the stream returns the expected bytes.
	 *
	 * @param metadataInterval
	 * 		The interval of the metadata
	 * @param length
	 * 		The length of the stream to test
	 * @param metadatas
	 * 		The metadata strings to write (empty strings allowed to signify “no
	 * 		metadata change”)
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	@Test(dataProvider = "testData")
	public void testMetadataStream(int metadataInterval, int length, String[] metadatas) throws IOException {
		byte[] randomData = generateData(length);
		InputStream testInputStream = generateInputStream(metadataInterval, randomData, "UTF-8", metadatas);
		MetadataStream metadataStream = new MetadataStream(testInputStream, metadataInterval);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		ByteStreams.copy(metadataStream, outputStream);
		assertThat(outputStream.toByteArray().length, is(length));
		assertThat(outputStream.toByteArray(), is(randomData));
	}

	/**
	 * Tests that the metadata is decoded correcty.
	 *
	 * @param metadata
	 * 		The string to encode
	 * @param charset
	 * 		The charset in which to encode the string
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	@Test(dataProvider = "encodingTestData")
	public void testMetadataEncoding(String metadata, String charset) throws IOException {
		InputStream testInputStream = generateInputStream(8192, 10240, charset, metadata);
		MetadataStream metadataStream = new MetadataStream(testInputStream, 8192);

		ByteStreams.copy(metadataStream, new ByteArrayOutputStream());
		assertThat(metadataStream.getContentMetadata().isPresent(), is(true));
		assertThat(metadataStream.getContentMetadata().get().title(), is(metadata));
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Generates a random amount of data.
	 *
	 * @param length
	 * 		The length of the data
	 * @return The generated random data
	 */
	private static byte[] generateData(int length) {
		Random random = new Random();
		byte[] buffer = new byte[length];
		random.nextBytes(buffer);
		return buffer;
	}

	/**
	 * Generates an input stream of the given length that has the given metadata
	 * strings (cycled) embedded in the given intervals.
	 *
	 * @param metadataInterval
	 * 		The interval of the embedded metadata
	 * @param length
	 * 		The length of the stream to generate
	 * @param charset
	 * 		The charset with which to encode the metadata strings
	 * @param metadatas
	 * 		The metadata strings which will be cycled
	 * @return The generated input stream
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	private static InputStream generateInputStream(int metadataInterval, int length, String charset, String... metadatas) throws IOException {
		return generateInputStream(metadataInterval, generateData(length), charset, metadatas);
	}

	/**
	 * Generates an input stream of the given buffer that has the given metadata
	 * strings (cycled) embedded in the given intervals.
	 *
	 * @param metadataInterval
	 * 		The interval of the embedded metadata
	 * @param buffer
	 * 		The data to embed the metadata into
	 * @param charset
	 * 		The charset with which to encode the metadata strings
	 * @param metadatas
	 * 		The metadata strings which will be cycled
	 * @return The generated input stream
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	private static InputStream generateInputStream(int metadataInterval, byte[] buffer, String charset, String... metadatas) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Iterator<String> metadataIterator = FluentIterable.from(Arrays.asList(metadatas)).cycle().iterator();
		int bufferPosition = 0;
		int remaining = buffer.length;
		while (remaining > 0) {
			int bytesToWrite = Math.min(remaining, metadataInterval);
			outputStream.write(buffer, bufferPosition, bytesToWrite);
			remaining -= bytesToWrite;
			bufferPosition += bytesToWrite;
			if (remaining > 0) {
				String nextMetadata = "StreamTitle='" + metadataIterator.next() + "';";
				byte[] metadata = nextMetadata.getBytes(charset);
				outputStream.write((metadata.length + 15) / 16);
				outputStream.write(metadata);
				outputStream.write(new byte[(16 - metadata.length % 16) % 16]);
			}
		}
		return new ByteArrayInputStream(outputStream.toByteArray());
	}

}
