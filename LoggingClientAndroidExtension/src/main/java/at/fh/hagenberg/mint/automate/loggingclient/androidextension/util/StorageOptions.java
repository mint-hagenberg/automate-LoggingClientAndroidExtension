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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StorageOptions {
	public static List<String> sMounts = new ArrayList<String>();
	public static List<String> sVold = new ArrayList<String>();

	public static String[] sLabels;
	public static String[] sPaths;
	public static int sCount = 0;

	public static void determineStorageOptions() {
		readMountsFile();
		readVoldFile();
		compareMountsWithVold();
		testAndCleanMountsList();
		setProperties();
	}

	private static void readMountsFile() {
		/*
         * Scan the /proc/mounts file and look for lines like this:
		 * /dev/block/vold/179:1 /mnt/sdcard vfat
		 * rw,dirsync,nosuid,nodev,noexec,
		 * relatime,uid=1000,gid=1015,fmask=0602,dmask
		 * =0602,allow_utime=0020,codepage
		 * =cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
		 * 
		 * When one is found, split it into its elements and then pull out the
		 * path to the that mount point and add it to the arraylist
		 */

		// some mount files don't list the default
		// path first, so we add it here to
		// ensure that it is first in our list
		sMounts.add(Environment.getExternalStorageDirectory().getAbsolutePath());

		Scanner scanner = null;
		try {
			scanner = new Scanner(new File("/proc/mounts"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("/dev/block/vold/")) {
					String[] lineElements = line.split(" ");
					String element = lineElements[1];

					if (!element.equalsIgnoreCase(Environment
							.getExternalStorageDirectory().getAbsolutePath())) {
						sMounts.add(element);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (scanner != null) {
				try {
					scanner.close();
				} catch (Exception ex) {
					// Ignore
				}
			}
		}
	}

	private static void readVoldFile() {
		/*
		 * Scan the /system/etc/vold.fstab file and look for lines like this:
		 * dev_mount sdcard /mnt/sdcard 1
		 * /devices/platform/s3c-sdhci.0/mmc_host/mmc0
		 * 
		 * When one is found, split it into its elements and then pull out the
		 * path to the that mount point and add it to the arraylist
		 */

		// some devices are missing the vold file entirely
		// so we add a path here to make sure the list always
		// includes the path to the first sdcard, whether real
		// or emulated.
		// Starting with Android 4.4 we will probably not have access to this
		// file anymore as security was tightened
		sVold.add(Environment.getExternalStorageDirectory().getAbsolutePath());

		Scanner scanner = null;
		try {
			scanner = new Scanner(new File("/system/etc/vold.fstab"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("dev_mount")) {
					String[] lineElements = line.split(" ");
					String element = lineElements[2];

					if (element.contains(":"))
						element = element.substring(0, element.indexOf(":"));

					if (!element.equalsIgnoreCase(Environment
							.getExternalStorageDirectory().getAbsolutePath())) {
						sVold.add(element);
					}
				}
			}
		} catch (FileNotFoundException ex) {
			// Ignore
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (scanner != null) {
				try {
					scanner.close();
				} catch (Exception ex) {
					// Ignore
				}
			}
		}
	}

	private static void compareMountsWithVold() {
		/*
		 * Sometimes the two lists of mount points will be different. We only
		 * want those mount points that are in both list.
		 * 
		 * Compare the two lists together and remove items that are not in both
		 * lists.
		 */

		for (int i = 0; i < sMounts.size(); i++) {
			String mount = sMounts.get(i);
			if (!sVold.contains(mount))
				sMounts.remove(i--);
		}

		// don't need this anymore, clear the vold list to reduce memory
		// use and to prepare it for the next time it's needed.
		sVold.clear();
	}

	private static void testAndCleanMountsList() {
		/*
		 * Now that we have a cleaned list of mount paths Test each one to make
		 * sure it's a valid and available path. If it is not, remove it from
		 * the list.
		 */

		for (int i = 0; i < sMounts.size(); i++) {
			String mount = sMounts.get(i);
			File root = new File(mount);
			if (!root.exists() || !root.isDirectory() || !root.canWrite())
				sMounts.remove(i--);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void setProperties() {
		/*
		 * At this point all the paths in the list should be valid. Build the
		 * public properties.
		 */
		ArrayList<String> mLabels = new ArrayList<String>();

		int j = 0;
		if (sMounts.size() > 0) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
				mLabels.add("Auto");
			else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				if (Environment.isExternalStorageRemovable()) {
					mLabels.add("External SD Card 1");
					j = 1;
				} else
					mLabels.add("Internal Storage");
			} else {
				if (!Environment.isExternalStorageRemovable()
						|| Environment.isExternalStorageEmulated())
					mLabels.add("Internal Storage");
				else {
					mLabels.add("External SD Card 1");
					j = 1;
				}
			}

			if (sMounts.size() > 1) {
				for (int i = 1; i < sMounts.size(); i++) {
					mLabels.add("External SD Card " + (i + j));
				}
			}
		}

		sLabels = new String[mLabels.size()];
		mLabels.toArray(sLabels);

		sPaths = new String[sMounts.size()];
		sMounts.toArray(sPaths);
		sCount = Math.min(sLabels.length, sPaths.length);

		// don't need this anymore, clear the mounts list to reduce memory
		// use and to prepare it for the next time it's needed.
		sMounts.clear();

	}
}