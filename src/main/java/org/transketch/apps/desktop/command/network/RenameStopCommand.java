/*
 * RenameStopCommand.java
 * 
 * Created by demory on Feb 7, 2011, 11:14:16 PM
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

package org.transketch.apps.desktop.command.network;

import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.stop.Stop;

/**
 *
 * @author demory
 */
public class RenameStopCommand extends EditorBasedCommand implements TSAction {

  private Stop stop_;
  private String oldName_, newName_;

  public RenameStopCommand(Editor ed, Stop stop, String name) {
    super(ed);
    stop_ = stop;
    newName_ = name;
  }

  @Override
  public boolean initialize() {
    if(stop_ == null) return false;
    oldName_ = stop_.getName();
    return true;
  }

  public boolean doThis(TranSketch ts) {
    stop_.setName(newName_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    stop_.setName(oldName_);
    return true;
  }

  public String getName() {
    return "Rename Stop";
  }

}
