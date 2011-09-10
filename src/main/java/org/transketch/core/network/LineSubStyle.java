/*
 * LineSubStyle.java
 * 
 * Created by demory on Mar 19, 2011, 10:44:49 PM
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

package org.transketch.core.network;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author demory
 */
public class LineSubStyle {

  private List<LineStyleLayer> layers_;
  private int envelope_ = 0;


  public LineSubStyle() {
    layers_ = new LinkedList<LineStyleLayer>();
  }

  public LineSubStyle(List<LineStyleLayer> layers) {
    setLayers(layers);
  }

  public LineSubStyle getCopy() {

    LineSubStyle copy = new LineSubStyle();

    for(LineStyleLayer layer : layers_) {
      if(layer.getColorMode() == LineStyleLayer.ColorMode.HARD_CODED)
        copy.addLayer(new LineStyleLayer(layer.getWidth(), layer.getColor(), layer.getDash()));
      else
        copy.addLayer(new LineStyleLayer(layer.getWidth(), layer.getColorKey(), layer.getDash()));
    }

    copy.envelope_ = envelope_;

    return copy;

  }

  public int getEnvelope() {
    return envelope_;
  }

  public void setEnvelope(int envelope) {
    envelope_ = envelope;
  }


  public List<LineStyleLayer> getLayers() {
    return layers_;
  }

  public void setLayers(List<LineStyleLayer> substyles) {
    layers_ = substyles;
    for(LineStyleLayer layer : layers_)
      if(layer.getWidth() > envelope_) envelope_ = layer.getWidth();
  }

  public boolean isKeyBased() {
    return isKeyBased(layers_);
  }

  public static boolean isKeyBased(List<LineStyleLayer> subs) {
    for(LineStyleLayer sub : subs)
      if(sub.getColorMode() == LineStyleLayer.ColorMode.KEY_SPECIFIED) return true;
    return false;
  }

  public void addLayer(LineStyleLayer layer) {
    layers_.add(layer);
    if(layer.getWidth() > envelope_) envelope_ = layer.getWidth();
  }

  public int getMaxLayerWidth() {
    int max = 0;
    for(LineStyleLayer style : layers_) max = Math.max(max, style.getWidth());
    return max;
  }

  public Set<String> getColorKeys() {
    Set<String> keys = new HashSet<String>();
    for(LineStyleLayer style : layers_)
      if(style.getColorMode() == LineStyleLayer.ColorMode.KEY_SPECIFIED) keys.add(style.getColorKey());
    return keys;
  }

  public LineStyleLayer getHighlight() {
    return new LineStyleLayer(getMaxLayerWidth()+4, Color.yellow);
  }

  public String getXML(String indent) {
    String xml = "";
    xml += indent+"<substyle envelope=\""+envelope_+"\">\n";
    for(LineStyleLayer layer : layers_) {
      String color = (layer.getColorMode() == LineStyleLayer.ColorMode.HARD_CODED) ?
        ""+layer.getColor().getRGB() : "$"+layer.getColorKey();
      xml += indent+"  <layer color=\""+color+"\" width=\""+layer.getWidth()+"\" dash=\""+layer.getDashStr()+"\" />\n";
    }
    xml += indent+"</substyle>\n";
    return xml;
  }

}
