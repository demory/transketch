/*
 * ModifyEditorPropertyCommand.java
 * 
 * Created by demory on Jan 22, 2011, 7:31:19 PM
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

package org.transketch.apps.desktop.command.edit;

import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.apps.desktop.gui.MenuBar;

/**
 *
 * @author demory
 */
public class ModifyEditorPropertyCommand extends EditorBasedCommand {

  private Editor.Property prop_;
  private String value_;

  public ModifyEditorPropertyCommand(Editor ed, Editor.Property prop, String value) {
    super(ed);
    prop_ = prop;
    value_ = value;
  }

  public boolean doThis(TranSketch ts) {
    ed_.setProperty(prop_, value_);
    ed_.getPane().getCanvas().repaint();

    switch(prop_) {
      case SHOW_GRID:
        boolean bval = Boolean.parseBoolean(value_);
        ts.getGUI().getTSMenuBar().getShowGridItem().setSelected(bval);
        ts.getActiveEditor().getPane().getToolbar().getShowGridButton().setSelected(bval);
        break;
      case SNAP_TO_GRID:
        bval = Boolean.parseBoolean(value_);
        ts.getGUI().getTSMenuBar().getSnapToGridItem().setSelected(bval);
        ts.getActiveEditor().getPane().getToolbar().getSnapToGridButton().setSelected(bval);
        break;
    }

    return true;
  }

}
