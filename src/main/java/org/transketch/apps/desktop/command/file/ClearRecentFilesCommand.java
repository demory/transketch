/*
 * ClearRecentFilesCommand.java
 * 
 * Created by demory on Jan 19, 2011, 10:42:27 PM
 * 
 * Copyright 2008 David D. Emory
 * 
 * This file is part of Transit Sketchpad. See <http://www.transketch.org>
 * for additional information regarding the project.
 * 
 * Transit Sketchpad is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Transit Sketchpad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Transit Sketchpad.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transketch.apps.desktop.command.file;

import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.AbstractCommand;

/**
 *
 * @author demory
 */
public class ClearRecentFilesCommand extends AbstractCommand {

  public boolean doThis(TranSketch ts) {
    ts.getRecentFiles().clear();
    return true;
  }

}
