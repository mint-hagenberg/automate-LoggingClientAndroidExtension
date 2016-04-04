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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.fileexport;

import java.util.List;

import at.fhhagenberg.mint.automate.loggingclient.javacore.event.Event;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * Interface for the specific file export handlers that will convert the transmission events to data for their respective files.
 */
public interface FileExportHandler {
    /**
     * List of transmission event types the file export manager has to listen to and delegate to this handler.
     *
     * @return -
     */
    List<Id> getTransmissionEvents();

    /**
     * Return all filenames that the manager should export in the end.
     *
     * @return -
     */
    List<String> getAllFilenames();

    /**
     * A filename (not a path) that the file export manager will create and manage for this event id.
     *
     * @param eventId -
     * @return -
     */
    String getFilename(Id eventId);

    /**
     * Allows the file to have a header which is written when the file is created.
     *
     * @param id -
     * @return -
     */
    String[] getFileHeader(Id id);

    /**
     * Asks the handler to create an array of (primitive!) objects that will be written to the file.
     *
     * @param event -
     * @return -
     */
    Object[] serialize(Event event);
}
