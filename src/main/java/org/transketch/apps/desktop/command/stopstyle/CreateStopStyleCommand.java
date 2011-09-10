/*
 * CreateStopStyleCommand.java
 * 
 * Created by demory on Feb 26, 2011, 8:38:32 PM
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

package org.transketch.apps.desktop.command.stopstyle;

import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.stop.StopStyle;

/**
 *
 * @author demory
 */
public class CreateStopStyleCommand extends EditorBasedCommand implements TSAction {

  private StopStyle style_;

  public CreateStopStyleCommand(Editor ed) {
    this(ed, new StopStyle());
  }

  public CreateStopStyleCommand(Editor ed, StopStyle style) {
    super(ed);
    style_ = style;
  }

  public boolean doThis(TranSketch ts) {
    ed_.getDocument().getStopStyles().addStyle(style_);
    ts.getGUI().getControlFrameManager().getStopStylesFrame().updateRows(ed_.getDocument().getStopStyles().getList());
    //ts.getGUI().getControlFrameManager().getStopStylesFrame().addItem(style_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    ed_.getDocument().getStopStyles().removeStyle(style_);
    ts.getGUI().getControlFrameManager().getStopStylesFrame().updateRows(ed_.getDocument().getStopStyles().getList());
    //ts.getGUI().getControlFrameManager().getStopStylesFrame().removeItem(style_);
    return true;
  }

  public String getName() {
    return "Create Stop Style";
  }

}