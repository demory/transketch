/*
 * EditPropertyRow.java
 * 
 * Created on Nov 4, 2007, 12:24:36 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.util.gui;

import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 *
 * @author demory
 */
public abstract class EditPropertyRow extends JPanel implements ActionListener {
  
  protected String name_;
  protected EditPropertyRowListener listener_;
  
  public EditPropertyRow(String name, EditPropertyRowListener listener) {
    
    name_ = name;
    listener_ = listener;
    
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setBorder(BorderFactory.createEtchedBorder());    
  }

  
  public String getPropertyName() { return name_; }

  public abstract Object getPropertyValue();
  
  public abstract void setPropertyValue(Object value);
  
}
