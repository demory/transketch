/*
 * RendererProp.java
 * 
 * Created by demory on Feb 27, 2011, 7:47:55 AM
 * 
 * Copyright (C) 2011 David D. Emory
 * 
 * This file is part of Transit Sketchpad. See <http://www.transketch.org>
 * * for additional information regarding the project.
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

/**
 *
 * @author demory
 */
public abstract class RendererProperty<V> {

  protected String key_, name_;
  protected V value_;

  public RendererProperty(String key, String name, V defaultVal) {
    key_ = key;
    name_ = name;
    value_ = defaultVal;
  }

  public String getName() {
    return name_;
  }

  public V getValue() {
    return value_;
  }

  public void setValue(V val) {
    value_ = val;
  }

  public abstract JComponent getEditingWidget();

  public abstract String getXML();

}
