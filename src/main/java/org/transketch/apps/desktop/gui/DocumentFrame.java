/*
 * CanvasFrame.java
 * 
 * Created by demory on Nov 21, 2009, 8:40:23 PM
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

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.command.file.SaveFileAsCommand;
import org.transketch.apps.desktop.gui.editor.EditorPane;
import org.transketch.apps.desktop.gui.viewer.ViewerPane;
import org.transketch.util.viewport.MapCoordinates;

/**
 *
 * @author demory
 */
public class DocumentFrame extends JInternalFrame {
  private final static Logger logger = Logger.getLogger(DocumentFrame.class);

  private TSDocument doc_;

  private TSInvoker invoker_;
  private TranSketchGUI gui_;
  private EditorPane edPane_;
  private ViewerPane viewPane_;

  private MapCoordinates coords_ = new MapCoordinates();

  public DocumentFrame(TSDocument doc, final TSInvoker invoker, TranSketchGUI gui, String title) {
    super(title, true, true, true, true);
    doc_ = doc;
    invoker_ = invoker;
    //canvas_ = new EditorCanvas(invoker, ed, gui);
    gui_ = gui;

    edPane_ = new EditorPane(invoker, doc.getEditor(), coords_, gui);
    doc_.getEditor().setPane(edPane_);

    setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
    
    this.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        //logger.debug("map resized");
        edPane_.getCanvas().resized();
      }
    });

    this.addInternalFrameListener(new InternalFrameAdapter() {
      @Override
      public void internalFrameActivated(InternalFrameEvent e) {
        DocumentFrame previous = gui_.getActiveDocumentFrame();
        gui_.setActiveDocumentFrame(DocumentFrame.this);
        gui_.updateUndoRedo(doc_.getEditor());
        if(previous != null) previous.getEditorPane().getCanvas().deactivateKeyListener();
        edPane_.getCanvas().activateKeyListener();
      }

      @Override
      public void internalFrameClosing(InternalFrameEvent e) {
        tryClose();
      }

      @Override
      public void internalFrameClosed(InternalFrameEvent e) {
        edPane_.getCanvas().deactivateKeyListener();
        gui_.documentFrameClosed(DocumentFrame.this);
        setVisible(false);
        logger.info("document frame closed");
      }
    });


    viewPane_ = new ViewerPane(doc_, gui_, coords_);

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add("Editor", edPane_);
    tabbedPane.add("Viewer", viewPane_);
    this.add(tabbedPane);
    this.pack();

    edPane_.getCanvas().requestFocusInWindow();
    logger.debug("rFIW");
    gui.documentFrameOpened(this);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        edPane_.resolutionChanged(coords_.getResolution());
        edPane_.getCanvas().resolutionChanged(coords_.getResolution());
      }
    });
  }

  public TSDocument getDocument() {
    return doc_;
  }

  public EditorPane getEditorPane() {
    return edPane_;
  }

  public ViewerPane getViewerPane() {
    return viewPane_;
  }

  public MapCoordinates getCoordinates() {
    return coords_;
  }

  public boolean tryClose() {
    if(doc_.getEditor().getHistory().isModified()) {
      int result = JOptionPane.showConfirmDialog(null, "There are unsaved changes. Do you want to save?", "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION);
      if(result == JOptionPane.YES_OPTION) {
        if(invoker_.doCommand(new SaveFileAsCommand(doc_.getEditor()))) {
          dispose();
          return true;
        }
      }
      else if (result == JOptionPane.NO_OPTION) {
        dispose();
        return true;
      }
    }
    else {
      dispose();
      return true;
    }
    return false;
  }
}
