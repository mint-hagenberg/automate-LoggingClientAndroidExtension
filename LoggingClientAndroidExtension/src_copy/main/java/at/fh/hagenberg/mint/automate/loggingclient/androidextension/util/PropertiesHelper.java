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
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper to get data from the automate properties file.
 */
public final class PropertiesHelper {
	private static final String DEBUG_FILE_NAME = "automate_debug.properties";
	private static final String ASSET_FILE_NAME = "automate.properties";

	/**
	 * Get a property from the debug file on the external storage or the assets automate properties file.
	 *
	 * @param context -
	 * @param name    -
	 * @return -
	 */
	public static String getProperty(Context context, String name) {
		Properties props = new Properties();
		try {
			File serverInfo = new File(Environment.getExternalStorageDirectory(), DEBUG_FILE_NAME);
			if (serverInfo.exists()) {
				FileInputStream fis = new FileInputStream(serverInfo);
				props.load(fis);
				fis.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String property = props.getProperty(name);
		if (property != null) {
			return property;
		}

		props = new Properties();
		try {
			AssetManager assetManager = context.getResources().getAssets();
			InputStream is = assetManager.open(ASSET_FILE_NAME);
			props.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		property = props.getProperty(name);
		return property;
	}

	/**
	 * Get a typed property.
	 *
	 * @param context -
	 * @param name    -
	 * @param type    -
	 * @param <T>     -
	 * @return -
	 * @throws Exception
	 */
	public static <T> T getProperty(Context context, String name, Class<T> type) throws Exception {
		String propertyString = getProperty(context, name);
		if (propertyString == null) {
			return null;
		}

		if (type == Integer.class) {
			return (T) Integer.valueOf(propertyString);
		} else if (type == Double.class) {
			return (T) Double.valueOf(propertyString);
		} else if (type == Float.class) {
			return (T) Float.valueOf(propertyString);
		} else if (type == Long.class) {
			return (T) Long.valueOf(propertyString);
		} else if (type == String[].class) {
			return (T) propertyString.split(",");
		} else {
			return null;
		}
	}

	private PropertiesHelper() {
	}
}
