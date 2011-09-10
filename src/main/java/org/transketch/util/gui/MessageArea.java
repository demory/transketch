/*
 * MessageArea.java
 * 
 * Created by demory on Mar 28, 2009, 1:04:28 PM
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

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author demory
 */
public class MessageArea extends JPanel {

  private JTextArea msgBox_;
  private JScrollPane msgPane_;

  public MessageArea() {
    super(new BorderLayout());

    msgBox_ = new JTextArea();
    msgBox_.setLineWrap(true);
    msgBox_.setWrapStyleWord(true);
    msgPane_ = new JScrollPane(msgBox_);
    msgPane_.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    msgPane_.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    msgPane_.setBorder(BorderFactory.createLoweredBevelBorder());

    add(msgPane_, BorderLayout.CENTER);

  }

  public MessageArea(int rows, int columns) {
    super();

    msgBox_ = new JTextArea(rows, columns);
    msgBox_.setLineWrap(true);
    msgBox_.setWrapStyleWord(true);
    msgPane_ = new JScrollPane(msgBox_);
    msgPane_.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    msgPane_.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    msgPane_.setBorder(BorderFactory.createLoweredBevelBorder());

    add(msgPane_);
  }

  public void msg(String text) {
    msg(text, true);
  }

  public void msg(String text, boolean cr) {
    msgBox_.setText(msgBox_.getText() + (cr ? "\n" : "") + text);
    //msgPane_.paintImmediately(0, 0, msgPane_.getWidth(), msgPane_.getHeight());
  }
}
