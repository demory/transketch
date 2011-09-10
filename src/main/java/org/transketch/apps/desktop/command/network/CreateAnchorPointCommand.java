/*
 * CreateAnchorPointCommand.java
 * 
 * Created by demory on Jan 8, 2011, 10:14:40 PM
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

import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.AnchorPoint;

/**
 *
 * @author demory
 */
public class CreateAnchorPointCommand extends EditorBasedCommand implements TSAction {

  private AnchorPoint point_;
  private double x_, y_;

  public CreateAnchorPointCommand(Editor ed, double x, double y) {
    super(ed);
    x_ = x;
    y_ = y;
  }

  public boolean doThis(TranSketch ts) {
    if(point_ == null)
      point_ = new AnchorPoint(ed_.getDocument().getNetwork().newAnchorPointID(), x_, y_);
    ed_.getDocument().getNetwork().addAnchorPoint(point_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    ed_.getDocument().getNetwork().deleteAnchorPoint(point_);
    return true;
  }

  public String getName() {
    return "Create Anchor Point";
  }

  public AnchorPoint getAnchorPoint() {
    return point_;
  }

}
