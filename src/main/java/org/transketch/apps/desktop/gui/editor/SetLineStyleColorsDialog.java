/*
 * SetLineStyleColorsDialog.java
 * 
 * Created by demory on Dec 6, 2010, 8:52:54 PM
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

package org.transketch.apps.desktop.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import org.transketch.core.network.Line;
import org.transketch.util.gui.ColorChooserDialog;
import org.transketch.util.gui.GUIFactory;

/**
 *
 * @author demory
 */
public class SetLineStyleColorsDialog extends JDialog implements ActionListener{

  private boolean okPressed_ = false;

  private Set<ColorRow> rows_;

  private JButton okBtn_, cancelBtn_;

  public SetLineStyleColorsDialog(JFrame parent, Line line) {
    super(parent, "Set LineStyle Colors", true);

    rows_ = new HashSet<ColorRow>();

    JPanel mainColumn = GUIFactory.newColumnPanel();
    for(String key : line.getStyle().getColorKeys()) {
      ColorRow row = new ColorRow(key, line.getStyleColor(key));
      mainColumn.add(row);
      rows_.add(row);
    }
    JScrollPane scrollPane = new JScrollPane(mainColumn);
    scrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));


    JPanel buttonRow = GUIFactory.newRowPanel();
    okBtn_ = GUIFactory.newButton("OK", 60, this);
    cancelBtn_ = GUIFactory.newButton("Cancel", 60, this);
    buttonRow.add(Box.createHorizontalGlue());
    buttonRow.add(okBtn_);
    buttonRow.add(Box.createHorizontalStrut(5));
    buttonRow.add(cancelBtn_);
    buttonRow.add(Box.createHorizontalStrut(5));
    buttonRow.add(Box.createHorizontalGlue());

    JPanel mainPanel = new JPanel(new BorderLayout());

    mainPanel.add(scrollPane, BorderLayout.CENTER);
    mainPanel.add(buttonRow, BorderLayout.SOUTH);

    getContentPane().add(mainPanel);
    setSize(200,200);
    setLocationRelativeTo(parent);
    setVisible(true);
  }

  public Map<String, Color> getColorMap() {
    Map<String, Color> map = new HashMap<String, Color>();
    for(ColorRow row : rows_) {
      map.put(row.key_, row.colorSwatch_.getBackground());
    }
    return map;
  }

  public boolean okPressed() {
    return okPressed_;
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == okBtn_) {
      okPressed_ = true;
      setVisible(false);
    }
    if(e.getSource() == cancelBtn_) {
      setVisible(false);
    }
  }

  private class ColorRow extends JPanel {

    private String key_;
    private JPanel colorSwatch_;
    private JButton changeBtn_;

    public ColorRow(String key, Color color) {
      super();
      
      key_ = key;

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setAlignmentX(LEFT_ALIGNMENT);

      add(new JLabel(key));
      colorSwatch_ = new JPanel();
      colorSwatch_.setPreferredSize(new Dimension(18,18));
      colorSwatch_.setMaximumSize(new Dimension(18,18));
      colorSwatch_.setBorder(new BevelBorder(BevelBorder.LOWERED));
      colorSwatch_.setBackground(color);
      add(Box.createHorizontalStrut(5));
      add(colorSwatch_);
      add(Box.createHorizontalStrut(5));
      changeBtn_ = GUIFactory.newButton("Change..", 60);
      changeBtn_.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ColorChooserDialog chooserDialog = new ColorChooserDialog(SetLineStyleColorsDialog.this, colorSwatch_.getBackground());
          colorSwatch_.setBackground(chooserDialog.getColor());
        }
      });
      add(changeBtn_);
    }


  }
}
