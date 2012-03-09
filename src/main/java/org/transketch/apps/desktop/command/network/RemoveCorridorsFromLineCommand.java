/*
 * RemoveCorridorsFromLineCommand.java
 * 
 * Created by demory on Feb 5, 2011, 7:41:00 PM
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
import org.transketch.apps.desktop.gui.editor.EditorToolbar;
import org.transketch.core.network.corridor.NetworkCorridor;
import org.transketch.core.network.Line;

/**
 *
 * @author demory
 */
public class RemoveCorridorsFromLineCommand extends EditorBasedCommand implements TSAction {

  private Line line_;
  private NetworkCorridor corridor_;

  public RemoveCorridorsFromLineCommand(Editor ed, Line line, NetworkCorridor corridor) {
    super(ed);
    line_ = line;
    corridor_ = corridor;
  }

  @Override
  public boolean initialize() {
    return line_ != null && corridor_ != null && (corridor_ == line_.firstCorridor() || corridor_ == line_.lastCorridor());
  }

  public boolean doThis(TranSketch ts) {
    boolean success = line_.removeCorridor(corridor_);

    if(success) {
      if(ed_.getSelectedLine() == line_ && ed_.getPane().getToolbar().getSelectedAction() == EditorToolbar.ActionType.MODIFY_LINE)
        ed_.getPane().getCanvas().startEditingLine(line_);
      ed_.getDocument().getNetwork().rebundle();
    }

    return success;
  }

  public boolean undoThis(TranSketch ts) {
    boolean success = line_.addCorridor(corridor_);

    if(success) {
      if(ed_.getSelectedLine() == line_ && ed_.getPane().getToolbar().getSelectedAction() == EditorToolbar.ActionType.MODIFY_LINE)
        ed_.getPane().getCanvas().startEditingLine(line_);
      ed_.getDocument().getNetwork().rebundle();
    }

    return success;
  }

  public String getName() {
    return "Remove Corridor From Line";
  }

}
