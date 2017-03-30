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
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertNotNull;

/**
 * Created by dustin on 28/03/2017.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class SupportEncryptionRSATest {
	private Context mContext;

	@Before
	public void setup() {
		mContext = InstrumentationRegistry.getContext();
	}

	@Test
	public void testInit() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
		SupportEncryptionRSA.newInstance(mContext);
	}

	@Test
	public void testRSAEncrypt() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, UnrecoverableEntryException, InvalidKeyException, SignatureException {
		byte[] bytes = SupportEncryptionRSA.newInstance(mContext).rsaSign("test".getBytes());
	}

	@Test
	public void testPublicKey() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, UnrecoverableEntryException, InvalidKeyException, SignatureException {
		String publicKey = SupportEncryptionRSA.newInstance(mContext).getPublicKey();
		assertNotNull(publicKey);
	}
}
