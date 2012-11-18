/*
 * CircleRenderer2.java
 * 
 * Created by demory on Feb 28, 2011, 10:27:57 AM
 * 
 * Copyright (C) 2011 David D. Emory
 * 
 * This file is part of Transit Sketchpad. See <http://www.transketch.org>
 * for additional information regarding the project.
 * 
 * Transit Sketchpad is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

package org.transketch.core.network.stop;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.core.network.Bundler.Bundle;

/**
 *
 * @author demory
 */
public class CircleRenderer extends ShapeRenderer {

  //private double defaultRadius_ = 0;

  public CircleRenderer() {
    super();
  }

  @Override
  public Type getType() {
    return Type.CIRCLE;
  }
  
  /*
  @Override
  public void initialize() {
    double width = 0;
    for(Bundle b : ((AnchorBasedStop) stop).getAnchorPoint().getBundles().values()) {
      width = Math.max(width, b.getWidth());
    }

    defaultRadius_ = width/2;
  }*/
  
  public double getRadius(Stop stop) {
    double width = 0;
    for(Bundle b : ((AnchorBasedStop) stop).getAnchorPoint().getBundles().values()) {
      width = Math.max(width, b.getWidth());
    }

    return width/2;
  }

  @Override
  public void drawStop(Stop stop, TSCanvas canvas) {
    Graphics2D g2d = canvas.getGraphics2D();

    double rs = canvas.getCoordinates().dxToWorld(getRadius(stop));
    Ellipse2D e = new Ellipse2D.Double(stop.getWorldX()-rs, stop.getWorldY()-rs, rs*2, rs*2);
    Path2D p = new Path2D.Double(e);
    p.transform(canvas.getCoordinates().getScaleTransform());
    p.transform(canvas.getCoordinates().getTranslateTransform());
    
    p.transform(AffineTransform.getTranslateInstance(stop.getScreenOffset().getX(), -stop.getScreenOffset().getY()));
    
    g2d.setColor(getFillColor());
    g2d.fill(p);
    g2d.setColor(getBorderColor());
    g2d.setStroke(new BasicStroke(getBorderWeight()));
    g2d.draw(p);

  }

  @Override
  public Point2D getLabelOrigin(Stop stop, TSCanvas c) {

    double r2 = getRadius(stop) + 4; // default buffer

    return new Point2D.Double(stop.getScreenX(c.getCoordinates()) + stop.getScreenOffset().getX() + r2*Math.cos(-stop.labelAngleR_),
                              stop.getScreenY(c.getCoordinates()) - stop.getScreenOffset().getY() + r2*Math.sin(-stop.labelAngleR_));
  }

  @Override
  public void drawHighlight(Stop stop, TSCanvas canvas, Color color) {
    Graphics2D g2d = canvas.getGraphics2D();
    
    double rs = canvas.getCoordinates().dxToWorld(getRadius(stop) + 5);
    Ellipse2D e = new Ellipse2D.Double(stop.getWorldX() - rs, stop.getWorldY() - rs, rs * 2, rs * 2);
    Path2D p = new Path2D.Double(e);
    p.transform(canvas.getCoordinates().getScaleTransform());
    p.transform(canvas.getCoordinates().getTranslateTransform());

    p.transform(AffineTransform.getTranslateInstance(stop.getScreenOffset().getX(), -stop.getScreenOffset().getY()));

    g2d.setColor(color);
    g2d.fill(p);
  }

  @Override
  public boolean containsPoint(Stop stop, TSCanvas c, double wx, double wy) {
    Point2D pt = new Point2D.Double(c.getCoordinates().xToScreen(wx), c.getCoordinates().yToScreen(wy));
    return (pt.distance(stop.getScreenX(c.getCoordinates()) + stop.getScreenOffset().getX(),
                        stop.getScreenY(c.getCoordinates()) - stop.getScreenOffset().getY()) < getRadius(stop));
  }

  @Override
  public StopRenderer getCopy() {
    CircleRenderer clone = new CircleRenderer();
    copyProperties(clone);
    return clone;
  }  
}


