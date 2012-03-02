/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package org.transketch.apps.desktop.command.network;

import java.io.File;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.core.network.Line;
import org.transketch.core.network.otp.OTPImporter;

/**
 *
 * @author demory
 */
public class ImportOTPCommand extends EditorBasedCommand {

  public ImportOTPCommand(Editor ed) {
    super(ed);
  }

  @Override
  public boolean doThis(TranSketch ts) {
    OTPImporter importer = new OTPImporter(ed_);
    importer.importFromFile(new File("/home/demory/otp/temp/pdxroutes5.json"));
    for(Line line : ed_.getDocument().getNetwork().getLines())
      ts.getGUI().getControlFrameManager().getLinesFrame().addItem(line);
    ts.getGUI().getControlFrameManager().getLinesFrame().refreshList();
    return true;
  }
}
