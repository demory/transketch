/*
 * CorridorMenu.java
 * 
 * Created by demory on Aug 10, 2010, 11:06:44 AM
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
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.network.CreateLineCommand;
import org.transketch.apps.desktop.command.network.DeleteCorridorCommand;
import org.transketch.apps.desktop.command.network.FlipCorridorCommand;
import org.transketch.apps.desktop.command.network.SetCorridorElbowAngleCommand;
import org.transketch.apps.desktop.command.network.SplitCorridorCommand;
import org.transketch.apps.desktop.gui.editor.EditorToolbar.Action;
import org.transketch.core.network.corridor.Corridor;
import org.transketch.util.FPUtil;

/**
 *
 * @author demory
 */
public class CorridorMenu extends MapContextMenu {

  private Corridor corridor_;

  public CorridorMenu(TSInvoker invoker, Editor ed) {
    super("Corridor", invoker, ed);

    new Item("Flip Orientation").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker_.doCommand(new FlipCorridorCommand(ed_, corridor_));
      }
    });


    JMenu elbowAngleSubmenu = new JMenu("Set Elbow Angle");

    new Item("135\u00B0", elbowAngleSubmenu).addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker_.doCommand(new SetCorridorElbowAngleCommand(ed_, corridor_, Math.toRadians(135)));
      }
    });

    new Item("90\u00B0", elbowAngleSubmenu).addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker_.doCommand(new SetCorridorElbowAngleCommand(ed_, corridor_, Math.toRadians(90)));
      }
    });

    new Item("Custom..", elbowAngleSubmenu).addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String str = JOptionPane.showInputDialog("Angle (in degrees, between 90\u00B0 and 135\u00B0):");
        if(FPUtil.isDouble(str))
          invoker_.doCommand(new SetCorridorElbowAngleCommand(ed_, corridor_, Math.toRadians(Double.parseDouble(str))));
      }
    });

    add(elbowAngleSubmenu);

    new Item("Create New Line").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(invoker_.doCommand(new CreateLineCommand(ed_, corridor_))) {
          ed_.getPane().getToolbar().selectAction(Action.MODIFY_LINE);
        }
      }
    });

    new Item("Split Corridor Here").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        double x = ed_.getPane().getCanvas().getLastWX();
        double y = ed_.getPane().getCanvas().getLastWY();
        invoker_.doCommand(new SplitCorridorCommand(ed_, corridor_, x, y));
      }
    });

    new Item("Delete Corridor").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker_.doCommand(new DeleteCorridorCommand(ed_, corridor_));
      }
    });
  }

  public void setCorridor(Corridor c) {
    corridor_ = c;
  }
  
}
