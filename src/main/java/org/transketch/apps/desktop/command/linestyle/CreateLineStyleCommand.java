/*
 * CreateLineStyleCommand.java
 * 
 * Created by demory on Jan 22, 2011, 6:53:13 PM
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

import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.LineStyle;

/**
 *
 * @author demory
 */
public class CreateLineStyleCommand extends EditorBasedCommand implements TSAction {

  private LineStyle style_;

  public CreateLineStyleCommand(Editor ed) {
    this(ed, new LineStyle());
  }

  public CreateLineStyleCommand(Editor ed, LineStyle style) {
    super(ed);
    style_ = style;
  }

  public boolean doThis(TranSketch ts) {
    ed_.getDocument().getLineStyles().addStyle(style_);
    ts.getGUI().getControlFrameManager().getLineStylesFrame().addItem(style_);
    ts.getGUI().getControlFrameManager().getLinesFrame().refreshStyles(ed_.getDocument().getLineStyles().getList());
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    ed_.getDocument().getLineStyles().removeStyle(style_);
    ts.getGUI().getControlFrameManager().getLineStylesFrame().removeItem(style_);
    ts.getGUI().getControlFrameManager().getLinesFrame().refreshStyles(ed_.getDocument().getLineStyles().getList());
    return true;
  }

  public String getName() {
    return "Create Line Style";
  }

}
