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

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import java.io.Serializable;
import java.util.List;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.network.packet.GenericPacket;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Interface for the specific network handlers that will convert the transmission events to serialized data and call the correct methods on the server.
 */
public interface ClientNetworkHandler {
	/**
	 * List of transmission event types the network manager has to listen to and delegate to this handler.
	 *
	 * @return -
	 */
	List<Id> getTransmissionEvents();

	/**
	 * Called to convert a generic packet to a serialized version for sending.
	 *
	 * @param packet -
	 * @return -
	 */
	Serializable convertToSerializable(GenericPacket packet);

	/**
	 * List of thrift event classes that can be sent using this handler.
	 *
	 * @return -
	 */
	List<Class<?>> getThriftEvents();

	/**
	 * Actually send a serializable packet to the thrift server.
	 *
	 * @param transportProtocol -
	 * @param packet            -
	 * @throws TException
	 */
	void sendPacket(TProtocol transportProtocol, Serializable packet) throws TException;
}
