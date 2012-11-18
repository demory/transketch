/*
 * StopRenderer2.java
 * 
 * Created by demory on Feb 28, 2011, 10:03:11 AM
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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.*;
import org.transketch.apps.desktop.TSCanvas;

/**
 *
 * @author demory
 */
public abstract class StopRenderer {

  // renderer-specific properties
  protected Map<String, RendererProperty> properties_ = new HashMap<String, RendererProperty>();

  public enum Type {
    BLANK(BlankRenderer.class),
    CIRCLE(CircleRenderer.class),
    FIXED_CIRCLE(FixedCircleRenderer.class),
    ROUNDED(RoundedRectRenderer.class);

    Class classObj_;

    Type(Class classObj) {
      classObj_ = classObj;
    }

    public Class getRendererClass() {
      return classObj_;
    }
  }


  public StopRenderer() {
  }

  public abstract Type getType();
  
  //public void rebundled(Stop stop) { }

  public Collection<RendererProperty> getProperties() {
    return properties_.values();
  }
  
  public void addProperty(RendererProperty prop) {
    properties_.put(prop.getKey(), prop);    
  }
  
  protected IntegerProperty createIntegerProperty(String key, String name, int defaultVal) {
    IntegerProperty prop = new IntegerProperty(key, name, defaultVal);
    addProperty(prop);
    return prop;
  }

  protected void setIntegerProperty(String key, int val) {
    ((IntegerProperty) properties_.get(key)).setValue(val);
  }

  protected ColorProperty createColorProperty(String key, String name, Color defaultVal) {
    ColorProperty prop = new ColorProperty(key, name, defaultVal);
    addProperty(prop);
    return prop;
  }  
  
  protected void setColorProperty(String key, Color val) {
    ((ColorProperty) properties_.get(key)).setValue(val);
  }
  
  public abstract void drawStop(Stop stop, TSCanvas c);

  public void drawLabel(Stop stop, TSCanvas c) {
    Graphics2D g2d = c.getGraphics2D();
    g2d.setColor(stop.getLabelStyle().getColor());
    g2d.setFont(stop.getLabelStyle().getFont());
    FontMetrics fm = g2d.getFontMetrics();
    double textWidth = fm.stringWidth(stop.getName());

    Point2D origin = getLabelOrigin(stop, c);

    double drawAngle = stop.labelAngleR_;

    if(stop.labelAngleR_ > Math.PI/2 && stop.labelAngleR_ < 1.5*Math.PI) {
      drawAngle -= Math.PI;
    }


    g2d.rotate(-drawAngle, origin.getX(), origin.getY());
    if(stop.labelAngleR_ > Math.PI/2 && stop.labelAngleR_ < 1.5*Math.PI) {
      g2d.translate(-textWidth, 0);
    }
    double x = origin.getX();
    double y = (origin.getY()+stop.getLabelStyle().getFont().getSize()*.375);
    g2d.drawString(stop.getName(), (int) x, (int) y);

    if(stop.labelAngleR_ > Math.PI/2 && stop.labelAngleR_ < 1.5*Math.PI) {
      g2d.translate(textWidth, 0);
    }
    g2d.rotate(drawAngle, origin.getX(), origin.getY());

  }

  public abstract Point2D getLabelOrigin(Stop stop, TSCanvas c);

  public abstract void drawHighlight(Stop stop, TSCanvas canvas, Color color);

  public abstract boolean containsPoint(Stop stop, TSCanvas c, double wx, double wy);
 
  public abstract StopRenderer getCopy();
}
