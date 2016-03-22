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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.util;

import android.content.Context;
import android.os.Environment;

import java.io.IOException;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.debuglogging.ConsoleLogger;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.debuglogging.FileLogger;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.kernel.AndroidKernel;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.NetworkManager;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.cache.NetworkCacheManager;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.time.TrustedTimeManager;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.userid.CredentialManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.DebugLogAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.filter.OrFilter;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.filter.PriorityFilter;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.EventManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.AbstractManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.Kernel;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.Manager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.util.ReflectionHelper;

/**
 * Helper to initialize the kernel and setup all required managers.
 */
public class KernelManagerHelper {
	private static final String TAG = KernelManagerHelper.class.getSimpleName();
	private static final String TAG_STORAGE_OPTIONS = "StorageOptions";

	public static Kernel initializeKernel(Context context) throws IOException {
		Kernel kernel = new AndroidKernel(context);

		kernel.addManager(new DebugLogManager(new ConsoleLogger(new OrFilter(new PriorityFilter(DebugLogManager.Priority.INFO),
				new PriorityFilter(DebugLogManager.Priority.DEBUG), new PriorityFilter(DebugLogManager.Priority.WARNING), new PriorityFilter(
				DebugLogManager.Priority.ERROR)))));

		kernel.addManager(new CredentialManager());
		kernel.addManager(new EventManager());
		kernel.addManager(new NetworkCacheManager());
		kernel.addManager(new NetworkManager());
		kernel.addManager(new TrustedTimeManager());


		String[] managerList = null;
		try {
			managerList = PropertiesHelper.getProperty(context, "manager", String[].class);
		} catch (Exception e) {
		}
		if (managerList != null) {
			for (String s : managerList) {
				if (s == null || s.length() == 0) {
					continue;
				}

				try {
					Manager manager = ReflectionHelper.instantiateClass(Manager.class, s);
					kernel.addManager(manager);
				} catch (Exception e) {
					new DebugLogAction(kernel, DebugLogManager.Priority.ERROR, TAG, "Couldn't add manager " + e.getMessage());
				}
			}
		} else {
			new DebugLogAction(kernel, DebugLogManager.Priority.ERROR, TAG, "No managers registered!");
		}

		String key = PropertiesHelper.getProperty(context, "logging.fileLogging");
		if (key != null && key.equals("true")) {
			AbstractManager.getInstance(kernel, DebugLogManager.class).addListener(new FileLogger(new PriorityFilter(DebugLogManager.Priority.INFO)));
		}

		StorageOptions.determineStorageOptions();

		new DebugLogAction(kernel, DebugLogManager.Priority.INFO, TAG_STORAGE_OPTIONS, "External: " + Environment.getExternalStorageDirectory());
		for (String s : StorageOptions.sPaths) {
			new DebugLogAction(kernel, DebugLogManager.Priority.INFO, TAG_STORAGE_OPTIONS, "Path: " + s);
		}

		for (String s : StorageOptions.sLabels) {
			new DebugLogAction(kernel, DebugLogManager.Priority.INFO, TAG_STORAGE_OPTIONS, "Label:" + s);
		}

		return kernel;
	}
}
