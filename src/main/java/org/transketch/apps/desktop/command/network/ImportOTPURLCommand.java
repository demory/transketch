/*
 * ImportOTPURLCommand.java
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

import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JOptionPane;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.Line;
import org.transketch.core.network.otp.OTPImporter;

public class ImportOTPURLCommand extends EditorBasedCommand {

  private URL url_;
  
  public ImportOTPURLCommand(Editor ed, URL url) {
    super(ed);
  }

  public ImportOTPURLCommand(Editor ed) {
    this(ed, null);
  }

  @Override
  public boolean initialize() {
    if(url_ == null) {
      try {
        url_ = new URL(JOptionPane.showInputDialog("URL:"));
      } catch (MalformedURLException ex) {
        JOptionPane.showMessageDialog(null, "Malformed URL");
        return false;
      }
    }
    return url_ != null;
  }
  
  @Override
  public boolean doThis(TranSketch ts) {
    OTPImporter importer = new OTPImporter(ed_);
    ed_.getPane().setStatusText("Loading OTP data..");
    importer.importFromURL(url_);
    for(Line line : ed_.getDocument().getNetwork().getLines())
      ts.getGUI().getControlFrameManager().getLinesFrame().addItem(line);
    ts.getGUI().getControlFrameManager().getLinesFrame().refreshList();
    ed_.getPane().setStatusText("");
    return true;
  }
}
