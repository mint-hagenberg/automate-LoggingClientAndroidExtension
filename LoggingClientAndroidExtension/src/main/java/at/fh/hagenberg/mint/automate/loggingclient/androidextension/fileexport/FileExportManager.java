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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.fileexport;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.R;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.kernel.AndroidKernel;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.userid.CredentialManager;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.util.PropertiesHelper;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.RegisterEventListenerAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.UnregisterEventListenerAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.Event;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.EventListener;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.EventManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.AbstractManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.KernelListener;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.ManagerException;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * A manager that uses handlers to save data to files, based on the transmission events also used in the network manager.
 */
public abstract class FileExportManager extends AbstractManager implements EventListener, KernelListener {
    public static final Id FILE_EXPORT_REQUESTED_EVENT = new Id("FILE_EXPORT_REQUESTED_EVENT");

    private static final int NOTIFICATION_EXPORT_ZIP = 9000;

    private static final int ZIP_OUTPUT_BUFFER_SIZE = 1024;

    private static final String EXPORT_DIR_BASE_NAME = "export";
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT_FILE_EXPORT = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");

    private final Set<Id> mRegisteredTransmissionEvents = new HashSet<>();
    private final List<FileExportHandler> mFileExportHandlers = new ArrayList<>();
    private final Map<Id, FileExportHandler> mTransmissionEventToHandlerMap = new HashMap<>();

    private boolean mStoreFilesExternal = false;

    private CredentialManager mCredentialManager;

    protected String mProjectId;
    protected String mDeviceId;
    protected UUID mSessionId;
    protected int mSequenceNr;
    protected String mAppVersion;

    private Map<Id, FileOutputStream> mOpenFileStreams = new HashMap<>();

    public FileExportManager() {
        addDependency(EventManager.ID);
        addDependency(CredentialManager.ID);
    }

