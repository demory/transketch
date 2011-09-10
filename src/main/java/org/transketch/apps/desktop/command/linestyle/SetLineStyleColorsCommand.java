/*
 * SetLineStyleColorsCommand.java
 * 
 * Created by demory on Jan 21, 2011, 10:58:24 PM
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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.apps.desktop.gui.editor.SetLineStyleColorsDialog;
import org.transketch.core.network.Line;

/**
 *
 * @author demory
 */
public class SetLineStyleColorsCommand extends EditorBasedCommand implements TSAction {

  private Line line_;

  private Map<String, Color> oldColors_, newColors_;

  public SetLineStyleColorsCommand(Editor ed, Line line) {
    super(ed);
    line_ = line;
  }

  @Override
  public boolean initialize() {
    SetLineStyleColorsDialog dialog = new SetLineStyleColorsDialog(null, line_);
    if(!dialog.okPressed()) return false;

    newColors_ = dialog.getColorMap();
    oldColors_ = new HashMap<String, Color>();
    for(String key : line_.getStyle().getColorKeys())
      oldColors_.put(key, line_.getStyleColor(key));

    return true;
  }

  public boolean doThis(TranSketch ts) {
    for(Map.Entry<String, Color> entry : newColors_.entrySet())
      line_.setStyleColor(entry.getKey(), entry.getValue());
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    for(Map.Entry<String, Color> entry : oldColors_.entrySet())
      line_.setStyleColor(entry.getKey(), entry.getValue());
    return true;
  }

  public String getName() {
    return "Set Line Style Colors";
  }

}
