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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.fileexport.impl;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.fileexport.FileExportManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.name.Id;

/**
 * An implementation of the file export manager that will export the data to a CSV file.
 */
public class CSVFileExportManager extends FileExportManager {
	public static final Id ID = new Id(CSVFileExportManager.class);

	private static final char FIELD_SEPARATOR = ',';
	private static final char DATA_SEPARATOR = '\n';
	private static final char WRAP_CHARACTER = '"';

	@Override
	protected void writeHeaderToFile(FileOutputStream stream, String[] headers) throws IOException {
		FileWriter writer = new FileWriter(stream.getFD());
		writeFileHeader(writer);
		writer.append(FIELD_SEPARATOR);
		writeAllObjects(writer, headers);
		endLine(writer);
	}

	@Override
	protected void writeToFile(FileOutputStream stream, Object[] objects) throws IOException {
		FileWriter writer = new FileWriter(stream.getFD());
		writeLineHeader(writer);
		writer.append(FIELD_SEPARATOR);
		writeAllObjects(writer, objects);
		endLine(writer);
	}

	private void writeObjects(FileWriter writer, Object... objects) throws IOException {
		writeAllObjects(writer, objects);
	}

	private void writeAllObjects(FileWriter writer, Object[] objects) throws IOException {
		for (int i = 0, len = objects.length; i < len; ++i) {
			Object object = objects[i];
			if (object != null) {
				String value = object.toString();
				boolean wrap = false;
				if (object instanceof String) {
					if (value.contains("\"")) {
						wrap = true;
						value = value.replaceAll("\"", "\"\"");
					}
					if (value.contains(",")) {
						wrap = true;
						value = value.replaceAll(",", "\\,");
					}
				}
				if (wrap) {
					writer.append(WRAP_CHARACTER);
				}
				writer.append(value);
				if (wrap) {
					writer.append(WRAP_CHARACTER);
				}
			}
			if (i + 1 < len) {
				writer.append(FIELD_SEPARATOR);
			}
		}
	}

	private void writeFileHeader(FileWriter writer) throws IOException {
		writeObjects(writer, "deviceId", "sessionId", "sequenceNr", "projectId", "appVersion");
	}

	private void writeLineHeader(FileWriter writer) throws IOException {
		writeObjects(writer, mDeviceId, mSessionId, mSequenceNr, mProjectId, mAppVersion);
	}

	private void endLine(FileWriter writer) throws IOException {
		writer.append(DATA_SEPARATOR);
		writer.flush();
	}

	@Override
	public Id getId() {
		return ID;
	}
}
