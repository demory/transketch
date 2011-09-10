/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.util.gui;

import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author demory
 */
public class EditTextFieldRow extends EditPropertyRow {
  
  private JButton editBtn_;
  private JTextField valField_;
  
  private String value_;
  
  public EditTextFieldRow(String name, String value, EditPropertyRowListener listener) {
    super(name, listener);
    value_ = value;
  
    editBtn_ = GUIFactory.newButton("Edit", 40, this);
    
    add(Box.createHorizontalStrut(5));
		add(new JLabel(name_+": "));

		valField_ = GUIFactory.newTextField(value_, 100, 0);
    valField_.setEditable(false);
    add(valField_);
		add(Box.createHorizontalStrut(5));
		add(editBtn_);    
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String value = JOptionPane.showInputDialog("New value for "+name_+":");
    if(value != null) {
      value_ = value;
      valField_.setText(value);
      setPropertyValue(value);
      if(listener_ != null) listener_.propertyEdited(name_, value);
    }
  }

  public void setPropertyValue(Object value) {
    value_ = (String) value;
    valField_.setText((String) value);
  }
  
  public Object getPropertyValue() { return value_; }    
}
