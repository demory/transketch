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
public class RoundedRectRenderer extends StopRenderer<FilledBorderedShapeRendererTemplate> {
  private final static Logger logger = Logger.getLogger(RoundedRectRenderer.class);

  private double radius_ = 3, width_, height_, rotR_;

  public RoundedRectRenderer(Stop stop, FilledBorderedShapeRendererTemplate template) {
    super(stop, template);
  }

  @Override
  public void initialize() {
    double width = 0;

    AnchorBasedStop abStop = (AnchorBasedStop) stop_;
    List<Integer> axes = new ArrayList<Integer>(abStop.getAnchorPoint().getBundleAxes());

    //logger.debug("axes size="+axes.size());

    //if(stop_.id_ == 6) System.out.println("rr stop init");
    if(axes.size() == 1) {
      Bundle b1 = abStop.getAnchorPoint().getBundles().get(axes.get(0));
      int w1 = (b1 != null) ? b1.getWidth() : 0;
      Bundle b2 = abStop.getAnchorPoint().getBundles().get(axes.get(0)+180);
      int w2 = (b2 != null) ? b2.getWidth() : 0;
      width_ = Math.max(w1, w2);
      //if(stop_.id_ == 6) System.out.println("rr stop width "+width_);
      height_ = radius_*2;
      rotR_ = -Math.toRadians(axes.get(0)+90);
    }
    else if(axes.size() == 2) {

    }
    else {

    }
  }

  @Override
  public void drawStop(TSCanvas canvas) {
    Graphics2D g2d = canvas.getGraphics2D();
    
    //System.out.println("rr stop "+stop_.id_+", "+stop_.name_);
    //int x = canvas.getCoordinates().xToScreen(stop_.getWorldX());
    //int y = canvas.getCoordinates().yToScreen(stop_.getWorldY());
    int x = stop_.getScreenX(canvas.getCoordinates());
    int y = stop_.getScreenY(canvas.getCoordinates());

    int bWeight = template_.getBorderWeight();

    g2d.translate(x, y);
    g2d.rotate(rotR_);

    g2d.setColor(template_.getBorderColor());
    g2d.fillRoundRect((int) (-width_/2 - bWeight + 0.5f), (int) (-height_/2 - bWeight + 0.5f), (int) width_+2*bWeight, (int) height_+2*bWeight, (int) (radius_+bWeight)*2, (int) (radius_+bWeight)*2);

    g2d.setColor(template_.getFillColor());
    g2d.fillRoundRect((int) (-width_/2 + 0.5f), (int) (-height_/2 + 0.5f), (int) width_, (int) height_, (int) radius_*2, (int) radius_*2);

    g2d.rotate(-rotR_);
    g2d.translate(-x, -y);

  }

  @Override
  public Point2D getLabelOrigin(TSCanvas c) {
    double r2 = width_/2 + 5; // default buffer

    //logger.debug("s.gLA" +stop_.getLabelAngle());
    int sign = stop_.getLabelAngle() < Math.PI/2 || stop_.getLabelAngle() > 3*Math.PI/2 ? -1 : 1;

    /*return new Point2D.Double(c.getCoordinates().xToScreen(stop_.getWorldX())+sign*r2*Math.cos(rotR_),
                              c.getCoordinates().yToScreen(stop_.getWorldY())+sign*r2*Math.sin(rotR_));*/

    return new Point2D.Double(stop_.getScreenX(c.getCoordinates())+sign*r2*Math.cos(rotR_),
                              stop_.getScreenY(c.getCoordinates())+sign*r2*Math.sin(rotR_));
  }

  @Override
  public void drawHighlight(TSCanvas canvas, Color color) {
    Graphics2D g2d = canvas.getGraphics2D();
    int x = canvas.getCoordinates().xToScreen(stop_.getWorldX());
    int y = canvas.getCoordinates().yToScreen(stop_.getWorldY());

    int bWeight = template_.getBorderWeight();

    g2d.translate(x, y);
    g2d.rotate(rotR_);

    g2d.setColor(color);
    g2d.fillRoundRect((int) -width_/2 - bWeight-3, (int) -height_/2 - bWeight-3, (int) width_+2*bWeight+6, (int) height_+2*bWeight+6, (int) (radius_+bWeight+3)*2, (int) (radius_+bWeight+3)*2);

    g2d.rotate(-rotR_);
    g2d.translate(-x, -y);

  }

  @Override
  public boolean containsPoint(TSCanvas c, double wx, double wy) {
    Point2D.Double srcPt = new Point2D.Double(wx -  stop_.getWorldX(), wy - stop_.getWorldY());
    Point2D.Double dstPt = new Point2D.Double();
    AffineTransform at = AffineTransform.getRotateInstance(rotR_);
    at.transform(srcPt, dstPt);
    int bWeight = template_.getBorderWeight();
    return Math.abs(dstPt.x) <= c.getCoordinates().dxToWorld(width_/2+bWeight) &&
           Math.abs(dstPt.y) <= c.getCoordinates().dxToWorld(height_/2+bWeight);
  }

}
