/*
 * TranSketchGUI.java
 * 
 * Created by demory on Sep 13, 2009, 5:18:15 PM
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.system.ExitCommand;
import org.transketch.apps.desktop.gui.control.ControlFrameManager;
import org.transketch.core.network.Bundler;
import org.transketch.util.SysProps;

/**
 *
 * @author demory
 */
public class TranSketchGUI extends JFrame {
  private final static Logger logger = Logger.getLogger(TranSketchGUI.class);

  private TranSketch ts_;

  //private ControlPanel controlPanel_;
  private MenuBar menuBar_;
  private ControlFrameManager cfm_;

  private Toolbar toolbar_;
  private int toolbarAction_;

  private JDesktopPane desktop_;

  private DocumentFrame activeFrame_;

  private Dimension lastSize_;

  //private int docFramesOpen_ = 0;
  private Set<DocumentFrame> docFrames_ = new HashSet<DocumentFrame>();

  private KeyEventDispatcher mainKED_;

  public TranSketchGUI(final TranSketch ts) {
    super("Transit Sketchpad v"+TranSketch.VERSION);

    ts_ = ts;

    toolbar_ = new Toolbar(ts, this, ts.getProperties().getProperty(SysProps.FP_HOME)+"img"+File.separator);
    toolbarAction_ = 0;

    desktop_ = new JDesktopPane();
    desktop_.setBackground(Color.lightGray);

    cfm_ = new ControlFrameManager(ts_, this);

    menuBar_ = new MenuBar(ts, this, ts.getRecentFiles().getFilenames());
    setJMenuBar(menuBar_);
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(toolbar_, BorderLayout.NORTH);
    mainPanel.add(desktop_, BorderLayout.CENTER);

    add(mainPanel);

    pack();
    setSize(new Dimension(800, 600));
    setLocationRelativeTo(null);
    setVisible(true);
    lastSize_ = getSize();

    mainKED_ = KeyboardFocusManager.getCurrentKeyboardFocusManager();

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // EXIT_ON_CLOSE);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        Dimension newSize = TranSketchGUI.this.getSize();
        cfm_.resized(newSize.width-lastSize_.width, newSize.height-lastSize_.height);
        lastSize_ = newSize;
      }
    });

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        ts.doCommand(new ExitCommand());
      }
    });
  }

  public void documentFrameOpened(DocumentFrame frame) {
    docFrames_.add(frame);
  }

  public void documentFrameClosed(DocumentFrame frame) {
    docFrames_.remove(frame);

    if(docFrames_.size() == 0) {
      //logger.debug("last frame closed");
      //cfm_.editorChanged(activeFrame_.getEditor(), null);
      cfm_.documentChanged(activeFrame_.getDocument(), null);
      activeFrame_ = null;
    }
  }

  public boolean closeAll() {
    for(DocumentFrame docFrame : new HashSet<DocumentFrame>(docFrames_)) {
      docFrame.setLayer(100);
      if(!docFrame.tryClose())
        return false;
    }
    return true;
  }

  public ControlFrameManager getControlFrameManager() {
    return cfm_;
  }

  public MenuBar getTSMenuBar() {
    return menuBar_;
  }

  public JDesktopPane getDesktop() {
    return desktop_;
  }

  public void dispatchKeyEvent(KeyEvent e) {
    mainKED_.dispatchKeyEvent(e);
  }

  /*public Canvas getCanvas() {
    return canvas_;
  }*/

  public void msg(String msg) {
    logger.debug("MSG: "+msg);
    cfm_.getOutputFrame().msg(msg);
  }

  public void setToolbarAction(int action) {
    toolbarAction_ = action;
  }

  public int getToolbarAction() {
    return toolbarAction_;
  }

  public void updateUndoRedo(Editor ed) {
    //Editor ed = ts_.getActiveEditor();
    //logger.debug("uUR = "+ed);
    boolean undoExists = (ed != null) ? ed.getHistory().undoActionExists() : false;
    String undoText = "Undo " + (undoExists ? ed.getHistory().undoActionName() : "");
    menuBar_.getUndoItem().setEnabled(undoExists);
    menuBar_.getUndoItem().setText(undoText);
    toolbar_.getUndoButton().setEnabled(undoExists);
    toolbar_.getUndoButton().setToolTipText(undoText);

    boolean redoExists = (ed != null) ? ed.getHistory().redoActionExists() : false;
    String redoText = "Redo " + (redoExists ? ed.getHistory().redoActionName() : "");
    menuBar_.getRedoItem().setEnabled(redoExists);
    menuBar_.getRedoItem().setText(redoText);
    toolbar_.getRedoButton().setEnabled(redoExists);
    toolbar_.getRedoButton().setToolTipText(redoText);
  }

  /*public void addEditor(Editor ed) {
    String title = ed.getDocument().hasActiveFile() ? ed.getDocument().getActiveFile().getName() : "Untitled-"+ed.getID();
    DocumentFrame frame = new DocumentFrame(ts_, ed, this, title);
    desktop_.add(frame);
    frame.setSize(400,400);
    frame.setVisible(true);
    //frame.getCanvas().repaint();
  }*/

  public void newDocumentFrame(TSDocument doc) {
    String title = doc.getWorkingTitle();
    DocumentFrame frame = new DocumentFrame(doc, ts_, this, title);
    desktop_.add(frame);
    frame.setSize(500,400);
    frame.setVisible(true);
    doc.setFrame(frame);
    doc.getNetwork().rebundle();
    frame.repaint();
  }

  public DocumentFrame getActiveDocumentFrame() {
    return activeFrame_;
  }

  public void setActiveDocumentFrame(DocumentFrame frame) {
    /*Editor oldEditor = activeFrame_ != null ? activeFrame_.getEditor() : null;
    activeFrame_ = frame;
    Editor newEditor = frame.getEditor();
    if(oldEditor != newEditor) {
      cfm_.editorChanged(oldEditor, newEditor);
      menuBar_.updateEdPropItems(newEditor);
    }*/

    TSDocument oldDoc = activeFrame_ != null ? activeFrame_.getDocument() : null;
    activeFrame_ = frame;
    TSDocument newDoc = frame.getDocument();
    if(oldDoc != newDoc) {
      cfm_.documentChanged(oldDoc, newDoc);
      menuBar_.updateEdPropItems(newDoc.getEditor());
    }

  }
}

