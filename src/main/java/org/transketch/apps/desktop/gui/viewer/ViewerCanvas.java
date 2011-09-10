/*
 * ViewerCanvas.java
 * 
 * Created by demory on Feb 12, 2011, 9:32:39 PM
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

package org.transketch.apps.desktop.gui.viewer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.apps.desktop.gui.TranSketchGUI;
import org.transketch.util.viewport.Viewport;
import org.transketch.core.network.Line;
import org.transketch.core.network.stop.Stop;
import org.transketch.util.viewport.MapCoordinates;

/**
 *
 * @author demory
 */
public class ViewerCanvas extends Viewport {

  public ViewerCanvas(TSDocument doc, TranSketchGUI gui, MapCoordinates coords) {
    super(doc, gui, coords);
  }

  private void initCoords() {
    Rectangle2D bbox = doc_.getNetwork().getBoundingBox();
    zoomRange(bbox);
    //coords_ = new MapCoordinates(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), this.getWidth(), this.getHeight());
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    if(coords_ == null) initCoords();

    g2d_ = (Graphics2D) g;

    // setup antialiasing
    RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2d_.setRenderingHints(renderHints);

    g2d_.setColor(doc_.getBGColor());
    g2d_.fillRect(0, 0, getWidth(), getHeight());

    for(Line line : doc_.getNetwork().getLines())
      if(line.isEnabled())
        line.draw(this);

    for(Stop stop : doc_.getNetwork().getStops())
      stop.draw(this);
  }

}
