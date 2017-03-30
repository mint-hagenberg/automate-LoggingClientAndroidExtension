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

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;

/**
 * Created by dustin on 24/03/2017.
 */
public abstract class SupportEncryption {
	protected static final String ANDROID_KEY_STORE = "AndroidKeyStore";

	protected KeyStore mKeystore;

	protected SupportEncryption(KeyStore keystore) {
		mKeystore = keystore;
	}

	public abstract String getPublicKey() throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, IOException;

	public abstract String sign(Context context, byte[] sign) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidKeyException, SignatureException, NoSuchProviderException, InvalidAlgorithmParameterException;
}
