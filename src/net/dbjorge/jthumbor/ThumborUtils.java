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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Utility functions for things like encryption and encoding. These are generally tailored to
 * Thumbor requirements and should not be used as general purpose functions.
 */
public class ThumborUtils {
	/**
	 * Encrypts the given plaintext with the given key according to AES-128 in ECB mode.
	 *
	 * This function performs NO padding on either of the key or plaintext. It REQUIRES that both
	 * key and plaintext be non-null, non-empty, and have sizes which are multiples of 16.
	 */
	public static byte[] aesEncrypt(String key, String plaintext) {
		try {
			SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
			mAesCipher.init(Cipher.ENCRYPT_MODE, keySpec);
			return mAesCipher.doFinal(plaintext.getBytes());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	private static final Cipher mAesCipher;
	static {
		try {
			mAesCipher = Cipher.getInstance("AES/ECB/NoPadding");
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encodes the given input byte array into URL-safe base64.
	 *
	 * It emulates Python's urlsafe_b64encode function.
	 *
	 * Input should be non-null and of length greater than 2.
	 */
	public static String urlSafeBase64Encode(byte[] input) {
		return mEncoder.encodeToString(input).trim();
	}
	private static final Base64 mEncoder = new Base64(true);


	/**
	 * Sanitizes a given URL to NOT include a protocol prefix. Raises an error if this function
	 * is given a URL with a protocol which is not the given one.
	 *
	 * Inputs may not be null.
	 */
	public static String sanitizeUrlWithoutProtocol(String url, String protocol) throws IllegalArgumentException {
		int i = url.indexOf("://");
		if(i >= 0 && url.indexOf('/')-1 == i) {
			if(!url.substring(0, i).equals(protocol)) {
				throw new IllegalArgumentException("Invalid URL: Uses protocol "+url.substring(0,i)+", but "+protocol+" required");
			}
			return url.substring(i+3);
		}
		return url;
	}

	/**
	 * Sanitizes a given URL to include a protocol prefix. If one is not found, it prepends the
	 * given default protocol.
	 *
	 * Note that unlike {@see #sanitizeUrlWithoutProtocol(String, String)}, this method does
	 * NOT raise any error if the given URL starts with a non-default protocol. It also does not
	 * override such a non-default protocol.
	 *
	 * Inputs may not be null.
	 */
	public static String sanitizeUrlWithProtocol(String url, String defaultProtocol) {
		if(url.contains("://") && url.indexOf('/')-1 == url.indexOf("://")) {
			return url;
		} else {
			return defaultProtocol + "://"+url;
		}
	}


	/**
	 * MD5 hashes the given input string and returns the hex digest in String form.
	 *
	 * Input may not be null or empty.
	 */
	public static String md5String(String input) {
		String result = "";
		MessageDigest algorithm;
		try {
			algorithm = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
        algorithm.reset();
        algorithm.update(input.getBytes());
        byte[] md5 = algorithm.digest();
        String tmp = "";
        for (int i = 0; i < md5.length; i++) {
            tmp = (Integer.toHexString(0xFF & md5[i]));
            if (tmp.length() == 1) {
                result += "0" + tmp;
            } else {
                result += tmp;
            }
        }
        return result;
	}
}
