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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.cache.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import at.fhhagenberg.mint.automate.loggingclient.javacore.event.SimpleEvent;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Event with cached packets.
 */
public class CachedPacketEvent extends SimpleEvent {
	public static final Id ID = new Id(CachedPacketEvent.class);

	private List<Serializable> mPackets;

	/**
	 * Creates a new CachedPacketEvent with one cached packet.
	 *
	 * @param packet -
	 */
	public CachedPacketEvent(Serializable packet) {
		super(ID);

		mPackets = new ArrayList<>();
		mPackets.add(packet);
	}

	/**
	 * Creates a new CachedPacketEvent with the given list of packets.
	 *
	 * @param packets -
	 */
	public CachedPacketEvent(Serializable... packets) {
		super(ID);

		mPackets = new ArrayList<>();
		for (Serializable packet : packets) {
			mPackets.add(packet);
		}
	}

	/**
	 * Creates a new CachedPacketEvent with the given list of packets.
	 *
	 * @param packets -
	 */
	public CachedPacketEvent(List<Serializable> packets) {
		super(ID);

		mPackets = packets;
	}

	/**
	 * Returns the cached packets.
	 *
	 * @return -
	 */
	public List<Serializable> getPackets() {
		return mPackets;
	}
}
