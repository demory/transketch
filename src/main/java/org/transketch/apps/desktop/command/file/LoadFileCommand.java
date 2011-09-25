/*
 * LoadFileCommand.java
 * 
 * Created by demory on Jan 19, 2011, 10:09:34 PM
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

package org.transketch.apps.desktop.command.file;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.Bundler;

/**
 *
 * @author demory
 */
public class LoadFileCommand extends EditorBasedCommand {

  private File file_;

  public LoadFileCommand(Editor ed) {
    super(ed);
  }

  public LoadFileCommand(Editor ed, File file) {
    super(ed);
    file_ = file;
  }



  public boolean doThis(TranSketch ts) {
    
    if(file_ == null) {
      JFileChooser chooser = new JFileChooser(ts.getWorkingDirectory());//new File(System.getProperty("user.dir")));
      FileNameExtensionFilter filter = new FileNameExtensionFilter("Transit Sketchpad Files", "tsk");
      chooser.setFileFilter(filter);
      int returnVal = chooser.showOpenDialog(ts.getGUI());
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        file_ = chooser.getSelectedFile();
        ts.setWorkingDirectory(new File(file_.getAbsolutePath()));
      }
      else return false;
    }

    if(!file_.exists()) {
      ts.getGUI().msg("File does not exist: "+file_.getPath());
      return false;
    }
    ts.getRecentFiles().addFile(file_.getPath());
    final TSDocument doc = ts.getDocumentFactory().createDocumentFromFile(file_);
    /*SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        //new Bundler(doc.getNetwork());
      }
    });*/
    //new Bundler(doc.getNetwork());
    ts.addOpenDocument(doc);
    System.out.println("opened file");
    return true;
  }


}
