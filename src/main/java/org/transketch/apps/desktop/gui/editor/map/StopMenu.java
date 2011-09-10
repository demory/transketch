/*
 * StopMenu.java
 * 
 * Created by demory on Nov 8, 2010, 6:25:05 PM
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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.network.DeleteStopCommand;
import org.transketch.apps.desktop.command.network.RenameStopCommand;
import org.transketch.apps.desktop.command.network.SetStopLabelAngleCommand;
import org.transketch.apps.desktop.command.stopstyle.SetStopStyleCommand;
import org.transketch.core.network.stop.Stop;
import org.transketch.core.network.stop.StopStyle;
import org.transketch.util.FPUtil;

/**
 *
 * @author demory
 */
public class StopMenu extends MapContextMenu {

  private Stop stop_;

  private JCheckBoxMenuItem showLabelItem_;

  public StopMenu(TSInvoker ccr, Editor ed) {
    super("Stop/Station", ccr, ed);

    add(new StylesMenu());

    showLabelItem_ = new JCheckBoxMenuItem("Show Label");
    showLabelItem_.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(showLabelItem_.isSelected()) {
          stop_.setShowLabel(true);
          ed_.getPane().getCanvas().repaint();
        }
        else {
          stop_.setShowLabel(false);
          ed_.getPane().getCanvas().repaint();
        }
      }
    });
    add(showLabelItem_);

    JMenu presetAnglesSubmenu = new JMenu("Preset Label Angles");
    
    new PresetAngleMenuItem("Right (0\u00B0)", 0, presetAnglesSubmenu);
    new PresetAngleMenuItem("Top Right (45\u00B0)", 45, presetAnglesSubmenu);
    new PresetAngleMenuItem("Top (90\u00B0)", 90, presetAnglesSubmenu);
    new PresetAngleMenuItem("Top Left (135\u00B0)", 135, presetAnglesSubmenu);
    new PresetAngleMenuItem("Left (180\u00B0)", 180, presetAnglesSubmenu);
    new PresetAngleMenuItem("Bottom Left (225\u00B0)", 225, presetAnglesSubmenu);
    new PresetAngleMenuItem("Bottom (270\u00B0)", 270, presetAnglesSubmenu);
    new PresetAngleMenuItem("Bottom Right (315\u00B0)", 315, presetAnglesSubmenu);

    add(presetAnglesSubmenu);

    new Item("Custom Label Angle").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String str = JOptionPane.showInputDialog("New angle (degrees):");
        if(!FPUtil.isDouble(str)) return;
        double angle = Math.toRadians(Double.parseDouble(str));
        setLabelAngleCommand(angle);
      }
    });

    new Item("Rename").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String str = JOptionPane.showInputDialog("New name:");
        if(str != null) invoker_.doCommand(new RenameStopCommand(ed_, stop_, str));
      }
    });

    new Item("Delete").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker_.doCommand(new DeleteStopCommand(ed_, stop_));
      }
    });

  }

  public void setStop(Stop stop) {
    stop_ = stop;
  }

  private void setLabelAngleCommand(double angle) {
    invoker_.doCommand(new SetStopLabelAngleCommand(ed_, stop_, angle));
  }

  @Override
  public void show(Component invoker, int x, int y) {
    showLabelItem_.setSelected(stop_.getShowLabel());
    super.show(invoker, x, y);
  }


  private class PresetAngleMenuItem extends Item {
    public PresetAngleMenuItem(String title, final double angle, JMenu menu) {
      super(title, menu);
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setLabelAngleCommand(Math.toRadians(angle));
        }
      });
    }
  }

  private class StylesMenu extends JMenu {

    public StylesMenu() {
      super("Select Style:");

      addMenuListener(new MenuListener() {

        public void menuSelected(MenuEvent e) {
          //System.out.println("StopStyle menu selected");
          removeAll();

          JMenuItem item = new JMenuItem("DEFAULT");
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              invoker_.doCommand(new SetStopStyleCommand(ed_, stop_, new StopStyle(StopStyle.Preset.DEFAULT)));
            }
          });
          add(item);


          for(final StopStyle style : ed_.getDocument().getStopStyles().getList()) {
            item = new JMenuItem(style.getName());
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                invoker_.doCommand(new SetStopStyleCommand(ed_, stop_, style));
              }
            });
            add(item);
          }
        }

        public void menuDeselected(MenuEvent e) { }

        public void menuCanceled(MenuEvent e) { }
      });
    }
  }
}