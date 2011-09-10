package org.transketch.util.gui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.transketch.util.gui.GUIFactory;

public class ColorChooserDialog extends JDialog implements ActionListener {

  private JColorChooser chooser_;
  private JButton okButton_;

  public ColorChooserDialog(Dialog owner, Color defaultColor) {
    super(owner, "Choose Color", true);
    init(defaultColor);
  }

  public ColorChooserDialog(Frame owner, Color defaultColor) {
    super(owner, "Choose Color", true);
    init(defaultColor);
  }
  
  private void init(Color defaultColor) {
    chooser_ = new JColorChooser(defaultColor);
    okButton_ = GUIFactory.newButton("OK", 50, this);
    JPanel mainPanel = GUIFactory.newColumnPanel();
    mainPanel.add(chooser_);
    mainPanel.add(okButton_);
    getContentPane().add(mainPanel);
    pack();
    setLocationRelativeTo(this);
    setVisible(true);
  }

  public Color getColor() {
    return chooser_.getColor();
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okButton_) {
      setVisible(false);
    }
  }
}
