/*
 *     Copyright (C) 2016 Mobile Interactive Systems Research Group
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.debuglogging;

import android.util.Log;

import java.util.Date;

import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.formatter.LogFormatter;

/**
 * Android specific log formatter. used to convert between Logging Client and Android priority.
 */
public class AndroidFormatter implements LogFormatter {
	@Override
	public String format(DebugLogManager.Priority priority, String src, String msg, Date time) {
		return null;
	}

	@Override
	public int formatPriority(DebugLogManager.Priority priority) {
		switch (priority) {
			case ASSERT:
				return Log.ASSERT;

			case DEBUG:
				return Log.DEBUG;

			case INFO:
				return Log.INFO;

			case WARNING:
				return Log.WARN;

			case ERROR:
				return Log.ERROR;

			case CRITICAL:
				return Log.ERROR;

			case VERBOSE:
				return Log.VERBOSE;

			default:
				return Log.DEBUG;
		}
	}
}
