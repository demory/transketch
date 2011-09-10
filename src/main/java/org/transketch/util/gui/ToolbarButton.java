package org.transketch.util.gui;

import javax.swing.*;

public class ToolbarButton extends JToggleButton {
	
	private int action_;
	
	public ToolbarButton(String btnText, int action) {
		super(btnText);
    setFont(GUIFactory.MAIN_FONT);
		action_ = action;
	}
	
	public ToolbarButton(ImageIcon icon, int action) {
		super(icon);
    action_ = action;
	}

  public int getClickAction() { return action_; }
}






















