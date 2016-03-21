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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.time;

import android.os.SystemClock;

import java.util.Date;
import java.util.TimeZone;

import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.ManagerException;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.UpdateableManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;
import at.fhhagenberg.mint.automate.loggingclient.javacore.time.TrustedTime;

/**
 * An actual trusted time implementation that gets the time from an NTP server and caches the time.
 */
public class TrustedTimeManager extends UpdateableManager implements TrustedTime {
	public static final Id ID = new Id(TrustedTimeManager.class);

	private static final String NTP_SERVER = "pool.ntp.org";
	private static final long NTP_TIMEOUT = 20 * 1000;
	private static final long CACHE_TIMEOUT = 60 * 60 * 1000;

	private SntpClient mSntpClient;

	private boolean mHasCache;
	private long mCachedNtpTime;
	private long mCachedNtpElapsedRealtime;
	private long mCachedNtpCertainty;

	public TrustedTimeManager() {
		addDependency(DebugLogManager.ID);
	}

	@Override
	protected void doStart() throws ManagerException {
		super.doStart();

		mSntpClient = new SntpClient();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean forceRefresh() {
		getLogger().logDebug(getLoggingSource(), "forceRefresh() from cache miss");

		if (mSntpClient.requestTime(NTP_SERVER, (int) NTP_TIMEOUT)) {
			mHasCache = true;
			mCachedNtpTime = mSntpClient.getNtpTime();
			mCachedNtpElapsedRealtime = mSntpClient.getNtpTimeReference();
			mCachedNtpCertainty = mSntpClient.getRoundTripTime() / 2;
			getLogger().logInfo(getLoggingSource(),
					"Refreshed NTP time: " + mCachedNtpTime + " Certainty: " + mCachedNtpCertainty);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasCache() {
		return mHasCache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCacheAge() {
		if (mHasCache) {
			return SystemClock.elapsedRealtime() - mCachedNtpElapsedRealtime;
		} else {
			return Long.MAX_VALUE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCacheCertainty() {
		if (mHasCache) {
			return mCachedNtpCertainty;
		} else {
			return Long.MAX_VALUE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long currentTimeMillis() {
		if (!mHasCache) {
			getLogger().logVerbose(getLoggingSource(), "NTP not available, using local time");
			return new Date().getTime();
		}

		// current time is age after the last ntp cache; callers who
		// want fresh values will hit makeAuthoritative() first.
		return mCachedNtpTime + getCacheAge();
	}

	@Override
	public int currentTimeZoneOffsetMillis() {
		return TimeZone.getDefault().getRawOffset();
	}

	/**
	 * Get the cached NTP time.
	 *
	 * @return -
	 */
	public long getCachedNtpTime() {
		return mCachedNtpTime;
	}

	/**
	 * Get the cached NTP time reference.
	 *
	 * @return -
	 */
	public long getCachedNtpTimeReference() {
		return mCachedNtpElapsedRealtime;
	}

	@Override
	public void update() {
		if (getCacheAge() > CACHE_TIMEOUT) {
			forceRefresh();
		}
	}

	@Override
	public Id getId() {
		return ID;
	}
}
