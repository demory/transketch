/*
 * EditingInstance.java
 * 
 * Created by demory on Nov 21, 2009, 8:58:52 PM
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

package org.transketch.apps.desktop;

import org.transketch.apps.desktop.command.ActionHistory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.transketch.apps.desktop.gui.control.ControlFrameManager;
import org.transketch.apps.desktop.gui.editor.EditorPane;
import org.transketch.apps.desktop.gui.editor.map.Drawable;
import org.transketch.core.network.AnchorPoint;
import org.transketch.core.network.TSNetwork;
import org.transketch.core.network.corridor.NetworkCorridor;
import org.transketch.core.network.Line;
import org.transketch.core.network.stop.Stop;

/**
 *
 * @author demory
 */
public class Editor {

  private TSDocument doc_;

  private EditorPane pane_;
  private ControlFrameManager cfm_;

  // the action history (for undo/redo support)
  private ActionHistory history_;

  private Line selectedLine_;

  private Map<Property, String> props_;

  public enum Property {
    SHOW_ANCHORS,
    SHOW_CORRIDORS,
    SHOW_STOPS,
    SHOW_LINES,
    SHOW_GRID,
    SNAP_TO_GRID,
    USE_ANTIALIASING
  }

  public Editor(TSDocument doc, ActionHistory history, ControlFrameManager cfm) {
    //id_ = id;
    doc_ = doc;
    history_ = history;
    history.setEditor(this);
    cfm_ = cfm;

    props_ = new HashMap<Property, String>();
    initProperties();
  }

  public TSDocument getDocument() {
    return doc_;
  }

  public ActionHistory getHistory() {
    return history_;
  }

  public EditorPane getPane() {
    return pane_;
  }

  public void setPane(EditorPane pane) {
    pane_ = pane;
  }

  public Line getSelectedLine() {
    return selectedLine_;
  }

  public void setSelectedLine(Line selectedLine) {
    setSelectedLine(selectedLine, true);
  }

  public void setSelectedLine(Line selectedLine, boolean updateLinesFrame) {
    if(selectedLine == null && selectedLine_ != null) {
      if(updateLinesFrame) cfm_.getLinesFrame().clearSelection();
      selectedLine_ = null;
    }
    else if(selectedLine != null) {
      selectedLine_ = selectedLine;
      if(updateLinesFrame) cfm_.getLinesFrame().itemSelectedExternally(selectedLine, true); //lineSelected(selectedLine);
      pane_.getCanvas().startEditingLine(selectedLine);
    }
  }

  public String getProperty(Property prop) {
    return props_.get(prop);
  }

  public boolean getBoolProperty(Property prop) {
    return getProperty(prop).equals("true");
  }

  public void setProperty(Property prop, String value) {
    props_.put(prop, value);
  }
  
  public void setBoolProperty(Property prop, boolean bool) {
    props_.put(prop, bool ? "true" : "false");
  }

  private void initProperties() {
    setBoolProperty(Property.SHOW_ANCHORS, true);
    setBoolProperty(Property.SHOW_CORRIDORS, true);
    setBoolProperty(Property.SHOW_LINES, true);
    setBoolProperty(Property.SHOW_STOPS, true);
    setBoolProperty(Property.SHOW_GRID, false);
    setBoolProperty(Property.SNAP_TO_GRID, false);
    setBoolProperty(Property.USE_ANTIALIASING, false);

  }

  public Drawable getDrawableAtXY(double wx, double wy, Set<Drawable.Type> eligibleTypes) {

    TSNetwork net = doc_.getNetwork();
    double tol = pane_.getCanvas().getClickToleranceW();

    // check anchors
    if(eligibleTypes.contains(Drawable.Type.ANCHOR_POINT) && getBoolProperty(Property.SHOW_ANCHORS)) {
      AnchorPoint closest = null;
      double closestDist = Double.MAX_VALUE;
      for(AnchorPoint anchor : net.getAnchorPoints()) {
        double dist = anchor.getPoint2D().distance(wx, wy);
        if(dist <= tol && dist < closestDist) {
          closestDist = dist;
          closest = anchor;
        }
      }
      if(closest != null) return closest;
    }

    // check stops/stations
    if(eligibleTypes.contains(Drawable.Type.STOP) && getBoolProperty(Property.SHOW_STOPS)) {
      for(Stop stop : net.getStops()) {
        if(stop.getRenderer().containsPoint(pane_.getCanvas(), wx, wy)) {
          return stop;
        }
      }
    }


    // check corridors
    tol = pane_.getCanvas().getCoordinates().dxToWorld(2);
    if(eligibleTypes.contains(Drawable.Type.CORRIDOR) && getBoolProperty(Property.SHOW_CORRIDORS)) {
      NetworkCorridor closest = null;
      double closestDist = Double.MAX_VALUE;
      for(NetworkCorridor corr : net.getCorridors()) {
        double dist = corr.getModel().distanceTo(wx, wy); // nearestPoint(wx, wy).distance(wx, wy);
        if(dist <= tol && dist < closestDist) {
          closestDist = dist;
          closest = corr;
        }
      }
      if(closest != null) return closest;
    }

    // check lines
    if(eligibleTypes.contains(Drawable.Type.LINE) && getBoolProperty(Property.SHOW_LINES)) {
      for(Line line : net.getLines()) {
        if(line.containsPoint(pane_.getCanvas(), wx, wy))
          return line;
      }
    }

    return null;
  }


}
