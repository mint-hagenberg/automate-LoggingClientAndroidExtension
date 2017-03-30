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
import android.support.annotation.NonNull;

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

/**
 * Created by dustin on 24/03/2017.
 */
public final class EncryptionUtil {
	@SuppressWarnings("unused")
	private static final String TAG = EncryptionUtil.class.getSimpleName();

	private static SupportEncryption sSupportEncryption;

	public static void init(Context context) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, CertificateException, UnrecoverableKeyException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
		if (sSupportEncryption == null) {
			sSupportEncryption = SupportEncryptionRSA.newInstance(context);
		}
	}

	public static String getPublicKey(Context context) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
		try {
			init(context);

			return sSupportEncryption.getPublicKey();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String signString(@NonNull Context context, String text) {
		try {
			init(context);
			if (text == null || text.isEmpty()) {
				return null;
			}

			return sSupportEncryption.sign(context, text.getBytes());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private EncryptionUtil() {
	}
}
