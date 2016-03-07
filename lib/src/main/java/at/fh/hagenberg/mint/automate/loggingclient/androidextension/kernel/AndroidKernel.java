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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.kernel;

import android.content.Context;

import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.Kernel;

/**
 * The Android extension of the Logging Client Kernel. Provides access to the Android Context.
 */
public class AndroidKernel extends Kernel {
	private final Context mContext;

	public AndroidKernel(Context context) {
		super();

		mContext = context.getApplicationContext();
	}

	/**
	 * Get the Android Context.
	 *
	 * @return -
	 */
	public Context getContext() {
		return mContext;
	}
}
