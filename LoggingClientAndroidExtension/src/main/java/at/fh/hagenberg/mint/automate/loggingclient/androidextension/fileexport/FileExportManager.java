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

import android.content.Context;
import android.content.pm.PackageInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

		if (mRegisteredTransmissionEvents.contains(eventTypeId)) {
			FileExportHandler exportHandler = mTransmissionEventToHandlerMap.get(eventTypeId);
			if (!mOpenFileStreams.containsKey(eventTypeId)) {
				String filename = exportHandler.getFilename(eventTypeId);
				try {
					Context context = ((AndroidKernel) getKernel()).getContext();
					FileOutputStream outputStream;
					boolean fileCreated = false;
					if (mStoreFilesExternal) {
						File filesDir = context.getExternalFilesDir(null);
						if (!filesDir.exists()) {
							filesDir.mkdir();
						}
						File file = new File(filesDir, filename);
						if (!file.exists() && file.createNewFile()) {
							fileCreated = true;
						}
						getLogger().logDebug(getLoggingSource(), "writing to external " + file);
						outputStream = new FileOutputStream(file, true);
					} else {
						getLogger().logDebug(getLoggingSource(), "writing to internal " + filename);
						if (!internalFileExists(context, filename)) {
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

	private boolean internalFileExists(Context context, String filename) {
		for (String name : context.fileList()) {
			if (name.equals(filename)) {
				return true;
			}
		}
		return false;
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
