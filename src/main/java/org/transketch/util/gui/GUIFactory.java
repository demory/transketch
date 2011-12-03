/*
 * GUIFactory.java
 *
 * Created on March 23, 2007, 9:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.transketch.util.gui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.*;
import org.apache.log4j.Logger;

/**
 *
 * @author demory
 */
public class GUIFactory {
  private final static Logger logger = Logger.getLogger(GUIFactory.class);
  
  public static final Font MAIN_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11); 
  
  /** Creates a new instance of GUIFactory */
  public GUIFactory() {
  }
  
  public static JPanel newRowPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    return panel;
  }
  
  public static JPanel newColumnPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    return panel;
  }
  
  public static JLabel newLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    return label;
  }
  
  public static JLabel newLabel(String text, float xAlign) {
    JLabel label = newLabel(text);
    label.setAlignmentX(xAlign);
    return label;
  }
  
  
  public static JTextField newTextField(String text, int chars, int width) {
    JTextField textField = chars > 0 ? new JTextField(text, chars) : new JTextField(text);
    textField.setFont(MAIN_FONT);
    if(width==0) {
      Dimension dim = new Dimension(800,18);
      textField.setMaximumSize(dim);
    }
    else setSize(textField, width, 18);
    //textField.setHorizontalAlignment(JTextField.CENTER);
    textField.setMargin(new Insets(0,0,2,0));
    return textField;
  }
  
  public static JTextField newTextField(String text, int chars, int width, float xAlign) {
    JTextField textField = newTextField(text, chars, width);
    textField.setAlignmentX(xAlign);
    return textField;
  }


  public static JComboBox newComboBox(int width) {
    return newComboBox(width, false);
  }
   
  public static JComboBox newComboBox(final int width, boolean oversizedDropdown) {
    final JComboBox box = new SmartComboBox(); // JComboBox();
    box.setFont(MAIN_FONT);
    if(width==0) {
      Dimension dim = new Dimension(800,18);
      box.setMaximumSize(dim);
    }
    else setSize(box, width, 18);

    /*box.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        int pw = box.getParent().getSize().width;
        for(Component c : box.getParent().getComponents()) {
          if(c != box) pw -= c.getSize().width;
        }
        logger.debug("cb resizing to "+pw);
        box.setMaximumSize(new Dimension(pw, box.getHeight()));
      }
    });*/


    /*box.getParent().addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        //logger.debug(e.getComponent().getClass().getName() + " resized to "+box.getSize());
        //logger.debug("parent size="+box.getParent().getSize());
        int pw = box.getParent().getSize().width;
        for(Component c : box.getParent().getComponents()) {
          if(c != box) pw -= c.getSize().width;
        }
        logger.debug("pw="+pw);
        box.setMaximumSize(new Dimension(pw, box.getHeight()));
      }
    });

    if(oversizedDropdown) {

      box.addPopupMenuListener(new PopupMenuListener() {

        //Popup state to prevent feedback
        boolean stateCmb = false;

        //Extend JComboBox's length and reset it
        public void popupMenuWillBecomeVisible(PopupMenuEvent e)
        {
          JComboBox cb = (JComboBox) e.getSource();
          //Extend JComboBox

          int textWidth = width;
          for (int i = 0; i < cb.getItemCount(); i++) {
            String item = cb.getItemAt(i).toString();
            int w = cb.getGraphics().getFontMetrics().stringWidth(item);
            if(w > textWidth) textWidth = w;
          }

          cb.setSize(textWidth+25, cb.getHeight());
          //If it pops up now JPopupMenu will still be short
          //Fire popupMenuCanceled...
          if(!stateCmb) cb.firePopupMenuCanceled();
          //Reset JComboBox and state
          stateCmb = false;
          logger.debug("setting cb width: "+width);
          cb.setSize(width, cb.getHeight());

        }

        //Show extended JPopupMenu
        public void popupMenuCanceled(PopupMenuEvent e) {
          JComboBox cmb = (JComboBox) e.getSource();
          stateCmb = true;
          //JPopupMenu is long now, so repop
          cmb.showPopup();
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          stateCmb = false;
        }

      });
    }*/
    return box;
  }
  
  public static JButton newButton(String text, int width, ActionListener al) {
    JButton btn = newButton(text, width);
    btn.addActionListener(al);
    return btn;
  }

  public static JButton newButton(String text, int width) {
    JButton btn = new JButton(text);
    btn.setFont(MAIN_FONT);
    setSize(btn, width, 22);
    btn.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(1,2,1,2)));
    return btn;
  }

  public static JToggleButton newToggleButton(String text, int width, ActionListener al) {
    JToggleButton btn = newToggleButton(text, width);
    btn.addActionListener(al);
    return btn;
  }

  public static JToggleButton newToggleButton(String text, int width) {
    JToggleButton btn = new JToggleButton(text);
    btn.setFont(MAIN_FONT);
    setSize(btn, width, 22);
    btn.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(1,2,1,2)));
    return btn;
  }

  public static JRadioButton newRadioButton(String text, ButtonGroup bg, ActionListener al) {
    JRadioButton rbtn = new JRadioButton(text);
    bg.add(rbtn);
    rbtn.addActionListener(al);
    return rbtn;    
  }
  
  public static JCheckBox newCheckBox(String text) {
    return newCheckBox(text, false);
  }
  
  public static JCheckBox newCheckBox(String text, boolean selected) {
    JCheckBox cb = new JCheckBox(text, selected);
    cb.setFont(MAIN_FONT);
    return cb;
  }
  
  public static Border createPaddedTitledBorder(String name, int buffer) {
    return createPaddedTitledBorder(name, buffer, buffer, buffer, buffer);
  }

 public static Border createPaddedTitledBorder(String name, int t, int l, int b, int r) {
    return new CompoundBorder(new TitledBorder(name), new EmptyBorder(t,l,b,r));
  }

  
  public static void setSize(Component comp, int w, int h) {
    Dimension dim = new Dimension(w,h);
    comp.setPreferredSize(dim);
    comp.setMinimumSize(dim);
    comp.setMaximumSize(dim);
  }
  
  public static void disableSet(Set<JComponent> set) {
    for (Iterator<JComponent> it = set.iterator(); it.hasNext();) {
      JComponent c = it.next();
      if(c instanceof JLabel) c.setForeground(Color.GRAY);
      else c.setEnabled(false);
    }
  }
  
  public static void enableSet(Set<JComponent> set) {
    for (Iterator<JComponent> it = set.iterator(); it.hasNext();) {
      JComponent c = it.next();
      if(c instanceof JLabel) c.setForeground(Color.BLACK);
      else c.setEnabled(true);
    }
  }
  
  // DIALOG GENERATION
  
	/*public static int showSelectItemDialog(JFrame frame, Iterator<String> items) {
		SelectItemDialog dialog = new SelectItemDialog(frame, items);
		return dialog.getSelectedIndex();
	}*/
}
