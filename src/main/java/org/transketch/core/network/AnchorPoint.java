/*
 * AnchorPoint.java
 * 
 * Created by demory on Mar 28, 2009, 12:30:43 PM
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

package org.transketch.core.network;


import java.awt.Color;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.transketch.core.network.Bundler.Bundle;
import org.transketch.core.network.corridor.Corridor;

/**
 *
 * @author demory
 */
public class AnchorPoint extends AbstractAnchorPoint {
  private final static Logger logger = Logger.getLogger(AnchorPoint.class);

  private int id_;
  private Point2D.Double point_;

  private Set<Corridor> corridors_;

  private Map<Integer, Bundle> bundleMap_ = new HashMap<Integer, Bundle>();
  private Set<Integer> bundleAxes_ = new HashSet<Integer>();
  //private Point2D.Double bundleOffset_ = new Point2D.Double(0, 0);
  
  private Set<Point2D.Double> bundleOffsets_ = new HashSet<Point2D.Double>();
  private Point2D.Double offsetCenter_;

  public AnchorPoint(int id, double x, double y) {
    super(Color.BLACK);
    id_ = id;
    point_ = new Point2D.Double(x,y);
    corridors_ = new HashSet<Corridor>();
  }

  public int getID() {
    return id_;
  }

  public double getX() { return point_.x; }

  public double getY() { return point_.y; }

  public void moveTo(double x, double y) {
    point_.x = x;
    point_.y = y;
  }

  public void moveBy(double dx, double dy) {
    point_.x += dx;
    point_.y += dy;
  }

  public void registerCorridor(Corridor c) {
    corridors_.add(c);
  }

  public void unregisterCorridor(Corridor c) {
    corridors_.remove(c);
  }

  public Collection<Corridor> getCorridors() {
    return corridors_;
  }

  public void clearBundlerData() {
    bundleMap_ = new HashMap<Integer, Bundle>();
    bundleAxes_ = new HashSet<Integer>();
    clearBundleOffsets();
  }
  
  public Map<Integer, Bundle> getBundles() {
    return bundleMap_;
  }

  public void setBundles(Map<Integer, Bundle> bundleMap) {
    bundleMap_ = bundleMap;
  }

  public void initBundleAxes() {
    bundleAxes_ = new HashSet<Integer>();
    for(int angle : bundleMap_.keySet()) {
      if(angle >= 180) angle -= 180;
      bundleAxes_.add(angle);
    }
  }

  public Set<Integer> getBundleAxes() {
    return bundleAxes_;
  }

  /*public void applyBundleOffset(double dx, double dy) {
    if(id_ == 3) logger.debug("aBO "+id_+": "+dx+","+dy);
    bundleOffset_.setLocation(bundleOffset_.x + dx, bundleOffset_.y + dy);
  }*/
  
  public void clearBundleOffsets() {
    bundleOffsets_ = new HashSet<Point2D.Double>();
  }

  public void addBundleOffset(Point2D.Double offset) {
    bundleOffsets_.add(offset);
  }
  
  public void computeOffsetCenter() {
    if(bundleOffsets_ == null || bundleOffsets_.size() == 0) {
      offsetCenter_ = new Point2D.Double(0,0);
      return;
    }
    
    double totalX = 0, totalY = 0;
    for(Point2D pt : bundleOffsets_) {
      totalX += pt.getX();
      totalY += pt.getY();
    }
    
    offsetCenter_ = new Point2D.Double(totalX/bundleOffsets_.size(), totalY/bundleOffsets_.size());
  }
  
  public Point2D.Double getOffsetCenter() {
    return offsetCenter_;
  }
  
  /*public Point2D getBundleOffset() {
    return bundleOffset_;
  }*/
  
  public Point2D.Double getPoint2D() {
    return point_;
  }

  public String getXML() {
    return "<point id=\""+id_+"\" x=\""+point_.x+"\" y=\""+point_.y+"\" />\n";
  }

  public JSONObject getJSON() {
    JSONObject json = new JSONObject();
    json.put("id", id_);
    json.put("x", point_.x);
    json.put("y", point_.y);
    return json;
  }
  
  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("#.###");
    return "AnchorPoint "+id_+" ("+df.format(point_.x)+", "+df.format(point_.y)+")";
  }

}
