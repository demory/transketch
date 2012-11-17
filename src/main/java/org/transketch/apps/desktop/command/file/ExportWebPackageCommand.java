/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.apps.desktop.command.file;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.EditorBasedCommand;

/**
 *
 * @author demory
 */

public class ExportWebPackageCommand extends EditorBasedCommand {

  public ExportWebPackageCommand(Editor ed) {
    super(ed);
  }

  @Override
  public boolean doThis(TranSketch ts) {
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(ed_.getPane());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      ed_.getDocument().writeJSONFile(file);
      JOptionPane.showMessageDialog(ed_.getPane(), "JSON file written");
      return true;
    }
    return false;
  }

}
