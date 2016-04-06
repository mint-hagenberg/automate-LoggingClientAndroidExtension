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
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.fhhagenberg.mint.automate.loggingclient.javacore.action.DebugLogAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.Kernel;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.Manager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * The Android extension of the Logging Client Kernel. Provides access to the Android Context.
 */
public class AndroidKernel extends Kernel {
    private static final String PREFERENCE_KEY_DISABLED_MANAGERS = "disabledManagers";

    private final Context mContext;

    public AndroidKernel(Context context) {
        super();

        mContext = context.getApplicationContext();
    }

    protected void storeDisabledManagers() {
        SharedPreferences preferences = getSharedPreferences();
        Set<String> disabled = new HashSet<>();
        for (Id id : getDisabledManagers()) {
            disabled.add(id.toString());
        }
        preferences.edit().putStringSet(PREFERENCE_KEY_DISABLED_MANAGERS, disabled).apply();
    }

    protected void restoreDisabledManagers() {
        SharedPreferences preferences = getSharedPreferences();
        Set<String> disabled = preferences.getStringSet(PREFERENCE_KEY_DISABLED_MANAGERS, null);
        if (disabled != null) {
            for (String id : disabled) {
                getDisabledManagers().add(new Id(id));
            }
        }
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(AndroidKernel.class.getName(), Context.MODE_PRIVATE);
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
