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

import java.io.IOException;
import java.io.Serializable;

/**
 * Interface for a class that can send a generic packet to the server.
 */
public interface PacketSender {
	/**
	 * Attempts to establish a connection with the given parameters.
	 *
	 * @param host    the server's address. May be an IP address or a hostname.
	 * @param port    the server's port.
	 * @param timeout how long to wait before throwing an IOException.
	 * @throws IOException if an I/O error occurs
	 */
	void connect(String host, int port, int timeout);

	/**
	 * Attempts to close the PacketSender's connection.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	void close();

	/**
	 * @return true if the PacketSender's connection is closed, false otherwise.
	 */
	boolean isClosed();

	/**
	 * Queues a new message for sending.
	 *
	 * @param packet The packet that should be sent.
	 */
	void sendPacket(GenericPacket packet);

	/**
	 * Queues an already serialized message for sending.
	 *
	 * @param s The serialized message that should be sent.
	 */
	void sendSerializedPacket(Serializable s);

	/**
	 * Finished the cache.
	 */
	void finishCache();
}
