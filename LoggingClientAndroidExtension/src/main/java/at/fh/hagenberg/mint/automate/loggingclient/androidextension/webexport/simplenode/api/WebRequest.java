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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport.simplenode.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport.simplenode.crypto.EncryptionUtil;

/**
 * Created by dustin on 30/03/2017.
 */
public abstract class WebRequest {
	@SerializedName("deviceId")
	private String mDeviceId;
	@SerializedName("timestamp")
	private long mTimestamp;
	@SerializedName("signature")
	private String mSignature;

	public WebRequest(@NonNull String deviceId) {
		mDeviceId = deviceId;
	}

	public String getDeviceId() {
		return mDeviceId;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public String getSignature() {
		return mSignature;
	}

	public void sign(Context context) {
		mTimestamp = Calendar.getInstance().getTimeInMillis();

		mSignature = EncryptionUtil.signString(context, mDeviceId + "." + mTimestamp);
	}
}
