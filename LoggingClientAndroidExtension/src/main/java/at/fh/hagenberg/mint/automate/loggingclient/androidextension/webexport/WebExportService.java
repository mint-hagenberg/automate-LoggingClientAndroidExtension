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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.util.PropertiesHelper;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.DebugLogAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.KernelBase;
import at.fhhagenberg.mint.automate.loggingclient.javacore.util.ReflectionHelper;

/**
 *
 */
public class WebExportService extends Service {
	private static final String TAG = WebExportService.class.getSimpleName();

	public static final String ACTION_UPLOAD = "performUpload";
	public static final String EXTRA_TIME = "time";

	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	public WebExportService() {
	}

	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread(WebExportService.class.getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand " + intent);
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		if (intent == null || intent.getAction() == null) {
			msg.arg2 = ServiceHandler.ACTION_INIT;
		} else if (intent.getAction().equals(ACTION_UPLOAD)) {
			msg.arg2 = ServiceHandler.ACTION_UPLOAD;
		}
		if (intent != null) {
			msg.setData(intent.getExtras());
		}
		mServiceHandler.sendMessage(msg);

		return START_STICKY;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO: maybe graceful shut down everything
	}

	private final class ServiceHandler extends Handler {
		private final String TAG = ServiceHandler.class.getSimpleName();

		public static final int ACTION_INIT = 1;
		public static final int ACTION_UPLOAD = 2;

		private WebExportServerHandler mServerHandler;

		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Context context = WebExportService.this;
			switch (msg.arg2) {
				case ACTION_INIT: {
					ensureServerHandler(context);
					break;
				}

				case ACTION_UPLOAD: {
					long time = msg.getData().getLong(EXTRA_TIME);
					uploadToServer(context, time);
					break;
				}

				default: {
					Log.d(TAG, "No message handler for " + msg.arg2);
					break;
				}
			}
		}

		private void ensureServerHandler(Context context) {
			if (mServerHandler != null) {
				return;
			}
			String serverType = PropertiesHelper.getProperty(context, "webexport.server.type");
			if (serverType == null) {
				new DebugLogAction(KernelBase.getKernel(), DebugLogManager.Priority.ERROR, TAG, "webexport.server.type not set in automate properties!");
				throw new RuntimeException("webexport.server.type not set in automate properties");
			}

			try {
				mServerHandler = ReflectionHelper.instantiateClass(WebExportServerHandler.class, serverType);
			} catch (Exception e) {
				new DebugLogAction(KernelBase.getKernel(), DebugLogManager.Priority.ERROR, TAG, "Could not find or instantiate class from webexport.server.type: " + e.getMessage());
				throw new RuntimeException("Could not find or instantiate class from webexport.server.type", e);
			}
		}

		private void uploadToServer(Context context, long time) {
			ensureServerHandler(context);

			mServerHandler.addPendingUpload(time);
			mServerHandler.performUpload(context);
		}
	}
}
