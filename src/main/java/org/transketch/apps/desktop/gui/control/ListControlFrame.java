/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.apps.desktop.gui.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.gui.TranSketchGUI;
import org.transketch.core.NamedItem;
import org.transketch.util.gui.GUIFactory;

/**
 *
 * @author demory
 */
public abstract class ListControlFrame<I extends NamedItem> extends ControlFrame {

  protected JPanel listPanel_;

  protected JScrollPane scrollPane_;
  protected JPanel buttonRow_;

  protected Map<I, ItemRow> rows_;

  protected ItemRow selectedRow_;
  protected I activeItem_;

  protected Set<Component> selectionDependent_ = new HashSet<Component>();

  public ListControlFrame(TSInvoker invoker, TranSketchGUI gui, ControlFrameManager cfm, String name) {
    super(invoker, gui, cfm, name);
    
    rows_ = new HashMap<I, ItemRow>();

    listPanel_ = GUIFactory.newColumnPanel();
    JPanel listPanelWrapper = new JPanel(new BorderLayout());
    listPanelWrapper.add(listPanel_, BorderLayout.NORTH);
    scrollPane_ = new JScrollPane(listPanelWrapper);
    scrollPane_.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 5, 5), new BevelBorder(BevelBorder.LOWERED)));


    buttonRow_ = GUIFactory.newRowPanel();

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(scrollPane_, BorderLayout.CENTER);
    mainPanel.add(buttonRow_, BorderLayout.NORTH);

    mainPanel.setBorder(new CompoundBorder(new LineBorder(Color.gray), new EmptyBorder(3, 0, 3, 0)));

    setLayout(new BorderLayout());
    add(mainPanel, BorderLayout.CENTER);

    pack();
    setSize(200, 200);
  }

  public void refreshList() {
    refreshList(false);
  }

  public void refreshList(boolean scrollToSelected) {
    listPanel_.removeAll();
    
    SortedSet<I> s = new TreeSet<I>(getItemComparator());
    s.addAll(rows_.keySet());
    int n=0, scrollTo=0;
    for(I item : s) {
      ItemRow row = rows_.get(item);
      listPanel_.add(row);

      row.setBorder(new EmptyBorder(3, 3, 3, 3));
      if(row == selectedRow_) {
        row.setBackground(Color.cyan);
        if(scrollToSelected) scrollTo = n;
      }
      else
        row.setBackground(listPanel_.getBackground());

      n += row.getHeight();
    }

    listPanel_.revalidate();
    if(scrollToSelected && n > 0) {
      //System.out.println("range="+scrollPane_.getVerticalScrollBar().getMinimum()+" to "+scrollPane_.getVerticalScrollBar().getMaximum());
      listPanel_.scrollRectToVisible(new Rectangle(0, n, selectedRow_.getWidth(), selectedRow_.getHeight()));
      final int val = scrollTo;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          final JScrollBar v = scrollPane_.getVerticalScrollBar();
          v.getModel().setValue( val );
        }
      });
    }
  }

  public ItemRow addItem(I item) {
    ItemRow row = new ItemRow(item);
    rows_.put(item, row);
    return row;
  }

  public void removeItem(I item) {
    ItemRow row = rows_.get(item);
    if(selectedRow_ == row) clearSelection();
    rows_.remove(item);
    refreshList();
  }

  public void clearRows() {
    rows_ = new HashMap<I, ItemRow>();
    selectedRow_ = null;
    refreshList();
  }

  public void repaintRows() {
    for(ItemRow row : rows_.values()) row.repaint();
  }


  public void updateRows(Collection<I> items) {
    clearRows();
    for(I item : items) {
      addItem(item);
    }
    refreshList();
  }

  public void updateRow(I item) {
    ItemRow row = rows_.get(item);
    itemModified(item);
    row.repaint();
  }

  public void clearSelection() {
    selectedRow_ = null;
    refreshList();
    for(Component comp : selectionDependent_) comp.setEnabled(false);
  }

  public void itemSelectedExternally(I item, boolean scrollTo) {
    ItemRow row = rows_.get(item);
    selectedRow_ = row;
    /*if(scrollTo) {
      System.out.println("scrolling");
      rows_.
      scrollPane_.scrollRectToVisible(row.getBounds());
      //scrollPane_.
    }*/
    for(Component comp : selectionDependent_) comp.setEnabled(true);
    refreshList(scrollTo);
  }

  public abstract AbstractRowPanel createRowPanel(I item);

  public abstract Comparator<I> getItemComparator();

  protected void itemSelected(I item) { }

  protected void itemModified(I item) { }
  
  protected void itemClicked(I item, MouseEvent e) { }

  
  public class ItemRow extends JPanel {

    AbstractRowPanel panel_;
    private I item_;

    public ItemRow(I item) {
      super(new BorderLayout());
      item_ = item;

      panel_ = createRowPanel(item);
      add(panel_);//new JLabel(item_.getName()), BorderLayout.CENTER);

      // add listeners

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          select();
          itemClicked(item_, e);
        }
      });
      
    }

    public I getItem() {
      return item_;
    }

    @Override
    public void setBackground(Color color) {
      super.setBackground(color);
      if(panel_ != null) panel_.setBackground(color);
    }

    public void select() {
      selectedRow_ = this;
      for(Component comp : selectionDependent_) comp.setEnabled(true);
      refreshList();
      itemSelected(item_);
    }
  }

 class CellRenderer implements TableCellRenderer {
   /*public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
     Component component = (Component) value;
     component.setBackground (isSelected ? Color.yellow : Color.white);
     return component;
   }*/

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
     Component component = (Component) value;
     component.setBackground (isSelected ? Color.yellow : Color.white);
     return component;
    }
 }


}
