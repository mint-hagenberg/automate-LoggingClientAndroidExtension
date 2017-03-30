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

/**
 * Interface that defines the server handler, which will do the actual connection and upload work.
 */
public interface WebExportServerHandler {
	/**
	 * Add a logfile to the cache so we'll upload it the next time performUpload is called.
	 *
	 * @param context possible Android context if required
	 * @param logtime the logtime of the file export
	 */
	void addPendingUpload(long logtime);

	/**
	 * Perform the actual uploads to the server.
	 *
	 * @param context possible Android context if required
	 */
	void performUpload(Context context);
}
