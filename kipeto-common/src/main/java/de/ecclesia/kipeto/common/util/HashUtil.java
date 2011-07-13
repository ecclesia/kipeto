/*
 * #%L
 * Kipeto Common
 * %%
 * Copyright (C) 2010 - 2011 Ecclesia Versicherungsdienst GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.ecclesia.kipeto.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.omg.CORBA.SystemException;

public class HashUtil {

	public static String hashFile(File file, String hashAlgorithm) throws SystemException, IOException {
		return hashStream(new FileInputStream(file), hashAlgorithm);
	}

	public static String hashStream(InputStream stream, String hashAlgorithm) throws IOException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(hashAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		int readBytes = 0;
		byte[] buffer = new byte[8192];

		try {
			while ((readBytes = stream.read(buffer)) != -1) {
				digest.update(buffer, 0, readBytes);
			}
		} finally {
			Streams.ensureClosed(stream);
		}

		return convertHashBytesToString(digest.digest());
	}

	public static String convertHashBytesToString(byte[] hash) {
		return String.format("%0" + hash.length * 2 + "x", new BigInteger(1, hash));
	}

}
