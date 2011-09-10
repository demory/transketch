/*
 * RecentFiles.java
 * 
 * Created by demory on Jan 19, 2011, 10:13:01 PM
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

package org.transketch.apps.desktop;

import java.util.LinkedList;
import java.util.List;
import org.transketch.util.SysProps;

/**
 *
 * @author demory
 */
public class RecentFiles {

  private TranSketch ts_;
  private List<String> filenames_;

  public RecentFiles(TranSketch ts) {
    ts_ = ts;
    filenames_ = new LinkedList<String>();
    String rFilesStr = ts_.getProperties().getProperty(SysProps.RECENT_FILES);
    if(rFilesStr != null && rFilesStr.length() > 0) {
      for(String filename : rFilesStr.split(";"))
        filenames_.add(filename);
    }
  }

  public List<String> getFilenames() {
    return filenames_;
  }

  public void addFile(String filename) {
    if(filenames_.contains(filename)) filenames_.remove(filename);
    filenames_.add(0, filename);
    while(filenames_.size() > 10) {
      filenames_.remove(filenames_.size()-1);
    }
    String rFilesStr = "";
    for(String fn : filenames_) {
      rFilesStr += (fn + ";");
    }
    rFilesStr = rFilesStr.substring(0, rFilesStr.length()-1);
    ts_.getProperties().setProperty(SysProps.RECENT_FILES, rFilesStr);
    ts_.writeProperties();
    ts_.getGUI().getTSMenuBar().updateRecentFilesMenu(filenames_);
  }

  public void clear() {
    filenames_.clear();
    ts_.getProperties().setProperty(SysProps.RECENT_FILES, "");
    ts_.writeProperties();
    ts_.getGUI().getTSMenuBar().updateRecentFilesMenu(filenames_);
  }
}
