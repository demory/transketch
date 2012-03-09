/*
 * FlipCorridorCommand.java
 * 
 * Created by demory on Jan 8, 2011, 11:42:14 PM
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
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.corridor.NetworkCorridor;

/**
 *
 * @author demory
 */
public class FlipCorridorCommand extends EditorBasedCommand implements TSAction {

  private NetworkCorridor corr_;

  public FlipCorridorCommand(Editor ed, NetworkCorridor corr) {
    super(ed);
    corr_ = corr;
  }

  public boolean doThis(TranSketch ts) {
    corr_.flip();
    ed_.getDocument().getNetwork().rebundle();
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    corr_.flip();
    ed_.getDocument().getNetwork().rebundle();
    return true;
  }

  public String getName() {
    return "Flip Corridor";
  }

}