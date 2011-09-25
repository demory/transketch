/*
 * TranSketch.java
 * 
 * Created by demory on Mar 28, 2009, 12:10:26 PM
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

package org.transketch.apps.desktop;

import java.io.File;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.command.TSCommand;
import org.transketch.apps.Application;
import org.transketch.apps.desktop.command.ActionHistory;
import org.transketch.apps.desktop.command.CommandFactory;
import org.transketch.apps.desktop.gui.TranSketchGUI;

/**
 *
 * @author demory
 */
public class TranSketch extends Application implements TSInvoker {

  public final static String VERSION = "0.0";

  // the graphical interface:
  private TranSketchGUI gui_;

  private DocumentFactory docFactory_;
  private CommandFactory cmdFactory_;

  private RecentFiles recentFiles_;
  private File workingDirectory_ = new File(System.getProperty("user.dir"));

  public TranSketch() {
    super("transketch", false);

    docFactory_ = new DocumentFactory();
    cmdFactory_ = new CommandFactory(this);

    recentFiles_ = new RecentFiles(this);
    
    gui_ = new TranSketchGUI(this);

  }
  
  public TranSketchGUI getGUI() {
    return gui_;
  }

  public RecentFiles getRecentFiles() {
    return recentFiles_;
  }

  public File getWorkingDirectory() {
    return workingDirectory_;
  }

  public void setWorkingDirectory(File workingDir) {
    workingDirectory_ = workingDir;
  }

  @Override
  public Editor getActiveEditor() {
    if(gui_.getActiveDocumentFrame() == null) return null;
    return gui_.getActiveDocumentFrame().getDocument().getEditor();
  }

  public void addOpenDocument(TSDocument doc) {
    Editor ed = new Editor(doc, new ActionHistory(this), gui_.getControlFrameManager());
    doc.setEditor(ed);
    gui_.newDocumentFrame(doc);
  }

  public DocumentFactory getDocumentFactory() {
    return docFactory_;
  }

  @Override
  public CommandFactory getCommandFactory() {
    return cmdFactory_;
  }

  @Override
  public boolean doCommand(TSCommand cmd) {

    if(!cmd.initialize()) return false;

    boolean success = cmd.doThis(this);

    if(success && cmd instanceof TSAction) {
      Editor ed = ((TSAction) cmd).getEditor();
      ed.getHistory().addAction((TSAction) cmd);
      gui_.updateUndoRedo(ed);
      ed.getPane().getCanvas().repaint();
    }

    return success;
  }


  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    new TranSketch();
  }

}
