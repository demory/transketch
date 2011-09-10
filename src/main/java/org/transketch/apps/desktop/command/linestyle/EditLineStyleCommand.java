/*
 * EditLineStyleCommand.java
 * 
 * Created by demory on Jan 22, 2011, 7:15:13 PM
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

package org.transketch.apps.desktop.command.linestyle;

import java.util.List;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.LineStyle;
import org.transketch.core.network.LineStyle.LineStyleAttributes;
import org.transketch.core.network.LineStyleLayer;

/**
 *
 * @author demory
 */
public class EditLineStyleCommand extends EditorBasedCommand implements TSAction {

  private LineStyle style_;
  //private String oldName_ , newName_;
  //private List<LineSubStyle> oldSubStyles_, newSubStyles_;
  private LineStyleAttributes oldAttrs_ , newAttrs_;

  public EditLineStyleCommand(Editor ed, LineStyle style, LineStyleAttributes newAttrs) {
    super(ed);
    style_ = style;
    /*oldName_ = style.getName();
    oldSubStyles_ = style.getSubStyles();

    newName_ = name;
    newSubStyles_ = subStyles;*/
    oldAttrs_ = style.getAttributes();
    newAttrs_ = newAttrs;
  }

  public boolean doThis(TranSketch ts) {
    //style_.setName(newName_);
    //style_.setLayers(newSubStyles_);
    style_.setAttributes(newAttrs_);
    ts.getGUI().getControlFrameManager().getLineStylesFrame().updateRows(ed_.getDocument().getLineStyles().getList());
    ts.getGUI().getControlFrameManager().getLinesFrame().refreshStyles(ed_.getDocument().getLineStyles().getList());
    if(style_.updateActiveSubStyle(ed_.getDocument().getFrame().getCoordinates().getResolution()))
      ed_.getDocument().getNetwork().rebundle();
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    //style_.setName(oldName_);
    //style_.setLayers(oldSubStyles_);
    style_.setAttributes(oldAttrs_);
    ts.getGUI().getControlFrameManager().getLineStylesFrame().updateRows(ed_.getDocument().getLineStyles().getList());
    ts.getGUI().getControlFrameManager().getLinesFrame().refreshStyles(ed_.getDocument().getLineStyles().getList());
    if(style_.updateActiveSubStyle(ed_.getDocument().getFrame().getCoordinates().getResolution()))
      ed_.getDocument().getNetwork().rebundle();
    return true;
  }

  public String getName() {
    return "Edit Line Style";
  }

}
