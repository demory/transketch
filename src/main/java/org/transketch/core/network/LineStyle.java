/*
 * LineStyle.java
 * 
 * Created by demory on Apr 1, 2009, 7:52:41 AM
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

package org.transketch.core.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.transketch.core.NamedItem;

/**
 *
 * @author demory
 */
public class LineStyle implements Comparable<LineStyle>, LineStyleView, NamedItem {

  private int id_;
  private String name_;

  private List<LineSubStyle> subStyles_ = new ArrayList<LineSubStyle>();
  private List<Double> subStyleBreakpoints_ = new ArrayList<Double>();

  private LineSubStyle activeSub_;

  public enum Preset {
    DEFAULT;
  }

  // indicates if this instance was created from a preset configuration (null if not):
  private Preset preset_ = null;


  public LineStyle() {
    name_ = "Unnamed Line Style";
    LineSubStyle sub = new LineSubStyle();
    sub.addLayer(new LineStyleLayer(10, Color.BLACK));
    subStyles_.add(sub);
    activeSub_ = sub;
  }

  public LineStyle(int id, String name) {
    id_ = id;
    name_ = name;
  }

  public LineStyle(Preset preset) {
    this();
    id_ = -1;
    switch(preset) {
      case DEFAULT:
        name_ = "DEFAULT";
        LineSubStyle sub = new LineSubStyle();
        sub.addLayer(new LineStyleLayer(10, Color.BLACK));
        break;
    }
    preset_ = preset;
  }

  public LineStyle getCopy() {
    LineStyle copy = new LineStyle(0, "Copy of "+name_);

    /*for(LineStyleLayer layer : layers_) {
      if(layer.getColorMode() == LineStyleLayer.ColorMode.HARD_CODED)
        copy.addLayer(new LineStyleLayer(layer.getWidth(), layer.getColor(), layer.getDash()));
      else
        copy.addLayer(new LineStyleLayer(layer.getWidth(), layer.getColorKey(), layer.getDash()));
    }*/


    return copy;
  }

  public List<LineSubStyle> getSubStyles() {
    return subStyles_;
  }

  /*public LineSubStyle getSubStyle(double reso) {
    return subStyles_.get(0);
  }*/

  public List<Double> getBreakpoints() {
    return subStyleBreakpoints_;
  }

  public void setBreakpoints(List<Double> breakpoints) {
    subStyleBreakpoints_ = breakpoints;
  }

  public LineStyleAttributes getAttributes() {
    return new LineStyleAttributes(this);
  }

  public void setAttributes(LineStyleAttributes backup) {
    name_ = backup.name_;
    subStyles_ = backup.subStyles_;
    subStyleBreakpoints_ = backup.subStyleBounds_;
  }

  public int getID() {
    return id_;
  }

  public String getIDForFile() {
    if(preset_ != null) return preset_.name();
    else return ""+id_;
  }

  public void setID(int id) {
    id_ = id;
  }

  public String getName() {
    return name_;
  }

  public void setName(String name) {
    name_ = name;
  }

  public Set<String> getColorKeys() {
    Set<String> keys = new HashSet<String>();
    for(LineSubStyle sub : subStyles_) keys.addAll(sub.getColorKeys());
    return keys;
  }

  public Collection<LineSubStyle> subStyles() {
    return subStyles_;
  }

  public void addSubStyle(LineSubStyle sub) {
    System.out.println("adding substyle");
    subStyles_.add(sub);
  }

  public LineSubStyle getActiveSubStyle() {
    if(activeSub_ == null) activeSub_ = subStyles_.get(0);
    return activeSub_;
  }
  
  public boolean updateActiveSubStyle(double reso) {
    int i = 0;
    LineSubStyle oldAS = activeSub_;
    for(double bp : subStyleBreakpoints_) {
      if(reso < bp) {
        activeSub_ = subStyles_.get(i);
        return (oldAS != activeSub_);
      }
      i++;
    }
    activeSub_ = subStyles_.get(i);
    return (oldAS != activeSub_);
  }

  public List<LineStyleLayer> getLayers() {
    if(getActiveSubStyle() == null) return new LinkedList<LineStyleLayer>();
    return getActiveSubStyle().getLayers();
  }
  
  private String compareStr() {
    return name_ + "_" + id_;
  }

  public int compareTo(LineStyle o) {
    return compareStr().compareTo(o.compareStr());
  }

  public String getXML(String indent) {
    String xml = "";
    xml += indent+"<style id=\""+id_+"\" name=\""+name_+"\">\n";
    String bounds = "";
    for(int i=0; i < subStyleBreakpoints_.size(); i++) {
      bounds += subStyleBreakpoints_.get(i)+(i < subStyleBreakpoints_.size()-1 ? "," : "");
    }
    xml += indent+"  <substyles breakpoints=\""+bounds+"\">\n";
    for(LineSubStyle sub : subStyles_)
      xml += sub.getXML(indent+"    ");
    xml += indent+"  </substyles>\n";
    xml += indent+"</style>\n";
    return xml;
  }

  public static class LineStyleAttributes {
    String name_;
    List<LineSubStyle> subStyles_;
    List<Double> subStyleBounds_;

    public LineStyleAttributes(LineStyle style) {
      this(style.name_, style.subStyles_, style.subStyleBreakpoints_);
    }

    public LineStyleAttributes(String name, List<LineSubStyle> subStyles, List<Double> subStyleBounds) {
      name_ = name;
      subStyles_ = subStyles;
      subStyleBounds_ = subStyleBounds;
    }


  }

}
