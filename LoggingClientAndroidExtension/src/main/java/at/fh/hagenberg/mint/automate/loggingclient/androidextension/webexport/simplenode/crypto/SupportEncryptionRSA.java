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
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

/**
 * Created by dustin on 24/03/2017.
 */
public class SupportEncryptionRSA extends SupportEncryption {
	@SuppressWarnings("unused")
	private static final String TAG = SupportEncryption.class.getSimpleName();

	private static final String KEY_ALIAS = "at.fh.hagenberg.mint.automate.webexport";
	private static final int KEY_VALIDITY_YEARS = 35;

	private static final String RSA_MODE = "RSA";
	private static final String SIGNATURE_MODE = "SHA256withRSA";

	public static SupportEncryptionRSA newInstance(Context context, KeyStore keystore) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException, KeyStoreException {
		return new SupportEncryptionRSA(context, keystore);
	}

	public static SupportEncryptionRSA newInstance(Context context) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
		KeyStore keystore = KeyStore.getInstance(ANDROID_KEY_STORE);
		keystore.load(null);
		return newInstance(context, keystore);
	}

	protected SupportEncryptionRSA(Context context, KeyStore keystore) throws KeyStoreException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
		super(keystore);


		if (!mKeystore.containsAlias(KEY_ALIAS)) {
			generateKey(context);
		}
	}

	private void generateKey(Context context) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(RSA_MODE, ANDROID_KEY_STORE);

		//if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
		Calendar notBefore = Calendar.getInstance();
		Calendar notAfter = Calendar.getInstance();
		notAfter.add(Calendar.YEAR, KEY_VALIDITY_YEARS);
		KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
				.setAlias(KEY_ALIAS)
				.setSubject(
						new X500Principal(String.format("CN=%s, OU=%s", KEY_ALIAS,
								context.getPackageName())))
				.setSerialNumber(BigInteger.ONE).setStartDate(notBefore.getTime())
				.setEndDate(notAfter.getTime()).build();
		kpGenerator.initialize(spec);
//		} else {
//			KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN)
//					.setDigests(KeyProperties.DIGEST_SHA256)
//					.build();
//			kpGenerator.initialize(spec);
//		}

		kpGenerator.generateKeyPair();
	}

	@Override
	public String getPublicKey() throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, IOException {
		KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) mKeystore.getEntry(KEY_ALIAS, null);
		PublicKey publicKey = keyEntry.getCertificate().getPublicKey();

		StringWriter publicStringWriter = new StringWriter();
		PemWriter pemWriter = new PemWriter(publicStringWriter);
		pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
		pemWriter.flush();
		pemWriter.close();
		return publicStringWriter.toString();
	}

	protected byte[] rsaSign(byte[] sign) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidKeyException, SignatureException, NoSuchProviderException, InvalidAlgorithmParameterException {
		KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) mKeystore.getEntry(KEY_ALIAS, null);
		PrivateKey privateKey = keyEntry.getPrivateKey();

		Signature signature = Signature.getInstance(SIGNATURE_MODE);
		signature.initSign(privateKey);

		signature.update(sign);
		return signature.sign();
	}

	@Override
	public String sign(Context context, byte[] sign) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidKeyException, SignatureException, NoSuchProviderException, InvalidAlgorithmParameterException {
		return Base64.encodeToString(rsaSign(sign), Base64.NO_PADDING | Base64.NO_WRAP);
	}
}
