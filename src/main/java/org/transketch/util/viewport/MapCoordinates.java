/*
 * CoordConverter.java
 *
 * Created on April 3, 2007, 6:21 PM
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
package org.transketch.util.viewport;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author demory
 */
public class MapCoordinates {

  private volatile double x1_,  x2_,  y1_,  y2_,  width_,  height_;

  private int xOff_, yOff_;

  private Set<ResolutionListener> resoListeners_ = new HashSet<ResolutionListener>();

  public MapCoordinates() {
    this(0,0,0,0,0,0);
  }

  public MapCoordinates(double x1, double y1, double x2, double y2, double w, double h) {
    x1_ = x1;
    x2_ = x2;
    y1_ = y1;
    y2_ = y2;
    width_ = w;
    height_ = h;

    xOff_ = yOff_ = 0;
  }

  public double getWidth() {
    return width_;
  }

  public double getHeight() {
    return height_;
  }

  public double getX1() {
    return x1_;
  }

  public double getY1() {
    return y1_;
  }

  public double getX2() {
    return x2_;
  }

  public double getY2() {
    return y2_;
  }

  /*public Rectangle2D getRange() {
    return new Rectangle2D.Double(x1_, y1_, x2_ - x1_, y2_ - y1_);
  }*/

  public double getXRange() {
    return x2_ - x1_;
  }

  public double getYRange() {
    return y2_ - y1_;
  }

  public Rectangle.Double getRange() {
    return new Rectangle.Double(x1_, y1_, x2_ - x1_, y2_ - y1_);
  }

  public Point2D.Double getCenter() {
    return new Point2D.Double((x1_ + x2_)/2, (y1_ + y2_)/2);
  }

  public void shiftRange(double dx, double dy) {
    x1_ += dx;
    y1_ += dy;
    x2_ += dx;
    y2_ += dy;
  }

  public void initRange(double x1, double y1, double x2, double y2) {
    x1_ = x1;
    x2_ = x2;
    y1_ = y1;
    y2_ = y2;
  }

  public void updateRange(double x1, double y1, double x2, double y2) {
    x1_ = x1;
    x2_ = x2;
    y1_ = y1;
    y2_ = y2;
    for(ResolutionListener rl : resoListeners_) rl.resolutionChanged(getResolution());
  }

  public void updateDim(double w, double h) {
    width_ = w;
    height_ = h;
  }

  public double xToWorld(double gx) {
    return (gx / width_) * (x2_ - x1_) + x1_;
  }

  public double yToWorld(double gy) {
    return (1.0 - (gy / height_)) * (y2_ - y1_) + y1_;
  }

  public double dxToWorld(double gdx) {
    return (gdx / width_) * (x2_ - x1_);
  }

  public double dyToWorld(double gdy) {
    return (gdy / height_) * (y2_ - y1_);
  }

  public void setOffsets(int x, int y) {
    xOff_ = x;
    yOff_ = y;
  }
  
  public int xToScreen(double wx) {
    double xd = (double) width_ * (wx - x1_) / (x2_ - x1_) + .5;
    return (int) xd +xOff_;
  }

  public int yToScreen(double wy) {
    double yd = (double) height_ - height_ * (wy - y1_) / (y2_ - y1_) + .5;
    return (int) yd + yOff_;
  }

  public int distToScreen(double dist) {
    double ds = width_ * dist / (x2_ - x1_) + .5;
    return (int) ds;
  }

  public double getResolution() {
    return 100*(x2_ - x1_) / width_;
  }


  public AffineTransform getScaleTransform() {
    double sx = width_ / (x2_ - x1_);
    double sy = -height_ / (y2_ - y1_);
    return AffineTransform.getScaleInstance(sx, sy);
  }

  public AffineTransform getTranslateTransform() {
    return AffineTransform.getTranslateInstance(- distToScreen(x1_), distToScreen(y2_));
  }

  public boolean pointNearEdge(Point2D pt, int tolerance) {

    if (!getRange().contains(pt)) {
      return true;
    }
    int xdist = distToScreen(Math.min(Math.abs(pt.getX() - x1_), Math.abs(pt.getX() - x2_)));
    int ydist = distToScreen(Math.min(Math.abs(pt.getY() - y1_), Math.abs(pt.getY() - y2_)));
    return xdist <= tolerance || ydist <= tolerance;

  }

  public void addResolutionListener(ResolutionListener rl) {
    resoListeners_.add(rl);
  }
}
