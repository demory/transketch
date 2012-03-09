/*
 * Corridor.java
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

package org.transketch.core.network.corridor;

import java.awt.Color;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.apps.desktop.gui.editor.map.Drawable;

/**
 *
 * @author demory
 */
public abstract class Corridor implements Drawable {
  
  protected CorridorModel model_;
  
  public abstract double x1();
  
  public abstract double y1();

  public abstract double x2();

  public abstract double y2();

  public abstract int getID();
  
  public CorridorModel getModel() {
    return model_;
  }
  
  @Override
  public Type getDrawableType() {
    return Type.CORRIDOR;
  }
  
  @Override
  public void draw(TSCanvas canvas) {
    model_.draw(canvas);
  }

  @Override
  public void drawHighlight(TSCanvas canvas, Color color) {
    model_.drawHighlight(canvas, color);
  }
  
}
