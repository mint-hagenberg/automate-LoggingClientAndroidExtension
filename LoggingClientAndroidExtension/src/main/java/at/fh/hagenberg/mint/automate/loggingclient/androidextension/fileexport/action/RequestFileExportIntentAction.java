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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.fileexport.action;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.fileexport.FileExportManager;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.Action;
import at.fhhagenberg.mint.automate.loggingclient.javacore.action.EventAction;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.KernelBase;

/**
 * Action to request from the current file export manager to prepare an export file and then open the intent to share the created file via the default android share intent.
 */
public class RequestFileExportIntentAction implements Action {
    @Override
    public void execute() {
        new EventAction(KernelBase.getKernel(), FileExportManager.FILE_EXPORT_REQUESTED_EVENT).execute();
    }
}
