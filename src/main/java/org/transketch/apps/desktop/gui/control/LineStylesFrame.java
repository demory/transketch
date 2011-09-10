/*
 * LineStylesFrame.java
 * 
 * Created by demory on Feb 26, 2011, 4:08:54 PM
 * 
 * Copyright (C) 2011 David D. Emory
 * 
 * This file is part of Transit Sketchpad. See <http://www.transketch.org>
 * * for additional information regarding the project.
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

package org.transketch.apps.desktop.gui.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.command.linestyle.CopyLineStyleCommand;
import org.transketch.apps.desktop.command.linestyle.CreateLineStyleCommand;
import org.transketch.apps.desktop.command.linestyle.DeleteLineStyleCommand;
import org.transketch.apps.desktop.command.linestyle.EditLineStyleCommand;
import org.transketch.apps.desktop.gui.TranSketchGUI;
import org.transketch.apps.desktop.gui.editor.EditLineStyleDialog;
import org.transketch.apps.desktop.gui.editor.LineStylePreviewPanel;
import org.transketch.core.NamedItemComparator;
import org.transketch.core.network.LineStyle;
import org.transketch.util.gui.GUIFactory;

/**
 *
 * @author demory
 */
public class LineStylesFrame extends ListControlFrame<LineStyle> implements ActionListener {

  private JButton newBtn_, delBtn_, editBtn_, copyBtn_;

  public LineStylesFrame(TSInvoker invoker, TranSketchGUI gui, ControlFrameManager cfm) {
    super(invoker, gui, cfm, "Line Styles");

    newBtn_ = GUIFactory.newButton("New", 40, this);
    delBtn_ = GUIFactory.newButton("Del", 40, this);
    editBtn_ = GUIFactory.newButton("Edit", 40, this);
    copyBtn_ = GUIFactory.newButton("Copy", 40, this);

    buttonRow_.add(newBtn_);
    buttonRow_.add(delBtn_);
    buttonRow_.add(editBtn_);
    buttonRow_.add(copyBtn_);

    selectionDependent_.add(delBtn_);
    selectionDependent_.add(editBtn_);
    selectionDependent_.add(copyBtn_);
  }

  public void actionPerformed(ActionEvent e) {
    Editor ed = gui_.getActiveDocumentFrame().getDocument().getEditor();
    if(e.getSource() == newBtn_) {
      invoker_.doCommand(new CreateLineStyleCommand(ed));
      refreshList();
    }
    if(selectedRow_ == null) return;
    if(e.getSource() == delBtn_) {
      if(selectedRow_ == null) return;
      invoker_.doCommand(new DeleteLineStyleCommand(ed, selectedRow_.getItem()));
      selectedRow_ = null;
    }
    if(e.getSource() == editBtn_) editSelectedStyle();
    if(e.getSource() == copyBtn_) {
      invoker_.doCommand(new CopyLineStyleCommand(ed, selectedRow_.getItem()));
      refreshList();
    }
  }

  private void editSelectedStyle() {
    Editor ed = gui_.getActiveDocumentFrame().getDocument().getEditor();
    double reso = ed.getPane().getCanvas().getCoordinates().getResolution();
    EditLineStyleDialog dialog = new EditLineStyleDialog(null, selectedRow_.getItem(), ed.getPane().getCanvas());
    if(dialog.okPressed()) {
      invoker_.doCommand(new EditLineStyleCommand(ed, selectedRow_.getItem(), dialog.getAttributes()));
    }
  }

  @Override
  public void documentChanged(TSDocument oldDoc, TSDocument newDoc) {
    clearRows();
    if(newDoc == null) return;
    for(LineStyle line : newDoc.getLineStyles().getList()) addItem(line);
    clearSelection();
  }

  @Override
  public AbstractRowPanel createRowPanel(LineStyle item) {
    return new LineStyleRow(item);
  }

  @Override
  public Comparator<LineStyle> getItemComparator() {
    return new NamedItemComparator<LineStyle>();
  }

  @Override
  public void itemClicked(LineStyle style, MouseEvent e) {
    if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
      editSelectedStyle();
  }

  public class LineStyleRow extends AbstractRowPanel {

    private LineStyle style_;

    private JPanel leftPanel_;
    private LineStylePreviewPanel swatch_;

    public LineStyleRow(LineStyle style) {
      super(style);

      style_ = style;
      swatch_ = new LineStylePreviewPanel(style, 30, 18, Color.gray, 1, true);

      leftPanel_ = GUIFactory.newRowPanel();
      leftPanel_.add(swatch_);
      leftPanel_.add(Box.createHorizontalStrut(4));

      // contruct row
      add(leftPanel_, BorderLayout.WEST);

    }

    @Override
    public void setBackground(Color color) {
      super.setBackground(color);
      if(leftPanel_ != null) leftPanel_.setBackground(color);
    }
  }

}
