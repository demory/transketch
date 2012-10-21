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
import java.awt.geom.*;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.core.network.Bundler.Bundle;

/**
 *
 * @author demory
 */
public class CircleRenderer extends StopRenderer<FilledBorderedShapeRendererTemplate> {

  private double radius_ = 0;

  public CircleRenderer(Stop stop, FilledBorderedShapeRendererTemplate template) {
    super(stop, template);
  }

  @Override
  public void initialize() {
    double width = 0;
    for(Bundle b : ((AnchorBasedStop) stop_).getAnchorPoint().getBundles().values()) {
      width = Math.max(width, b.getWidth());
    }

    radius_ = width/2;
  }

  @Override
  public void drawStop(TSCanvas canvas) {
    Graphics2D g2d = canvas.getGraphics2D();

    double rs = canvas.getCoordinates().dxToWorld(radius_);
    Ellipse2D e = new Ellipse2D.Double(stop_.getWorldX()-rs, stop_.getWorldY()-rs, rs*2, rs*2);
    Path2D p = new Path2D.Double(e);
    p.transform(canvas.getCoordinates().getScaleTransform());
    p.transform(canvas.getCoordinates().getTranslateTransform());
    
    Point2D offset = stop_.getScreenOffset();
    if(offset != null) {
      //System.out.println("offsetCenter="+offset);
      p.transform(AffineTransform.getTranslateInstance(offset.getX(), -offset.getY()));
    }
    
    g2d.setColor(template_.getFillColor());
    g2d.fill(p);
    g2d.setColor(template_.getBorderColor());
    g2d.setStroke(new BasicStroke(template_.getBorderWeight()));
    g2d.draw(p);

  }

  @Override
  public Point2D getLabelOrigin(TSCanvas c) {

    double r2 = radius_ + 4; // default buffer

    /*return new Point2D.Double(c.getCoordinates().xToScreen(stop_.getWorldX())+r2*Math.cos(-stop_.labelAngleR_),
                              c.getCoordinates().yToScreen(stop_.getWorldY())+r2*Math.sin(-stop_.labelAngleR_));*/
    
    return new Point2D.Double(stop_.getScreenX(c.getCoordinates()) + stop_.getScreenOffset().getX() + r2*Math.cos(-stop_.labelAngleR_),
                              stop_.getScreenY(c.getCoordinates()) - stop_.getScreenOffset().getY() + r2*Math.sin(-stop_.labelAngleR_));
  }

  @Override
  public void drawHighlight(TSCanvas canvas, Color color) {
    Graphics2D g2d = canvas.getGraphics2D();
    
    double rs = canvas.getCoordinates().dxToWorld(radius_ + 5);
    Ellipse2D e = new Ellipse2D.Double(stop_.getWorldX() - rs, stop_.getWorldY() - rs, rs * 2, rs * 2);
    Path2D p = new Path2D.Double(e);
    p.transform(canvas.getCoordinates().getScaleTransform());
    p.transform(canvas.getCoordinates().getTranslateTransform());

    Point2D offset = stop_.getScreenOffset();
    if (offset != null) {
      p.transform(AffineTransform.getTranslateInstance(offset.getX(), -offset.getY()));
    }

    g2d.setColor(color);
    g2d.fill(p);
  }

  public boolean containsPoint(TSCanvas c, double wx, double wy) {
    Point2D pt = new Point2D.Double(c.getCoordinates().xToScreen(wx), c.getCoordinates().yToScreen(wy));
    //return (pt.distance(c.getCoordinates().xToScreen(stop_.getWorldX()), c.getCoordinates().yToScreen(stop_.getWorldY())) < radius_);
    return (pt.distance(stop_.getScreenX(c.getCoordinates()) + stop_.getScreenOffset().getX(),
                        stop_.getScreenY(c.getCoordinates()) - stop_.getScreenOffset().getY()) < radius_);
  }

}


