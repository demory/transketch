/*
 * DeleteAnchorPointCommand.java
 * 
 * Created by demory on Jan 8, 2011, 10:23:02 PM
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
import org.transketch.core.network.AnchorPoint;
import org.transketch.core.network.corridor.NetworkCorridor;

/**
 *
 * @author demory
 */
public class DeleteAnchorPointCommand extends EditorBasedCommand implements TSAction {

  private AnchorPoint point_;
  private double x_, y_;

  private Set<DeleteCorridorCommand> delCorrCommands_;

  public DeleteAnchorPointCommand(Editor ed, double x, double y) {
    super(ed);
    x_ = x;
    y_ = y;
  }

  public DeleteAnchorPointCommand(Editor ed, AnchorPoint point) {
    super(ed);
    point_ = point;
  }

  @Override
  public boolean initialize() {

    if(point_ == null)
      point_ = ed_.getDocument().getNetwork().getPointAtXY(x_, y_, ed_.getPane().getCanvas().getClickToleranceW());

    if(point_ == null) return false;

    delCorrCommands_ = new HashSet<DeleteCorridorCommand>();
    for(NetworkCorridor corr : ed_.getDocument().getNetwork().incidentCorridors(point_))
        delCorrCommands_.add(new DeleteCorridorCommand(ed_, corr));

    ed_.getPane().getCanvas().deletingDrawable(point_);
    return true;
  }

  public boolean doThis(TranSketch ts) {
    for(DeleteCorridorCommand dcc : delCorrCommands_) dcc.doThis(ts);
    ed_.getDocument().getNetwork().deleteAnchorPoint(point_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    ed_.getDocument().getNetwork().addAnchorPoint(point_);
    for(DeleteCorridorCommand dcc : delCorrCommands_) dcc.undoThis(ts);
    return true;
  }

  public String getName() {
    return "Delete Anchor Point";
  }

}
