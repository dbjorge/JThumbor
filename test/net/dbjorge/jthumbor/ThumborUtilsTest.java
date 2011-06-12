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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ThumborUtilsTest {
	private static final String t1key = "my-security-keym";
	private static final String t1plaintext = "300x200/84996242f65a4d864aceb125e1c4c5ba{{{{{{{{";
	private static final byte[] t1expectedEncrypted = {
		-105, -115, -91, -25, -123, 106, 105, 95,
		-55, -8, 71, 1, -26, -85, -115, 48,
		-2, -126, -98, -61, 125, 5, 126, -31,
		-74, -72, 126, 65, -77, -18, 14, -3,
		2, -19, -89, 84, 93, 126, -53, 78,
		-114, -125, 30, 111, -16, -96, -101, 89
	};
	private static final String t1expectedEncoded = "l42l54VqaV_J-EcB5quNMP6CnsN9BX7htrh-QbPuDv0C7adUXX7LTo6DHm_woJtZ";

	@Test
	public void testAesEncrypt() {
		assertArrayEquals(t1expectedEncrypted, ThumborUtils.aesEncrypt(t1key, t1plaintext));
	}

	@Test
	public void testUrlSafeBase64Encode() {
		assertEquals(t1expectedEncoded, ThumborUtils.urlSafeBase64Encode(t1expectedEncrypted));
	}

	@Test
	public void testSanitizeUrlWithoutProtocol() {
		assertEquals("test.com", ThumborUtils.sanitizeUrlWithoutProtocol("test.com", "http"));
		assertEquals("test.com", ThumborUtils.sanitizeUrlWithoutProtocol("http://test.com", "http"));
		try {
			ThumborUtils.sanitizeUrlWithoutProtocol("https://test.com", "http");
			fail();
		} catch(IllegalArgumentException e) {
			// good
		}

		assertEquals("test.com", ThumborUtils.sanitizeUrlWithoutProtocol("test.com", "ftp"));
		assertEquals("test.com", ThumborUtils.sanitizeUrlWithoutProtocol("ftp://test.com", "ftp"));
		try {
			ThumborUtils.sanitizeUrlWithoutProtocol("http://test.com", "ftp");
			fail();
		} catch(IllegalArgumentException e) {
			// good
		}
	}

	@Test
	public void testSanitizeUrlWithProtocol() {
		assertEquals("http://test.com", ThumborUtils.sanitizeUrlWithProtocol("test.com", "http"));
		assertEquals("http://test.com", ThumborUtils.sanitizeUrlWithProtocol("http://test.com", "http"));
		assertEquals("https://test.com", ThumborUtils.sanitizeUrlWithProtocol("https://test.com", "http"));

		assertEquals("ftp://test.com", ThumborUtils.sanitizeUrlWithProtocol("test.com", "ftp"));
		assertEquals("ftp://test.com", ThumborUtils.sanitizeUrlWithProtocol("ftp://test.com", "ftp"));
		assertEquals("http://test.com", ThumborUtils.sanitizeUrlWithProtocol("http://test.com", "ftp"));
	}

	@Test
	public void testMd5String() {
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", ThumborUtils.md5String("my.server.com/some/path/to/image.jpg"));
	}

}
