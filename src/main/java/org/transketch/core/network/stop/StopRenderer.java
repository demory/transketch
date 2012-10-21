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
import org.transketch.apps.desktop.TSCanvas;

/**
 *
 * @author demory
 */
public abstract class StopRenderer<T extends StopRendererTemplate> {

  protected Stop stop_;

  protected T template_;
  //protected Class<T> tClass_;

  public enum Type {
    BLANK(BlankRenderer.class, BlankRendererTemplate.class),
    CIRCLE(CircleRenderer.class, FilledBorderedShapeRendererTemplate.class),
    ROUNDED(RoundedRectRenderer.class, FilledBorderedShapeRendererTemplate.class);

    Class classObj_, templateClassObj_;

    Type(Class classObj, Class tClassObj) {
      classObj_ = classObj;
      templateClassObj_ = tClassObj;
    }

    public Class getRendererClass() {
      return classObj_;
    }

    public Class getTemplateClass() {
      return templateClassObj_;
    }
  }


  public StopRenderer(Stop stop, T template) { //, Class<T> tClass) {
    stop_ = stop;
    template_ = template;
    //tClass_ = tClass;
  }

  public void initialize() { }

  public T getTemplate() {
    return template_;
  }

  public abstract void drawStop(TSCanvas c);

  public void drawLabel(TSCanvas c) {
    Graphics2D g2d = c.getGraphics2D();
    g2d.setColor(stop_.getLabelStyle().getColor());
    g2d.setFont(stop_.getLabelStyle().getFont());
    FontMetrics fm = g2d.getFontMetrics();
    double textWidth = fm.stringWidth(stop_.getName());

    Point2D origin = getLabelOrigin(c);

    double drawAngle = stop_.labelAngleR_;

    if(stop_.labelAngleR_ > Math.PI/2 && stop_.labelAngleR_ < 1.5*Math.PI) {
      drawAngle -= Math.PI;
    }


    g2d.rotate(-drawAngle, origin.getX(), origin.getY());
    if(stop_.labelAngleR_ > Math.PI/2 && stop_.labelAngleR_ < 1.5*Math.PI) {
      g2d.translate(-textWidth, 0);
    }
    double x = origin.getX();
    double y = (origin.getY()+stop_.getLabelStyle().getFont().getSize()*.375);
    g2d.drawString(stop_.getName(), (int) x, (int) y);

    if(stop_.labelAngleR_ > Math.PI/2 && stop_.labelAngleR_ < 1.5*Math.PI) {
      g2d.translate(textWidth, 0);
    }
    g2d.rotate(drawAngle, origin.getX(), origin.getY());

  }

  /*public T getDefaultTemplate() {
    try {
      return tClass_.newInstance();
    } catch (InstantiationException ex) {
      Logger.getLogger(StopRenderer2.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(StopRenderer2.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }*/

  public abstract Point2D getLabelOrigin(TSCanvas c);

  public abstract void drawHighlight(TSCanvas canvas, Color color);

  public abstract boolean containsPoint(TSCanvas c, double wx, double wy);
  
}
