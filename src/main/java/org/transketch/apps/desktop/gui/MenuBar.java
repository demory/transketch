/*
 * MenuBar.java
 * 
 * Created by demory on Mar 29, 2009, 4:08:11 PM
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.CommandFactory;
import org.transketch.apps.desktop.command.edit.ModifyEditorPropertyCommand;
import org.transketch.apps.desktop.command.file.LoadFileCommand;
import org.transketch.apps.desktop.gui.control.ControlFrame;

/**
 *
 * @author demory
 */
public class MenuBar extends JMenuBar {

  private TSInvoker invoker_;

  private JMenu recentFilesMenu_;

  private JMenuItem undoItem_, redoItem_, showGridItem_, snapToGridItem_;

  private Set<EditorPropCheckBoxMenuItem> edPropItems_;

  public MenuBar(TSInvoker invoker, TranSketchGUI gui, List<String> recentFiles) {
    super();
    invoker_ = invoker;
    edPropItems_ = new HashSet<EditorPropCheckBoxMenuItem>();

    JMenu fileMenu = new JMenu("File");
    newMenuItem(fileMenu, "New", CommandFactory.Key.FILE_NEW);
    newMenuItem(fileMenu, "Open...", CommandFactory.Key.FILE_LOAD);
    
    recentFilesMenu_ = new JMenu("Open Recent File");
    updateRecentFilesMenu(recentFiles);
    fileMenu.add(recentFilesMenu_);

    fileMenu.addSeparator();
    newMenuItem(fileMenu, "Save", CommandFactory.Key.FILE_SAVE);
    newMenuItem(fileMenu, "Save As...", CommandFactory.Key.FILE_SAVE_AS);
    JMenu exportMenu = new JMenu("Export...");
    newMenuItem(exportMenu, "PNG", CommandFactory.Key.FILE_EXPORT_PNG);
    newMenuItem(exportMenu, "SVG", CommandFactory.Key.FILE_EXPORT_SVG);
    fileMenu.add(exportMenu);
    fileMenu.addSeparator();
    newMenuItem(fileMenu, "Exit", CommandFactory.Key.FILE_EXIT);
    add(fileMenu);

    JMenu editMenu = new JMenu("Edit");
    undoItem_ = newMenuItem(editMenu, "Undo", CommandFactory.Key.EDIT_UNDO);
    redoItem_ = newMenuItem(editMenu, "Redo", CommandFactory.Key.EDIT_REDO);
    add(editMenu);


    JMenu toolsMenu = new JMenu("Tools");
    newMenuItem(toolsMenu, "Run Bundler", CommandFactory.Key.TOOLS_REBUNDLE);
    add(toolsMenu);

    JMenu viewMenu = new JMenu("View");
    newEPCBMenuItem(viewMenu, "Show Anchors", Editor.Property.SHOW_ANCHORS);
    newEPCBMenuItem(viewMenu, "Show Corridors", Editor.Property.SHOW_CORRIDORS);
    newEPCBMenuItem(viewMenu, "Show Lines", Editor.Property.SHOW_LINES);
    newEPCBMenuItem(viewMenu, "Show Stations/Stops", Editor.Property.SHOW_STOPS);
    viewMenu.addSeparator();
    showGridItem_ = newEPCBMenuItem(viewMenu, "Show Grid", Editor.Property.SHOW_GRID);
    snapToGridItem_ = newEPCBMenuItem(viewMenu, "Snap to Grid", Editor.Property.SNAP_TO_GRID);
    viewMenu.addSeparator();
    newEPCBMenuItem(viewMenu, "Use Antialiasing", Editor.Property.USE_ANTIALIASING);
    add(viewMenu);


    JMenu windowMenu = new JMenu("Window");
    for(final ControlFrame frame : gui.getControlFrameManager().getFrames()) {
      final JCheckBoxMenuItem item = new JCheckBoxMenuItem(frame.getTitle());
      frame.setMenuItem(item);
      item.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          frame.setVisible(item.isSelected());
          //System.out.println("iSC "+item.isSelected());
        }
      });
      windowMenu.add(item);
    }
    add(windowMenu);

    JMenu debugMenu = new JMenu("Debug");
    newMenuItem(debugMenu, "Debug Action", CommandFactory.Key.DEBUG);
    add(debugMenu);

    JMenu helpMenu = new JMenu("Help");
    newMenuItem(helpMenu, "About", CommandFactory.Key.HELP_ABOUT);
    add(helpMenu);


  }

  public JMenuItem getUndoItem() {
    return undoItem_;
  }

  public JMenuItem getRedoItem() {
    return redoItem_;
  }

  public JMenuItem getShowGridItem() {
    return showGridItem_;
  }

  public JMenuItem getSnapToGridItem() {
    return snapToGridItem_;
  }

  public void updateRecentFilesMenu(List<String> recentFiles) {
    recentFilesMenu_.removeAll();
    for(final String filename : recentFiles) {
      JMenuItem item = new JMenuItem(filename);
      recentFilesMenu_.add(item);
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          LoadFileCommand cmd = new LoadFileCommand(invoker_.getActiveEditor(), new File(filename));
          invoker_.doCommand(cmd);
          System.out.println("end uRFM actPer");
        }
      });
    }
    recentFilesMenu_.addSeparator();
    newMenuItem(recentFilesMenu_, "Clear Recent Files", CommandFactory.Key.FILE_CLEAR_RECENT);
  }

  private MenuItem newMenuItem(JMenu menu, String name, CommandFactory.Key key) {
    MenuItem item = new MenuItem(name, key);
    menu.add(item);
    return item;
  }

  public class MenuItem extends JMenuItem {

    private  CommandFactory.Key key_;

    public MenuItem(String name, CommandFactory.Key key) {
      super(name);
      key_ = key;
      this.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          invoker_.doCommand(invoker_.getCommandFactory().createCommand(key_));
        }
      });
    }
  }

  private JMenuItem newEPCBMenuItem(JMenu menu, String title, Editor.Property prop) {
    EditorPropCheckBoxMenuItem item = new EditorPropCheckBoxMenuItem(title, prop);
    edPropItems_.add(item);
    menu.add(item);
    return item;
  }

  public void updateEdPropItems(Editor ed) {
    for(EditorPropCheckBoxMenuItem item : edPropItems_)
      item.setSelected(ed.getBoolProperty(item.prop_));
  }

  private class EditorPropCheckBoxMenuItem extends JCheckBoxMenuItem {

    private Editor.Property prop_;

    public EditorPropCheckBoxMenuItem(String title, Editor.Property prop) {
      super(title);
      prop_ = prop;
      addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          String val = isSelected() ? "true" : "false";
          invoker_.doCommand(new ModifyEditorPropertyCommand(invoker_.getActiveEditor(), prop_, val));
        }
      });
    }
  }
}
