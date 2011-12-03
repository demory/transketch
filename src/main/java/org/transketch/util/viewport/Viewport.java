/*
 * Viewport.java
 * 
 * Created by demory on Jan 22, 2011, 7:40:16 PM
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

package org.transketch.util.viewport;

import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.apps.desktop.gui.TranSketchGUI;

/**
 *
 * @author demory
 */
public class Viewport extends JPanel implements TSCanvas, ResolutionListener {
  private final static Logger logger = Logger.getLogger(Viewport.class);

  protected TranSketchGUI gui_;
  protected TSDocument doc_;

  protected final MapCoordinates coords_;
  protected Graphics2D g2d_;

  protected int mx_, my_, mButton_;

  public Viewport(TSDocument doc, TranSketchGUI gui, MapCoordinates coords) {
    doc_ = doc;
    gui_ = gui;
    coords_ = coords;

    coords_.addResolutionListener(this);

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        mx_ = e.getX();
        my_ = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) {
          leftClick(e.getX(), e.getY(), e.getClickCount(), e.isShiftDown(), e.isControlDown());
        } else if (e.getButton() == MouseEvent.BUTTON3) {
          rightClick(e.getX(), e.getY(), e.getClickCount(), e.isShiftDown(), e.isControlDown());
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        mx_ = e.getX();
        my_ = e.getY();
        mButton_ = e.getButton();
        mouseDown();
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        mouseUp();
      }
    });

    addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseMoved(MouseEvent e) {
        Viewport.this.mouseMoved(e.getX(), e.getY(), e.isShiftDown(), e.isControlDown());
      }

      public void mouseDragged(MouseEvent e) {
        if(mButton_ == MouseEvent.BUTTON3) {
          double dx = -coords_.dxToWorld(e.getX() - mx_);
          double dy = coords_.dyToWorld(e.getY() - my_);
          coords_.shiftRange(dx, dy);
          repaint();
        }
        else
          Viewport.this.mouseDragged(e.getX(), e.getY());

        mx_ = e.getX();
        my_ = e.getY();
      }
    });

    addMouseWheelListener(new MouseWheelListener() {

      public void mouseWheelMoved(MouseWheelEvent e) {
        if(e.getWheelRotation() == -1) zoom(.1);
        if(e.getWheelRotation() == 1) zoom(-.1);      }
    });
  }

  public MapCoordinates getCoordinates() {
    return coords_;
  }

  public Graphics2D getGraphics2D() {
    return g2d_;
  }

  // RESOLUTION LISTENER METHODS

  public void resolutionChanged(double reso) {
    if(doc_.getLineStyles().updateAll(reso)) {
      doc_.getNetwork().rebundle();
      gui_.getControlFrameManager().getLineStylesFrame().repaintRows();
    }
  }

  // MOUSE LISTENER METHODS

  // methods for subclasses to (optionally) override to add mouse functionality

  protected void mouseDown() { }

  protected void mouseUp() { }

  protected void mouseMoved(int x, int y, boolean shift, boolean control) { }

  protected void mouseDragged(int x, int y) { }

  protected void leftClick(int mx, int my, int numClicks, boolean shift, boolean control) { }

  protected void rightClick(int mx, int my, int numClicks, boolean shift, boolean control) { }

  // ZOOM/PAN/RESIZE METHODS

  public void resized() {
    //if(coords_ == null) initCoords();
    if(getWidth() == coords_.getWidth() && getHeight() == coords_.getHeight()) return;

    double mx = (coords_.getX2() + coords_.getX1()) / 2, my = (coords_.getY2() + coords_.getY1()) / 2;
    double hx = (coords_.getX2() - coords_.getX1()) / 2, hy = (coords_.getY2() - coords_.getY1()) / 2;
    double xRatio = (double) getWidth() / (double) coords_.getWidth();
    double yRatio = (double) getHeight() / (double) coords_.getHeight();

    double newX1 = mx - hx * xRatio;
    double newX2 = mx + hx * xRatio;
    double newY1 = my - hy * yRatio;
    double newY2 = my + hy * yRatio;

    coords_.updateRange(newX1, newY1, newX2, newY2);
    coords_.updateDim(getWidth(), getHeight());
    repaint();
  }

  public void zoom(double factor) {
    double dx = factor * (coords_.getX2() - coords_.getX1());
    double dy = factor * (coords_.getY2() - coords_.getY1());

    coords_.updateRange(coords_.getX1() + dx,
                        coords_.getY1() + dy,
                        coords_.getX2() - dx,
                        coords_.getY2() - dy);

    repaint();
  }

  private Rectangle2D refRect_;

  public void setReference() {
    refRect_ = new Rectangle2D.Double(coords_.getX1(), coords_.getY1(), coords_.getXRange(), coords_.getYRange());
    //logger.debug("refRect "+refRect_);
  }

  public void zoomRelative(double factor) {
    //logger.debug("f="+factor);
    if(refRect_ == null) return;

    double mult = (factor > 0) ? .5 : 2;
    double dx = factor * refRect_.getWidth()*mult;
    double dy = factor * refRect_.getHeight()*mult;

    coords_.updateRange(refRect_.getMinX() + dx,
                        refRect_.getMinY() + dy,
                        refRect_.getMaxX() - dx,
                        refRect_.getMaxY() - dy);

    repaint();
  }

  public void zoomRange(Rectangle2D rect) {
    zoomRange(rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
  }

  public void zoomRange(double wx1, double wy1, double wx2, double wy2) {
    /*double gx1 = coords_.xToScreen(wx1);
    double gy1 = coords_.yToScreen(wy2);
    double gx2 = coords_.xToScreen(wx2);
    double gy2 = coords_.yToScreen(wy1);*/

    double screenAspect = (double) getWidth() / getHeight();
    double newAspect = (wx2 - wx1) / (wy2 - wy1);


    double x1, y1, x2, y2;
    if (newAspect < screenAspect) { // i.e. more vertical
      y1 = wy1;
      y2 = wy2;
      double mx = (wx1 + wx2) / 2;
      double wxRange = screenAspect * (wy2 - wy1);
      x1 = mx - wxRange / 2;
      x2 = mx + wxRange / 2;
    } else { // more horizontal
      x1 = wx1;
      x2 = wx2;
      double my = (wy1 + wy2) / 2;
      double wyRange = (1 / screenAspect) * (wx2 - wx1);
      y1 = my - wyRange / 2;
      y2 = my + wyRange / 2;
    }
    
    if(coords_.getWidth() == 0) {
      coords_.updateRange(x1, y1, x2, y2);
      coords_.updateDim(getWidth(), getHeight());
    }
    else coords_.updateRange(x1, y1, x2, y2);
    repaint();
  }


}
