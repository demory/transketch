/*
 * PreviewCorridor.java
 * 
 * Created by demory on Jan 28, 2011, 10:13:22 PM
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
import java.awt.Color;
import java.awt.geom.Point2D;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.core.network.LineStyleLayer;

/**
 *
 * @author demory
 */
public class PreviewCorridor extends AbstractCorridor {

  private Point2D.Double fPoint_, tPoint_;
  private Color color_;
  private boolean reversed_ = false;

  public PreviewCorridor(Point2D.Double fPoint, Point2D.Double tPoint, Color color) {
    fPoint_ = fPoint;
    tPoint_ = tPoint;
    color_ = color;
    updateGeometry();
  }

  @Override
  public double x1() {
    return fPoint_.x;
  }

  @Override
  public double y1() {
    return fPoint_.y;
  }

  @Override
  public double x2() {
    return tPoint_.x;
  }

  @Override
  public double y2() {
    return tPoint_.y;
  }

  @Override
  public void draw(TSCanvas canvas) {
    draw(canvas, new LineStyleLayer(2, color_, new float[] { 2, 2 } ));
  }

  public void flip() {
    Point2D.Double pt = fPoint_;
    fPoint_ = tPoint_;
    tPoint_ = pt;
    updateGeometry();
  }

  public void setReversed(boolean r) {
    reversed_ = r;
  }

  public void setFPoint(Point2D.Double fPoint) {
    fPoint_ = fPoint;
  }

  public void setTPoint(Point2D.Double tPoint) {
    tPoint_ = tPoint;
  }
}
