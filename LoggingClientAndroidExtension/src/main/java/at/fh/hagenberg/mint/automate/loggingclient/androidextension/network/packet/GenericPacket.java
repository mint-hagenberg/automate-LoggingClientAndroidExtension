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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.packet;

import java.util.UUID;

import at.fhhagenberg.mint.automate.loggingclient.javacore.event.Event;

/**
 * Packet that has at least a header as well as a generic event and will be sent to the server.
 */
public class GenericPacket {
	private GenericPacketHeader mHeader;
	private Event mEvent;

	public GenericPacket(String deviceId, UUID sessionId, int sequenceNr, String projectId, String appVersion, Event event) {
		mHeader = new GenericPacketHeader(deviceId, sessionId, sequenceNr, projectId, appVersion);
		mEvent = event;
	}

	/**
	 * @return the header
	 */
	public GenericPacketHeader getHeader() {
		return mHeader;
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(GenericPacketHeader header) {
		mHeader = header;
	}

	/**
	 * @return the event
	 */
	public Event getEvent() {
		return mEvent;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(Event event) {
		mEvent = event;
	}
}
