/*
 * SetStopStyleCommand.java
 * 
 * Created by demory on Mar 1, 2011, 1:04:29 AM
 * 
 * Copyright (C) 2011 David D. Emory
 * 
 * This file is part of Transit Sketchpad. See <http://www.transketch.org>
 * for additional information regarding the project.
 * 
 * Transit Sketchpad is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

package org.transketch.apps.desktop.command.stopstyle;

import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.core.network.stop.Stop;
import org.transketch.core.network.stop.StopStyle;

/**
 *
 * @author demory
 */
public class SetStopStyleCommand extends EditorBasedCommand implements TSAction {

  private Stop stop_;
  private StopStyle oldStyle_, newStyle_;

  public SetStopStyleCommand(Editor ed, Stop stop, StopStyle style) {
    super(ed);
    stop_ = stop;
    newStyle_ = style;
    System.out.println("line = "+stop);
    oldStyle_ = stop.getStyle();
  }

  public boolean doThis(TranSketch ts) {
    stop_.setStyle(newStyle_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    stop_.setStyle(oldStyle_);
    return true;
  }

  public String getName() {
    return "Change Stop Style";
  }

}