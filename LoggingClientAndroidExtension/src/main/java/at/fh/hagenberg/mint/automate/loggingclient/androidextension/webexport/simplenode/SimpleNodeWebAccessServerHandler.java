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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport.simplenode;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.fileexport.FileExportManager;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.kernel.AndroidKernel;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.userid.CredentialManager;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.util.PropertiesHelper;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport.WebExportServerHandler;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport.simplenode.api.RegisterDeviceRequest;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport.simplenode.api.UploadLogsRequest;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport.simplenode.api.WebAPI;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport.simplenode.api.WebResponse;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport.simplenode.crypto.EncryptionUtil;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.KernelBase;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Web export server handler for the automate SimpleNodeWebAccess server.
 */
@SuppressWarnings("unused")
public class SimpleNodeWebAccessServerHandler implements WebExportServerHandler {
	private static final String TAG = SimpleNodeWebAccessServerHandler.class.getSimpleName();

	private static final String BACKSLASH = "/";

	private static final String PREFKEY_KEY_UPLOADED = "keyUploaded";
	private static final String PREFKEY_PENDING_UPLOADS = "pendingUploads";

	private boolean mKeyUploaded = false;
	private String mDeviceId;

	private final Context mContext;

	public SimpleNodeWebAccessServerHandler() {
		mContext = ((AndroidKernel) KernelBase.getKernel()).getContext();

		mKeyUploaded = getPrefs().getBoolean(PREFKEY_KEY_UPLOADED, false);
		mDeviceId = ((CredentialManager) KernelBase.getKernel().getManager(CredentialManager.ID)).getUserId();

		init();
	}

	private void init() {
		Log.d(TAG, "init");

		try {
			EncryptionUtil.init(mContext);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (!mKeyUploaded) {
			registerDevice();
		}
	}

	private WebAPI getWebAPIService() {
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

		String url = PropertiesHelper.getProperty(mContext, "webexport.server.url");
		if (!url.endsWith(BACKSLASH)) {
			url += BACKSLASH;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(url)
				.client(client)
				.addConverterFactory(ScalarsConverterFactory.create())
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		return retrofit.create(WebAPI.class);
	}

	private void registerDevice() {
		Log.d(TAG, "registerDevice");
		WebAPI service = getWebAPIService();

		try {
			RegisterDeviceRequest request = new RegisterDeviceRequest(mDeviceId, EncryptionUtil.getPublicKey(mContext));
			request.sign(mContext);
			Response<WebResponse> response = service.registerDevice(request).execute();
			if (response.isSuccessful() && response.body().isSuccess()) {
				getPrefs().edit().putBoolean(PREFKEY_KEY_UPLOADED, true).apply();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addPendingUpload(long logtime) {
		SharedPreferences prefs = getPrefs();
		Set<String> pending = prefs.getStringSet(PREFKEY_PENDING_UPLOADS, new HashSet<String>());
		pending.add(String.valueOf(logtime));
		prefs.edit().putStringSet(PREFKEY_PENDING_UPLOADS, pending).apply();
	}

	@Override
	public void performUpload(Context context) {
		init();

		Log.d(TAG, "performUpload");
		WebAPI service = getWebAPIService();

		UploadLogsRequest request = new UploadLogsRequest(mDeviceId);
		request.sign(context);

		SharedPreferences prefs = getPrefs();
		Set<String> pending = prefs.getStringSet(PREFKEY_PENDING_UPLOADS, new HashSet<String>());
		if (pending.isEmpty()) {
			return;
		}
		prefs.edit().remove(PREFKEY_PENDING_UPLOADS).apply();

		for (String time : pending) {
			long logtime = Long.valueOf(time);
			if (!performUpload(context, service, request, logtime)) {
				addPendingUpload(logtime);
			}
		}
	}

	private boolean performUpload(Context context, WebAPI service, UploadLogsRequest request, long logtime) {
		try {
			File file = new File(context.getExternalFilesDir(null), "export-" + FileExportManager.DATE_FORMAT_FILE_EXPORT.format(logtime) + ".zip");
			if (file.length() == 0) {
				return true;
			}

			RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

			MultipartBody.Part logFiles = MultipartBody.Part.createFormData("logfile", file.getName(), requestFile);

			Response<WebResponse> response = service.uploadLogs(request.getDeviceId(), request.getTimestamp(), request.getSignature(), logFiles).execute();
			return response.isSuccessful() && response.body().isSuccess();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private SharedPreferences getPrefs() {
		return mContext.getSharedPreferences(SimpleNodeWebAccessServerHandler.class.getSimpleName(), Context.MODE_PRIVATE);
	}
}
