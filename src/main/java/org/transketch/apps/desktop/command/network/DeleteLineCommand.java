/*
 * DeleteLineCommand.java
 * 
 * Created by demory on Jan 9, 2011, 10:05:10 PM
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
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.apps.desktop.Editor;
import org.transketch.core.network.Line;

/**
 *
 * @author demory
 */
public class DeleteLineCommand extends EditorBasedCommand implements TSAction {

  private Line line_;

  public DeleteLineCommand(Editor ed, Line line) {
    super(ed);
    line_ = line;
  }

  public boolean doThis(TranSketch ts) {
    ed_.getDocument().getNetwork().deleteLine(line_);
    ts.getGUI().getControlFrameManager().getLinesFrame().removeItem(line_);
    ed_.getPane().getCanvas().deletingDrawable(line_);
    ed_.getDocument().getNetwork().rebundle();
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    ed_.getDocument().getNetwork().addLine(line_);
    ts.getGUI().getControlFrameManager().getLinesFrame().addItem(line_);
    ed_.getDocument().getNetwork().rebundle();
    return true;
  }

  public String getName() {
    return "Delete Line";
  }

}