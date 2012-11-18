/*
 * EditStopStyleCommand.java
 * 
 * Created by demory on Feb 28, 2011, 6:21:35 PM
 * 
 * Copyright (C) 2011 David D. Emory
 * 
 * This file is part of Transit Sketchpad. See <http://www.transketch.org>
 * for additional information regarding the project.
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

import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.core.network.stop.StopRenderer;
import org.transketch.core.network.stop.StopStyle;

/**
 *
 * @author demory
 */
public class EditStopStyleCommand extends EditorBasedCommand implements TSAction {

  private StopStyle style_;
  private String oldName_, newName_;
  //private StopRenderer.Type oldType_, newType_;
  //private StopRendererTemplate oldTemplate_, newTemplate_;
  private StopRenderer oldRenderer_, newRenderer_;
  
  public EditStopStyleCommand(Editor ed, StopStyle style, String name, StopRenderer renderer) {
    super(ed);
    style_ = style;
    oldName_ = style.getName();
    newName_ = name;
    /*oldType_ = style_.getRendererType();
    newType_ = type;
    oldTemplate_ = style_.getTemplate();
    newTemplate_ = template;*/
    oldRenderer_ = style.getRenderer();
    newRenderer_ = renderer;
  }

  public boolean doThis(TranSketch ts) {
    style_.setName(newName_);
    style_.setRenderer(newRenderer_);
    //style_.setTemplate(newTemplate_);
    //ed_.getDocument().getNetwork().updateStopRenderers();
    ts.getGUI().getControlFrameManager().getStopStylesFrame().updateRows(ed_.getDocument().getStopStyles().getList());
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    style_.setName(oldName_);
    style_.setRenderer(oldRenderer_);
    //style_.setTemplate(oldTemplate_);
    //ed_.getDocument().getNetwork().updateStopRenderers();
    ts.getGUI().getControlFrameManager().getStopStylesFrame().updateRows(ed_.getDocument().getStopStyles().getList());
    return true;
  }

  public String getName() {
    return "Edit Stop Style";
  }
}