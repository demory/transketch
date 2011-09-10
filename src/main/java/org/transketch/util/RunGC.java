/**
 * RunGC.java
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

import java.awt.event.*;
import javax.swing.Timer;

public class RunGC implements ActionListener {

  private Runtime runtime_;
  private Timer timer_;

  public RunGC(int ms) {
    runtime_ = Runtime.getRuntime();
    timer_ = new Timer(500, this);
    timer_.setRepeats(false);
    timer_.start();

  }

  public void actionPerformed(ActionEvent e) {
    System.out.println("Direct call to GC");
    System.out.println("mem before gc: " + (runtime_.totalMemory() - runtime_.freeMemory()));
    System.gc();
    System.out.println("mem after gc: " + (runtime_.totalMemory() - runtime_.freeMemory()));
  }
}





















