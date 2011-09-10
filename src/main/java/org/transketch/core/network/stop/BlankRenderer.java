/*
 * BlankRenderer.java
 * 
 * Created by demory on Feb 27, 2011, 12:03:23 AM
 * 
 * Copyright (C) 2011 David D. Emory
 * 
 * This file is part of Transit Sketchpad. See <http://www.transketch.org>
 * * for additional information regarding the project.
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
import java.awt.geom.Point2D;
import org.transketch.apps.desktop.TSCanvas;

/**
 *
 * @author demory
 */
public class BlankRenderer extends StopRenderer {

  public BlankRenderer(Stop stop, BlankRendererTemplate template) {
    super(stop, template);
  }

  @Override
  public void drawStop(TSCanvas c) {
  }

  @Override
  public Point2D getLabelOrigin(TSCanvas c) {
    return new Point2D.Double(c.getCoordinates().xToScreen(stop_.getWorldX()), c.getCoordinates().yToScreen(stop_.getWorldY()));
  }

  @Override
  public void drawHighlight(TSCanvas canvas, Color color) {
  }

  @Override
  public boolean containsPoint(TSCanvas c, double wx, double wy) {
    return false;
  }

}
