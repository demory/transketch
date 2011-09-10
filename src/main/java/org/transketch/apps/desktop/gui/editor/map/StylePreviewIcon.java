/*
 * StylePreviewIcon.java
 * 
 * Created by demory on Oct 23, 2010, 10:20:13 PM
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

package org.transketch.apps.desktop.gui.editor.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import org.transketch.core.network.Line;
import org.transketch.core.network.LineStyleView;
import org.transketch.core.network.LineStyleLayer;
import org.transketch.core.network.LineSubStyle;

/**
 *
 * @author demory
 */
public class StylePreviewIcon implements Icon {

  private LineStyleView styleInfo_;

  private Color borderColor_;
  private int borderWidth_;
  private boolean antialias_;

  private int width_, height_;

  private Line line_;

  public StylePreviewIcon(LineStyleView style, int width, int height, Color borderColor, int borderWidth, boolean antialias) {
    styleInfo_ = style;
    width_ = width;
    height_ = height;
    borderColor_ = borderColor;
    borderWidth_ = borderWidth;
    antialias_ = antialias;
  }

  public void setStyleInfo(LineStyleView info) {
    styleInfo_ = info;
  }

  public void setLine(Line line) {
    line_ = line;
  }

  public void setAntialias(boolean antialias) {
    antialias_ = antialias;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2d = (Graphics2D) g;
    if (antialias_) {
      RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHints(renderHints);
    }
    g2d.setColor(borderColor_);
    g2d.fillRect(x, y, x + width_, y + height_);
    g2d.setColor(Color.WHITE);
    g2d.fillRect(x + borderWidth_, y + borderWidth_, x + width_ - 2*borderWidth_, y + height_ - 2*borderWidth_);
    List<LineStyleLayer> subStylesCopy = new LinkedList(styleInfo_.getLayers());

    Collections.reverse(subStylesCopy);
    int y2 = height_ / 2;
    for (LineStyleLayer ss : subStylesCopy) {
      g2d.setStroke(ss.getStroke());
      if(line_ != null) g2d.setColor(ss.getColor(line_));
      else g2d.setColor(ss.getColor());
      g2d.drawLine(x + 4, y + y2, x + width_ - 4, y + y2);
    }

    if(LineSubStyle.isKeyBased(subStylesCopy)) {
      g2d.setColor(Color.BLACK);//borderColor_);
      g2d.fillRect(x, y, 7, 5);
      g2d.setColor(Color.GRAY);
      g2d.fillRect(x+1, y+1, 3, 3);
      g2d.setColor(Color.WHITE);
      g2d.fillRect(x+1, y+2, 5, 1);
      g2d.fillRect(x+2, y+1, 1, 3);
      g2d.fillRect(x+5, y+1, 1, 1);
    }
  }

  public int getIconWidth() {
    return width_;
  }

  public int getIconHeight() {
    return height_;
  }

}
