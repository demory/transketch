/*
 * StopStyle.java
 * 
 * Created by demory on Nov 29, 2010, 8:02:15 PM
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

package org.transketch.core.network.stop;

import org.transketch.core.NamedItem;

/**
 *
 * @author demory
 */
public class StopStyle implements NamedItem {

  private int id_;
  private String name_;
  
  private StopRenderer renderer_;

  // any "preset" (i.e. system-defined) styles are declared here:
  public enum Preset {
    DEFAULT;
  }

  // indicates if this instance was created from a preset configuration (null if not):
  private Preset preset_ = null;

  public StopStyle() {
    name_ = "Unnamed Stop Style";
    renderer_ = new CircleRenderer();
    id_ = -1;
  }

  public StopStyle(Preset preset) {
    this();
    id_ = -1;
    switch(preset) {
      case DEFAULT:
        renderer_ = new CircleRenderer();
        break;
    }
  }

  public String getName() {
    return name_;
  }

  public void setName(String name) {
    name_ = name;
  }

  public int getID() {
    return id_;
  }

  public void setID(int id) {
    id_ = id;
  }
  
  public StopRenderer getRenderer() {
    return renderer_;
  }

  public void setRenderer(StopRenderer renderer) {
    renderer_ = renderer;
  }

  public String getXML(String indent) {
    String xml = "";
    xml += indent+"<style id=\""+id_+"\" name=\""+name_+"\" renderertype=\""+renderer_.getType()+"\" >\n";
    for(RendererProperty prop : renderer_.getProperties()) {
      xml += indent+"  "+prop.getXML();
    }
    xml += indent+"</style>\n";
    return xml;
  }
}
