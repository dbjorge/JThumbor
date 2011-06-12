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
import net.dbjorge.jthumbor.ThumborServer.Options;
import net.dbjorge.jthumbor.ThumborServer.Options.HorizontalAlignment;
import net.dbjorge.jthumbor.ThumborServer.Options.VerticalAlignment;

import org.junit.Before;
import org.junit.Test;

public class ThumborServerOfflineTest {
	private ThumborServer s;

	@Before
	public void setupExampleServer() {
		s = new ThumborServer("example.com", "blank");
	}

	@Test
	public void testCropOptions() {
		assertEquals("5x3:2x1/", s.getOptionsPath(new Options().crop(5,3,2,1)));
		assertEquals("", s.getOptionsPath(new Options().crop(0,0,0,0)));
		assertEquals("0x0:0x1/", s.getOptionsPath(new Options().crop(0,0,0,1)));
		assertEquals("1x0:0x0/", s.getOptionsPath(new Options().crop(1,0,0,0)));
	}

	@Test
	public void testResizeOptions() {
		assertEquals("", s.getOptionsPath(new Options().resize(0, 0)));

		assertEquals("27x953/", s.getOptionsPath(new Options().resize(27, 953)));
		assertEquals("-27x953/", s.getOptionsPath(new Options().resize(27, 953).flipHorizontal()));
		assertEquals("27x-953/", s.getOptionsPath(new Options().resize(27, 953).flipVertical()));
		assertEquals("-27x-953/", s.getOptionsPath(new Options().resize(27, 953).flipVertical().flipHorizontal()));

		assertEquals("0x56/", s.getOptionsPath(new Options().resize(0, 56)));
		assertEquals("43x0/", s.getOptionsPath(new Options().resize(43, 0)));

		assertEquals("-0x0/", s.getOptionsPath(new Options().flipHorizontal()));
		assertEquals("0x-0/", s.getOptionsPath(new Options().flipVertical()));
		assertEquals("-0x-0/", s.getOptionsPath(new Options().flipVertical().flipHorizontal()));
	}

	@Test
	public void testAlignmentOptions() {
		assertEquals("left/", s.getOptionsPath(new Options().horizontalAlign(HorizontalAlignment.LEFT)));
		assertEquals("", s.getOptionsPath(new Options().horizontalAlign(HorizontalAlignment.CENTER)));
		assertEquals("right/", s.getOptionsPath(new Options().horizontalAlign(HorizontalAlignment.RIGHT)));

		assertEquals("top/", s.getOptionsPath(new Options().verticalAlign(VerticalAlignment.TOP)));
		assertEquals("", s.getOptionsPath(new Options().verticalAlign(VerticalAlignment.MIDDLE)));
		assertEquals("bottom/", s.getOptionsPath(new Options().verticalAlign(VerticalAlignment.BOTTOM)));

		assertEquals("left/bottom/", s.getOptionsPath(new Options().horizontalAlign(HorizontalAlignment.LEFT).verticalAlign(VerticalAlignment.BOTTOM)));
		assertEquals("right/top/", s.getOptionsPath(new Options().verticalAlign(VerticalAlignment.TOP).horizontalAlign(HorizontalAlignment.RIGHT)));

		assertEquals("", s.getOptionsPath(new Options().verticalAlign(VerticalAlignment.MIDDLE).horizontalAlign(HorizontalAlignment.CENTER)));
		assertEquals("top/", s.getOptionsPath(new Options().verticalAlign(VerticalAlignment.TOP).horizontalAlign(HorizontalAlignment.CENTER)));
		assertEquals("left/", s.getOptionsPath(new Options().verticalAlign(VerticalAlignment.MIDDLE).horizontalAlign(HorizontalAlignment.LEFT)));
	}

