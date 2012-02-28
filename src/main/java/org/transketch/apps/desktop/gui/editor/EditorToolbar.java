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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import org.transketch.apps.desktop.Editor.Property;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.command.edit.ModifyEditorPropertyCommand;
import org.transketch.apps.desktop.gui.editor.map.Drawable;
import org.transketch.util.gui.GUIFactory;

/**
 *
 * @author demory
 */
public class EditorToolbar extends JPanel { //JToolBar {

  private EditorPane pane_;
  private ButtonGroup group_ = new ButtonGroup();
  private Map<ActionType, Button> buttonLookup_ = new HashMap<ActionType, Button>();
  private Action selectedAction_ = null;
  
  private SelectControlButton selAnchorBtn_, selCorridorBtn_, selLineBtn_, selStopBtn_;

  private JToggleButton showGrid_, snapToGrid_;;

  private SelectableMenu selMenu_ = new SelectableMenu();

  public enum ActionType {
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
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
    this.setMinimumSize(new Dimension(1,60));
    
    pane_ = pane;
    final Button pointerBtn = new Button("*P*", "Pointer", new Action(ActionType.SELECT, Drawable.Type.values()));
    
    JPanel selectablePanel = GUIFactory.newColumnPanel(); //new JPanel(new BorderLayout());
    JLabel selectableLabel = new JLabel("selectable:");
    selectableLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
    selectableLabel.setAlignmentX(CENTER_ALIGNMENT);
    
    selAnchorBtn_ = new SelectControlButton("A");
    selCorridorBtn_ = new SelectControlButton("C");
    selLineBtn_ = new SelectControlButton("L");
    selStopBtn_ = new SelectControlButton("S");
    JPanel selBtnRow = GUIFactory.newRowPanel();
    selBtnRow.add(selAnchorBtn_);
    selBtnRow.add(selCorridorBtn_);
    selBtnRow.add(selLineBtn_);
    selBtnRow.add(selStopBtn_);

