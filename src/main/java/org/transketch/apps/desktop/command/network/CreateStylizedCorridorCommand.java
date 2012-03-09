/*
 * CreateCorridorCommand.java
 * 
 * Created by demory on Jan 8, 2011, 10:32:10 PM
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
import org.transketch.core.network.corridor.NetworkCorridor;
import org.transketch.core.network.corridor.StylizedCorridorModel;

/**
 *
 * @author demory
 */
public class CreateStylizedCorridorCommand extends EditorBasedCommand implements TSAction {

  private NetworkCorridor corridor_;
  private AnchorPoint from_, to_;
  private boolean checkOrientation_;
  private double thetaR_;

  public CreateStylizedCorridorCommand(Editor ed, AnchorPoint from, AnchorPoint to) {
    this(ed, from, to, 3*Math.PI/4, true);
  }

  public CreateStylizedCorridorCommand(Editor ed, AnchorPoint from, AnchorPoint to, double thetaR) {
    this(ed, from, to, thetaR, true);
  }

  public CreateStylizedCorridorCommand(Editor ed, AnchorPoint from, AnchorPoint to, double thetaR, boolean checkOrientation) {
    super(ed);
    from_ = from;
    to_ = to;
    thetaR_ = thetaR;
    checkOrientation_ = checkOrientation;
  }

  public boolean doThis(TranSketch ts) {
    if(corridor_ == null) corridor_ = new NetworkCorridor(ed_.getDocument().getNetwork().newCorridorID(), from_, to_, checkOrientation_);
    
    StylizedCorridorModel scm = new StylizedCorridorModel(corridor_);
    scm.setElbowAngle(thetaR_);
    corridor_.setModel(scm); 
    ed_.getDocument().getNetwork().addCorridor(corridor_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    if(corridor_ == null) return false;

    ed_.getDocument().getNetwork().deleteCorridor(corridor_);
    return true;
  }

  public String getName() {
    return "Create Corridor";
  }

  public NetworkCorridor getCorridor() {
    return corridor_;
  }

}