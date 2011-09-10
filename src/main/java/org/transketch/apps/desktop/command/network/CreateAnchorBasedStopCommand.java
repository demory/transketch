/*
 * CreateAnchorBasedStopCommand.java
 * 
 * Created by demory on Jan 21, 2011, 10:20:50 PM
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

package org.transketch.apps.desktop.command.network;

import javax.swing.JOptionPane;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.AnchorPoint;
import org.transketch.core.network.stop.AnchorBasedStop;

/**
 *
 * @author demory
 */
public class CreateAnchorBasedStopCommand extends EditorBasedCommand implements TSAction {

  private AnchorPoint anchor_;
  private AnchorBasedStop stop_;

  public CreateAnchorBasedStopCommand(Editor ed, AnchorPoint anchor) {
    super(ed);
    anchor_ = anchor;
  }

  @Override
  public boolean initialize() {
    String name = JOptionPane.showInputDialog(ed_.getPane(), "Station name?");
    if(name == null) return false;
    stop_ = new AnchorBasedStop(ed_.getDocument().getNetwork().newStopID(), anchor_, name);
    return true;
  }


  public boolean doThis(TranSketch ts) {
    // add the stop
    ed_.getDocument().getNetwork().addStop(stop_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    ed_.getDocument().getNetwork().deleteStop(stop_);
    return true;
  }

  public String getName() {
    return "Create Anchor-based Stop";
  }

}
