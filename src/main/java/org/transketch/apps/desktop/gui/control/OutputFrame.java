/*
 * OutputFrame.java
 * 
 * Created by demory on Nov 28, 2009, 11:41:38 AM
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

import java.awt.BorderLayout;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.gui.TranSketchGUI;
import org.transketch.util.gui.MessageArea;

/**
 *
 * @author demory
 */
public class OutputFrame extends ControlFrame {

  private MessageArea messages_;

  public OutputFrame(TSInvoker cep, TranSketchGUI gui, ControlFrameManager cfm) {
    super(cep, gui, cfm, "Output");

    messages_ = new MessageArea();
    setLayout(new BorderLayout());
    add(messages_, BorderLayout.CENTER);
    pack();
    setSize(200, 200);
  }

  public void msg(String msg) {
    messages_.msg(msg);
  }

}
