/*
 * AbstractRowPanel.java
 *
 * Created by demory on Feb 27, 2011, 7:51:17 AM
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

package org.transketch.apps.desktop.gui.control;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.transketch.core.NamedItem;

/**
 *
 * @author demory
 */
public class AbstractRowPanel extends JPanel {

  protected ListControlFrame.ItemRow row_;

  public AbstractRowPanel(NamedItem item) {
    super(new BorderLayout());

    add(new JLabel(item.getName()), BorderLayout.CENTER);
  }

}
