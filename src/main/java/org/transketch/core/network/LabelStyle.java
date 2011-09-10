/*
 * LabelStyle.java
 * 
 * Created by demory on Apr 11, 2010, 2:03:52 PM
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

import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author demory
 */
public class LabelStyle {

  private String name_;

  private Font font_;
  private float angle_;
  private double visibleRes_;
  private Color color_ = Color.BLACK;

  public LabelStyle(String name, Font font, float angle) {
    name_ = name;
    font_ = font;
    angle_ = angle;
  }

  public LabelStyle() {
    //this("Default", new Font(Font.SANS_SERIF, Font.PLAIN, 14), 0);
    this("Default", new Font("Calibri", Font.BOLD, 16), 0);
  }

  public Font getFont() {
    return font_;
  }

  public float getAngle() {
    return angle_;
  }

  public Color getColor() {
    return color_;
  }
}
