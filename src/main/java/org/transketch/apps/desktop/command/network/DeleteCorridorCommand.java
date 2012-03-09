/*
 * DeleteCorridorCommand.java
 * 
 * Created by demory on Jan 8, 2011, 11:07:41 PM
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

import java.util.HashSet;
import java.util.Set;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.corridor.NetworkCorridor;
import org.transketch.core.network.Line;

/**
 *
 * @author demory
 */
public class DeleteCorridorCommand extends EditorBasedCommand implements TSAction {

  private NetworkCorridor corridor_;
  private double wx_, wy_;

  private Set<DeleteLineCommand> deleteLineCommands_;

  public DeleteCorridorCommand(Editor ed, double wx, double wy) {
    super(ed);
    wx_ = wx;
    wy_ = wy;
  }

  public DeleteCorridorCommand(Editor ed, NetworkCorridor corr) {
    super(ed);
    corridor_ = corr;
  }

  @Override
  public boolean initialize() {
    if(corridor_ != null) return true;
    corridor_ = ed_.getDocument().getNetwork().getCorridorAtXY(wx_, wy_,
      ed_.getPane().getCanvas().getClickToleranceW());

    return corridor_ != null;
  }

  public boolean doThis(TranSketch ts) {
    if(corridor_ == null) return false;

    deleteLineCommands_ = new HashSet<DeleteLineCommand>();
    if(corridor_.getLines().size() > 0)
      ts.getGUI().msg("Deleting "+corridor_.getLines().size()+" line"+(deleteLineCommands_.size() > 1 ? "s" : ""));
    for(Line line : corridor_.getLines()) {
      DeleteLineCommand dlc = new DeleteLineCommand(ed_, line);
      dlc.doThis(ts);
      deleteLineCommands_.add(dlc);
    }
    ed_.getDocument().getNetwork().deleteCorridor(corridor_);
    ed_.getPane().getCanvas().deletingDrawable(corridor_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    if(corridor_ == null) return false;

    if(deleteLineCommands_.size() > 0)
      ts.getGUI().msg("Restoring "+deleteLineCommands_.size()+" line"+(deleteLineCommands_.size() > 1 ? "s" : ""));

    for(DeleteLineCommand dlc : deleteLineCommands_)
      dlc.undoThis(ts);

    ed_.getDocument().getNetwork().addCorridor(corridor_);
    return true;
  }

  public String getName() {
    return "Delete Corridor";
  }

}