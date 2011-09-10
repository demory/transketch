/*
 * LineMenu.java
 * 
 * Created by demory on Dec 4, 2010, 11:22:36 PM
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.network.SetLineLayerIndexCommand;
import org.transketch.apps.desktop.command.linestyle.SetLineStyleColorsCommand;
import org.transketch.apps.desktop.gui.editor.EditorToolbar.Action;
import org.transketch.core.network.Line;
import org.transketch.util.FPUtil;

/**
 *
 * @author demory
 */
public class LineMenu extends MapContextMenu {

  private Line line_;

  private JCheckBoxMenuItem isBundledItem_;

  public LineMenu(TSInvoker ccr, Editor ed) {
    super("Line", ccr, ed);

    //new Item("Set Style Colors", ItemAction.SET_COLORS);

    new Item("Zoom to Extents").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ed_.getPane().getCanvas().zoomRange(line_.getBoundingBox());
        ed_.getPane().getCanvas().zoom(-.1);
      }
    });

    new Item("Set Style Colors").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker_.doCommand(new SetLineStyleColorsCommand(ed_, line_));
      }
    });

    new Item("Set Layer Index").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker_.doCommand(new SetLineLayerIndexCommand(ed_, line_));
      }
    });

    new Item("Modify Line Corridors").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ed_.getPane().getToolbar().selectAction(Action.MODIFY_LINE);
        ed_.getPane().getCanvas().startEditingLine(line_);
      }
    });

    isBundledItem_ = new JCheckBoxMenuItem("Apply Bundled");
    isBundledItem_.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(isBundledItem_.isSelected()) {
          System.out.println("checked!");
          line_.setBundled(true);
        }
        else {
          System.out.println("unchecked!");
          line_.setBundled(false);
        }
      }
    });
    add(isBundledItem_);

  }

  public void setLine(Line line) {
    line_ = line;
  }

  @Override
  public void show(Component invoker, int x, int y) {
    isBundledItem_.setSelected(line_.isBundled());
    super.show(invoker, x, y);
  }

}
