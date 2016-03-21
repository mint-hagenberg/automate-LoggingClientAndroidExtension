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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.network;

import android.content.Context;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.util.PropertiesHelper;

/**
 * Helper for network stuff.
 */
public final class NetworkHelper {
	private static final String IP_STRING = "serverip";
	private static final String PORT_STRING = "serverport";

	/**
	 * Get the server ip address.
	 *
	 * @param context -
	 * @return -
	 */
	public static String getServerIP(Context context) {
		return PropertiesHelper.getProperty(context, IP_STRING);
	}

	/**
	 * Get the server thrift port.
	 *
	 * @param context -
	 * @return -
	 */
	public static int getServerThriftPort(Context context) {
		try {
			return PropertiesHelper.getProperty(context, PORT_STRING, Integer.class);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	private NetworkHelper() {
	}
}
