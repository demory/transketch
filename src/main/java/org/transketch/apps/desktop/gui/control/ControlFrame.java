/*
 * ControlFrame.java
 * 
 * Created by demory on Nov 28, 2009, 7:37:07 AM
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

package org.transketch.apps.desktop.gui.control;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.gui.TranSketchGUI;

/**
 *
 * @author demory
 */

public class ControlFrame extends JInternalFrame implements Comparable {

  protected TSInvoker invoker_;
  protected TranSketchGUI gui_;

  //private String name_;

  private JCheckBoxMenuItem menuItem_;

  public ControlFrame(TSInvoker cep, TranSketchGUI gui, final ControlFrameManager cfm, String title) {
    super(title, true, true);
    invoker_ = cep;
    gui_ = gui;
    //name_ = name;

    this.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
    addInternalFrameListener(new InternalFrameAdapter() {

      @Override
      public void internalFrameOpened(InternalFrameEvent e) {
        menuItem_.setSelected(true);
        setLocation(cfm.getPlacementPoint(ControlFrame.this));
      }

      @Override
      public void internalFrameClosing(InternalFrameEvent e) {
        menuItem_.setSelected(false);
      }
    });
    
    cfm.addFrame(this);
    gui.getDesktop().add(this);
    this.setLayer(JLayeredPane.PALETTE_LAYER);
  }

  public void setMenuItem(JCheckBoxMenuItem menuItem) {
    menuItem_ = menuItem;
  }

  //public void editorChanged(Editor oldEd, Editor newEd) { }

  public void documentChanged(TSDocument oldDoc, TSDocument newDoc) { }

  public int compareTo(Object o) {
    return getTitle().compareTo(((ControlFrame) o).getTitle());
  }


}
