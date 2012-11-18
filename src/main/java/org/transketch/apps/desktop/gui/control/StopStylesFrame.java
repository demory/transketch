/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.transketch.apps.desktop.command.stopstyle.CreateStopStyleCommand;
import org.transketch.apps.desktop.command.stopstyle.EditStopStyleCommand;
import org.transketch.apps.desktop.gui.TranSketchGUI;
import org.transketch.apps.desktop.gui.editor.EditStopStyleDialog;
import org.transketch.core.NamedItemComparator;
import org.transketch.core.network.stop.StopStyle;
import org.transketch.util.gui.GUIFactory;

/**
 *
 * @author demory
 */

public class StopStylesFrame extends ListControlFrame<StopStyle> implements ActionListener {

  private JButton newBtn_, delBtn_, editBtn_, copyBtn_;

  public StopStylesFrame(TSInvoker invoker, TranSketchGUI gui, ControlFrameManager cfm) {
    super(invoker, gui, cfm, "Stop Styles");

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
      invoker_.doCommand(new CreateStopStyleCommand(ed));
      refreshList();
    }
    if(e.getSource() == editBtn_) {
      editSelectedStyle();
    }
  }

  private void editSelectedStyle() {
    Editor ed = gui_.getActiveDocumentFrame().getDocument().getEditor();
    EditStopStyleDialog dialog = new EditStopStyleDialog(null, selectedRow_.getItem());
    if(dialog.okPressed()) {
      //StopRendererTemplate template = dialog.getRendererTemplate();
      //template.print();
      invoker_.doCommand(new EditStopStyleCommand(ed, selectedRow_.getItem(), dialog.getStyleName(), dialog.getRenderer()));
    }
  }

  @Override
  public void documentChanged(TSDocument oldDoc, TSDocument newDoc) {
    clearRows();
    if(newDoc == null) return;
    updateRows(newDoc.getStopStyles().getList());
    clearSelection();
  }

  /*public void updateStyles(StopStyles styles) {
    clearRows();
    for(StopStyle style : styles.getList()) {
      addItem(style);
    }
    refreshList();
  }*/

  @Override
  public AbstractRowPanel createRowPanel(StopStyle item) {
    return new StopStyleRow(item);
  }

  @Override
  public Comparator<StopStyle> getItemComparator() {
    return new NamedItemComparator<StopStyle>();
  }

  @Override
  public void itemClicked(StopStyle style, MouseEvent e) {
    if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
      editSelectedStyle();
  }

  public class StopStyleRow extends AbstractRowPanel {

    private StopStyle style_;

    private JPanel leftPanel_;
    private JPanel swatch_;

    public StopStyleRow(StopStyle style) {
      super(style);

      style_ = style;
      //swatch_ = new LineStylePreviewPanel(style, 30, 18, Color.gray, 1, true);

      leftPanel_ = GUIFactory.newRowPanel();
      //leftPanel_.add(swatch_);
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
