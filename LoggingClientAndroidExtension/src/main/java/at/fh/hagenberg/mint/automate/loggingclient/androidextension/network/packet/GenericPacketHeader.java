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

/**
 * The header that will be sent via the network.
 */
public class GenericPacketHeader {
	private static final String PLATFORM = "android";

	private String mProjectId;
	private String mDeviceId;
	private UUID mSessionId;
	private int mSequenceNr;

	private String mAppVersion;

	public GenericPacketHeader(String deviceId, UUID sessionId, int sequenceNr, String projectId, String appVersion) {
		mProjectId = projectId;
		mDeviceId = deviceId;
		mSessionId = sessionId;
		mSequenceNr = sequenceNr;
		mAppVersion = appVersion;
	}

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return mDeviceId;
	}

	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		mDeviceId = deviceId;
	}

	/**
	 * @return the sessionId
	 */
	public UUID getSessionId() {
		return mSessionId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(UUID sessionId) {
		mSessionId = sessionId;
	}

	/**
	 * @return the sequenceNr
	 */
	public int getSequenceNr() {
		return mSequenceNr;
	}

	/**
	 * @param sequenceNr the sequenceNr to set
	 */
	public void setSequenceNr(int sequenceNr) {
		mSequenceNr = sequenceNr;
	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return mProjectId;
	}

	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(String projectId) {
		mProjectId = projectId;
	}

	/**
	 * Get the platform.
	 *
	 * @return -
	 */
	public String getPlatform() {
		return PLATFORM;
	}

	/**
	 * Get the app version.
	 *
	 * @return -
	 */
	public String getAppVersion() {
		return mAppVersion;
	}
}
