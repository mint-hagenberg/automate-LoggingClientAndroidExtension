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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.webexport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.fileexport.FileExportManager;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.util.PropertiesHelper;

/**
 * Broadcast receiver to start uploading files after the file export was finished.
 */
public class WebExportBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = WebExportBroadcastReceiver.class.getSimpleName();

	public WebExportBroadcastReceiver() {
		Log.d(TAG, "init");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive " + intent);

		if (!PropertiesHelper.getProperty(context, "webexport.enabled", Boolean.class, false)) {
			Log.d(TAG, "webexport is not enabled");
			return;
		}

		Intent serviceIntent = new Intent(context, WebExportService.class);
		if (intent.getAction().equals(FileExportManager.ACTION_EXPORT_STARTING)) {
			context.startService(serviceIntent);
		} else if (intent.getAction().equals(FileExportManager.ACTION_EXPORT_FINISHED)) {
			long time = intent.getLongExtra(FileExportManager.EXTRA_TIME, 0);
			if (time <= 0) {
				// TODO: error!
				return;
			}
			serviceIntent.setAction(WebExportService.ACTION_UPLOAD);
			serviceIntent.putExtra(WebExportService.EXTRA_TIME, time);
			context.startService(serviceIntent);
		}
	}
}
