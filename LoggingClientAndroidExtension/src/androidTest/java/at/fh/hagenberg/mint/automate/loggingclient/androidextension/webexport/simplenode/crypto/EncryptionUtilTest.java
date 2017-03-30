/*
 *     Copyright (C) 2017 Research Group Mobile Interactive Systems
 *     Email: mint@fh-hagenberg.at, Website: http://mint.fh-hagenberg.at
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport.simplenode.crypto;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by dustin on 28/03/2017.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class EncryptionUtilTest {
	private static final String STRING = "test";

	private static final String PUBLIC_KEY_BEGIN = "-----BEGIN PUBLIC KEY-----\n";
	private static final String PUBLIC_KEY_END = "-----END PUBLIC KEY-----\n";

	private Context mContext;

	@Before
	public void setup() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, NoSuchPaddingException, KeyStoreException {
		mContext = InstrumentationRegistry.getContext();
		EncryptionUtil.init(mContext);
	}

	@Test
	public void testSignature() {
		String signed = EncryptionUtil.signString(mContext, STRING);
		assertNotNull(signed);
	}

	@Test
	public void testPublicKey() throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
		String key = EncryptionUtil.getPublicKey(mContext);
		assertNotNull(key);
		assertTrue(key.startsWith(PUBLIC_KEY_BEGIN));
		assertTrue(key.endsWith(PUBLIC_KEY_END));
	}
}
