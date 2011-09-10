/*
 * MapContextMenu.java
 * 
 * Created by demory on Dec 4, 2010, 10:59:40 PM
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

package org.transketch.apps.desktop.gui.editor.map;

import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.Editor;

/**
 *
 * @author demory
 */
public abstract class MapContextMenu extends JPopupMenu {

  protected TSInvoker invoker_;
  protected Editor ed_;

  //protected enum ItemAction { };

  public MapContextMenu(String name, TSInvoker ccr, Editor ed) {
    super(name);
    invoker_ = ccr;
    ed_ = ed;
  }

  public void setEditor(Editor ed) {
    ed_ = ed;
  }

  protected class Item extends JMenuItem {

    public Item(String name) {
      super(name);
      MapContextMenu.this.add(this);
    }

    public Item(String name, JMenu menu) {
      super(name);
      menu.add(this);
    }
  }

}
