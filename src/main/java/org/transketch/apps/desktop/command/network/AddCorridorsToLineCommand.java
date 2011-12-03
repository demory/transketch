/*
 * ModifyLineCommand.java
 * 
 * Created by demory on Jan 21, 2011, 8:52:36 PM
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

import java.util.List;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.apps.desktop.gui.editor.EditorToolbar;
import org.transketch.core.network.corridor.Corridor;
import org.transketch.core.network.Line;

/**
 *
 * @author demory
 */
public class AddCorridorsToLineCommand extends EditorBasedCommand implements TSAction {

  private Line line_;
  private List<Corridor> corridors_;

  private boolean added_;

  public AddCorridorsToLineCommand(Editor ed, Line line, List<Corridor> corridors) {
    super(ed);
    line_ = line;
    corridors_ = corridors;
  }

  /*@Override
  public boolean initialize() {
    if(line_ == null || corr_ == null) {
      return false;
    }
    if(line_.contains(corr_) && corr_ != line_.firstCorridor() && corr_ != line_.lastCorridor()) {
      //ts_.getGUI().msg("Cannot remove selected corridor");
      return false;
    }
    if(line_.getCorridors().size() > 1 &&  !corr_.adjacentTo(line_.startPoint()) && !corr_.adjacentTo(line_.endPoint())) {
      //ts_.getGUI().msg("Invalid corridor selected");
      return false;
    }
    return true;
  }*/

  public boolean doThis(TranSketch ts) {
    if(line_ == null || corridors_ == null || corridors_.size() == 0) return false;

    boolean success = true;
    for(Corridor c : corridors_) success = success & line_.addCorridor(c);

    if(success) {
      if(ed_.getSelectedLine() == line_ && ed_.getPane().getToolbar().getSelectedAction() == EditorToolbar.Action.MODIFY_LINE)
        ed_.getPane().getCanvas().startEditingLine(line_);
      ed_.getDocument().getNetwork().rebundle();
    }
    return success;
  }

  public boolean undoThis(TranSketch ts) {
    if(line_ == null || corridors_ == null || corridors_.size() == 0) return false;

    boolean success = true;
    for(int i = corridors_.size()-1; i >= 0; i--) // iterate backwards through list
      success = success & line_.removeCorridor(corridors_.get(i));

    if(success) {
      if(ed_.getSelectedLine() == line_ && ed_.getPane().getToolbar().getSelectedAction() == EditorToolbar.Action.MODIFY_LINE)
        ed_.getPane().getCanvas().startEditingLine(line_);
      ed_.getDocument().getNetwork().rebundle();
    }
    return success;
  }


  public String getName() {
    return "Add Corridor(s) to Line";
  }

}
