/*
 * ColorProperty.java
 * 
 * Created by demory on Mar 1, 2011, 9:49:47 PM
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

package org.transketch.core.network.stop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.transketch.util.gui.ColorChooserDialog;

/**
 *
 * @author demory
 */
public class ColorProperty extends RendererProperty<Color> {

  public ColorProperty(String key, String name, Color color) {
    super(key, name, color);
  }

  @Override
  public JComponent getEditingWidget() {
    final JButton colorBtn = new JButton(new ColorSwatchIcon());
    colorBtn.setMargin(new Insets(0, 0, 0, 0));
    colorBtn.setMaximumSize(new Dimension(20, 20));
    colorBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFrame owner = null;
        ColorChooserDialog chooserDialog = new ColorChooserDialog(owner, value_);
        value_ = chooserDialog.getColor();
      }
    });
    return colorBtn;
  }

  private class ColorSwatchIcon implements Icon {

    private int w_ = 24, h_ = 20;

    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(value_);
      g.fillRect(0, 0, w_, h_);
    }

    public int getIconWidth() {
      return w_;
    }

    public int getIconHeight() {
      return h_;
    }

  }

  @Override
  public String getXML() {
    return "<colorprop key=\""+key_+"\" name=\""+name_+"\" value=\""+value_.getRGB()+"\" />\n";
  }
}
