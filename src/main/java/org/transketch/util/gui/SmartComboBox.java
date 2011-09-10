/*
 * SmartComboBox.java
 * 
 * Created by demory on Oct 23, 2010, 9:50:52 PM
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

package org.transketch.util.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComboBox;

/**
 *
 * @author demory
 */
public class SmartComboBox extends JComboBox {

  Container parent;

  public SmartComboBox() {
    super();

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        if(parent == null) {
          parent = SmartComboBox.this.getParent();
          parent.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
              parentResized();
            }
          });
        }
      }
    });
  }

  private void parentResized() {
    int pw = parent.getSize().width;
    for(Component c : parent.getComponents()) {
      if(c != this) pw -= c.getSize().width;
    }

    System.out.println("parent resized to "+pw);
    setMaximumSize(new Dimension(pw, getHeight()));
    setSize(new Dimension(pw, getHeight()));
    System.out.println("setSize");
  }

}