    @Override
    protected void doStart() throws ManagerException {
        super.doStart();

        getKernel().addListener(this);

        Context context = ((AndroidKernel) getKernel()).getContext();
        mStoreFilesExternal = PropertiesHelper.getProperty(context, "fileexport.storeexternal", Boolean.class, false);

        String serviceHandlerProperty = PropertiesHelper.getProperty(context, "fileexport.handler");
        String[] serviceHandlers = serviceHandlerProperty != null && !serviceHandlerProperty.isEmpty() ? serviceHandlerProperty.split(",") : null;
        if (serviceHandlers != null && serviceHandlers.length > 0) {
            for (String handlerName : serviceHandlers) {
                try {
                    Class<?> handlerClass = Class.forName(handlerName);
                    FileExportHandler handler = (FileExportHandler) handlerClass.newInstance();
                    mFileExportHandlers.add(handler);
                    registerTransmissionEvent(handler.getTransmissionEvents());
                } catch (Exception ex) {
                    getLogger().logDebug(getLoggingSource(), "Could not register client network handler " + handlerName);
                    ex.printStackTrace();
                }
            }
        }
        initTransmissionEvents();

        new RegisterEventListenerAction(getKernel(), this, FILE_EXPORT_REQUESTED_EVENT).execute();
        new RegisterEventListenerAction(getKernel(), this, mRegisteredTransmissionEvents.toArray(new Id[0])).execute();

        mCredentialManager = AbstractManager.getInstance(getKernel(), CredentialManager.class);
        mDeviceId = mCredentialManager.getUserId();
        mSessionId = mCredentialManager.getSessionId();
        mProjectId = mCredentialManager.getProjectId();
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            mAppVersion = String.valueOf(pInfo.versionCode);
        } catch (Exception e) {
            mAppVersion = "UNDEFINED";
        }
        mSequenceNr = 0;
    }

    private void registerTransmissionEvent(List<Id> types) {
        mRegisteredTransmissionEvents.addAll(types);
    }

    private void initTransmissionEvents() {
        for (FileExportHandler handler : mFileExportHandlers) {
            for (Id event : handler.getTransmissionEvents()) {
                mTransmissionEventToHandlerMap.put(event, handler);
            }
        }
    }

    @Override
    protected void doStop() {
        getKernel().removeListener(this);

        new UnregisterEventListenerAction(getKernel(), this);

        super.doStop();
    }

    @Override
    public void handleEvent(Event event) {
        // Fetch deviceId again, in case it has changed.
        mDeviceId = mCredentialManager.getUserId();
        Id eventTypeId = event.getTypeId();
        getLogger().logWarning(getLoggingSource(), "Handle: " + eventTypeId);

        if (event.isOfType(FILE_EXPORT_REQUESTED_EVENT)) {
            startFileExport();
        } else if (mRegisteredTransmissionEvents.contains(eventTypeId)) {
            FileExportHandler exportHandler = mTransmissionEventToHandlerMap.get(eventTypeId);
            if (!mOpenFileStreams.containsKey(eventTypeId)) {
                String filename = exportHandler.getFilename(eventTypeId);
                try {
                    Context context = ((AndroidKernel) getKernel()).getContext();
                    FileOutputStream outputStream;
                    boolean fileCreated = false;
                    if (mStoreFilesExternal) {
                        File filesDir = new File(context.getExternalFilesDir(null), EXPORT_DIR_BASE_NAME);
                        if (!filesDir.exists()) {
                            filesDir.mkdirs();
                        }
                        File file = new File(filesDir, filename);
                        if (!file.exists() && file.createNewFile()) {
                            fileCreated = true;
                        }
                        getLogger().logDebug(getLoggingSource(), "writing to external " + file);
                        outputStream = new FileOutputStream(file, true);
                    } else {
                        getLogger().logDebug(getLoggingSource(), "writing to internal " + filename);
                        File file = new File(context.getFilesDir(), filename);
                        if (!file.exists()) {
                            fileCreated = true;
                        }
                        outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
                    }
                    if (fileCreated) {
                        writeHeaderToFile(outputStream, exportHandler.getFileHeader(eventTypeId));
                    }
                    mOpenFileStreams.put(eventTypeId, outputStream);
                } catch (Exception ex) {
                    getLogger().logCritical(getLoggingSource(), ex);
                }
            }
            FileOutputStream outputStream = mOpenFileStreams.get(eventTypeId);
            if (outputStream != null) {
                try {
                    writeToFile(outputStream, exportHandler.serialize(event));
                } catch (IOException ex) {
                    getLogger().logCritical(getLoggingSource(), ex);
                    // TODO: we probably need some kind of cache here…
                }
            } else {
                getLogger().logError(getLoggingSource(), "The output stream for " + eventTypeId + " is null");
                // TODO: we probably need some kind of cache here…
            }
            ++mSequenceNr;
        }
    }

    private File getExportFile(Context context, String filename) {
        return getExportFile(context, filename, null);
    }

    private File getExportFile(Context context, String filename, Date date) {
        File parent = getExportParentDir(context, date);
        return new File(parent, filename);
    }

    private File getExportParentDir(Context context, Date date) {
        File parent;
        if (mStoreFilesExternal) {
            parent = context.getExternalFilesDir(null);
        } else {
            parent = context.getFilesDir();
        }
        parent = new File(parent, File.separator + EXPORT_DIR_BASE_NAME);
        if (date != null) {
            String nowFormattedDate = DATE_FORMAT_FILE_EXPORT.format(date);
            parent = new File(parent, File.separator + nowFormattedDate);
        }
        return parent;
    }

    private void startFileExport() {
        new AsyncTask<Void, Integer, Boolean>() {
            private List<String> mFilenames;

            @Override
            protected void onPreExecute() {
                mFilenames = new ArrayList<>();
                for (FileExportHandler handler : mFileExportHandlers) {
                    mFilenames.addAll(handler.getAllFilenames());
                }
                getLogger().logDebug(getLoggingSource(), "Starting ZIP file export. " + mFilenames.size() + " files to export");
                showExportNotification();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Context context = ((AndroidKernel) getKernel()).getContext();
                ZipOutputStream zos = null;
                try {
                    Date now = Calendar.getInstance().getTime();

                    FileInputStream fis;
                    List<FileOutputStream> openStreams = new ArrayList<>(mOpenFileStreams.values());
                    mOpenFileStreams.clear();
                    for (OutputStream stream : openStreams) {
                        try {
                            stream.close();
                        } catch (IOException ex) {
                            // Ignore
                        }
                    }
                    List<File> filesToInclude = new ArrayList<>();
                    File parent = getExportParentDir(context, now);
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    for (String filename : mFilenames) {
                        File file = getExportFile(context, filename);
                        if (!file.exists()) {
                            continue;
                        }
                        File newName = getExportFile(context, filename, now);
                        boolean success = file.renameTo(newName);
                        filesToInclude.add(success ? newName : file);
                    }


                    File zipFile = new File(context.getExternalFilesDir(null), "export-" + DATE_FORMAT_FILE_EXPORT.format(now) + ".zip");
                    if (!zipFile.exists()) {
                        zipFile.createNewFile();
                    }
                    OutputStream os = new FileOutputStream(zipFile);
                    zos = new ZipOutputStream(new BufferedOutputStream(os));

                    byte[] dataBlock = new byte[ZIP_OUTPUT_BUFFER_SIZE];
                    ZipEntry entry;
                    for (File file : filesToInclude) {
                        if (!file.exists()) {
                            continue;
                        }
                        fis = new FileInputStream(file);

                        entry = new ZipEntry(file.getName());
                        zos.putNextEntry(entry);
                        writeFileToZip(fis, zos, dataBlock);
                        fis.close();
                        zos.closeEntry();
                    }
                    parent.delete();
                } catch (IOException ex) {
                    getLogger().logCritical(getLoggingSource(), ex);
                    return false;
                } finally {
                    if (zos != null) {
                        try {
                            zos.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                getLogger().logDebug(getLoggingSource(), "ZIP file export DONE: " + result);
                if (result) {
                    showExportDoneNotification();
                } else {
                    showExportErrorNotification();
                }
            }
        }.execute();
    }

    private void showExportNotification() {
        Context context = ((AndroidKernel) getKernel()).getContext();
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Exporting automate data")
                .setSmallIcon(R.drawable.ic_notification_file_export)
                .setOngoing(true)
                .setProgress(0, 0, true)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_EXPORT_ZIP, notification);
    }

    private void showExportDoneNotification() {
        // TODO: add intent to send/share file
        Context context = ((AndroidKernel) getKernel()).getContext();
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Export automate data done")
                .setSmallIcon(R.drawable.ic_notification_file_export)
                .setAutoCancel(true)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_EXPORT_ZIP, notification);
    }

    private void showExportErrorNotification() {
        Context context = ((AndroidKernel) getKernel()).getContext();
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Error exporting automate data")
                .setSmallIcon(R.drawable.ic_notification_file_export)
                .setAutoCancel(true)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_EXPORT_ZIP, notification);
    }

    private void writeFileToZip(FileInputStream inputStream, ZipOutputStream outputStream, byte[] dataBlock) throws IOException {
        int count = inputStream.read(dataBlock, 0, ZIP_OUTPUT_BUFFER_SIZE);
        while (count != -1) {
            outputStream.write(dataBlock, 0, count);
            count = inputStream.read(dataBlock, 0, ZIP_OUTPUT_BUFFER_SIZE);
        }
    }

    protected abstract void writeHeaderToFile(FileOutputStream stream, String[] headers) throws IOException;

    protected abstract void writeToFile(FileOutputStream stream, Object[] objects) throws IOException;

    @Override
    public void startupFinished() {
    }

    @Override
    public void onPrepareShutdown() {
    }

    @Override
    public void onShutdown() {
        for (FileOutputStream outputStream : mOpenFileStreams.values()) {
            try {
                outputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        mOpenFileStreams.clear();
    }
}
