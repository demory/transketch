/*
 * SetCorridorElbowCommand.java
 * 
 * Created by demory on Feb 6, 2011, 11:13:47 PM
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
import org.transketch.core.network.corridor.Corridor;

/**
 *
 * @author demory
 */
public class SetCorridorElbowAngleCommand extends EditorBasedCommand implements TSAction {

  private Corridor corr_;
  private double oldAngle_, angleR_;

  public SetCorridorElbowAngleCommand(Editor ed, Corridor corr, double angleR) {
    super(ed);
    corr_ = corr;
    angleR_ = angleR;
    oldAngle_ = corr_.getElbowAngle();
  }

  @Override
  public boolean initialize() {
    return (angleR_ >= Math.PI/2 && angleR_ <= (3.0/4.0)*Math.PI);
  }

  public boolean doThis(TranSketch ts) {
    corr_.setElbowAngle(angleR_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    corr_.setElbowAngle(oldAngle_);
    return true;
  }

  public String getName() {
    return "Set Corridor Elbow Angle";
  }

}
