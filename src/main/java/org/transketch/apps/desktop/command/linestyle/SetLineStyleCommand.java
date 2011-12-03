/*
 * SetLineStyleCommand.java
 * 
 * Created by demory on Jan 21, 2011, 10:54:57 PM
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

package org.transketch.apps.desktop.command.linestyle;

import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.Line;
import org.transketch.core.network.LineStyle;

/**
 *
 * @author demory
 */
public class SetLineStyleCommand extends EditorBasedCommand implements TSAction {

  private Line line_;
  private LineStyle oldStyle_, newStyle_;

  public SetLineStyleCommand(Editor ed, Line line, LineStyle style) {
    super(ed);
    line_ = line;
    newStyle_ = style;
    oldStyle_ = line.getStyle();
  }

  public boolean doThis(TranSketch ts) {
    line_.setStyle(newStyle_);
    ts.getGUI().getControlFrameManager().getLinesFrame().updateRow(line_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    line_.setStyle(oldStyle_);
    ts.getGUI().getControlFrameManager().getLinesFrame().updateRow(line_);
    return true;
  }

  public String getName() {
    return "Change Line Style";
  }

}
