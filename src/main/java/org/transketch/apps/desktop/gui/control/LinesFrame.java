/*
 * LinesFrame.java
 *
 * Created by demory on Nov 28, 2009, 10:26:19 PM
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

package org.transketch.apps.desktop.gui.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.command.network.CreateLineCommand;
import org.transketch.apps.desktop.command.network.DeleteLineCommand;
import org.transketch.apps.desktop.command.linestyle.SetLineStyleCommand;
import org.transketch.apps.desktop.command.viewport.RefreshViewportCommand;
import org.transketch.apps.desktop.gui.TranSketchGUI;
import org.transketch.apps.desktop.gui.editor.map.LineMenu;
import org.transketch.apps.desktop.gui.editor.map.StylePreviewIcon;
import org.transketch.core.network.Line;
import org.transketch.core.network.LineStyle;
import org.transketch.core.network.line.LineNameComparator;
import org.transketch.util.gui.GUIFactory;

/**
 *
 * @author demory
 */
public class LinesFrame extends ListControlFrame<Line> implements ActionListener {

  private JButton newBtn_, delBtn_, editBtn_;

  private Line activeLine_;

  private Map<Integer, LineStyle> styleLookup_;
  private JPopupMenu stylePopupMenu_;
  private LineMenu lineMenu_;

  public LinesFrame(TSInvoker invoker, TranSketchGUI gui, ControlFrameManager cfm) {
    super(invoker, gui, cfm, "Lines");

    lineMenu_ = new LineMenu(invoker, null);


    newBtn_ = GUIFactory.newButton("New", 40, this);
    delBtn_ = GUIFactory.newButton("Del", 40, this);
    editBtn_ = GUIFactory.newButton("E", 40, this);

    buttonRow_.add(newBtn_);
    buttonRow_.add(delBtn_);
    //buttonRow_.add(editBtn_);

    selectionDependent_.add(delBtn_);

  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == newBtn_) {
      invoker_.doCommand(new CreateLineCommand(invoker_.getActiveEditor()));
    }
    if(e.getSource() == delBtn_) {
      invoker_.doCommand(new DeleteLineCommand(invoker_.getActiveEditor(), getSelectedLine()));
      selectedRow_ = null;
      refreshList();
    }
    if(e.getSource() == editBtn_) {
    }
  }

  public Line getSelectedLine() {
    return selectedRow_ == null ? null : selectedRow_.getItem();
  }

  public void refreshStyles(List<LineStyle> styles) {
    stylePopupMenu_ = new JPopupMenu();
    LineStyle def = new LineStyle(LineStyle.Preset.DEFAULT);
    stylePopupMenu_.add(createStyleMenuItem(def));
    styleLookup_ = new HashMap<Integer, LineStyle>();
    styleLookup_.put(0, def);
    int i = 1;
    for(LineStyle style : styles) {
      styleLookup_.put(i++, style);
      stylePopupMenu_.add(createStyleMenuItem(style));
    }
    stylePopupMenu_.addSeparator();
    stylePopupMenu_.add(new JMenuItem("Create new.."));
  }

  public JMenuItem createStyleMenuItem(final LineStyle style) {
    JMenuItem item = new JMenuItem(style.getName(), new StylePreviewIcon(style, 24, 16, Color.black, 0, true));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        invoker_.doCommand(new SetLineStyleCommand(invoker_.getActiveEditor(), activeLine_, style));
      }
    });
    return item;
  }

  @Override
  public void itemSelected(Line line) {
    gui_.getActiveDocumentFrame().getDocument().getEditor().setSelectedLine(line, false);
    invoker_.doCommand(new RefreshViewportCommand(gui_.getActiveDocumentFrame().getDocument().getEditor().getPane().getCanvas()));
  }

  @Override
  public void documentChanged(TSDocument oldDoc, TSDocument newDoc) {
    //if(oldEd != null) oldEd.setSelectedLine(selectedLine);
    clearRows();
    if(newDoc == null) return;
    for(Line line : newDoc.getNetwork().getLines()) {
      ItemRow row = addItem(line);
      //row.checkbox_.setSelected(line.isEnabled());
    }
    refreshStyles(newDoc.getLineStyles().getList());
    lineMenu_.setEditor(newDoc.getEditor());
    clearSelection();
  }

  @Override
  public AbstractRowPanel createRowPanel(Line item) {
    return new LineRow(item);
  }

  public Comparator<Line> getItemComparator() {
    return new LineNameComparator();
  }

  @Override
  protected void itemClicked(Line item, MouseEvent e) {
    if(e.getButton() == MouseEvent.BUTTON3) {
      lineMenu_.setLine(item);
      lineMenu_.show(rows_.get(item), e.getX(), e.getY());
    }
  }

  @Override
  public void itemModified(Line item) {
    ((LineRow) rows_.get(item).panel_).swatch_.setStyleInfo(item.getStyle());
  }


  public class LineRow extends AbstractRowPanel {

    //private Line line_;

    private JPanel leftPanel_;
    private JCheckBox checkbox_;
    private StylePreviewIcon swatch_;
    private JButton styleBtn_;

    public LineRow(final Line line) {
      super(line);

      //line_ = line;

      checkbox_ = new JCheckBox();
      checkbox_.setSelected(true);
      checkbox_.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          line.setEnabled(checkbox_.isSelected());
          invoker_.doCommand(new RefreshViewportCommand(invoker_.getActiveEditor().getPane().getCanvas()));
        }
      });

      //swatch_ = new StylePreviewPanel(line.getStyle(), 30, 18, Color.gray, 1, true);
      swatch_ = new StylePreviewIcon(line.getStyle(), 30, 18, Color.gray, 1, true);
      swatch_.setLine(line);
      styleBtn_ = new JButton(swatch_);
      styleBtn_.setMargin(new Insets(0, 0, 0, 0));
      styleBtn_.setMaximumSize(new Dimension(32, 20));
      styleBtn_.addMouseListener(new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent e) {
          activeLine_ = line;
          stylePopupMenu_.show(styleBtn_, e.getX(), e.getY());
        }
      });

      leftPanel_ = GUIFactory.newRowPanel();
      
      leftPanel_.add(checkbox_);
      leftPanel_.add(Box.createHorizontalStrut(4));

      leftPanel_.add(styleBtn_);
      leftPanel_.add(Box.createHorizontalStrut(4));

      add(leftPanel_, BorderLayout.WEST);
    }

    
    @Override
    public void setBackground(Color color) {
      super.setBackground(color);
      if(checkbox_ != null) checkbox_.setBackground(color);
      if(leftPanel_ != null) leftPanel_.setBackground(color);
    }

  }

}
