/*
 * EditStyleDialog.java
 * 
 * Created by demory on Feb 22, 2010, 10:57:08 PM
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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.transketch.apps.desktop.gui.editor.map.EditorCanvas;
import org.transketch.core.network.LineStyle;
import org.transketch.core.network.LineStyleView;
import org.transketch.core.network.LineStyleLayer;
import org.transketch.core.network.LineSubStyle;
import org.transketch.util.FPUtil;
import org.transketch.util.gui.ColorChooserDialog;
import org.transketch.util.gui.GUIFactory;

/**
 *
 * @author demory
 */
public class EditLineStyleDialog extends JDialog implements ActionListener, LineStyleView, RRSListener {

  private JPanel layersListPanel_, colorSwatch_;
  private LineStylePreviewPanel previewPanel_;
  private JTextField nameField_, dashField_;
  private JSpinner widthSpinner_, envelopeSpinner_;
  private JButton newBtn_, delBtn_, upBtn_, downBtn_, changeColorBtn_, applyBtn_, okBtn_, cancelBtn_;
  private JRadioButton colorHardCodedRBtn_, colorKeySpecifiedRBtn_;
  private JCheckBox antialiasCB_;

  //private List<Double> breakpoints_;
  private SortedMap<Double, LineSubStyle> subStyles_ = new TreeMap<Double, LineSubStyle>();
  private LineSubStyle activeSub_;
  private ResolutionRangeSelector rrs_;

  private List<LayerRow> layerRows_;
  private LayerRow selectedRow_;
  int maxRank_ = 0;

  private boolean okPressed_ = false;

