/*
 * ControlFrameManager.java
 * 
 * Created by demory on Nov 28, 2009, 8:21:03 AM
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

package org.transketch.apps.desktop.gui.control;

import java.awt.Point;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.gui.TranSketchGUI;

/**
 *
 * @author demory
 */
public class ControlFrameManager {
  private final static Logger logger = Logger.getLogger(ControlFrameManager.class);

  private TranSketchGUI gui_;

  private SortedSet<ControlFrame> frames_;

  private OutputFrame outputFrame_;
  private LinesFrame linesFrame_;
  private LineStylesFrame lineStylesFrame_;
  private StopStylesFrame stopStylesFrame_;
  
  private Point placementPoint_ = null;

  public ControlFrameManager(TSInvoker invoker, TranSketchGUI gui) {
    gui_ = gui;
    frames_ = new TreeSet<ControlFrame>();
    outputFrame_ = new OutputFrame(invoker, gui, this);
    linesFrame_ = new LinesFrame(invoker, gui, this);
    lineStylesFrame_ = new LineStylesFrame(invoker, gui, this);
    stopStylesFrame_ = new StopStylesFrame(invoker, gui, this);
  }

  protected Point getPlacementPoint(ControlFrame frame) {
    if(placementPoint_ == null)
      placementPoint_ = new Point(gui_.getDesktop().getWidth() - frame.getWidth(), 0);

    logger.debug("dh="+(gui_.getDesktop().getHeight() - placementPoint_.y));
    if(gui_.getDesktop().getHeight() - placementPoint_.y < frame.getHeight())
      placementPoint_.move(placementPoint_.x-frame.getWidth(), 0);
    
    Point toReturn = new Point(placementPoint_);

    placementPoint_.translate(0, frame.getHeight());

    logger.debug("returning "+toReturn);
    return toReturn;
  }

  public void addFrame(ControlFrame frame) {
    frames_.add(frame);
  }
  
  public void documentChanged(TSDocument oldDoc, TSDocument newDoc) {
    for(ControlFrame frame : frames_) frame.documentChanged(oldDoc, newDoc);
  }

  public Collection<ControlFrame> getFrames() {
    return frames_;
  }

  public OutputFrame getOutputFrame() {
    return outputFrame_;
  }

  public LinesFrame getLinesFrame() {
    return linesFrame_;
  }

  public LineStylesFrame getLineStylesFrame() {
    return lineStylesFrame_;
  }

  public StopStylesFrame getStopStylesFrame() {
    return stopStylesFrame_;
  }

  public void resized(int dw, int dh) {
    for(ControlFrame frame : frames_)
      frame.setLocation(frame.getLocation().x+dw, frame.getLocation().y);
  }
}
