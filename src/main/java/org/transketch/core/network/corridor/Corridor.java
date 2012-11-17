/*
 * Corridor.java
 * 
 * Created by demory on Mar 28, 2009, 5:39:30 PM
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

package org.transketch.core.network.corridor;

import org.transketch.core.network.corridor.AbstractCorridor;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.json.simple.JSONObject;
import org.transketch.core.network.AnchorPoint;
import org.transketch.core.network.Line;
import org.transketch.util.FPUtil;
import org.transketch.util.viewport.MapCoordinates;

/**
 *
 * @author demory
 */
public class Corridor extends AbstractCorridor {

  private int id_;
  private AnchorPoint fPoint_, tPoint_;

  private Set<Line> lines_;

  public Corridor(int id, AnchorPoint from, AnchorPoint to, boolean checkOrientation) {
    id_ = id;

    if(checkOrientation)
      setPoints(from, to);
    else {
      fPoint_ = from;
      tPoint_ = to;
    }

    fPoint_.registerCorridor(this);
    tPoint_.registerCorridor(this);

    lines_ = new HashSet<Line>();

    updateGeometry();
  }

  private void setPoints(AnchorPoint from, AnchorPoint to) {
    Collection<Corridor> fCorrs = from.getCorridors();
    if(fCorrs.size() == 1) {
      Corridor adjacent = fCorrs.iterator().next();
      if(adjacent.fPoint() == from) {
        fPoint_ = from; tPoint_ = to;
        return;
      }
      else {
        fPoint_ = to; tPoint_ = from;
        return;
      }
    }

    Collection<Corridor> tCorrs = to.getCorridors();
    if(tCorrs.size() == 1) {
      Corridor adjacent = tCorrs.iterator().next();
      if(adjacent.fPoint() == to) {
        fPoint_ = to; tPoint_ = from;
        return;
      }
      else {
        fPoint_ = from; tPoint_ = to;
        return;
      }
    }


    fPoint_ = from;
    tPoint_ = to;

    updateGeometry();
  }

  public int getID() {
    return id_;
  }

  public AnchorPoint fPoint() {
    return fPoint_;
  }

  public AnchorPoint tPoint() {
    return tPoint_;
  }

  public AnchorPoint opposite(AnchorPoint pt) {
    if(pt == fPoint_) return tPoint_;
    if(pt == tPoint_) return fPoint_;
    return null;
  }

  public void setFromPoint(AnchorPoint fPoint) {
    fPoint_ = fPoint;
    updateGeometry();
  }

  public void setToPoint(AnchorPoint tPoint) {
    tPoint_ = tPoint;
    updateGeometry();
  }

  public double x1() {
    return fPoint_.getX();
  }

  public double y1() {
    return fPoint_.getY();
  }

  public double x2() {
    return tPoint_.getX();
  }

  public double y2() {
    return tPoint_.getY();
  }

  public int getAngle(AnchorPoint pt) {
    if(pt == fPoint_) {
      if(isStraight()) return (int) (Math.toDegrees(FPUtil.getTheta(x2()-x1(), y2()-y1())) + 0.5);
      Point2D.Double e = getElbow();
      return (int) (Math.toDegrees(FPUtil.getTheta(e.x-x1(), e.y-y1())) + 0.5);
    }
    if(pt == tPoint_) {
      if(isStraight()) return (int) (Math.toDegrees(FPUtil.getTheta(x1()-x2(), y1()-y2())) + 0.5);
      Point2D.Double e = getElbow();
      return (int) (Math.toDegrees(FPUtil.getTheta(e.x-x2(), e.y-y2())) + 0.5);
    }
    
    return -1;
  }

  public boolean adjacentTo(Corridor c) {
    return fPoint_ == c.fPoint_ || fPoint_ == c.tPoint_ ||
           tPoint_ == c.fPoint_ || tPoint_ == c.tPoint_;
  }

  public boolean adjacentTo(AnchorPoint anchor) {
    return fPoint_ == anchor || tPoint_ == anchor;
  }

  public Line2D getTangent(AnchorPoint pt, double offset) {
    if(pt == fPoint_) return getFromTangent(offset);
    if(pt == tPoint_) return getToTangent(offset);
    return null;
  }

  public void flip() {
    AnchorPoint pt = fPoint_;
    fPoint_ = tPoint_;
    tPoint_ = pt;
    updateGeometry();
  }

  public void unregisterFromEndpoints() {
    fPoint_.unregisterCorridor(this);
    tPoint_.unregisterCorridor(this);
  }

  public void registerLine(Line line) {
    lines_.add(line);
  }

  public void unregisterLine(Line line) {
    lines_.remove(line);
  }

  public Collection<Line> getLines() {
    return lines_;
  }

  public int getLineBundleWidth() {
    int width = 0;

    for(Line line : lines_)
      width += line.getStyle().getActiveSubStyle().getEnvelope(); //getMaxLayerWidth();

    return width;
  }
  
  public Path2D getPath(AnchorPoint fromPt, int offsetFrom, int offsetTo, Line2D prev, Line2D next, MapCoordinates coords) {
    return getPath(offsetFrom, offsetTo, prev, next, coords, (fromPt != this.fPoint_));
  }

  public String getXML() {
    return "<corridor id=\""+id_+"\" fpoint=\""+fPoint_.getID()+"\" tpoint=\""+tPoint_.getID()+"\" theta=\""+thetaR_+"\" />\n";
  }

  public JSONObject getJSON() {
    JSONObject json = new JSONObject();
    json.put("id", id_);
    json.put("fPoint", fPoint_.getID());
    json.put("tPoint", tPoint_.getID());
    return json;
  }
  
  @Override
  public String toString() {
    return "Corridor "+id_+" (Anchors "+fPoint_.getID()+" to "+tPoint_.getID()+")";
  }

  public boolean sharesEndpointsWith(Corridor corr) {
    return (fPoint_ == corr.fPoint_ && tPoint_ == corr.tPoint()) || (fPoint_ == corr.tPoint_ && tPoint_ == corr.fPoint());
  }
}
