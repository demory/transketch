/*
 * Toolbar.java
 * 
 * Created by demory on Mar 28, 2009, 5:07:03 PM
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

package org.transketch.apps.desktop.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.command.CommandFactory.Key;
import org.transketch.apps.desktop.command.SwingActionCommandAdapter;
import org.transketch.apps.desktop.command.viewport.ZoomCommand;

/**
 *
 * @author demory
 */
public class Toolbar extends JToolBar implements ActionListener {

  private TSInvoker invoker_;
  private TranSketchGUI gui_;
  private ButtonGroup btnGroup_;

  private JButton undoBtn_, redoBtn_, zoomInBtn_, zoomOutBtn_;

  public Toolbar(TSInvoker invoker, TranSketchGUI gui, String iconRoot) {
    super();
    invoker_ = invoker;
    gui_ = gui;

    btnGroup_ = new ButtonGroup();

    undoBtn_ = new JButton(new SwingActionCommandAdapter(invoker, Key.EDIT_UNDO));
    undoBtn_.setIcon(new ImageIcon(getClass().getResource("/icons/undo.png")));

    redoBtn_ = new JButton(new SwingActionCommandAdapter(invoker, Key.EDIT_REDO));
    redoBtn_.setIcon(new ImageIcon(TranSketch.class.getResource("/icons/redo.png")));

    //redoBtn_ = new JButton(new ImageIcon(iconRoot + "redo.png"));
    addButton(undoBtn_, "Undo");
    addButton(redoBtn_, "Redo");
    undoBtn_.setEnabled(false);
    redoBtn_.setEnabled(false);

    addSeparator();

    zoomInBtn_ = new JButton(new ImageIcon(TranSketch.class.getResource("/icons/zoomin.png")));
    zoomOutBtn_ = new JButton(new ImageIcon(TranSketch.class.getResource("/icons/zoomout.png")));
    addButton(zoomInBtn_, "Zoom In");
    addButton(zoomOutBtn_, "Zoom Out");

    addSeparator();

  }

  public void addButton(JButton btn, String text) {
    btn.addActionListener(this);
    btn.setMargin(new Insets(0, 0, 0, 0));
    btn.setMaximumSize(new Dimension(34, 34));
    btn.setToolTipText(text);
    this.add(btn);
  }

  /*
  private ToolbarButton newButton(ImageIcon icon, int action, String tip) {
    return addButton(new ToolbarButton(icon, action), tip);
  }

  private ToolbarButton newButton(String text, int action, String tip) {
    return addButton(new ToolbarButton(text, action), tip);
  }

  private ToolbarButton addButton(ToolbarButton btn, String tip) {
    btn.setToolTipText(tip);
    add(btn);
    btnGroup_.add(btn);
    btn.addActionListener(this);
    return btn;
  }*/

  @Override
  public void actionPerformed(ActionEvent e) {
    if(invoker_.getActiveEditor() == null) return;
    if(e.getSource() == zoomInBtn_)
      invoker_.doCommand(new ZoomCommand(invoker_.getActiveEditor().getPane().getCanvas(), 0.1));
    if(e.getSource() == zoomOutBtn_)
      invoker_.doCommand(new ZoomCommand(invoker_.getActiveEditor().getPane().getCanvas(), -0.1));
  }

  public JButton getUndoButton() {
    return undoBtn_;
  }

  public JButton getRedoButton() {
    return redoBtn_;
  }

}
