/*
 * CreateLineCommand.java
 * 
 * Created by demory on Jan 9, 2011, 9:45:13 PM
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
import org.transketch.core.network.Bundler;
import org.transketch.core.network.corridor.NetworkCorridor;
import org.transketch.core.network.Line;

/**
 *
 * @author demory
 */
public class CreateLineCommand extends EditorBasedCommand implements TSAction {

  private Line line_;
  private NetworkCorridor initCorr_;

  private int id_;
  private String name_;

  public CreateLineCommand(Editor ed) {
    super(ed);
    id_ = 0;
  }

  public CreateLineCommand(Editor ed, NetworkCorridor initCorr) {
    super(ed);
    id_ = 0;
    initCorr_ = initCorr;
  }

  @Override
  public boolean initialize() {
    name_ = JOptionPane.showInputDialog("Line name?");
    if(name_ == null) return false;
    
    id_ = ed_.getDocument().getNetwork().newLineID();
    line_ = new Line(id_, name_);
    if(initCorr_ != null)
      if(!line_.addCorridor(initCorr_)) return false;
    
    return true;
  }

  public boolean doThis(TranSketch ts) {
    ed_.getDocument().getNetwork().addLine(line_);
    ts.getGUI().getControlFrameManager().getLinesFrame().addItem(line_);
    ed_.setSelectedLine(line_);
    if(initCorr_ != null) new Bundler(ed_.getDocument().getNetwork());
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    if(line_ == null) return false;

    ed_.getDocument().getNetwork().deleteLine(line_);
    ts.getGUI().getControlFrameManager().getLinesFrame().removeItem(line_);
    ed_.setSelectedLine(null);
    if(initCorr_ != null) new Bundler(ed_.getDocument().getNetwork());
    return true;
  }

  public String getName() {
    return "New Line";
  }

}