  public EditLineStyleDialog(JFrame parent, LineStyle style, EditorCanvas canvas) {
    super(parent, "Edit Style", true);

    //activeSub_ = style.getActiveSubStyle();

    // init the lookup table of substyles
    List<Double> breakpoints = new ArrayList<Double>(style.getBreakpoints());
    breakpoints.add(0, 0.0);
    List<LineSubStyle> subs = style.getSubStyles();
    int activeSubIndex = 0;
    for(int i = 0; i < breakpoints.size(); i++) {
      LineSubStyle subCopy = subs.get(i).getCopy();
      subStyles_.put(breakpoints.get(i), subCopy);
      if(subs.get(i) == style.getActiveSubStyle()) {
        activeSub_ = subCopy;
        activeSubIndex = i;
      }
    }

    // layer selector
    updateLayersList();

    layersListPanel_ = GUIFactory.newColumnPanel();
    layersListPanel_.setBorder(new EmptyBorder(3, 3, 3, 3));
    refreshLayers();
    JPanel listPanelWrapper = new JPanel(new BorderLayout());
    listPanelWrapper.add(layersListPanel_, BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(listPanelWrapper);
    scrollPane.setBorder(new CompoundBorder(new EmptyBorder(2, 0, 5, 0), new BevelBorder(BevelBorder.LOWERED)));

    newBtn_ = GUIFactory.newButton("N", 30, this);
    delBtn_ = GUIFactory.newButton("D", 30, this);
    upBtn_ = GUIFactory.newButton("^", 30, this);
    downBtn_ = GUIFactory.newButton("v", 30, this);
    JPanel buttonRow = GUIFactory.newRowPanel();

    //buttonRow.add(Box.createHorizontalGlue());
    buttonRow.add(newBtn_);
    buttonRow.add(delBtn_);
    buttonRow.add(upBtn_);
    buttonRow.add(downBtn_);
    buttonRow.add(Box.createHorizontalGlue());

    JPanel envelopeRow = GUIFactory.newRowPanel();
    //envelopeRow.setAlignmentX(LEFT_ALIGNMENT);
    envelopeRow.add(GUIFactory.newLabel("Envelope: "));
    envelopeSpinner_ = new JSpinner();
    envelopeSpinner_.setValue(activeSub_.getEnvelope());
    envelopeSpinner_.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        envelopeChanged();
      }
    });
    envelopeRow.add(envelopeSpinner_);
    //envelopeRow.add(Box.createHorizontalGlue());

    JPanel layerSelPanel = new JPanel(new BorderLayout());
    layerSelPanel.add(scrollPane, BorderLayout.CENTER);
    layerSelPanel.add(buttonRow, BorderLayout.NORTH);
    layerSelPanel.add(envelopeRow, BorderLayout.SOUTH);
    layerSelPanel.setBorder(new EmptyBorder(0, 0, 0, 10));

    // LAYER OPTIONS PANEL

    colorHardCodedRBtn_ = new JRadioButton("Hard-coded: ");
    colorHardCodedRBtn_.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    colorHardCodedRBtn_.addActionListener(this);
    colorKeySpecifiedRBtn_ = new JRadioButton("Specified by Key");
    colorKeySpecifiedRBtn_.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    colorKeySpecifiedRBtn_.addActionListener(this);
    ButtonGroup colorRBtns_ = new ButtonGroup();
    colorRBtns_.add(colorHardCodedRBtn_);
    colorRBtns_.add(colorKeySpecifiedRBtn_);

    JPanel colorHeaderRow = GUIFactory.newRowPanel();
    colorHeaderRow.setAlignmentX(LEFT_ALIGNMENT);
    colorHeaderRow.add(GUIFactory.newLabel("Color: "));

    JPanel colorHardCodedRow = GUIFactory.newRowPanel();
    colorHardCodedRow.setAlignmentX(LEFT_ALIGNMENT);
    colorHardCodedRow.add(colorHardCodedRBtn_);
    //colorHardCodedRow.add(GUIFactory.newLabel("Hard-coded: "));
    colorSwatch_ = new JPanel();
    colorSwatch_.setPreferredSize(new Dimension(18,18));
    colorSwatch_.setMaximumSize(new Dimension(18,18));
    colorSwatch_.setBorder(new BevelBorder(BevelBorder.LOWERED));
    colorHardCodedRow.add(colorSwatch_);
    colorHardCodedRow.add(Box.createHorizontalStrut(5));
    changeColorBtn_ = GUIFactory.newButton("Change..", 60, this);
    colorHardCodedRow.add(changeColorBtn_);
    colorHardCodedRow.add(Box.createHorizontalGlue());

    JPanel colorLineSpecifiedRow = GUIFactory.newRowPanel();
    colorLineSpecifiedRow.setAlignmentX(LEFT_ALIGNMENT);
    colorLineSpecifiedRow.add(colorKeySpecifiedRBtn_);
    
    JPanel widthRow = GUIFactory.newRowPanel();
    widthRow.setAlignmentX(LEFT_ALIGNMENT);
    widthRow.add(GUIFactory.newLabel("Width: "));
    widthSpinner_ = new JSpinner();
    widthSpinner_.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        applyLayerEdits();
      }
    });
    widthRow.add(widthSpinner_);
    widthRow.add(Box.createHorizontalGlue());

    JPanel dashRow = GUIFactory.newRowPanel();
    dashRow.setAlignmentX(LEFT_ALIGNMENT);
    dashField_ = GUIFactory.newTextField("", 50, 0);
    applyBtn_ = GUIFactory.newButton("Apply", 40, this);
    dashRow.add(GUIFactory.newLabel("Dashing: "));
    dashRow.add(dashField_);
    dashRow.add(Box.createHorizontalStrut(5));
    dashRow.add(applyBtn_);


    JPanel layerPropsSubPanel = GUIFactory.newColumnPanel();
    layerPropsSubPanel.add(colorHeaderRow);
    layerPropsSubPanel.add(colorHardCodedRow);
    layerPropsSubPanel.add(colorLineSpecifiedRow);
    layerPropsSubPanel.add(Box.createVerticalStrut(6));
    layerPropsSubPanel.add(widthRow);
    layerPropsSubPanel.add(Box.createVerticalStrut(6));
    layerPropsSubPanel.add(dashRow);
    layerPropsSubPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
    //layerPropsSubPanel.add(Box.createVerticalGlue());

    /*JPanel layerPropsBtnRow = GUIFactory.newRowPanel();
    layerPropsBtnRow.add(Box.createHorizontalGlue());
    layerPropsBtnRow.add(applyBtn_);
    layerPropsBtnRow.add(Box.createHorizontalGlue());*/

    JPanel layerPropsPanel = new JPanel(new BorderLayout());
    layerPropsPanel.setBorder(new TitledBorder("Layer Properties"));
    layerPropsPanel.add(layerPropsSubPanel, BorderLayout.NORTH);
    //layerPropsPanel.add(layerPropsBtnRow, BorderLayout.SOUTH);

    JPanel previewRow = new JPanel(new BorderLayout());
    previewRow.add(new JLabel("Preview: "), BorderLayout.WEST);
    previewPanel_ = new LineStylePreviewPanel(this, 1, 24, Color.BLACK, 2, false);
    previewRow.add(previewPanel_, BorderLayout.CENTER);
    antialiasCB_ = GUIFactory.newCheckBox("Show antialiased", false);
    antialiasCB_.addActionListener(this);
    previewRow.add(antialiasCB_, BorderLayout.EAST);

    JPanel bottomRow = GUIFactory.newRowPanel();

    nameField_ = GUIFactory.newTextField(style.getName(), 50, 0);
    nameField_.addMouseListener(new MouseAdapter() {
      boolean clicked = false;
      @Override
      public void mouseClicked(MouseEvent e) {
        if(!clicked) {
          nameField_.setSelectionStart(0);
          nameField_.setSelectionEnd(nameField_.getText().length());
          clicked = true;
        }
      }
    });

    okBtn_ = GUIFactory.newButton("OK", 60, this);
    cancelBtn_ = GUIFactory.newButton("Cancel", 60, this);
    bottomRow.add(new JLabel("Name: "));
    bottomRow.add(nameField_);
    bottomRow.add(Box.createHorizontalStrut(25));
    bottomRow.add(okBtn_);
    bottomRow.add(Box.createHorizontalStrut(5));
    bottomRow.add(cancelBtn_);
    //bottomRow.setBorder(new EmptyBorder(8, 0, 0, 0));

    rrs_ = new ResolutionRangeSelector(canvas, style.getBreakpoints(), activeSubIndex);
    rrs_.setBorder(new EmptyBorder(0, 0, 5, 0));
    rrs_.addListener(this);

    // TOP-LEVEL LAYOUT
    JPanel topPanel = new JPanel(new BorderLayout()); //new GridLayout(1, 2));
    topPanel.add(layerSelPanel, BorderLayout.WEST);
    topPanel.add(layerPropsPanel, BorderLayout.CENTER);

    JPanel bottomPanel = GUIFactory.newColumnPanel();
    bottomPanel.add(Box.createVerticalStrut(6));
    bottomPanel.add(previewRow);
    bottomPanel.add(Box.createVerticalStrut(6));
    bottomPanel.add(bottomRow);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel, BorderLayout.CENTER);
    mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    mainPanel.add(rrs_, BorderLayout.NORTH);

    mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    getContentPane().add(mainPanel);
    setSize(360, 320);
    setLocationRelativeTo(parent);
    setPropertiesEnabled(false);
    setVisible(true);

  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == newBtn_) newLayer();
    if(e.getSource() == delBtn_) deleteLayer(selectedRow_);
    if(e.getSource() == upBtn_) bringForward(selectedRow_);
    if(e.getSource() == downBtn_) sendBackward(selectedRow_);


    if(e.getSource() == colorHardCodedRBtn_) {
      selectedRow_.colorMode_ = LineStyleLayer.ColorMode.HARD_CODED;
      selectedRow_.colorKey_ = null;
      selectedRow_.color_ = Color.BLACK;
      colorSwatch_.setBackground(Color.BLACK);
      colorKeySpecifiedRBtn_.setText("Specified by key");
      applyLayerEdits();
    }
    if(e.getSource() == colorKeySpecifiedRBtn_) {
      String key = JOptionPane.showInputDialog("Key:");
      if(key == null) {
        colorHardCodedRBtn_.setSelected(true);
        return;
      }      

      selectedRow_.colorMode_ = LineStyleLayer.ColorMode.KEY_SPECIFIED;
      selectedRow_.colorKey_ = key;
      selectedRow_.color_ = null;
      colorSwatch_.setBackground(this.getBackground());
      colorKeySpecifiedRBtn_.setText("Specified by '"+key+"'");
      applyLayerEdits();
    }


    if(e.getSource() == changeColorBtn_) {
      ColorChooserDialog chooserDialog = new ColorChooserDialog(this, colorSwatch_.getBackground());
      colorSwatch_.setBackground(chooserDialog.getColor());
      applyLayerEdits();
    }
    if(e.getSource() == applyBtn_) {
      try {
        selectedRow_.dash_ = LineStyleLayer.parseDash(dashField_.getText());
        applyLayerEdits();
      } catch(NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Invalid dash input; must be comma-separated list of numbers");
      }
    }

    if(e.getSource() == antialiasCB_) {
      previewPanel_.setAntialias(antialiasCB_.isSelected());
      previewPanel_.repaint();
    }
    
    if(e.getSource() == okBtn_) {
      activeSub_.setLayers(getLayers());
      okPressed_ = true;
      setVisible(false);
    }
    if(e.getSource() == cancelBtn_) {
      setVisible(false);
    }
  }
  
  public boolean okPressed() {
    return okPressed_;
  }

  public void updateLayersList() {
    layerRows_ = new LinkedList<LayerRow>();
    int rank = 0;
    for(LineStyleLayer layer : activeSub_.getLayers()) {
      //System.out.println("adding row");
      LayerRow row = null;
      if(layer.getColorMode() == LineStyleLayer.ColorMode.HARD_CODED)
        row = new LayerRow(++rank, layer.getColor(), layer.getWidth());
      else if(layer.getColorMode() == LineStyleLayer.ColorMode.KEY_SPECIFIED)
        row = new LayerRow(++rank, layer.getColorKey(), layer.getWidth());

      if(row == null) continue;

      if(layer.getDash() != null) row.dash_ = layer.getDash();
      layerRows_.add(row);
    }
  }

  public String getName() {
    return nameField_.getText();
  }

  public List<LineStyleLayer> getLayers() {
    List<LineStyleLayer> list = new LinkedList<LineStyleLayer>();
    for(LayerRow row : layerRows_) {
      if(row.colorMode_ == LineStyleLayer.ColorMode.HARD_CODED)
        list.add(new LineStyleLayer(row.width_, row.color_, row.dash_));
      else
        list.add(new LineStyleLayer(row.width_, row.colorKey_, row.dash_));

    }
    return list;
  }

  public LineStyle.LineStyleAttributes getAttributes() {
    //List<LineSubStyle> subs = new ArrayList<LineSubStyle>();
    ///LineSubStyle sub = new LineSubStyle(getLayers());
    //subs.add(sub);
    List<LineSubStyle> subs = new ArrayList<LineSubStyle>(subStyles_.values());
    List<Double> breakpoints = new ArrayList(subStyles_.keySet()); //new ArrayList<Double>();
    breakpoints.remove(0); // remove the the first (key=0) breakpoint
    System.out.println("breakpoints: "+breakpoints);
    for(LineSubStyle sub : subs) {
      System.out.println(" sub: "+sub.getLayers().size());
    }
    return new LineStyle.LineStyleAttributes(getName(), subs, breakpoints);
  }

  private void setPropertiesEnabled(boolean enabled) {
    if(enabled == false) {
      colorSwatch_.setBackground(this.getBackground());
      dashField_.setText("");
    }
    colorHardCodedRBtn_.setEnabled(enabled);
    colorKeySpecifiedRBtn_.setEnabled(enabled);
    changeColorBtn_.setEnabled(enabled);
    widthSpinner_.setEnabled(enabled);
    dashField_.setEnabled(enabled);
    applyBtn_.setEnabled(enabled);
  }

  private void envelopeChanged() {
    System.out.println("envelope changed!");
    if((Integer) envelopeSpinner_.getValue() < getMaxLayerWidth()) {
      envelopeSpinner_.setValue(getMaxLayerWidth());
    }
    activeSub_.setEnvelope((Integer) envelopeSpinner_.getValue());
  }

  private int getMaxLayerWidth() {
    int max = 0;
    for(LayerRow row : layerRows_) {
      if(row.width_ > max) max = row.width_;
    }
    return max;
  }

  private void applyLayerEdits() {
    if(selectedRow_.colorMode_ == LineStyleLayer.ColorMode.HARD_CODED) selectedRow_.color_ = colorSwatch_.getBackground();
    if(FPUtil.isInteger(widthSpinner_.getValue().toString())) {
      selectedRow_.width_ = Integer.parseInt(widthSpinner_.getValue().toString());
      if(selectedRow_.width_ > (Integer) envelopeSpinner_.getValue()) envelopeSpinner_.setValue(selectedRow_.width_);
    }
    refreshLayers();
    previewPanel_.repaint();
  }

  private void newLayer() {

    for(LayerRow row : layerRows_) row.rank_++;
    layerRows_.add(new LayerRow(1, Color.BLACK, 2));
    System.out.println("new layer");
    refreshLayers();
    previewPanel_.repaint();
  }

  private void deleteLayer(LayerRow row) {
    if(row == null) return;
    layerRows_.remove(row);
    if(row == selectedRow_) selectedRow_ = null;
    refreshLayers();
    setPropertiesEnabled(false);
    previewPanel_.repaint();
  }

  private void bringForward(LayerRow row) {
    if(row == null) return;
    int index = layerRows_.indexOf(row);
    if(index <= 0) return;
    row.rank_--;
    layerRows_.get(index-1).rank_++;
    refreshLayers();
    previewPanel_.repaint();
  }

  private void sendBackward(LayerRow row) {
    if(row == null) return;
    int index = layerRows_.indexOf(row);
    if(index >= layerRows_.size()-1) return;
    row.rank_++;
    layerRows_.get(index+1).rank_--;
    refreshLayers();
    previewPanel_.repaint();
  }

  private void refreshLayers() {
    Collections.sort(layerRows_);
    layersListPanel_.removeAll();
    //layersListPanel_.revalidate();
    for(LayerRow row : layerRows_) {
      layersListPanel_.add(row);
      layersListPanel_.add(Box.createVerticalStrut(3));
      /*if(row == selectedRow_)
        row.setBorder(new LineBorder(Color.red, 2));
      else
        row.setBorder(new LineBorder(Color.gray, 1));*/
      row.repaint();//revalidate();
    }
    layersListPanel_.revalidate();
  }

  private void rowSelected(LayerRow row) {
    selectedRow_ = row;
    setPropertiesEnabled(true);
    if(selectedRow_.colorMode_ == LineStyleLayer.ColorMode.HARD_CODED) {
      colorHardCodedRBtn_.setSelected(true);
      System.out.println("row.color_="+row.color_.toString());
      colorSwatch_.setBackground(row.color_);
      colorKeySpecifiedRBtn_.setText("Specified by Key");
    }

    if(selectedRow_.colorMode_ == LineStyleLayer.ColorMode.KEY_SPECIFIED) {
      colorKeySpecifiedRBtn_.setSelected(true);
      //colorSwatch_.setBackground(this.getBackground());
      colorKeySpecifiedRBtn_.setText("Specified by '"+selectedRow_.colorKey_+"'");
    }
    widthSpinner_.setValue(row.width_);
    dashField_.setText(LineStyleLayer.getDashStr(row.dash_));
    refreshLayers();
  }

  public void breakpointAdded(double reso) {
    //breakpoints_.add(reso);
    //Collections.sort(breakpoints_);
    subStyles_.put(reso, new LineSubStyle());
  }

  public void breakpointRemoved(double reso) {
    System.out.println("rm bp: "+reso);
    List<Double> bpResos = new ArrayList<Double>(subStyles_.keySet());
    //bpResos.add(Double.MAX_VALUE);
    int i;
    for(i=0; i < bpResos.size(); i++)
      if(bpResos.get(i) == reso) break;

    DecimalFormat df = new DecimalFormat("#.#");

    String left = df.format(bpResos.get(i-1)) + " to " + df.format(bpResos.get(i));
    String right = df.format(bpResos.get(i)) + " to " + ((i+1 == bpResos.size()) ? new DecimalFormatSymbols().getInfinity() : df.format(bpResos.get(i+1)));
    
    int n = JOptionPane.showOptionDialog(null, "Which range's SubStyle should be kept?", "Question", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] {left, right}, null);
    System.out.println("n="+n+" removing "+bpResos.get(i-n));
    subStyles_.remove(bpResos.get(i-n));

    bpResos = new ArrayList<Double>(subStyles_.keySet());
    bpResos.remove(0);
    rrs_.updateBreakpoints(bpResos);
    rrs_.setSelectedRange(i-1);
    //rrs_.repaintAll();
  }

  public void rangeSelected(int index) {
    activeSub_.setLayers(getLayers());
    List<LineSubStyle> subs = new ArrayList<LineSubStyle>(subStyles_.values());
    activeSub_ = subs.get(index);
    updateLayersList();
    refreshLayers();
    envelopeSpinner_.setValue(activeSub_.getEnvelope());
  }

  private class LayerRow extends JPanel implements Comparable<LayerRow> {

    private int rank_, width_;
    private Color color_;
    private String colorKey_;

    private LineStyleLayer.ColorMode colorMode_;

    private float[] dash_;

    public LayerRow(int rank, Color color, int width) {
      this(rank, width);
      colorMode_ = LineStyleLayer.ColorMode.HARD_CODED;
      color_ = color;
      System.out.println("set color = "+color_.toString());
    }

    public LayerRow(int rank, String colorKey, int width) {
      this(rank, width);
      colorMode_ = LineStyleLayer.ColorMode.KEY_SPECIFIED;
      colorKey_ = colorKey;
    }

    public LayerRow(int rank, int width) {
      setPreferredSize(new Dimension(1, 24));
      rank_ = rank;
      width_ = width;

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          rowSelected(LayerRow.this);
        }
      });
    }

    @Override
    public void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;

      g2d.setColor(this == selectedRow_ ? Color.red : Color.BLACK);
      g2d.fillRect(0, 0, getWidth(), getHeight());
      g2d.setColor(EditLineStyleDialog.this.getBackground());
      int bWidth = this == selectedRow_ ? 2 : 1;
      g2d.fillRect(bWidth, bWidth, getWidth()-bWidth*2, getHeight()-bWidth*2);


      int y = getHeight()/2;
      g2d.setStroke(new BasicStroke(width_, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, dash_, 0));
      g2d.setColor(color_ != null ? color_ : Color.GRAY);

      g2d.drawLine(5, y, getWidth()-5, y);
    }

    public int compareTo(LayerRow o) {
      return new Integer(rank_).compareTo(new Integer(o.rank_));
    }

  }

}
