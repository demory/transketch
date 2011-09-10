/*
 * AnchorPointMenu.java
 * 
 * Created by demory on Apr 23, 2010, 10:09:47 PM
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

package org.transketch.apps.desktop.gui.editor.map;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.network.CreateAnchorBasedStopCommand;
import org.transketch.apps.desktop.command.network.DeleteAnchorPointCommand;
import org.transketch.core.network.AnchorPoint;

/**
 *
 * @author demory
 */
public class AnchorPointMenu extends MapContextMenu {

  private AnchorPoint anchor_;

  public AnchorPointMenu(TSInvoker invoker, Editor ed) {
    super("Anchor Point", invoker, ed);

    new Item("Add Station").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker_.doCommand(new CreateAnchorBasedStopCommand(ed_, anchor_));
      }
    });

    new Item("Merge Anchor Point").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ed_.getPane().getCanvas().mergeStarted(anchor_);
      }
    });

    new Item("Delete Anchor Point").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker_.doCommand(new DeleteAnchorPointCommand(ed_, anchor_));
      }
    });

  }

  public void setAnchorPoint(AnchorPoint anchor) {
    anchor_ = anchor;
  }

}
