/*
 * ImportOTPFileCommand.java
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


package org.transketch.apps.desktop.command.network;

import java.io.File;
import javax.swing.JFileChooser;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.Line;
import org.transketch.core.network.otp.OTPImporter;

/**
 *
 * @author demory
 */
public class ImportOTPFileCommand extends EditorBasedCommand {

  private File file_;
  
  public ImportOTPFileCommand(Editor ed, File file) {
    super(ed);
  }

  public ImportOTPFileCommand(Editor ed) {
    this(ed, null);
  }

  @Override
  public boolean initialize() {
    if(file_ == null) {
      JFileChooser chooser = new JFileChooser();
      int returnVal = chooser.showOpenDialog(ed_.getPane());
      if (returnVal == JFileChooser.APPROVE_OPTION)
        file_ = chooser.getSelectedFile();
    }
    return file_ != null && file_.exists();
  }
  
  @Override
  public boolean doThis(TranSketch ts) {
    OTPImporter importer = new OTPImporter(ed_);
    importer.importFromFile(file_);
    for(Line line : ed_.getDocument().getNetwork().getLines())
      ts.getGUI().getControlFrameManager().getLinesFrame().addItem(line);
    ts.getGUI().getControlFrameManager().getLinesFrame().refreshList();
    return true;
  }
}
