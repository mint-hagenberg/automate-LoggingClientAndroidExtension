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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.cache;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.kernel.AndroidKernel;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.cache.event.CachedPacketEvent;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.cache.event.NetworkConnectionEstablishedEvent;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.event.FailedTransmissionEvent;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.DebugLogAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.EventAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.RegisterEventListenerAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.UnregisterEventListenerAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.Event;
import at.fhhagenberg.mint.automate.loggingclient.javacore.event.EventListener;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.AbstractManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.KernelBase;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.ManagerException;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Manager that can cache events on the local filesystem.
 */
public class NetworkCacheManager extends AbstractManager implements EventListener {
	private static final String TAG = NetworkCacheManager.class.getSimpleName();

	private static final String CACHE_FILENAME = "automatepacketcache";

	public static final Id ID = new Id(NetworkCacheManager.class);

	private Context mContext;

	@Override
	protected void doStart() throws ManagerException {
		super.doStart();

		new RegisterEventListenerAction(getKernel(), this, FailedTransmissionEvent.ID, NetworkConnectionEstablishedEvent.ID)
				.execute();

		mContext = ((AndroidKernel) getKernel()).getContext();
	}

	@Override
	protected void doStop() {
		new UnregisterEventListenerAction(getKernel(), this);

		super.doStop();
	}

	@Override
	public void handleEvent(Event event) {
		if (event.isOfType(FailedTransmissionEvent.ID)) {
			FailedTransmissionEvent temp = (FailedTransmissionEvent) event;
			appendToCache(temp.getPackets());
		} else if (event.isOfType(NetworkConnectionEstablishedEvent.ID)) {
			List<Serializable> thriftPackets = readFromCache();
			clearCache();
			new EventAction(KernelBase.getKernel(), new CachedPacketEvent(thriftPackets)).execute();
		}
	}

	private void clearCache() {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = mContext.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		new DebugLogAction(getKernel(), DebugLogManager.Priority.INFO, TAG, "cleared cache").execute();
		readFromCache();
	}

	private void appendToCache(List<Serializable> packets) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			// Add already existing packets to new cache stream
			packets.addAll(readFromCache());
			fos = mContext.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);

			for (Serializable packet : packets) {
				oos.writeObject(packet);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		new DebugLogAction(getKernel(), DebugLogManager.Priority.INFO, TAG, "amount of packets in cache after writing: " + packets.size()).execute();
		readFromCache();
	}

	private List<Serializable> readFromCache() {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		List<Serializable> packets = new ArrayList<Serializable>();

		try {
			fis = mContext.openFileInput(CACHE_FILENAME);
			ois = new ObjectInputStream(fis);

			// We don't know the amount of objects to read. Therefore we read
			// until there is an exception, and interpret this exception as the
			// end of the stream.
			try {
				//noinspection InfiniteLoopStatement
				while (true) {
					Serializable packet = (Serializable) ois.readObject();
					packets.add(packet);
				}
			} catch (IOException e) {
				// We're done reading. This is normal behaviour.
			}
		} catch (IOException e) {
			// Cache file doesn't exist yet. This is normal behaviour.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ois != null) {
					ois.close();
				}
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				// May happen after reading a non-existant cache file. Normal
				// behaviour.
			}
		}
		new DebugLogAction(getKernel(), DebugLogManager.Priority.INFO, TAG, "amount of packets in cache: " + packets.size()).execute();
		return packets;
	}

	@Override
	public Id getId() {
		return ID;
	}
}
