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

import at.fhhagenberg.mint.automate.loggingclient.javacore.event.SimpleEvent;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Information that the network connection was established or lost.
 */
public class NetworkConnectionEstablishedEvent extends SimpleEvent {
	public static final Id ID = new Id(NetworkConnectionEstablishedEvent.class);

	private boolean mConnectionEstablished;

	public NetworkConnectionEstablishedEvent(boolean connectionEstablished) {
		super(ID);
		mConnectionEstablished = connectionEstablished;
	}

	/**
	 * Returns if the connection was established or not.
	 *
	 * @return -
	 */
	public boolean isConnectionEstablished() {
		return mConnectionEstablished;
	}
}
