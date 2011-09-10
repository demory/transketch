/*
 * IntegerProperty.java
 * 
 * Created by demory on Feb 27, 2011, 7:51:17 AM
 * 
 * Copyright (C) 2011 David D. Emory
 * 
 * This file is part of Transit Sketchpad. See <http://www.transketch.org>
 * for additional information regarding the project.
 * 
 * Transit Sketchpad is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

package org.transketch.core.network.stop;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author demory
 */
public class IntegerProperty extends RendererProperty<Integer> {

  private SpinnerNumberModel spModel_;

  public IntegerProperty(String key, String name, int value) {
    super(key, name, value);
    spModel_ = new SpinnerNumberModel();
  }

  @Override
  public JComponent getEditingWidget() {
    JPanel widget = new JPanel();
    spModel_.setValue(value_);
    final JSpinner spinner = new JSpinner(spModel_);
    widget.add(spinner);
    spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        //System.out.println("new val = "+ ((Integer) spinner.getValue()));
        value_ = (Integer) spinner.getValue();
      }
    });
    return widget;
  }

  @Override
  public String getXML() {
    return "<intprop key=\""+key_+"\" name=\""+name_+"\" value=\""+value_+"\" />\n";
  }

}
