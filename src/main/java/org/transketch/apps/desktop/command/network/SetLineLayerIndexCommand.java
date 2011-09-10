/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.apps.desktop.command.network;

import javax.swing.JOptionPane;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.EditorBasedCommand;
import org.transketch.apps.desktop.command.TSAction;
import org.transketch.core.network.Line;
import org.transketch.util.FPUtil;

/**
 *
 * @author demory
 */
public class SetLineLayerIndexCommand extends EditorBasedCommand implements TSAction {

  private Line line_;
  private double oldIndex_, newIndex_;

  public SetLineLayerIndexCommand(Editor ed, Line line) {
    super(ed);
    line_ = line;
  }

  @Override
  public boolean initialize() {
    String str = JOptionPane.showInputDialog("Layer index?");
    if(str == null || !FPUtil.isDouble(str)) return false;
    oldIndex_ = line_.getLayerIndex();
    newIndex_ = Double.parseDouble(str);
    return true;
  }

  public boolean doThis(TranSketch ts) {
    line_.setLayerIndex(newIndex_);
    return true;
  }

  public boolean undoThis(TranSketch ts) {
    line_.setLayerIndex(oldIndex_);
    return true;
  }

  public String getName() {
    return "Set Layer Index";
  }

}
