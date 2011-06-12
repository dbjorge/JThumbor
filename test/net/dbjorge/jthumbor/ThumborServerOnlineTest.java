/*
 *  Copyright 2011 Dan Bjorge
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dbjorge.jthumbor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.dbjorge.jthumbor.ThumborServer.Options.HorizontalAlignment;
import net.dbjorge.jthumbor.ThumborServer.Options.VerticalAlignment;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Test;

/**
 * Contains test cases which actually call out to thumbor itself for testing
 *
 * All of these tests assume working command line access to a thumbor instance, and will not be run
 * if no such instance is found.
 *
 * These tests are derived very directly from those suggested at the Thumbor Libraries wiki.
 */
public class ThumborServerOnlineTest {
	@Before
	public void assumeThumbor() {
		// bash command to call thumbor's decrypt method
		String command = "python -c 'import thumbor.crypto; print (2+3)'";

		// execute it in the shell and read output to an array
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec(command);
		} catch (IOException e1) {
			assumeNoException(e1);
		}

		BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line = null;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			while((line = stdout.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e1) {
			assumeNoException(e1);
		}

		try {
			int errorLevel = proc.waitFor();
			assumeTrue(errorLevel != 0);
		} catch (InterruptedException e) {
			assumeNoException(e);
		}

		// assume that it was valid. This throws out results on systems without thumbor or python

		assumeTrue(lines.size() == 1);
		assumeTrue(lines.get(0).trim().equals("5"));
	}

	private JSONObject decryptInThumbor(String key, String input) throws Exception {
		// bash command to call thumbor's decrypt method
		String command = "python -c 'from thumbor.crypto import Crypto; cr = Crypto(\"" + key + "\"); print cr.decrypt(\"" + input + "\")'";

		// execute it in the shell and read output to an array
		Process proc = Runtime.getRuntime().exec(command);
		BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		JSONTokener tokener = new JSONTokener(stdout);
		int errorLevel = proc.waitFor();

		assertTrue(errorLevel != 0);
		return new JSONObject(tokener);
	}

	@Test
	public void testWithResize() throws Exception {
		String key = "my-security-key";
		ThumborServer s = new ThumborServer("irrelevant", key);
		String token = s.getSecureToken("my.server.com/some/path/to/image.jpg", new ThumborServer.Options()
			.resize(300, 200)
		);
		JSONObject r = decryptInThumbor(key, token);

		assertEquals(false, r.getBoolean("horizontal_flip"));
		assertEquals(false, r.getBoolean("vertical_flip"));
		assertEquals(false, r.getBoolean("smart"));
		assertEquals(false, r.getBoolean("meta"));
		assertEquals(false, r.getBoolean("fit_in"));
		JSONObject crop = r.getJSONObject("crop");
		assertEquals(0, crop.getInt("left"));
		assertEquals(0, crop.getInt("top"));
		assertEquals(0, crop.getInt("right"));
		assertEquals(0, crop.getInt("bottom"));
		assertEquals("middle", r.getString("valign"));
		assertEquals("center", r.getString("halign"));
		assertEquals(300, r.getInt("width"));
		assertEquals(200, r.getInt("height"));
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", r.getString("image_hash"));
	}

	@Test
	public void testWithMeta() throws Exception {
		String key = "my-security-key";
		ThumborServer s = new ThumborServer("irrelevant", key);
		String token = s.getSecureToken("my.server.com/some/path/to/image.jpg", new ThumborServer.Options()
			.meta()
		);
		JSONObject r = decryptInThumbor(key, token);

		assertEquals(true, r.getBoolean("meta"));
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", r.getString("image_hash"));
	}

	@Test
	public void testWithSmart() throws Exception {
		String key = "my-security-key";
		ThumborServer s = new ThumborServer("irrelevant", key);
		String token = s.getSecureToken("my.server.com/some/path/to/image.jpg", new ThumborServer.Options()
			.smart()
		);
		JSONObject r = decryptInThumbor(key, token);

		assertEquals(true, r.getBoolean("smart"));
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", r.getString("image_hash"));
	}

	@Test
	public void testWithFitIn() throws Exception {
		String key = "my-security-key";
		ThumborServer s = new ThumborServer("irrelevant", key);
		String token = s.getSecureToken("my.server.com/some/path/to/image.jpg", new ThumborServer.Options()
			.fitIn()
		);
		JSONObject r = decryptInThumbor(key, token);

		assertEquals(true, r.getBoolean("fit_in"));
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", r.getString("image_hash"));
	}

	@Test
	public void testWithFlip() throws Exception {
		String key = "my-security-key";
		ThumborServer s = new ThumborServer("irrelevant", key);
		String token = s.getSecureToken("my.server.com/some/path/to/image.jpg", new ThumborServer.Options()
			.flipHorizontal()
		);
		JSONObject r = decryptInThumbor(key, token);

		assertEquals(true, r.getBoolean("flip_horizontally"));
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", r.getString("image_hash"));
	}

	@Test
	public void testWithFlop() throws Exception {
		String key = "my-security-key";
		ThumborServer s = new ThumborServer("irrelevant", key);
		String token = s.getSecureToken("my.server.com/some/path/to/image.jpg", new ThumborServer.Options()
			.flipVertical()
		);
		JSONObject r = decryptInThumbor(key, token);

		assertEquals(true, r.getBoolean("flip_vertical"));
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", r.getString("image_hash"));
	}

	@Test
	public void testWithHAlign() throws Exception {
		String key = "my-security-key";
		ThumborServer s = new ThumborServer("irrelevant", key);
		String token = s.getSecureToken("my.server.com/some/path/to/image.jpg", new ThumborServer.Options()
			.horizontalAlign(HorizontalAlignment.RIGHT)
		);
		JSONObject r = decryptInThumbor(key, token);

		assertEquals("right", r.getString("halign"));
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", r.getString("image_hash"));
	}

	@Test
	public void testWithVAlign() throws Exception {
		String key = "my-security-key";
		ThumborServer s = new ThumborServer("irrelevant", key);
		String token = s.getSecureToken("my.server.com/some/path/to/image.jpg", new ThumborServer.Options()
			.verticalAlign(VerticalAlignment.TOP)
		);
		JSONObject r = decryptInThumbor(key, token);

		assertEquals("top", r.getString("valign"));
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", r.getString("image_hash"));
	}

	@Test
	public void testWithCrop() throws Exception {
		String key = "my-security-key";
		ThumborServer s = new ThumborServer("irrelevant", key);
		String token = s.getSecureToken("my.server.com/some/path/to/image.jpg", new ThumborServer.Options()
			.crop(10, 20, 30, 40)
		);
		JSONObject r = decryptInThumbor(key, token);
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", r.getString("image_hash"));

		JSONObject crop = r.getJSONObject("crop");
		assertEquals(10, crop.getString("left"));
		assertEquals(20, crop.getString("top"));
		assertEquals(30, crop.getString("right"));
		assertEquals(40, crop.getString("bottom"));
	}

}
