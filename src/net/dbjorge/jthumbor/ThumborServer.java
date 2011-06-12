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

import java.util.Arrays;

import net.dbjorge.jthumbor.ThumborServer.Options.HorizontalAlignment;
import net.dbjorge.jthumbor.ThumborServer.Options.VerticalAlignment;

public class ThumborServer {
	/**
	 * Simple storage structure defining the options that may be passed in a Thumbor request.
	 *
	 * Options are set with method chaining.
	 */
	public static class Options {
		public static enum HorizontalAlignment {
			LEFT,
			CENTER,
			RIGHT
		}
		public static enum VerticalAlignment {
			TOP,
			MIDDLE,
			BOTTOM
		}

		private boolean mMeta = false;
		private int mCropLeft = 0;
		private int mCropTop = 0;
		private int mCropRight = 0;
		private int mCropBottom = 0;
		private int mWidth = 0;
		private int mHeight = 0;
		private boolean mFlipHorizontal = false;
		private boolean mFlipVertical = false;
		private boolean mFitIn = false;
		private HorizontalAlignment mHorizontalAlignment = HorizontalAlignment.CENTER;
		private VerticalAlignment mVerticalAlignment = VerticalAlignment.MIDDLE;
		private boolean mSmart = false;

		public Options() {}
		public Options meta() { mMeta = true; return this; }
		public Options crop(int left, int top, int right, int bottom)  {
			mCropLeft=left; mCropTop=top; mCropRight=right; mCropBottom=bottom;
			return this;
		}
		public Options resize(int width, int height)  { mWidth = width; mHeight = height; return this; }
		public Options flipHorizontal() { mFlipHorizontal = true; return this; }
		public Options flipVertical() { mFlipVertical = true; return this; }
		public Options fitIn() { mFitIn = true; return this; }
		public Options horizontalAlign(HorizontalAlignment ha) { mHorizontalAlignment = ha; return this; }
		public Options verticalAlign(VerticalAlignment va) { mVerticalAlignment = va; return this; }
		public Options smart() { mSmart = true; return this; }
	}

	private String mServerUrl;
	private String mSecureKey;

	/**
	 * Both inputs must be non-null and non-empty
	 */
	public ThumborServer(String serverUrl, String secureKey) {
		if(serverUrl == null || serverUrl.isEmpty()) {
			throw new IllegalArgumentException("Server may not be null or empty");
		}

		mServerUrl = ThumborUtils.sanitizeUrlWithProtocol(serverUrl, "http");

		if(secureKey == null || secureKey.isEmpty()) {
			throw new IllegalArgumentException("Secure key may not be null or empty");
		}

		while(secureKey.length() < 16) {
			secureKey += secureKey;
		}
		mSecureKey = secureKey.substring(0, 16);
	}

	/** Just the options portion of the path */
	protected String getOptionsPath(Options opts) {
		StringBuilder p = new StringBuilder();

		if(opts.mMeta) { p.append("meta/"); }

		if(opts.mCropLeft > 0 || opts.mCropTop > 0 || opts.mCropRight > 0 || opts.mCropBottom > 0) {
			p.append(opts.mCropLeft);
			p.append('x');
			p.append(opts.mCropTop);
			p.append(':');
			p.append(opts.mCropRight);
			p.append('x');
			p.append(opts.mCropBottom);
			p.append('/');
		}

		if(opts.mFitIn) { p.append("fit-in/"); }

		if(opts.mWidth > 0 || opts.mHeight > 0 || opts.mFlipHorizontal || opts.mFlipVertical) {
			if(opts.mFlipHorizontal) { p.append('-'); }
			p.append(opts.mWidth);
			p.append('x');
			if(opts.mFlipVertical) { p.append('-'); }
			p.append(opts.mHeight);
			p.append('/');
		}

		if(opts.mHorizontalAlignment != HorizontalAlignment.CENTER) {
			p.append(opts.mHorizontalAlignment.toString().toLowerCase());
			p.append('/');
		}
		if(opts.mVerticalAlignment != VerticalAlignment.MIDDLE) {
			p.append(opts.mVerticalAlignment.toString().toLowerCase());
			p.append('/');
		}

		if(opts.mSmart) { p.append("smart/"); }

		return p.toString();
	}

	/** Options path plus hashed image URI */
	protected String getOptionsUrl(String imageUrl, Options opts) {
		return getOptionsPath(opts) + ThumborUtils.md5String(ThumborUtils.sanitizeUrlWithoutProtocol(imageUrl, "http"));
	}

	/** Returns just the encrypted and base64'd token part of a secure thumbor URL */
	public String getSecureToken(String imageUrl, Options options) {
		// Get the options URL
		String optionsUrl = getOptionsUrl(imageUrl, options);

		// Pad it until its length is a multiple of 16
		while(optionsUrl.length() % 16 != 0) optionsUrl += "{";

		System.out.println("input: "+optionsUrl);
		// Encrypt with AES using the stored secure key
		byte[] encrypted = ThumborUtils.aesEncrypt(mSecureKey, optionsUrl);
		System.out.println("output: "+Arrays.toString(encrypted));

		// encode it (emulating python's urlsafe_b64encode)
		String encoded = ThumborUtils.urlSafeBase64Encode(encrypted);
		System.out.println("encoded: "+encoded);

		return encoded;
	}

	/** Creates the encrypted, secure path for use with the Thumbor server */
	public String getSecureUrlPath(String imageUrl, Options options) {
		return "/" + getSecureToken(imageUrl, options) + "/" + ThumborUtils.sanitizeUrlWithoutProtocol(imageUrl, "http");
	}

	/**
	 * Creates the encrypted, secure path for use with the Thumbor server and attaches it to the
	 * base server URL for a complete URL to the thumbnail image.
	 */
	public String getSecureUrl(String imageUrl, Options options) {
		return mServerUrl + getSecureUrlPath(imageUrl, options);
	}
}
