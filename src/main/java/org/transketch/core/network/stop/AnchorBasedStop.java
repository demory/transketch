/*
 * AnchorBasedStop.java
 * 
 * Created by demory on Apr 11, 2010, 1:39:30 PM
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

import java.util.Collection;
import java.util.Map;
import org.transketch.core.network.AnchorPoint;
import org.transketch.core.network.TSNetwork;
import org.transketch.core.network.corridor.Corridor;
import org.transketch.util.viewport.MapCoordinates;

/**
 *
 * @author demory
 */
public class AnchorBasedStop extends Stop {

  private AnchorPoint anchor_;

  public AnchorBasedStop(Integer id, String name) {
    super(id, name);
  }

  public AnchorBasedStop(int id, AnchorPoint anchor, String name) {
    super(id, name);
    anchor_ = anchor;
  }

  @Override
  public Type getType() {
    return Stop.Type.ANCHORBASED;
  }

  @Override
  public double getWorldX() {
    return anchor_.getX();
  }

  @Override
  public double getWorldY() {
    return anchor_.getY();
  }

  @Override
  public int getScreenX(MapCoordinates coords) {
    return coords.xToScreen(getWorldX()) + (int) anchor_.getBundleOffset().getX();
  }

  @Override
  public int getScreenY(MapCoordinates coords) {
    return coords.yToScreen(getWorldY()) - (int) anchor_.getBundleOffset().getY();    
  } 
  
  @Override
  public Collection<Corridor> getCorridors() {
    return anchor_.getCorridors();
  }

  @Override
  public String toString() {
    return name_ + " Station (fixed to Anchor "+anchor_.getID()+")";
  }

  public String getTypePropertiesXML(String indent) {
    String xml = "";
    xml += indent + "<property name=\"anchor\" value=\""+anchor_.getID()+"\" />\n";
    return xml;
  }

  @Override
  public void applyTypeProperties(Map<String, String> properties, TSNetwork network) {
    if(properties.containsKey("anchor")) {
      anchor_ = network.getAnchorPoint(new Integer(properties.get("anchor")));
    }
  }

  public AnchorPoint getAnchorPoint() {
    return anchor_;
  }
}
