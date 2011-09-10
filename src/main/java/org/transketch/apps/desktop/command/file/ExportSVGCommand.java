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
public class ExportSVGCommand extends EditorBasedCommand {

  public ExportSVGCommand(Editor ed) {
    super(ed);
  }

  public boolean doThis(TranSketch ts) {
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter("SVG Files", "svg");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(ed_.getPane());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      ed_.getDocument().writeSVGFile(file, ed_.getPane().getCanvas().getCoordinates());
      JOptionPane.showMessageDialog(ed_.getPane(), "SVG file written");
      return true;
    }
    return false;
  }

}
