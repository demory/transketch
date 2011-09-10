/*
 * ExportCommand.java
 * 
 * Created by demory on Feb 7, 2011, 11:55:02 PM
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
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.EditorBasedCommand;

/**
 *
 * @author demory
 */
public class ExportPNGCommand extends EditorBasedCommand {

  public ExportPNGCommand(Editor ed) {
    super(ed);
  }

  public boolean doThis(TranSketch ts) {
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Files", "png");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(ed_.getPane());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      ed_.getDocument().writePNGFile(file, ed_.getPane().getCanvas().getCoordinates());
      JOptionPane.showMessageDialog(ed_.getPane(), "PNG file written");
      return true;
    }
    return false;
  }

}