    selBtnRow.setAlignmentX(CENTER_ALIGNMENT);
    selectablePanel.setBackground(Color.lightGray);
    selectablePanel.add(selectableLabel);
    selectablePanel.add(selBtnRow);
    selectablePanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
          selMenu_.show(EditorToolbar.this, pointerBtn.getWidth()+e.getX(), e.getY());
      }
    });
    
    add(selectablePanel);
    
    add(Box.createHorizontalStrut(5));
    
    new Button("Draw", "Draw Network", new Action(ActionType.DRAW_NETWORK, new Drawable.Type[] {Drawable.Type.ANCHOR_POINT}), 48);
    
    add(Box.createHorizontalStrut(5));

    // anchor tools
    Toolset anchorToolset = new Toolset("ANCHOR", "Anchor Tools");
    anchorToolset.addTool(new Action(ActionType.CREATE_ANCHOR_POINT), "Create");
    anchorToolset.addTool(new Action(ActionType.DELETE_ANCHOR_POINT, new Drawable.Type[] {Drawable.Type.ANCHOR_POINT}), "Delete");
    anchorToolset.addTool(new Action(ActionType.MERGE_ANCHOR_POINT, new Drawable.Type[] {Drawable.Type.ANCHOR_POINT}), "Merge");

    add(anchorToolset);
    
    add(Box.createHorizontalStrut(5));

    // anchor tools
    Toolset corridorToolset = new Toolset("CORRIDOR", "Corridor Tools");
    corridorToolset.addTool(new Action(ActionType.CREATE_CORRIDOR, new Drawable.Type[] {Drawable.Type.ANCHOR_POINT}), "Create");
    corridorToolset.addTool(new Action(ActionType.DELETE_CORRIDOR, new Drawable.Type[] {Drawable.Type.CORRIDOR}), "Delete");
    corridorToolset.addTool(new Action(ActionType.SPLIT_CORRIDOR, new Drawable.Type[] {Drawable.Type.CORRIDOR}), "Split");

    add(corridorToolset);
    
    add(Box.createHorizontalStrut(5));
    new Button("ML", "Modify Line", new Action(ActionType.MODIFY_LINE, new Drawable.Type[] {Drawable.Type.LINE}));

    add(Box.createHorizontalStrut(5));
    
    JLabel gridLabel = new JLabel("GRID");
    gridLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
    gridLabel.setAlignmentX(CENTER_ALIGNMENT);
      
      
    showGrid_ = new JToggleButton("Show");
    setComponentSize(showGrid_, new Dimension(45, 18));
    showGrid_.setBorder(new EtchedBorder());
    showGrid_.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
    showGrid_.setToolTipText("Show Grid");
    showGrid_.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker.doCommand(new ModifyEditorPropertyCommand(invoker.getActiveEditor(), Property.SHOW_GRID, ""+showGrid_.isSelected()));
      }
    });

    snapToGrid_ = new JToggleButton("Snap");
    setComponentSize(snapToGrid_, new Dimension(45, 18));
    snapToGrid_.setBorder(new EtchedBorder());
    snapToGrid_.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
    snapToGrid_.setToolTipText("Snap to Grid");
    snapToGrid_.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker.doCommand(new ModifyEditorPropertyCommand(invoker.getActiveEditor(), Property.SNAP_TO_GRID, ""+snapToGrid_.isSelected()));
      }
    });
    
    JPanel gridButtonPanel = GUIFactory.newRowPanel();
    gridButtonPanel.add(showGrid_);
    gridButtonPanel.add(snapToGrid_);
    
    JPanel gridPanel = GUIFactory.newColumnPanel();
    gridPanel.setBackground(Color.lightGray);
    gridPanel.add(gridLabel);
    gridPanel.add(gridButtonPanel);

    add(gridPanel);
    
    selectedAction_ = new Action(ActionType.SELECT);
  }

  public void selectAction(ActionType action) {
    buttonLookup_.get(action).setSelected(true);
    applyAction(buttonLookup_.get(action).action_);
  }

  public void applyAction(Action action) {
    selectedAction_ = action;     

    pane_.getCanvas().setHoverableTypes(action.type_ == ActionType.SELECT ?
      getSelectableTypes() : action.hoverableTypes_);
    pane_.getCanvas().cancelActiveAction();
    pane_.getCanvas().toolbarActionChanged();
  }
  
  public ActionType getSelectedAction() {
    return selectedAction_.type_;
  }

  public Drawable.Type[] getSelectableTypes() {
    Set<Drawable.Type> types = new HashSet<Drawable.Type>();
    if(selAnchorBtn_.isSelected()) types.add(Drawable.Type.ANCHOR_POINT);
    if(selCorridorBtn_.isSelected()) types.add(Drawable.Type.CORRIDOR);
    if(selLineBtn_.isSelected()) types.add(Drawable.Type.LINE);
    if(selStopBtn_.isSelected()) types.add(Drawable.Type.STOP);
    Drawable.Type[] r = new Drawable.Type[0];
    return (Drawable.Type[]) types.toArray(r);
  }
    
  public JToggleButton getShowGridButton() {
    return showGrid_;
  }

  public JToggleButton getSnapToGridButton() {
    return snapToGrid_;
  }

  public class SelectControlButton extends JToggleButton {

    public SelectControlButton(String str) {
      super(str);
      setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
      Dimension dim = new Dimension(18, 18);
      setBorder(new EtchedBorder()); //new BevelBorder(BevelBorder.RAISED));
      EditorToolbar.setComponentSize(SelectControlButton.this, dim);
      setSelected(true);
      
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          pane_.getCanvas().setHoverableTypes(getSelectableTypes());
        }
      });
    }
    
  }
  
  private class Button extends JToggleButton {

    private Action action_;

    public Button(String btnText, String tipText, Action action) {
      this(btnText, tipText, action, 32);
    }

    public Button(String btnText, String tipText, Action action, int width) {
      super(btnText);
      
      Dimension dim = new Dimension(width, 32);
      setBorder(new EtchedBorder()); //BevelBorder(BevelBorder.RAISED));
      EditorToolbar.setComponentSize(Button.this, dim);
      
      action_ = action;

      setToolTipText(tipText);
      group_.add(this);
      EditorToolbar.this.add(Button.this);

      if(action != null) {
        addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            applyAction(action_);
          }
        });
        buttonLookup_.put(action.type_, this);
      }
    }
  }

  private class SelectableMenu extends JPopupMenu {

    JMenuItem apItem_, corrItem_, lineItem_, stopItem_;

    public SelectableMenu() {
      super("Selectable Types:");

      JMenuItem selectAll = new JMenuItem("Select All");
      selectAll.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selAnchorBtn_.setSelected(true);
          selCorridorBtn_.setSelected(true);
          selLineBtn_.setSelected(true);
          selStopBtn_.setSelected(true);
          pane_.getCanvas().setHoverableTypes(getSelectableTypes());
        }
      });
      add(selectAll);

      JMenuItem deselectAll = new JMenuItem("Deselect All");
      deselectAll.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selAnchorBtn_.setSelected(false);
          selCorridorBtn_.setSelected(false);
          selLineBtn_.setSelected(false);
          selStopBtn_.setSelected(false);
          pane_.getCanvas().setHoverableTypes(getSelectableTypes());
        }
      });
      add(deselectAll);

    }

  }

  /*private class SelMenuItem extends JCheckBoxMenuItem {
    public SelMenuItem(String title, Drawable.Type type, SelectableMenu menu) {
      super(title, true);
      menu.add(this);

      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          pane_.getCanvas().setHoverableTypes(getSelectableTypes());
        }
      });
    }

  }*/
  
  private class Action {
    ActionType type_;
    private Drawable.Type[] hoverableTypes_;    
    
    public Action(ActionType type, Drawable.Type[] hoverableTypes) {
      type_ = type;
      hoverableTypes_ = hoverableTypes;
    }
    
    public Action(ActionType type) {
      this(type, new Drawable.Type[] { });
    }
 
  }
 
  private class Toolset extends JPanel {

    Button mainButton_;
    JComboBox comboBox_ = new JComboBox();
      
    public Toolset(String labelText, String tip) {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      JPanel rightPanel = GUIFactory.newColumnPanel();
      mainButton_ = new Button(""+labelText.charAt(0), tip, null, 24);
      JLabel topLabel = new JLabel(labelText);
      topLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
      topLabel.setAlignmentX(CENTER_ALIGNMENT);
      comboBox_.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
      comboBox_.setAlignmentX(CENTER_ALIGNMENT);
      comboBox_.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          mainButton_.setSelected(true);
          applyAction(((CBActionWrapper) comboBox_.getSelectedItem()).action_);        
        }
      });
      mainButton_.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          //System.out.println("action: "+((CBActionWrapper) comboBox_.getSelectedItem()).action_.type_);
          applyAction(((CBActionWrapper) comboBox_.getSelectedItem()).action_);        
        }
      });
      rightPanel.setBackground(Color.lightGray);
      rightPanel.add(topLabel);
      rightPanel.add(comboBox_); 
      
      add(mainButton_);
      add(rightPanel);
    }
    
    public void addTool(Action action, String label) {
      comboBox_.addItem(new CBActionWrapper(action, label));      
    }
    
    private class CBActionWrapper {
      Action action_;
      String text_;
      
      public CBActionWrapper(Action action, String text) {
        action_ = action;
        text_ = text;
      }
      
      @Override
      public String toString() {
        return text_;
      }
      
    }
    
  }
  
  public static void setComponentSize(JComponent comp, Dimension dim) {
    comp.setSize(dim);
    comp.setPreferredSize(dim);
    comp.setMinimumSize(dim);
    comp.setMaximumSize(dim);
  }
  
}

