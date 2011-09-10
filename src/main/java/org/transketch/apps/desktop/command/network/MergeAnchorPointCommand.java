/*
 * MergeAnchorPointCommand.java
 * 
 * Created by demory on Feb 11, 2011, 8:06:04 PM
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.AnchorPoint;
import org.transketch.core.network.Bundler;
import org.transketch.core.network.TSNetwork;
import org.transketch.core.network.corridor.Corridor;
import org.transketch.core.network.Line;
import org.transketch.core.network.stop.Stop;

/**
 *
 * @author demory
 */
public class MergeAnchorPointCommand extends EditorBasedCommand implements TSAction {

  private AnchorPoint toMerge_, mergeTo_;

  private Set<Corridor> deletedCorridors_, realignedCorridors_;
  private Set<Stop> deletedStops_;
  private Map<Line, Line.AlignmentSnapshot> affectedLines_;

  public MergeAnchorPointCommand(Editor ed, AnchorPoint toMerge, AnchorPoint mergeTo) {
    super(ed);
    toMerge_ = toMerge;
    mergeTo_ = mergeTo;
  }

  @Override
  public boolean initialize() {
    deletedCorridors_ = new HashSet<Corridor>();
    realignedCorridors_ = new HashSet<Corridor>();
    deletedStops_ = new HashSet<Stop>(ed_.getDocument().getNetwork().getStops(toMerge_));
    affectedLines_ = new HashMap<Line, Line.AlignmentSnapshot>();

    for(Corridor corr : ed_.getDocument().getNetwork().incidentCorridors(toMerge_)) {
      if(corr.opposite(toMerge_) == mergeTo_) {
        deletedCorridors_.add(corr);
        for(Line line : corr.getLines()) {
          affectedLines_.put(line, line.getAlignmentSnapshot());
        }
      }
      else
        realignedCorridors_.add(corr);
    }
    return true;
  }

  public boolean doThis(TranSketch ts) {
    TSNetwork net = ed_.getDocument().getNetwork();
    net.deleteAnchorPoint(toMerge_, false);
    for(Corridor corr : deletedCorridors_) {
      //System.out.println("del corr");
      net.deleteCorridor(corr);
      for(Line line : corr.getLines())
        line.removeCorridor(corr, false);
    }
    for(Corridor corr : realignedCorridors_) {
      if(corr.fPoint() == toMerge_)
        net.realignCorridorFrom(corr, mergeTo_); //corr.setFromPoint(mergeTo_);
      else
        net.realignCorridorTo(corr, mergeTo_); //corr.setToPoint(mergeTo_);
    }
    for(Stop stop : deletedStops_)
      net.deleteStop(stop);
    if(!affectedLines_.isEmpty())
      new Bundler(ed_.getDocument().getNetwork());
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    TSNetwork net = ed_.getDocument().getNetwork();
    net.addAnchorPoint(toMerge_);
    for(Corridor corr : deletedCorridors_) {
      net.addCorridor(corr);
    }
    for(Corridor corr : realignedCorridors_) {
      if(corr.fPoint() == mergeTo_)
        net.realignCorridorFrom(corr, toMerge_); //corr.setFromPoint(toMerge_);
      else
        net.realignCorridorTo(corr, toMerge_); //corr.setToPoint(toMerge_);
    }
    for(Stop stop : deletedStops_)
      net.addStop(stop);
    for(Map.Entry<Line, Line.AlignmentSnapshot> entries : affectedLines_.entrySet()) {
      entries.getKey().restoreAlignment(entries.getValue());
    }
    if(!affectedLines_.isEmpty())
      new Bundler(ed_.getDocument().getNetwork());
    return true;
  }

  public String getName() {
    return "Merge Anchor Point";
  }

}
