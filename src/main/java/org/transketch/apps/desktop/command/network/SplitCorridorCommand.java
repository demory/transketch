/*
 * SplitCorridorCommand.java
 * 
 * Created by demory on Jan 8, 2011, 11:32:58 PM
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

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.AnchorPoint;
import org.transketch.core.network.corridor.NetworkCorridor;
import org.transketch.core.network.Line;
import org.transketch.core.network.corridor.CorridorModel;
import org.transketch.core.network.corridor.StylizedCorridorModel;

/**
 *
 * @author demory
 */
public class SplitCorridorCommand extends EditorBasedCommand implements TSAction {
  private final static Logger logger = Logger.getLogger(SplitCorridorCommand.class);

  private NetworkCorridor initialCorr_, splitCorr1_, splitCorr2_;
  private AnchorPoint point_;
  private double wx_, wy_;

  private CreateStylizedCorridorCommand subCorr1Cmd_, subCorr2Cmd_;
  private DeleteCorridorCommand delCorrCmd_;
  private CreateAnchorPointCommand anchorPointCmd_;

  public SplitCorridorCommand(Editor ed, NetworkCorridor corr, double wx, double wy) {
    super(ed);
    initialCorr_ = corr;
    wx_ = wx; wy_ = wy;
  }

  public SplitCorridorCommand(Editor ed, double wx, double wy) {
    super(ed);
    wx_ = wx; wy_ = wy;
  }
  
  @Override
  public boolean initialize() {
    if(initialCorr_ == null)
      initialCorr_ = ed_.getDocument().getNetwork().getCorridorAtXY(wx_, wy_,
        ed_.getPane().getCanvas().getClickToleranceW());
    
    if(initialCorr_ == null || initialCorr_.getModel().getType() != CorridorModel.Type.STYLIZED) return false;
    
    return true;
  }
  
  public boolean doThis(TranSketch ts) {

    boolean result = true;
    if(anchorPointCmd_ == null) {
      Point2D pt = initialCorr_.getModel().nearestPoint(wx_, wy_);
      logger.debug(pt);
      anchorPointCmd_ = new CreateAnchorPointCommand(ed_, pt.getX(), pt.getY());
      // TODO: snap to grid
    }

    result = result & anchorPointCmd_.doThis(ts);
    point_ = anchorPointCmd_.getAnchorPoint();

    if(subCorr1Cmd_ == null) {
      double elbowAngle = ((StylizedCorridorModel) initialCorr_.getModel()).getElbowAngle();
      subCorr1Cmd_ = new CreateStylizedCorridorCommand(ed_, initialCorr_.fPoint(), point_, elbowAngle, false);
      subCorr2Cmd_ = new CreateStylizedCorridorCommand(ed_, point_, initialCorr_.tPoint(), elbowAngle, false);
    }

    result = result & subCorr1Cmd_.doThis(ts);
    result = result & subCorr2Cmd_.doThis(ts);
    splitCorr1_ = subCorr1Cmd_.getCorridor();
    splitCorr2_ = subCorr2Cmd_.getCorridor();


    for(Line line : new HashSet<Line>(initialCorr_.getLines())) {
      line.splitCorridor(initialCorr_, splitCorr1_, splitCorr2_);
    }

    if(delCorrCmd_ == null) {
      delCorrCmd_ = new DeleteCorridorCommand(ed_, initialCorr_);
    }

    result = result & delCorrCmd_.doThis(ts);


    return result;
  }

  public boolean undoThis(TranSketch ts) {
    boolean result = true;

    result = result & delCorrCmd_.undoThis(ts);

    Set<Line> affectedLines = new HashSet<Line>();
    affectedLines.addAll(splitCorr1_.getLines());
    affectedLines.addAll(splitCorr2_.getLines());

    for(Line line : affectedLines) {
      line.unsplitCorridor(initialCorr_, splitCorr1_, splitCorr2_);
    }

    result = result & subCorr1Cmd_.undoThis(ts);
    result = result & subCorr2Cmd_.undoThis(ts);

    result = result & anchorPointCmd_.undoThis(ts);

    return result;
  }

  public String getName() {
    return "Split Corridor";
  }

}