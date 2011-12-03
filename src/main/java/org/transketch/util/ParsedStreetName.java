/**
 * ParsedStreetName.java
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
package org.transketch.util;

import org.transketch.util.FPUtil;

public class ParsedStreetName {

  private String dir_,  name_,  type_;
  private int quad_;

  public ParsedStreetName(String dir, String name, String type, int quad) {
    dir_ = dir;
    name_ = name;
    type_ = type;
    quad_ = quad;
  }

  public String getDirPrefix() {
    return dir_;
  }

  public String getName() {
    return name_;
  }

  public String getFacType() {
    return type_;
  }

  public int getQuadrant() {
    return quad_;
  }

  public String getFullName() {
    return (dir_.length() > 0 ? dir_ + " " : "") + name_ + (type_.length() > 0 ? " " + type_ : "") + (quad_ > 0 ? " " + FPUtil.getQuadrant(quad_) : "");
  }

  public boolean compareTo(ParsedStreetName ssn) {
    boolean nameOK = (this.getName().equals(ssn.getName()));
    boolean quadOK = (this.getQuadrant() == 0 || this.getQuadrant() == ssn.getQuadrant());
    boolean dirOK = (this.getDirPrefix().length() == 0 || this.getDirPrefix().equals(ssn.getDirPrefix()));
    boolean typeOK = (this.getFacType().length() == 0 || this.getFacType().equals(ssn.getFacType()));
    return (nameOK && quadOK && dirOK && typeOK);
  }
}
	


















