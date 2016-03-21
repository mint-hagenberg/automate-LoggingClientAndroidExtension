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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.thrift;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.ClientNetworkHandler;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.cache.event.NetworkConnectionEstablishedEvent;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.event.FailedTransmissionEvent;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.packet.GenericPacket;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.packet.PacketSender;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.EventAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.KernelBase;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Actual implementation of the packet sender interface using the Apache Thrift network protokoll.
 */
public class ThriftPacketSender extends Thread implements PacketSender {
	private static final String TAG = ThriftPacketSender.class.getSimpleName();

	private final List<ClientNetworkHandler> mClientNetworkHandler;
	private final Map<Id, ClientNetworkHandler> mTransmissionEventToHandlerMap;
	private final Map<Class<?>, ClientNetworkHandler> mThriftPacketsToHandlerMap;
	private final DebugLogManager mLogger;
	private final ConcurrentLinkedQueue<Serializable> mPacketQueue;

	private Context mContext;

	private boolean mStop;
	private String mHost;
	private int mPort;
	private int mTimeout;
	private TTransport mThriftSocket;

	public ThriftPacketSender(DebugLogManager logger, List<ClientNetworkHandler> handler, Context context) {
		mLogger = logger;
		mClientNetworkHandler = handler;
		mContext = context;

		mTransmissionEventToHandlerMap = new HashMap<>();
		mThriftPacketsToHandlerMap = new HashMap<>();
		initTransmissionEvents();

		mPacketQueue = new ConcurrentLinkedQueue<>();
		mStop = true;
	}

	private void initTransmissionEvents() {
		for (ClientNetworkHandler handler : mClientNetworkHandler) {
			for (Id event : handler.getTransmissionEvents()) {
				mTransmissionEventToHandlerMap.put(event, handler);
			}

			for (Class event : handler.getThriftEvents()) {
				mThriftPacketsToHandlerMap.put(event, handler);
			}
		}
	}

	@Override
	public void connect(String host, int port, int timeout) {
		mHost = host;
		mPort = port;
		mTimeout = timeout;

		start();
	}

	@Override
	public void close() {
		mStop = true;
	}

	private void doClose() {
		if (mStop && mThriftSocket != null && mThriftSocket.isOpen()) {
			mThriftSocket.close();
		}
	}

	@Override
	public boolean isClosed() {
		return mStop;
	}

	@Override
	public void sendPacket(GenericPacket packet) {
		Serializable s = null;
		Log.i(TAG, "Getting: " + packet.getEvent());

		if (mTransmissionEventToHandlerMap.containsKey(packet.getEvent().getTypeId())) {
			mLogger.logWarning(TAG, "Converting " + packet.getEvent().getClass().getSimpleName() + " event");
			try {
				final ClientNetworkHandler ClientNetworkHandler = mTransmissionEventToHandlerMap.get(packet.getEvent().getTypeId());
				s = ClientNetworkHandler.convertToSerializable(packet);
				Log.i(TAG, "Converted: " + packet.getEvent());
			} catch (Exception e) {
				mLogger.logError(TAG, "Could not convert " + packet.getEvent().getClass().getSimpleName());
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "Unhandled packet event: " + packet.getEvent().getTypeId());
		}

		if (s != null) {
			sendSerializedPacket(s);
		}
	}

	@Override
	public void run() {
		boolean packetLossPossible = false;
		try {
			InetAddress address = InetAddress.getByName(mHost);
			String ip = address.getHostAddress();
			// Open the connection to the server (cannot be done in
			// pre-execute, since pre-execute is called from the UI thread).
			mThriftSocket = new TSocket(ip, mPort, mTimeout);

			TTransport transport = new TFramedTransport(mThriftSocket);
			transport.open();

			// TODO: Use TCompactProtocol to save over-the-wire size?
			TProtocol transportProtocol = new TBinaryProtocol(transport);
			mStop = false;

			// Send an event to the PacketCacheService that the connection has been established.
			new EventAction(KernelBase.getKernel(), new NetworkConnectionEstablishedEvent(true)).execute();

			// Loop while the stop flag isn't set and periodically check if
			// new messages are ready for sending.
			while (!mStop) {
				Serializable packet = null;
				try {
					if (!mPacketQueue.isEmpty()) {
						packet = mPacketQueue.poll();
					}
					if (packet != null) {
						if (mThriftPacketsToHandlerMap.containsKey(packet.getClass())) {
							try {
								mLogger.logWarning(TAG, "Sending " + packet.getClass().getSimpleName());
								mThriftPacketsToHandlerMap.get(packet.getClass()).sendPacket(transportProtocol, packet);
								mLogger.logWarning(TAG, "Success sending " + packet.getClass().getSimpleName());
							} catch (TTransportException ex) {
								// Not that nice but quick and dirty ;)
								try {
									if (hasNetworkConnection()) {
										mThriftSocket.close();
										mThriftSocket.open();
									}
								} catch (Exception ex2) {
									// Ignore for now
								}
							} catch (Exception ex) {
								mLogger.logError(TAG, "Problem sending " + packet.getClass().getSimpleName());
								ex.printStackTrace();
							}
						} else {
							mLogger.logError(TAG, "The packet of type " + packet.getClass().getName() + " is not supported by the Thrift packet sender");
						}
					}
				} catch (Exception e) {
					// Put the packet back into the queue, and continue with
					// serializing the queue by sending it to the
					// PacketCacheService in the finally block.
					mLogger.logError(TAG, "Problem " + e.getMessage());
					Log.i("Thrift", "Problem: " + e.getMessage());
					mPacketQueue.add(packet);
					packetLossPossible = true;
					throw e;
				}
			}
			packetLossPossible = true;
			doClose();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			close();
		} catch (TException e) {
			e.printStackTrace();
			packetLossPossible = true;
			close();
		} finally {
			if (packetLossPossible) {
				finishCache();
			}
		}
	}

	@Override
	public void sendSerializedPacket(Serializable s) {
		if (isClosed()) {
			new EventAction(KernelBase.getKernel(), new FailedTransmissionEvent(s)).execute();
		} else {
			// Queue is thread-safe, no checks or synchronizations needed
			mPacketQueue.offer(s);
		}
	}

	@Override
	public void finishCache() {
		Serializable[] failedPackets = mPacketQueue.toArray(new Serializable[mPacketQueue.size()]);
		new EventAction(KernelBase.getKernel(), new FailedTransmissionEvent(failedPackets)).execute();
	}

	private boolean hasNetworkConnection() {
		if (mContext != null) {
			ConnectivityManager connectivityService = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityService.getActiveNetworkInfo();
			return networkInfo != null && networkInfo.isConnectedOrConnecting() && !networkInfo.isRoaming();
		}
		return false;
	}
}
