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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.debuglogging;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.BasicLogger;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.DebugLogManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.filter.LogFilter;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.formatter.LogFormatter;
import at.fhhagenberg.mint.automate.loggingclient.javacore.debuglogging.formatter.StandardFormatter;

/**
 * Android logger for file debug logging.
 */
@SuppressWarnings("unused")
public class FileLogger extends BasicLogger {
    private static final String LOG_FILENAME = "automate.log";

    /**
     * Name of the file to log into.
     */
    private final String mFilename;

    /**
     * Constructor.
     */
    public FileLogger() {
        this("automate", null, new StandardFormatter());
    }

    /**
     * Constructor with filename and filter.
     *
     * @param filename -
     * @param filter   -
     */
    public FileLogger(String filename, LogFilter filter) {
        this(filename, filter, new StandardFormatter());
    }

    /**
     * Constructor with filename, filter and formatter.
     *
     * @param filename  -
     * @param filter    -
     * @param formatter -
     */
    public FileLogger(String filename, LogFilter filter, LogFormatter formatter) {
        super(filter, formatter);

        @SuppressLint("SimpleDateFormat") Format date = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        mFilename = filename + "_" + date.format(new Date()) + ".log";
    }

    /**
     * Constructor with filter and formatter
     *
     * @param filter    -
     * @param formatter -
     */
    public FileLogger(LogFilter filter, LogFormatter formatter) {
        this("automate", filter, formatter);
    }

    /**
     * Constructor with filter.
     *
     * @param filter -
     */
    public FileLogger(LogFilter filter) {
        this("automate", filter, new StandardFormatter());
    }

    @Override
    public void doLogMessage(DebugLogManager.Priority priority, String source, String message) {
        File logFile = new File(Environment.getExternalStorageDirectory() + "/" + mFilename);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(getFormatter().format(priority, source, message, new Date()));
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doStart(DebugLogManager instance) {
        printHeader(instance);
    }

    @Override
    public void doStop(DebugLogManager instance) {
        printFooter(instance);
    }

    /**
     * Print the header for the given log manager.
     *
     * @param instance -
     */
    private void printHeader(DebugLogManager instance) {
        File logFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + LOG_FILENAME);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append("Logger started (" + new Date() + ")");
            buf.newLine();
            buf.append("Framework Kernel V" + instance.getKernel().getVersion() + " running on "
                    + android.os.Build.VERSION.CODENAME + ", V" + android.os.Build.VERSION.RELEASE + " ("
                    + android.os.Build.VERSION.SDK_INT + ")");
            buf.newLine();
            buf.append("-------------------------------------------------------------------------------");
            buf.newLine();
            buf.flush();

            buf.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print the footer for the given log manager.
     *
     * @param instance -
     */
    private void printFooter(DebugLogManager instance) {
        File logFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + LOG_FILENAME);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append("Logger stopped (" + new Date() + ")");
            buf.newLine();
            buf.newLine();
            buf.flush();

            buf.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doLogMessage(DebugLogManager.Priority priority, String source, Throwable throwable) {
        doLogMessage(priority, source, throwable.getMessage());
    }

    @Override
    public void doLogMessage(DebugLogManager.Priority priority, String source, Object message) {
        doLogMessage(priority, source, message.toString());
    }
}