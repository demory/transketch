/*
 * EditorPanel.java
 * 
 * Created by demory on Jan 17, 2011, 5:17:42 PM
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
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.gui.TranSketchGUI;
import org.transketch.apps.desktop.gui.editor.map.EditorCanvas;
import org.transketch.util.gui.GUIFactory;
import org.transketch.util.viewport.MapCoordinates;
import org.transketch.util.viewport.ResolutionListener;

/**
 *
 * @author demory
 */
public class EditorPane extends JPanel implements KeyListener, ResolutionListener {

  private EditorCanvas canvas_;

  private EditorToolbar toolbar_;

  private JLabel coordsLabel_, statusLabel_, resoLabel_;

  public EditorPane(TSInvoker invoker, Editor ed, MapCoordinates coords, TranSketchGUI gui) {
    super(new BorderLayout());

    canvas_ = new EditorCanvas(invoker, ed, coords, gui);
    canvas_.getCoordinates().addResolutionListener(this);
    
    statusLabel_ = GUIFactory.newLabel("");
    statusLabel_.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

    coordsLabel_ = GUIFactory.newLabel(" ");
    coordsLabel_.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    coordsLabel_.setPreferredSize(new Dimension(100, 20));

    resoLabel_ = GUIFactory.newLabel(" ");
    resoLabel_.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    resoLabel_.setPreferredSize(new Dimension(60, 20));

    JPanel rightPanel = new JPanel(new BorderLayout()); //GUIFactory.newRowPanel();
    rightPanel.add(coordsLabel_, BorderLayout.CENTER);
    rightPanel.add(resoLabel_, BorderLayout.EAST);

    JPanel statusBar = new JPanel(new BorderLayout());
    statusBar.add(statusLabel_, BorderLayout.CENTER);
    statusBar.add(rightPanel, BorderLayout.EAST);

    toolbar_ = new EditorToolbar(this, invoker);

    toolbar_.selectAction(EditorToolbar.ActionType.SELECT);

    //JPanel mainPanel = new JPanel(new BorderLayout());
    add(toolbar_, BorderLayout.NORTH);
    add(canvas_, BorderLayout.CENTER);
    add(statusBar, BorderLayout.SOUTH);

  }

  public EditorToolbar getToolbar() {
    return toolbar_;
  }

  public EditorCanvas getCanvas() {
    return canvas_;
  }

  public void setStatusText(String text) {
    statusLabel_.setText(text);
    statusLabel_.paintImmediately(statusLabel_.getBounds());
  }

  public void setCoords(double x, double y) {
    DecimalFormat two = new DecimalFormat("#.##");
    coordsLabel_.setText("("+two.format(x)+","+two.format(y)+")");
  }

  public void resolutionChanged(double r) {
    DecimalFormat two = new DecimalFormat("#.##");
    resoLabel_.setText("r="+two.format(r));
  }
  

  public void keyTyped(KeyEvent e) {
    canvas_.keyTyped(e);
  }

  public void keyPressed(KeyEvent e) {
    canvas_.keyPressed(e);
  }

  public void keyReleased(KeyEvent e) {
    canvas_.keyReleased(e);
  }
}
