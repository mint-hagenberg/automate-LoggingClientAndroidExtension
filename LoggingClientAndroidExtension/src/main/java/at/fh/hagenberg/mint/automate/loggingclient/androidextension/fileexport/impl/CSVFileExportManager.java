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

/**
 * An implementation of the file export manager that will export the data to a CSV file.
 */
public class CSVFileExportManager extends FileExportManager {
	private static final char FIELD_SEPARATOR = ',';
	private static final char DATA_SEPARATOR = '\n';
	private static final char WRAP_CHARACTER = '"';

	@Override
	protected void writeToFile(FileOutputStream stream, Object[] objects) throws IOException {
		FileWriter writer = new FileWriter(stream.getFD());
		writeLineHeader(writer);
		writeObjects(writer, objects);
		endLine(writer);
	}

	private void writeObjects(FileWriter writer, Object[] objects) throws IOException {
		for (int i = 0, len = objects.length; i < len; ++i) {
			Object object = objects[i];
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
			if (i + 1 < len) {
				writer.append(FIELD_SEPARATOR);
			}
		}
	}

	private void writeLineHeader(FileWriter writer) throws IOException {
		writer.append(mDeviceId).append(FIELD_SEPARATOR)
				.append(mSessionId.toString()).append(FIELD_SEPARATOR)
				.append(String.valueOf(mSequenceNr)).append(FIELD_SEPARATOR)
				.append(mProjectId).append(FIELD_SEPARATOR)
				.append(mAppVersion).append(FIELD_SEPARATOR);
	}

	private void endLine(FileWriter writer) throws IOException {
		writer.append(DATA_SEPARATOR);
		writer.flush();
	}
}
