/*
 * RenderedMap.java
 * 
 * Created by demory on Sep 8, 2009, 2:41:14 PM
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

package org.transketch.apps.desktop.movie;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Date;
import org.transketch.util.viewport.MapCoordinates;
import org.transketch.core.network.Line;
import org.transketch.core.network.TSNetwork;

/**
 *
 * @author demory
 */
public class RenderedMap {

  private int x_, y_, width_, height_;
  private String name_;

  private TSNetwork network_;

  private MapCoordinates coords_;

  public RenderedMap(int x, int y, int width, int height, String name, String netFile) {
    x_ = x;
    y_ = y;
    width_ = width;
    height_ = height;
    name_ = name;

    coords_ = new MapCoordinates();
    int margin = 5;
    coords_.updateDim(width-6-margin*2, width-33-margin*2);
    coords_.setOffsets(x+3+margin, y+30+margin);

    Rectangle2D.Double bbox = network_.getBoundingBox();
    if(bbox.getHeight() > bbox.getWidth()) {
      double cx = bbox.getCenterX(), cy = bbox.getCenterY(), d = bbox.getHeight()/2;
      coords_.updateRange(cx-d, cy-d, cx+d, cy+d);
    }
    else {
      double cx = bbox.getCenterX(), cy = bbox.getCenterY(), d = bbox.getWidth()/2;
      coords_.updateRange(cx-d, cy-d, cx+d, cy+d);
    }

  }

  public void renderMap(Graphics2D g2d, Date now) {

    g2d.setColor(Color.gray);
    g2d.fillRect(x_, y_, width_, height_);
    g2d.setColor(Color.white);
    g2d.fillRect(x_+3, y_+30, width_-6, height_-33);
    g2d.setFont(new Font("Arial", Font.BOLD, 24));
    g2d.drawString(name_, x_+3, y_+24);

    for(Line line : network_.getLines()) {
      Date date = line.getOpenDate();
      if(now.after(date)) {
        line.draw(g2d, coords_);
      }
    }
  }

}
