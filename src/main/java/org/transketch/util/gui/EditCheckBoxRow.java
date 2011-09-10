/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.util.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.JCheckBox;

/**
 *
 * @author demory
 */
public class EditCheckBoxRow extends EditPropertyRow {
  
  private JCheckBox cb_;
  
  public EditCheckBoxRow(String name, boolean value, EditPropertyRowListener listener) {
    super(name, listener);
    
    cb_ = GUIFactory.newCheckBox(name, value);
    cb_.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
    add(cb_);
    add(Box.createHorizontalGlue());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if(listener_ != null) listener_.propertyEdited(name_, new Boolean(cb_.isSelected()));
  }

  public void setPropertyValue(Object value) {
    if(!(value instanceof Boolean)) return;
    cb_.setSelected((Boolean) value);
  }
  
  public Object getPropertyValue() { return  cb_.isSelected(); }    
}
