/*
 * EditorToolbar.java
 * 
 * Created by demory on Jan 25, 2011, 8:25:43 PM
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

package org.transketch.apps.desktop.gui.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.transketch.apps.desktop.Editor.Property;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.command.edit.ModifyEditorPropertyCommand;
import org.transketch.apps.desktop.gui.editor.map.Drawable;

/**
 *
 * @author demory
 */
public class EditorToolbar extends JToolBar {

  private EditorPane pane_;
  private ButtonGroup group_ = new ButtonGroup();
  private Map<Action, Button> buttonLookup_ = new HashMap<Action, Button>();
  private Action selectedAction_ = null;

  private JToggleButton selDropdown_, showGrid_, snapToGrid_;;

  private SelectableMenu selMenu_ = new SelectableMenu();

  public enum Action {
    SELECT,
    DRAW_NETWORK,
    CREATE_ANCHOR_POINT,
    CREATE_CORRIDOR,
    DELETE_ANCHOR_POINT,
    MERGE_ANCHOR_POINT,
    DELETE_CORRIDOR,
    SPLIT_CORRIDOR,
    MODIFY_LINE
  }

  public EditorToolbar(EditorPane pane, final TSInvoker invoker) {
    pane_ = pane;
    new Button("*P*", Action.SELECT, "Pointer", Drawable.Type.values()); //.setSelected(true);
    selDropdown_ = new JToggleButton("v");
    selDropdown_.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(selDropdown_.isSelected() == true)
          selMenu_.show(EditorToolbar.this, selDropdown_.getX()+selDropdown_.getWidth()+1, selDropdown_.getY());
        else
          selMenu_.setVisible(false);
      }
    });
    add(selDropdown_);
    
    add(Box.createHorizontalStrut(5));
    new Button("Dr", Action.DRAW_NETWORK, "Draw Network", new Drawable.Type[] {Drawable.Type.ANCHOR_POINT});
    new Button("NA", Action.CREATE_ANCHOR_POINT, "New Anchor", new Drawable.Type[] { });
    new Button("NC", Action.CREATE_CORRIDOR, "New Corridor", new Drawable.Type[] {Drawable.Type.ANCHOR_POINT});
    new Button("DA", Action.DELETE_ANCHOR_POINT, "Delete Anchor", new Drawable.Type[] {Drawable.Type.ANCHOR_POINT});
    new Button("MA", Action.MERGE_ANCHOR_POINT, "Merge Anchor", new Drawable.Type[] {Drawable.Type.ANCHOR_POINT});
    new Button("DC", Action.DELETE_CORRIDOR, "Delete Corridor", new Drawable.Type[] {Drawable.Type.CORRIDOR});
    new Button("SC", Action.SPLIT_CORRIDOR, "Split Corridor", new Drawable.Type[] {Drawable.Type.CORRIDOR});
    new Button("ML", Action.MODIFY_LINE, "Modify Line", new Drawable.Type[] {Drawable.Type.LINE});

    add(Box.createHorizontalStrut(5));
    showGrid_ = new JToggleButton("ShG");
    showGrid_.setToolTipText("Show Grid");
    showGrid_.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker.doCommand(new ModifyEditorPropertyCommand(invoker.getActiveEditor(), Property.SHOW_GRID, ""+showGrid_.isSelected()));
      }
    });
    add(showGrid_);

    snapToGrid_ = new JToggleButton("SpG");
    snapToGrid_.setToolTipText("Snap to Grid");
    snapToGrid_.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker.doCommand(new ModifyEditorPropertyCommand(invoker.getActiveEditor(), Property.SNAP_TO_GRID, ""+snapToGrid_.isSelected()));
      }
    });
    add(snapToGrid_);

    selectedAction_ = Action.SELECT;
  }

  public void selectAction(Action action) {
    buttonLookup_.get(action).setSelected(true);
    buttonLookup_.get(action).actionPerformed(null);
  }

  public Action getSelectedAction() {
    return selectedAction_;
  }

  public void clearMenu() {
    if(selMenu_.isVisible()) {
      selMenu_.setVisible(false);
      selDropdown_.setSelected(false);
    }
  }

  public JToggleButton getShowGridButton() {
    return showGrid_;
  }

  public JToggleButton getSnapToGridButton() {
    return snapToGrid_;
  }

  public class Button extends JToggleButton implements ActionListener {

    private Action action_;
    private Drawable.Type[] hoverableTypes_;

    public Button(String btnText, Action action, String tipText, Drawable.Type[] hoverableTypes) {
      super(btnText);
      
      action_ = action;
      hoverableTypes_ = hoverableTypes;

      setToolTipText(tipText);
      group_.add(this);
      EditorToolbar.this.add(Button.this);

      addActionListener(this);

      buttonLookup_.put(action, this);
    }

    public void actionPerformed(ActionEvent e) {
      selectedAction_ = action_;

      pane_.getCanvas().setHoverableTypes(action_ == Action.SELECT ?
        selMenu_.getTypes() : hoverableTypes_);
      pane_.getCanvas().cancelActiveAction();
      pane_.getCanvas().toolbarActionChanged();
      selDropdown_.setEnabled(action_ == Action.SELECT);
      if(action_ != Action.SELECT) clearMenu();
    }
  }

  private class SelectableMenu extends JPopupMenu {

    JMenuItem apItem_, corrItem_, lineItem_, stopItem_;

    public SelectableMenu() {
      super("Selectable Types:");

      apItem_ = new SelMenuItem("Anchor Points", Drawable.Type.ANCHOR_POINT, this);
      corrItem_ = new SelMenuItem("Corridors", Drawable.Type.CORRIDOR, this);
      lineItem_ = new SelMenuItem("Lines", Drawable.Type.LINE, this);
      stopItem_ = new SelMenuItem("Stops", Drawable.Type.STOP, this);

      addSeparator();

      JMenuItem selectAll = new JMenuItem("Select All");
      selectAll.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          for(Component c : getComponents())
            if(c instanceof JCheckBoxMenuItem) ((JCheckBoxMenuItem) c).setSelected(true);
        }
      });
      add(selectAll);

      JMenuItem deselectAll = new JMenuItem("Deselect All");
      deselectAll.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          for(Component c : getComponents())
            if(c instanceof JCheckBoxMenuItem) ((JCheckBoxMenuItem) c).setSelected(false);
        }
      });
      add(deselectAll);

    }

    @Override
    public void menuSelectionChanged(boolean isIncluded) {
    }


    public Drawable.Type[] getTypes() {
      Set<Drawable.Type> types = new HashSet<Drawable.Type>();
      if(apItem_.isSelected()) types.add(Drawable.Type.ANCHOR_POINT);
      if(corrItem_.isSelected()) types.add(Drawable.Type.CORRIDOR);
      if(lineItem_.isSelected()) types.add(Drawable.Type.LINE);
      if(stopItem_.isSelected()) types.add(Drawable.Type.STOP);
      Drawable.Type[] r = new Drawable.Type[0];
      return (Drawable.Type[]) types.toArray(r);
    }

    @Override
    public void show(Component invoker, int x, int y) {
      /*apItem_.setSelected(pane_.getCanvas().isHoverable(Drawable.Type.ANCHOR_POINT));
      corrItem_.setSelected(pane_.getCanvas().isHoverable(Drawable.Type.CORRIDOR));
      lineItem_.setSelected(pane_.getCanvas().isHoverable(Drawable.Type.LINE));
      stopItem_.setSelected(pane_.getCanvas().isHoverable(Drawable.Type.STOP));*/
      super.show(invoker, x, y);
    }

  }

  private class SelMenuItem extends JCheckBoxMenuItem {
    public SelMenuItem(String title, Drawable.Type type, SelectableMenu menu) {
      super(title, true);
      menu.add(this);

      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          pane_.getCanvas().setHoverableTypes(selMenu_.getTypes());
        }
      });
    }

  }

}
