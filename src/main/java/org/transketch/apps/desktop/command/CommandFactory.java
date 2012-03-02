/*
 * CommandFactory.java
 * 
 * Created by demory on Jan 2, 2011, 9:17:08 PM
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

import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.edit.RedoCommand;
import org.transketch.apps.desktop.command.edit.UndoCommand;
import org.transketch.apps.desktop.command.file.ClearRecentFilesCommand;
import org.transketch.apps.desktop.command.file.ExportPNGCommand;
import org.transketch.apps.desktop.command.file.ExportSVGCommand;
import org.transketch.apps.desktop.command.file.LoadFileCommand;
import org.transketch.apps.desktop.command.file.NewFileCommand;
import org.transketch.apps.desktop.command.file.SaveFileAsCommand;
import org.transketch.apps.desktop.command.file.SaveFileCommand;
import org.transketch.apps.desktop.command.network.CreateLineCommand;
import org.transketch.apps.desktop.command.network.ImportOTPCommand;
import org.transketch.apps.desktop.command.network.RebundleCommand;
import org.transketch.apps.desktop.command.system.DebugActionCommand;
import org.transketch.apps.desktop.command.system.ExitCommand;
import org.transketch.apps.desktop.command.system.ShowAboutCommand;
import org.transketch.apps.desktop.command.viewport.ZoomToExtentsCommand;

/**
 *
 * @author demory
 */
public class CommandFactory {
  
  private TranSketch ts_;

  public CommandFactory(TranSketch ts) {
    ts_ = ts;
  }

  public enum Key {

    // Menu keys:
    FILE_NEW, FILE_LOAD, FILE_SAVE, FILE_SAVE_AS, FILE_EXPORT_PNG, FILE_EXPORT_SVG, FILE_EXIT, FILE_CLEAR_RECENT,
    EDIT_UNDO, EDIT_REDO,
    TOOLS_REBUNDLE, TOOLS_IMPORT_OTP,
    VIEW_ZOOM_TO_EXTENTS,
    HELP_ABOUT,
    DEBUG,

    // Other keys:
    ZOOM_IN, ZOOM_OUT, SELECT_LINE, CREATE_LINE,;

    /*TOOLS_MOVIE, TOOLS_BUNDLER,
    CREATE_ANCHOR, DELETE_ANCHOR, MOVE_ANCHOR, CREATE_ANCHOR_BASED_STOP,
    SET_STOP_LABEL_ANGLE,
    CORRIDOR_ENDPOINT, DELETE_CORRIDOR, FLIP_CORRIDOR, SPLIT_CORRIDOR,
    CREATE_LINE, DELETE_LINE, SET_LINE_STYLE, MODIFY_LINE, EDIT_LINE_PROPS, SET_LINE_STYLE_COLORS,
    CREATE_STYLE, DELETE_STYLE, EDIT_STYLE, COPY_STYLE,
    REFRESH_CANVAS, MODIFY_EDITOR_PROPERTY;*/

  }

  public TSCommand createCommand(Key key) {
    switch(key) {
      case FILE_NEW: return new NewFileCommand();
      case FILE_LOAD: return new LoadFileCommand(ts_.getActiveEditor());
      case FILE_SAVE: return new SaveFileCommand(ts_.getActiveEditor());
      case FILE_SAVE_AS: return new SaveFileAsCommand(ts_.getActiveEditor());
      case FILE_EXPORT_PNG: return new ExportPNGCommand(ts_.getActiveEditor());
      case FILE_EXPORT_SVG: return new ExportSVGCommand(ts_.getActiveEditor());
      case FILE_CLEAR_RECENT: return new ClearRecentFilesCommand();
      case FILE_EXIT: return new ExitCommand();
      case EDIT_UNDO: return new UndoCommand(ts_.getActiveEditor());
      case EDIT_REDO: return new RedoCommand(ts_.getActiveEditor());
      case CREATE_LINE: return new CreateLineCommand(ts_.getActiveEditor());
      case TOOLS_REBUNDLE: return new RebundleCommand(ts_.getActiveEditor());
      case TOOLS_IMPORT_OTP: return new ImportOTPCommand(ts_.getActiveEditor());
      case VIEW_ZOOM_TO_EXTENTS: return new ZoomToExtentsCommand(ts_.getActiveEditor().getPane().getCanvas(), ts_.getActiveEditor().getDocument().getNetwork().getBoundingBox());
      case HELP_ABOUT: return new ShowAboutCommand();
      case DEBUG: return new DebugActionCommand(ts_.getActiveEditor());
    }
    return null;
  }

}
