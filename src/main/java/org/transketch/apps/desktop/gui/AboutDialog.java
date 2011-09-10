/*
 * AboutDialog.java
 * 
 * Created by demory on Feb 14, 2011, 8:12:43 PM
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

package org.transketch.apps.desktop.gui;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import org.apache.commons.io.IOUtils;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.util.gui.GUIFactory;

/**
 *
 * @author demory
 */
public class AboutDialog extends JDialog {

  public AboutDialog(Frame owner) {
    super(owner, "About");

    JPanel mainPanel = GUIFactory.newColumnPanel();


    JPanel info = GUIFactory.newColumnPanel();
    info.add(new JLabel("Transit Sketchpad, version "+TranSketch.VERSION, SwingConstants.LEFT));
    info.add(new JLabel("by David D. Emory (demory@transketch.org)", SwingConstants.LEFT));
    info.setAlignmentX(LEFT_ALIGNMENT);
    mainPanel.add(info);
    
    mainPanel.add(Box.createVerticalStrut(10));

    JTextArea license = new JTextArea(12, 80);
    license.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
    license.setBorder(new EmptyBorder(4, 4, 4, 4));
    JScrollPane pane = new JScrollPane(license);
    pane.setBorder(new BevelBorder(BevelBorder.LOWERED));
    pane.setAlignmentX(LEFT_ALIGNMENT);
    StringWriter writer = new StringWriter();
    try {
      IOUtils.copy(TranSketch.class.getResourceAsStream("/gpl-3.0.txt"), writer, "UTF-8");
    } catch (IOException ex) {
      Logger.getLogger(AboutDialog.class.getName()).log(Level.SEVERE, null, ex);
    }
    license.setText(writer.toString());
    license.setCaretPosition(0);
    license.setEditable(false);
    mainPanel.add(pane);

    mainPanel.add(Box.createVerticalStrut(10));

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    JPanel buttonRow = GUIFactory.newRowPanel();
    buttonRow.add(Box.createHorizontalGlue());
    buttonRow.add(closeButton);
    buttonRow.add(Box.createHorizontalGlue());
    buttonRow.setAlignmentX(LEFT_ALIGNMENT);
    mainPanel.add(buttonRow);


    mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
    add(mainPanel);


    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

}
