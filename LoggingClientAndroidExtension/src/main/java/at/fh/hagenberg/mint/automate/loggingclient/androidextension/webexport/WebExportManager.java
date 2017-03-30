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

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import java.util.Calendar;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.fileexport.action.RequestFileExportIntentAction;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.kernel.AndroidKernel;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.EventManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.AbstractManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.ManagerException;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Simple manager that starts a file export and then uploads the files to the specified server.
 */
@SuppressWarnings("unused")
public class WebExportManager extends AbstractManager {
	private static final String TAG = WebExportManager.class.getSimpleName();

	public static final Id ID = new Id(WebExportManager.class);

	private static final String PREFKEY_LAST_UPLOAD = "lastUploadTimestamp";

	public WebExportManager() {
		addDependency(EventManager.ID);
	}

	@Override
	protected void doStart() throws ManagerException {
		long lastUploadTime = getSharedPreferences().getLong(PREFKEY_LAST_UPLOAD, 0);
		if (!DateUtils.isToday(lastUploadTime)) {
			getSharedPreferences().edit().putLong(PREFKEY_LAST_UPLOAD, Calendar.getInstance().getTimeInMillis()).apply();

			new RequestFileExportIntentAction().execute();
		}
	}

	private SharedPreferences getSharedPreferences() {
		return ((AndroidKernel) getKernel()).getContext().getSharedPreferences(WebExportManager.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	@Override
	public Id getId() {
		return ID;
	}
}
