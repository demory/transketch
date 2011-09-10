/*
 * ActionHistory.java
 * 
 * Created by demory on Oct 14, 2009, 9:48:23 PM
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

package org.transketch.apps.desktop.command;

import java.util.Stack;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.Editor;

/**
 *
 * @author demory
 */
public class ActionHistory {

  private TranSketch ts_;
  private Editor ed_;
  private Stack<TSAction> undoStack_,  redoStack_;

  private TSAction lastSavedAction_ = null;

  public ActionHistory(TranSketch ts) {
    ts_ = ts;
    undoStack_ = new Stack<TSAction>();
    redoStack_ = new Stack<TSAction>();
  }

  public void setEditor(Editor ed) {
    ed_ = ed;
  }

  public void addAction(TSAction action) {
    undoStack_.add(action);
    redoStack_.clear();
    ts_.getGUI().updateUndoRedo(ed_);
  }

  public boolean undoActionExists() {
    return !undoStack_.isEmpty();
  }

  public boolean redoActionExists() {
    return !redoStack_.isEmpty();
  }

  public String undoActionName() {
    if (undoStack_.isEmpty()) {
      return "";
    }
    return undoStack_.peek().getName();
  }

  public String redoActionName() {
    if (redoStack_.isEmpty()) {
      return "";
    }
    return redoStack_.peek().getName();
  }

  public void undoLast() {
    if (undoStack_.isEmpty()) {
      ts_.getGUI().msg("No actions to undo!");
      return;
    }
    TSAction action = undoStack_.peek();
    try {
      action.undoThis(ts_);
    } catch (UnsupportedOperationException uoe) {
      ts_.getGUI().msg("Last action cannot be undone");
      return;
    }
    redoStack_.add(undoStack_.pop());
    ts_.getGUI().updateUndoRedo(ed_);
  }

  public void redoLast() {
    if (redoStack_.isEmpty()) {
      ts_.getGUI().msg("No actions to redo!");
      return;
    }
    TSAction action = redoStack_.peek();
    try {
      action.doThis(ts_);
    } catch (UnsupportedOperationException uoe) {
      ts_.getGUI().msg("Last action cannot be redone");
      return;
    }
    undoStack_.add(redoStack_.pop());
    ts_.getGUI().updateUndoRedo(ed_);
  }

  public void fileSaved() {
    lastSavedAction_ = undoActionExists() ? undoStack_.peek() : null;
  }

  public boolean isModified() {
    //if(lastSavedAction_ == null && !undoActionExists()) return
    return lastSavedAction_ != (undoActionExists() ? undoStack_.peek() : null);
  }
}