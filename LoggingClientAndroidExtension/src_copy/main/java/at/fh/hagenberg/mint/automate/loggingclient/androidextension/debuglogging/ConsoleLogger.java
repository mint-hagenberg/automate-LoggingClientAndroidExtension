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

import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.BasicLogger;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.filter.LogFilter;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.formatter.LogFormatter;

/**
 * Logger that outputs to the Android console.
 */
public class ConsoleLogger extends BasicLogger {
	public ConsoleLogger() {
		this(null, new AndroidFormatter());
	}

	public ConsoleLogger(LogFilter filter) {
		this(filter, new AndroidFormatter());
	}

	public ConsoleLogger(LogFilter filter, LogFormatter formatter) {
		super(filter, formatter);
	}

	@Override
	public void doLogMessage(DebugLogManager.Priority priority, String source, String message) {
		//noinspection WrongConstant
		Log.println(getFormatter().formatPriority(priority), source, message);
	}

	@Override
	public void doStart(DebugLogManager instance) {
		printHeader(instance);
	}

	@Override
	public void doStop(DebugLogManager instance) {
		printFooter(instance);
	}

	private void printHeader(DebugLogManager instance) {
		Log.i("ConsoleLogger", "Logger started (" + new Date() + ")");

		Log.i("ConsoleLogger", "Framework Kernel V" + instance.getKernel().getVersion() + " running on "
				+ android.os.Build.VERSION.CODENAME + ", V" + android.os.Build.VERSION.RELEASE + " ("
				+ android.os.Build.VERSION.SDK_INT + ")");
		Log.i("ConsoleLogger", "-------------------------------------------------------------------------------");
	}

	private void printFooter(DebugLogManager instance) {
		Log.i("ConsoleLogger", "Logger stopped (" + new Date() + ")");
	}

	@Override
	public void doLogMessage(DebugLogManager.Priority priority, String source, Throwable throwable) {
		doLogMessage(priority, source, throwable.getMessage());
	}

	@Override
	public void doLogMessage(DebugLogManager.Priority priority, String source, Object message) {
		doLogMessage(priority, source, message.toString());
	}
}