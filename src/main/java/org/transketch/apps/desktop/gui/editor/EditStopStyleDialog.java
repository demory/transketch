/*
 * EditStopStyleDialog.java
 * 
 * Created by demory on Feb 26, 2011, 8:46:59 PM
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

package org.transketch.apps.desktop.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import org.transketch.core.network.stop.RendererProperty;
import org.transketch.core.network.stop.Stop;
import org.transketch.core.network.stop.StopRenderer;
import org.transketch.core.network.stop.StopRendererTemplate;
import org.transketch.core.network.stop.StopStyle;
import org.transketch.util.gui.GUIFactory;

/**
 *
 * @author demory
 */
public class EditStopStyleDialog extends JDialog implements ActionListener {

  private StopStyle style_;

  private JPanel propsList_;
  private JTextField nameField_;
  private JComboBox rendererCB_;
  private JButton okBtn_, cancelBtn_;

  private StopRenderer renderer_;

  private boolean okPressed_ = false;

  public EditStopStyleDialog(JFrame parent, StopStyle style) {
    super(parent, "Edit Style", true);

    style_ = style;
    
    // init renderer
    try {
      //System.out.println("class="+stopStyle_.getRendererType().className_);
      Class cl = style_.getRendererType().getRendererClass();
      Constructor co = cl.getConstructor(new Class[] {Stop.class, style.getRendererType().getTemplateClass() } );
      renderer_ = (StopRenderer) co.newInstance(new Object[] { null, style_.getTemplate().clone() } );
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.getLogger(Stop.class.getName()).log(Level.SEVERE, null, ex);
    }

    JPanel previewPanel = new JPanel();
    previewPanel.setBorder(new TitledBorder("Preview"));
    previewPanel.add(Box.createRigidArea(new Dimension(50, 50)));

    rendererCB_ = new JComboBox();
    int toSelect = 0, i = 0;
    for(StopRenderer.Type rendererType: StopRenderer.Type.values()) {
      if(style.getRendererType() == rendererType) toSelect = i;
      rendererCB_.addItem(rendererType);
      i++;
    }
    rendererCB_.setSelectedIndex(toSelect);
    rendererCB_.addActionListener(this);

    JPanel rendererRow = new JPanel(new BorderLayout());
    rendererRow.setBorder(new EmptyBorder(0, 0, 5, 0));
    rendererRow.add(GUIFactory.newLabel("Renderer: "), BorderLayout.WEST);
    rendererRow.add(rendererCB_, BorderLayout.CENTER);

    propsList_ = GUIFactory.newColumnPanel();
    propsList_.setBorder(new EmptyBorder(5, 0, 0, 0));
    //propsList_.add(new JLabel("property"));

    JPanel propsPanel = new JPanel(new BorderLayout());
    propsPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
    propsPanel.add(rendererRow, BorderLayout.NORTH);
    propsPanel.add(new JScrollPane(propsList_), BorderLayout.CENTER);


    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
    topPanel.add(previewPanel, BorderLayout.WEST);
    topPanel.add(propsPanel, BorderLayout.CENTER);



    JPanel bottomRow = GUIFactory.newRowPanel();
    nameField_ = GUIFactory.newTextField(style.getName(), 50, 0);
    okBtn_ = GUIFactory.newButton("OK", 60, this);
    cancelBtn_ = GUIFactory.newButton("Cancel", 60, this);
    bottomRow.add(new JLabel("Name: "));
    bottomRow.add(nameField_);
    bottomRow.add(Box.createHorizontalStrut(25));
    bottomRow.add(okBtn_);
    bottomRow.add(Box.createHorizontalStrut(5));
    bottomRow.add(cancelBtn_);


    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel, BorderLayout.CENTER);
    mainPanel.add(bottomRow, BorderLayout.SOUTH);
    mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    //rendererTypeSelected();
    showRendererProperties();

    getContentPane().add(mainPanel);
    setSize(360, 240);
    setLocationRelativeTo(parent);
    setVisible(true);


  }

  public void actionPerformed(ActionEvent e) {

    if(e.getSource() == rendererCB_) {
      rendererTypeSelected();
    }

    if(e.getSource() == okBtn_) {
      okPressed_ = true;
      setVisible(false);
    }
    if(e.getSource() == cancelBtn_) {
      setVisible(false);
    }
  }

  public boolean okPressed() {
    return okPressed_;
  }

  public String getStyleName() {
    return nameField_.getText();
  }

  public StopRenderer.Type getRendererType() {
    return (StopRenderer.Type) rendererCB_.getSelectedItem();
  }
  
  public StopRendererTemplate getRendererTemplate() {
    return renderer_.getTemplate();
  }

  private void rendererTypeSelected() {
    propsList_.removeAll();
    //System.out.println("selected: "+rendererCB_.getSelectedItem());
    
    StopRenderer.Type type = (StopRenderer.Type) rendererCB_.getSelectedItem();

    StopRenderer renderer = null;

    try {
      //System.out.println("class="+stopStyle_.getRendererType().className_);
      Class cl = type.getRendererClass();
      Constructor co = cl.getConstructor(new Class[] {Stop.class, type.getTemplateClass() } );
      renderer_ = (StopRenderer) co.newInstance(new Object[] { null, type.getTemplateClass().newInstance() } );
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.getLogger(Stop.class.getName()).log(Level.SEVERE, null, ex);
    }

    showRendererProperties();
  }

  public void showRendererProperties() {

    for(RendererProperty prop : renderer_.getTemplate().getProperties()) {
      JPanel row = GUIFactory.newRowPanel();
      row.setMaximumSize(new Dimension(200, 25));
      row.add(new JLabel(prop.getName()));
      row.add(Box.createHorizontalStrut(5));
      row.add(prop.getEditingWidget());
      row.add(Box.createHorizontalGlue());
      row.setAlignmentX(LEFT_ALIGNMENT);
      propsList_.add(row);
    }

    propsList_.add(Box.createVerticalGlue());

    propsList_.revalidate();
    propsList_.repaint();
  }

}
