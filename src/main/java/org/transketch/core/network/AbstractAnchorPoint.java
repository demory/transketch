/*
 * AbstractAnchorPoint.java
 * 
 * Created by demory on Jan 30, 2011, 9:36:58 PM
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.apps.desktop.gui.editor.map.Drawable;

/**
 *
 * @author demory
 */
public abstract class AbstractAnchorPoint implements Drawable {

  private Color color_;

  public AbstractAnchorPoint(Color color) {
    color_ = color;
  }

  public abstract double getX();

  public abstract double getY();

  public Type getDrawableType() {
    return Type.ANCHOR_POINT;
  }

  public void draw(TSCanvas canvas) {
    Graphics2D g2d = canvas.getGraphics2D();

    int gx = canvas.getCoordinates().xToScreen(getX());
    int gy = canvas.getCoordinates().yToScreen(getY());

    g2d.setColor(color_);
    g2d.setStroke(new BasicStroke(2));

    g2d.drawLine(gx-4, gy, gx+4, gy);
    g2d.drawLine(gx, gy-4, gx, gy+4);

  }

  public void drawHighlight(TSCanvas canvas, Color color) {
    Graphics2D g2d = canvas.getGraphics2D();

    int gx = canvas.getCoordinates().xToScreen(getX());
    int gy = canvas.getCoordinates().yToScreen(getY());

    g2d.setColor(color);
    g2d.setStroke(new BasicStroke(6));

    g2d.drawLine(gx-4, gy, gx+4, gy);
    g2d.drawLine(gx, gy-4, gx, gy+4);
  }

}
