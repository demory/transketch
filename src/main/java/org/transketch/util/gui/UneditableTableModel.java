/*
 * UneditableTableModel.java
 * 
 * Created by demory on Jan 19, 2009, 6:28:13 PM
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

package org.transketch.util.gui;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author demory
 */
public class UneditableTableModel extends DefaultTableModel {

  public UneditableTableModel(String[][] table, String[] colNames) {
    super(table, colNames);
  }
  
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

}