	@Test
	public void testCombinedOptions() {
		assertEquals("", s.getOptionsPath(new Options()));

		assertEquals("meta/", s.getOptionsPath(new Options().meta()));

		assertEquals("meta/5x3:2x1/", s.getOptionsPath(new Options().meta().crop(5,3,2,1)));
		assertEquals("meta/5x3:2x1/", s.getOptionsPath(new Options().crop(5,3,2,1).meta()));

		assertEquals("fit-in/", s.getOptionsPath(new Options().fitIn()));
		assertEquals("5x3:2x1/fit-in/", s.getOptionsPath(new Options().fitIn().crop(5,3,2,1)));
		assertEquals("fit-in/2x2/", s.getOptionsPath(new Options().fitIn().resize(2,2)));

		assertEquals("20x30:40x50/20x20/", s.getOptionsPath(new Options().crop(20,30,40,50).resize(20, 20)));
		assertEquals("20x30:40x50/fit-in/20x20/", s.getOptionsPath(new Options().crop(20,30,40,50).resize(20, 20).fitIn()));
		assertEquals("meta/20x30:40x50/20x20/", s.getOptionsPath(new Options().crop(20,30,40,50).resize(20, 20).meta()));
		assertEquals("meta/20x20/", s.getOptionsPath(new Options().resize(20, 20).meta()));

		assertEquals("meta/20x30:40x50/20x20/right/top/", s.getOptionsPath(new Options()
			.crop(20,30,40,50)
			.verticalAlign(VerticalAlignment.TOP)
			.resize(20, 20)
			.horizontalAlign(HorizontalAlignment.RIGHT)
			.meta()));

		assertEquals("meta/20x30:40x50/20x20/right/top/smart/", s.getOptionsPath(new Options()
			.crop(20,30,40,50)
			.verticalAlign(VerticalAlignment.TOP)
			.resize(20, 20)
			.smart()
			.horizontalAlign(HorizontalAlignment.RIGHT)
			.meta()));

		assertEquals("meta/20x30:40x50/fit-in/20x20/right/top/smart/", s.getOptionsPath(new Options()
			.crop(20,30,40,50)
			.verticalAlign(VerticalAlignment.TOP)
			.resize(20, 20)
			.smart()
			.horizontalAlign(HorizontalAlignment.RIGHT)
			.fitIn()
			.meta()));
	}

	@Test
	public void testOptionsUrl() {
		assertEquals("84996242f65a4d864aceb125e1c4c5ba", s.getOptionsUrl("my.server.com/some/path/to/image.jpg", new Options()));

		assertEquals("meta/20x30:40x50/fit-in/20x20/right/top/smart/84996242f65a4d864aceb125e1c4c5ba", s.getOptionsUrl("my.server.com/some/path/to/image.jpg", new Options()
			.crop(20,30,40,50)
			.verticalAlign(VerticalAlignment.TOP)
			.resize(20, 20)
			.smart()
			.horizontalAlign(HorizontalAlignment.RIGHT)
			.fitIn()
			.meta()));
	}

	@Test
	public void testGetSecureUrlPath() {
		s = new ThumborServer("nothing", "my-security-key");
		assertEquals(
				"/l42l54VqaV_J-EcB5quNMP6CnsN9BX7htrh-QbPuDv0C7adUXX7LTo6DHm_woJtZ/my.server.com/some/path/to/image.jpg",
				s.getSecureUrlPath("my.server.com/some/path/to/image.jpg", new Options().resize(300, 200)));
	}

	@Test
	public void testGetSecureUrl() {
		s = new ThumborServer("http://dbjorge.net", "my-security-key");
		assertEquals(
				"http://dbjorge.net/l42l54VqaV_J-EcB5quNMP6CnsN9BX7htrh-QbPuDv0C7adUXX7LTo6DHm_woJtZ/my.server.com/some/path/to/image.jpg",
				s.getSecureUrl("my.server.com/some/path/to/image.jpg", new Options().resize(300, 200)));

		s = new ThumborServer("dbjorge.net", "my-security-key");
		assertEquals(
				"http://dbjorge.net/l42l54VqaV_J-EcB5quNMP6CnsN9BX7htrh-QbPuDv0C7adUXX7LTo6DHm_woJtZ/my.server.com/some/path/to/image.jpg",
				s.getSecureUrl("my.server.com/some/path/to/image.jpg", new Options().resize(300, 200)));

		s = new ThumborServer("https://dbjorge.net", "my-security-key");
		assertEquals(
				"https://dbjorge.net/l42l54VqaV_J-EcB5quNMP6CnsN9BX7htrh-QbPuDv0C7adUXX7LTo6DHm_woJtZ/my.server.com/some/path/to/image.jpg",
				s.getSecureUrl("my.server.com/some/path/to/image.jpg", new Options().resize(300, 200)));
	}

	@Test
	public void testGetSecureToken() {
		s = new ThumborServer("http://dbjorge.net", "my-security-key");
		assertEquals(
				"l42l54VqaV_J-EcB5quNMP6CnsN9BX7htrh-QbPuDv0C7adUXX7LTo6DHm_woJtZ",
				s.getSecureToken("my.server.com/some/path/to/image.jpg", new Options().resize(300, 200)));
	}
}
