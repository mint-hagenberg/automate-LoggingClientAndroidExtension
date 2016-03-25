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
import android.content.pm.PackageInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.kernel.AndroidKernel;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.cache.NetworkCacheManager;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.cache.event.CachedPacketEvent;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.packet.GenericPacket;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.packet.PacketSender;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.thrift.ThriftPacketSender;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.userid.CredentialManager;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.util.PropertiesHelper;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.RegisterEventListenerAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.UnregisterEventListenerAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.Event;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.EventListener;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.EventManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.AbstractManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.KernelListener;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.ManagerException;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Handles the generic sending of packets to the server.
 */
public class NetworkManager extends AbstractManager implements EventListener, KernelListener {
	public static final Id ID = new Id(NetworkManager.class);

	private final Set<Id> mRegisteredTransmissionEvents = new HashSet<>();
	private final List<ClientNetworkHandler> mClientNetworkHandlers = new ArrayList<>();

	private String mProjectId;
	private String mDeviceId;
	private UUID mSessionId;
	private int mSequenceNr;
	private String mAppVersion;
	private PacketSender mSender;

	private CredentialManager mCredentialManager;

	public NetworkManager() {
		addDependency(DebugLogManager.ID);
		addDependency(EventManager.ID);
		addDependency(CredentialManager.ID);
		addDependency(NetworkCacheManager.ID);
	}

	private void registerTransmissionEvent(List<Id> types) {
		mRegisteredTransmissionEvents.addAll(types);
	}

	@Override
	protected void doStart() throws ManagerException {
		super.doStart();

		getKernel().addListener(this);

		Context context = ((AndroidKernel) getKernel()).getContext();
		String serviceHandlerProperty = PropertiesHelper.getProperty(context, "network.handler");
		String[] serviceHandlers = serviceHandlerProperty != null && !serviceHandlerProperty.isEmpty() ? serviceHandlerProperty.split(",") : null;
		if (serviceHandlers != null && serviceHandlers.length > 0) {
			for (String handlerName : serviceHandlers) {
				try {
					Class<?> handlerClass = Class.forName(handlerName);
					ClientNetworkHandler handler = (ClientNetworkHandler) handlerClass.newInstance();
					mClientNetworkHandlers.add(handler);
					registerTransmissionEvent(handler.getTransmissionEvents());
				} catch (Exception ex) {
					getLogger().logDebug(getLoggingSource(), "Could not register client network handler " + handlerName);
					ex.printStackTrace();
				}
			}
		}

		new RegisterEventListenerAction(getKernel(), this, CachedPacketEvent.ID).execute();
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

		String ip = NetworkHelper.getServerIP(context);
		int port = NetworkHelper.getServerThriftPort(context);
		getLogger().logDebug(getLoggingSource(), "Connecting to server at " + ip + ":" + port);
		mSender = new ThriftPacketSender(getLogger(), mClientNetworkHandlers, context);
		mSender.connect(ip, port, 5000);
	}

	@Override
	protected void doStop() {
		getKernel().removeListener(this);

		new UnregisterEventListenerAction(getKernel(), this);
		mSender.finishCache();

		super.doStop();
	}

	@Override
	public void handleEvent(Event event) {
		// Fetch deviceId again, in case it has changed.
		mDeviceId = mCredentialManager.getUserId();
		getLogger().logWarning(getLoggingSource(), "Handle: " + event.getTypeId());

		if (event.isOfType(CachedPacketEvent.ID)) {
			CachedPacketEvent temp = (CachedPacketEvent) event;
			for (Serializable s : temp.getPackets()) {
				mSender.sendSerializedPacket(s);
			}
		} else if (mRegisteredTransmissionEvents.contains(event.getTypeId())) {
			getLogger().logInfo(getLoggingSource(), "Contained : " + event.getTypeId());
			getLogger().logWarning(getLoggingSource(), "Preparing " + event.getClass().getSimpleName() + " to send");
			GenericPacket p = new GenericPacket(mDeviceId, mSessionId, mSequenceNr, mProjectId, mAppVersion, event);
			mSender.sendPacket(p);
			++mSequenceNr;
		}
	}

	@Override
	public void startupFinished() {
	}

	@Override
	public void onPrepareShutdown() {
		try {
			mSender.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onShutdown() {
	}

	@Override
	public Id getId() {
		return ID;
	}
}
