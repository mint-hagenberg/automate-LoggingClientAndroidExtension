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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.userid;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.kernel.AndroidKernel;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.util.PropertiesHelper;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.AbstractManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.ManagerException;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Manager that generates a random session id for every (kernel) restart and has the possibility to store a user id.
 */
public class CredentialManager extends AbstractManager {
    /**
     * Manager ID type.
     */
    public static final Id ID = new Id(CredentialManager.class);

    /**
     * Credentials filename.
     */
    private static final String FILENAME_CREDENTIALS = "AutomateCredentials";

    /**
     * Properties file key to get/store the user id.
     */
    private static final String PROPERTY_KEY_USERID = "user-id";

    /**
     * The current user id.
     */
    private String mUserId;
    /**
     * The current session it.
     */
    private UUID mSessionId;

    @Override
    protected void doStart() throws ManagerException {
        super.doStart();

        Properties props = new Properties();
        File credentialFile = new File(Environment.getExternalStorageDirectory(), FILENAME_CREDENTIALS);
        if (credentialFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(credentialFile);
                props.load(fis);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String temp = props.getProperty(PROPERTY_KEY_USERID, null);

        if (temp != null) {
            mUserId = temp;
        } else {
            mUserId = Settings.Secure.getString(((AndroidKernel) getKernel()).getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }

        mSessionId = UUID.randomUUID();

        Credentials.setup(getKernel());
    }

    /**
     * Get the project id from the assets automate properties file.
     *
     * @return -
     */
    public String getProjectId() {
        Context context = ((AndroidKernel) getKernel()).getContext();
        String key = PropertiesHelper.getProperty(context, "pro.key");
        getLogger().logInfo(getLoggingSource(), "Reading project key: " + key);
        return key;
    }

    /**
     * Set a user id and store it in a file for safekeeping.
     *
     * @param userId -
     */
    public void setUserId(String userId) {
        mUserId = userId;

        Properties props = new Properties();
        props.put(PROPERTY_KEY_USERID, userId);
        File credentialFile = new File(Environment.getExternalStorageDirectory(), FILENAME_CREDENTIALS);
        try {
            if (!credentialFile.exists()) {
                credentialFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(credentialFile);
            props.store(fos, null);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the session id.
     *
     * @return -
     */
    public UUID getSessionId() {
        return mSessionId;
    }

    /**
     * Get the user id.
     *
     * @return -
     */
    public String getUserId() {
        return mUserId;
    }

    @Override
    public Id getId() {
        return ID;
    }
}
