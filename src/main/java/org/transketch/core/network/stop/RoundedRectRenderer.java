/*
 * RoundedRectRenderer.java
 * 
 * Created by demory on Mar 1, 2011, 9:48:15 PM
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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.core.network.Bundler.Bundle;

/**
 *
 * @author demory
 */
public class RoundedRectRenderer extends ShapeRenderer {
  private final static Logger logger = Logger.getLogger(RoundedRectRenderer.class);

  private double radius_ = 3, width_, height_, rotR_;

  public RoundedRectRenderer() {
    super();
  }

  @Override
  public Type getType() {
    return Type.ROUNDED;
  }
    
  public void initialize(Stop stop) {
    double width = 0;

    AnchorBasedStop abStop = (AnchorBasedStop) stop;
    List<Integer> axes = new ArrayList<Integer>(abStop.getAnchorPoint().getBundleAxes());

    //logger.debug("axes size="+axes.size());

    //if(stop.id_ == 6) System.out.println("rr stop init");
    if(axes.size() == 1) {
      Bundle b1 = abStop.getAnchorPoint().getBundles().get(axes.get(0));
      int w1 = (b1 != null) ? b1.getWidth() : 0;
      Bundle b2 = abStop.getAnchorPoint().getBundles().get(axes.get(0)+180);
      int w2 = (b2 != null) ? b2.getWidth() : 0;
      width_ = Math.max(w1, w2);
      //if(stop.id_ == 6) System.out.println("rr stop width "+width_);
      height_ = radius_*2;
      rotR_ = -Math.toRadians(axes.get(0)+90);
    }
    else if(axes.size() == 2) {

    }
    else {

    }
  }

  @Override
  public void drawStop(Stop stop, TSCanvas canvas) {
    initialize(stop);
    Graphics2D g2d = canvas.getGraphics2D();
    
    double x = stop.getScreenX(canvas.getCoordinates()) + stop.getScreenOffset().getX();
    double y = stop.getScreenY(canvas.getCoordinates()) - stop.getScreenOffset().getY();

    //int bWeight = template_.getBorderWeight();

    g2d.translate(x, y);
    g2d.rotate(rotR_);

    //g2d.setColor(template_.getBorderColor());
    //g2d.fillRoundRect((int) (-width_/2 - bWeight + 0.5f), (int) (-height_/2 - bWeight + 0.5f), (int) width_+2*bWeight, (int) height_+2*bWeight, (int) (radius_+bWeight)*2, (int) (radius_+bWeight)*2);

    g2d.setColor(getFillColor());
    g2d.fillRoundRect((int) (-width_/2 + 0.5f), (int) (-height_/2 + 0.5f), (int) width_, (int) height_, (int) radius_*2, (int) radius_*2);

    g2d.setColor(getBorderColor());
    g2d.setStroke(new BasicStroke(getBorderWeight()));
    g2d.drawRoundRect((int) (-width_/2 + 0.5f), (int) (-height_/2 + 0.5f), (int) width_, (int) height_, (int) radius_*2, (int) radius_*2);

    
    g2d.rotate(-rotR_);
    g2d.translate(-x, -y);

  }

  @Override
  public Point2D getLabelOrigin(Stop stop, TSCanvas c) {
    double r2 = width_/2 + 5; // default buffer

    //logger.debug("s.gLA" +stop.getLabelAngle());
    int sign = stop.getLabelAngle() < Math.PI/2 || stop.getLabelAngle() > 3*Math.PI/2 ? -1 : 1;

    /*return new Point2D.Double(c.getCoordinates().xToScreen(stop.getWorldX())+sign*r2*Math.cos(rotR_),
                              c.getCoordinates().yToScreen(stop.getWorldY())+sign*r2*Math.sin(rotR_));*/

    return new Point2D.Double(stop.getScreenX(c.getCoordinates())+sign*r2*Math.cos(rotR_) + stop.getScreenOffset().getX(),
                              stop.getScreenY(c.getCoordinates())+sign*r2*Math.sin(rotR_) - stop.getScreenOffset().getY());
  }

  @Override
  public void drawHighlight(Stop stop, TSCanvas canvas, Color color) {
    Graphics2D g2d = canvas.getGraphics2D();
    double x = canvas.getCoordinates().xToScreen(stop.getWorldX()) + stop.getScreenOffset().getX();
    double y = canvas.getCoordinates().yToScreen(stop.getWorldY()) - stop.getScreenOffset().getY();

    int bWeight = getBorderWeight();

    g2d.translate(x, y);
    g2d.rotate(rotR_);

    g2d.setColor(color);
    //g2d.fillRoundRect((int) -width_/2 - bWeight-3, (int) -height_/2 - bWeight-3, (int) width_+2*bWeight+6, (int) height_+2*bWeight+6, (int) (radius_+bWeight+3)*2, (int) (radius_+bWeight+3)*2);
    int buffer = 3;
    g2d.fillRoundRect((int) (-width_/2 + 0.5f) - buffer, (int) (-height_/2 + 0.5f) - buffer, (int) width_+buffer*2, (int) height_+buffer*2, (int) (radius_+buffer)*2, (int) (radius_+buffer)*2);

    g2d.rotate(-rotR_);
    g2d.translate(-x, -y);

  }

  @Override
  public boolean containsPoint(Stop stop, TSCanvas c, double wx, double wy) {
    Point2D.Double srcPt = new Point2D.Double(wx - stop.getWorldX() - c.getCoordinates().dxToWorld(stop.getScreenOffset().getX()),
                                              wy - stop.getWorldY() + c.getCoordinates().dyToWorld(stop.getScreenOffset().getY()));
    Point2D.Double dstPt = new Point2D.Double();
    AffineTransform at = AffineTransform.getRotateInstance(rotR_);
    at.transform(srcPt, dstPt);
    int bWeight = getBorderWeight();
    return Math.abs(dstPt.x) <= c.getCoordinates().dxToWorld(width_/2+bWeight) &&
           Math.abs(dstPt.y) <= c.getCoordinates().dxToWorld(height_/2+bWeight);
  }
  
  @Override
  public StopRenderer getCopy() {
    RoundedRectRenderer clone = new RoundedRectRenderer();
    clone.copyProperties(this);
    return clone;
  }    

}
