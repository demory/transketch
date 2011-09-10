/*
 * ResolutionRangeSelector.java
 * 
 * Created by demory on Mar 19, 2011, 10:35:30 PM
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

package org.transketch.apps.desktop.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.transketch.util.FPUtil;
import org.transketch.util.gui.GUIFactory;
import org.transketch.util.viewport.Viewport;

/**
 *
 * @author demory
 */
public class ResolutionRangeSelector extends JPanel {


  private Viewport viewport_;
  //private List<Double> bounds_  = new ArrayList<Double>();

  private SortedSet<Double> breakpoints_;
  //private SortedMap<Double, Object> breakpoints_ = new TreeMap<Double, Object>();
  private RangePanel rangePanel_;
  private LowerPanel lowerPanel_;
  private JButton breakpointBtn_;
  private JPopupMenu breakpointPopup_;
  private JSlider zoomSlider_;

  private double minReso_, maxReso_;
  private boolean doZoom_;

  private int selRange_ = -1;

  private Set<RRSListener> listeners_ = new HashSet<RRSListener>();

  private final int labelRowH_ = 20, arrowW_ = 10, barH_ = 20;
  private final double resoBuffer_ = 5;

  public ResolutionRangeSelector(Viewport viewport, List<Double> breakpoints, int selRange) {
    super(new BorderLayout());

    viewport_ = viewport;
    rangePanel_ = new RangePanel();

    updateBreakpoints(breakpoints);

    selRange_ = selRange;
    
    breakpointPopup_ = new JPopupMenu();
    JMenuItem currentMI = new JMenuItem("At Current Resolution..");
    currentMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addBreakpoint(viewport_.getCoordinates().getResolution());
      }
    });
    breakpointPopup_.add(currentMI);
    JMenuItem customMI = new JMenuItem("Custom Value..");
    customMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String reso = JOptionPane.showInputDialog("Specify Resolution:");
        if(reso == null || !FPUtil.isDouble(reso)) return;
        addBreakpoint(Double.parseDouble(reso));
      }
    });
    breakpointPopup_.add(customMI);

    breakpointBtn_ = GUIFactory.newButton("Breakpoint..", 80);
    breakpointBtn_.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        breakpointPopup_.show(breakpointBtn_, e.getX(), e.getY());
      }
    });

    zoomSlider_ = new JSlider();
    zoomSlider_.setPreferredSize(new Dimension(50, 20));
    zoomSlider_.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        //System.out.println(""+zoomSlider_.getValue());
        double factor = (double) (zoomSlider_.getValue()-50) / 50.0;
        if(doZoom_) viewport_.zoomRelative(factor);
      }
    });
    zoomSlider_.addMouseListener(new MouseAdapter() {

      @Override
      public void mousePressed(MouseEvent e) {
        viewport_.setReference();
        doZoom_ = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        //System.out.println("release!");
        doZoom_ = false;
        zoomSlider_.setValue(50);
      }
    });
    
    JPanel rightPanel = GUIFactory.newColumnPanel();
    rightPanel.add(breakpointBtn_);
    rightPanel.add(Box.createVerticalStrut(2));
    rightPanel.add(zoomSlider_);

    JPanel topPanel = new JPanel(new BorderLayout());

    topPanel.add(rangePanel_, BorderLayout.CENTER);
    topPanel.add(rightPanel, BorderLayout.EAST);

    lowerPanel_ = new LowerPanel();

    add(topPanel, BorderLayout.CENTER);
    add(lowerPanel_, BorderLayout.SOUTH);

    rangePanel_.revalidate();
  }

  public void addListener(RRSListener listener) {
    listeners_.add(listener);
  }

  private void addBreakpoint(double reso) {
    System.out.println("new breakpoint at "+reso);
    if(breakpoints_.contains(reso)) {
      JOptionPane.showMessageDialog(null, "Breakpoint at that resolution already exists");
      return;
    }

    breakpoints_.add(reso);
    minReso_ = breakpoints_.first() - resoBuffer_;
    maxReso_ = breakpoints_.last() + resoBuffer_;
    
    rangePanel_.repaint();

    lowerPanel_.repaint();

    for(RRSListener listener : listeners_) listener.breakpointAdded(reso);
  }

  private int resoToPixel(double reso) {
    double w = rangePanel_.getWidth() - arrowW_*2;
    double resoSpan = maxReso_ - minReso_;
    double x = arrowW_ + w * (reso-minReso_) / resoSpan;
    return (int) x;
  }

  void updateBreakpoints(List<Double> breakpoints) {
    breakpoints_ = new TreeSet<Double>();
    for(Double bp : breakpoints) breakpoints_.add(bp);
    if(!breakpoints_.isEmpty()) {
      minReso_ = breakpoints_.first() - resoBuffer_;
      maxReso_ = breakpoints_.last() + resoBuffer_;
    }
  }

  public void setSelectedRange(int selRange) {
    System.out.println("selecting "+selRange);
    selRange_ = selRange;
    rangePanel_.repaint();
    lowerPanel_.repaint();
    for(RRSListener listener : listeners_) listener.rangeSelected(selRange_);
  }

  public void repaintAll() {
    this.repaint();
    rangePanel_.repaint();
    lowerPanel_.repaint();
  }
  
  private class RangePanel extends JPanel {

    private int mouseOverRange_  = -1, mouseOverLabel_ = -1, mouseOverLabelMenu_;

    private List<Rectangle2D> bpLabelBounds_;

    private JPopupMenu labelPopup_;

    public RangePanel() {
      super();
      this.setMinimumSize(new Dimension(1, 45));
      this.setPreferredSize(new Dimension(1, 45));

      labelPopup_ = new JPopupMenu();
      JMenuItem deleteBreakpointMI = new JMenuItem("Delete Breakpoint");
      deleteBreakpointMI.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          for(RRSListener listener : listeners_) {
            listener.breakpointRemoved((Double) breakpoints_.toArray()[mouseOverLabelMenu_]);
          }
        }
      });
      labelPopup_.add(deleteBreakpointMI);

      this.addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
          if(e.getY() < labelRowH_ || e.getY() > labelRowH_+barH_) {
            mouseOverLabel_ = mouseOverRange_ = -1;
            for(int i=0; i<bpLabelBounds_.size(); i++) {
              if(bpLabelBounds_.get(i).contains(e.getX(), e.getY())) {
                //System.out.println("over bp "+i);
                mouseOverLabel_ = i;
              }
            }
          }
          else if(breakpoints_.size() == 0) {
            mouseOverRange_ = 0;
          }
          else {
            int i = 0;
            mouseOverRange_ = breakpoints_.size();
            for(double reso : breakpoints_) {
              if(e.getX() < resoToPixel(reso)) {
                mouseOverRange_ = i;
                break;
              }
              i++;
            }
          }
          repaint();
        }
      });

      this.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if(e.getButton() == MouseEvent.BUTTON1) leftClick(e.getX(), e.getY());
          else if(e.getButton() == MouseEvent.BUTTON3) rightClick(e.getX(), e.getY());
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
          mouseOverRange_ = -1;
          repaint();
        }
      });

    }

    public void leftClick(int x, int y) {
      if(mouseOverRange_ >= 0) {
        setSelectedRange(mouseOverRange_);
      }
    }

    public void rightClick(int x, int y) {
      if(mouseOverLabel_ >= 0) {
        labelPopup_.show(this, x, y);
        mouseOverLabelMenu_ = mouseOverLabel_;
      }
    }

    @Override
    public void paintComponent(Graphics g) {
      //System.out.println("paint rrs rp");

      Graphics2D g2d = (Graphics2D) g;

      RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHints(renderHints);

      g2d.setColor(ResolutionRangeSelector.this.getBackground());
      g2d.fillRect(0, 0, getWidth(), getHeight());

      double w = getWidth();
      int iw = (int) w;
      g2d.setColor(Color.blue);

      Polygon leftPoly = new Polygon();
      leftPoly.addPoint(arrowW_, labelRowH_);
      leftPoly.addPoint(0, labelRowH_ + barH_/2);
      leftPoly.addPoint(arrowW_, labelRowH_+barH_);
      

      Polygon rightPoly = new Polygon();
      rightPoly.addPoint(iw-arrowW_, labelRowH_);
      rightPoly.addPoint(iw, labelRowH_ + barH_/2);
      rightPoly.addPoint(iw-arrowW_, labelRowH_+barH_);

      ArrayList<Double> bpResos = new ArrayList<Double>(breakpoints_);
      for(int i=0; i<=breakpoints_.size(); i++) {
        if(selRange_ >= 0 && selRange_ == i) g2d.setColor(new Color(30, 144, 255));
        else g2d.setColor((i == mouseOverRange_) ? Color.blue: new Color(0, 0, 128));

        int left, right;
        if(i == 0) {
          left = arrowW_;
          right = (breakpoints_.size() == 0) ? rangePanel_.getWidth() - arrowW_ : resoToPixel(bpResos.get(0))-2;
          g2d.fillPolygon(leftPoly);
          if(breakpoints_.size() == 0) g2d.fillPolygon(rightPoly);
        }
        else if(i == breakpoints_.size()) {
          left = resoToPixel(bpResos.get(i-1))+2;
          right = rangePanel_.getWidth() - arrowW_;
          g2d.fillPolygon(rightPoly);
        }
        else {
          left = resoToPixel(bpResos.get(i-1))+2;
          right = resoToPixel(bpResos.get(i))-2;
        }
        g2d.fillRect(left, labelRowH_, right-left, barH_);
      }

      bpLabelBounds_ = new ArrayList<Rectangle2D>();
      Font font = new Font(Font.DIALOG, Font.BOLD, 10);
      g2d.setFont(font);
      FontMetrics fm = this.getFontMetrics(font);
      int i = 0;
      for(double bp : breakpoints_) {
        int x = resoToPixel(bp);
        DecimalFormat df = new DecimalFormat("#.#");
        int strWidth = fm.stringWidth(df.format(bp));

        g2d.setColor(Color.red);
        g2d.fillRoundRect(x-strWidth/2-2, labelRowH_-18, strWidth+4, 13, 6, 6);
        g2d.fillRect(x-1, labelRowH_-5, 2, 5+barH_);
        bpLabelBounds_.add(new Rectangle2D.Double(x-strWidth/2-2, labelRowH_-18, strWidth+4, 13));
        
        g2d.setColor(i == mouseOverLabel_ ? Color.yellow : Color.white);
        g2d.drawString(df.format(bp), x-strWidth/2, labelRowH_-8);

        i++;
      }
    }
  }

  /*private class Breakpoint {
    private int x_, y_, width_, height_;

    public void draw(Graphics2D g2d) {

    }
  }*/

  /*private class Breakpoint extends JPanel {

    private double reso_;

    private JLabel label_;

    public Breakpoint(double reso) {
      setPreferredSize(new Dimension(3, barH_));
      label_ = new JLabel();
      label_.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
      rangePanel_.add(label_);
      setResolution(reso);

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          setCursor(new Cursor(Cursor.MOVE_CURSOR));
        }

        @Override
        public void mouseExited(MouseEvent e) {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
      });
    }

    public double getResolution() {
      return reso_;
    }

    public void setResolution(double reso) {
      reso_ = reso;
      DecimalFormat df = new DecimalFormat("#.#");
      label_.setText(""+df.format(reso));
    }

    public void updateLocation() {
      int x = resoToPixel(reso_);
      System.out.println("uL bp="+reso_+" x="+x);
      setLocation((int) x - 1, labelRowH_);
      int lx = x - label_.getWidth()/2;
      System.out.println(" lx="+lx);
      label_.setLocation(lx, labelRowH_-14);
    }

    @Override
    public void paintComponent(Graphics g) {
      System.out.println("pC bp="+reso_);
      Graphics2D g2d = (Graphics2D) g;
      updateLocation();
      g2d.setColor(rangePanel_.getBackground());
      g2d.fillRect(0, 0, 3, barH_);
    }

  }*/

  private class LowerPanel extends JPanel {

    public LowerPanel() {
      super();
      this.setMinimumSize(new Dimension(1, 25));
      this.setPreferredSize(new Dimension(1, 25));
    }

    @Override
    public void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      
      g2d.setColor(ResolutionRangeSelector.this.getBackground());
      g2d.fillRect(0, 0, getWidth(), getHeight());

      if(selRange_ < 0) return;

      RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHints(renderHints);

      int left, right;
      ArrayList<Double> bpResos = new ArrayList<Double>(breakpoints_);

      if(selRange_ == 0) {
        left = arrowW_;
        right = (breakpoints_.size() == 0) ? rangePanel_.getWidth() - arrowW_ : resoToPixel(bpResos.get(0));
      }
      else if(selRange_ == breakpoints_.size()) {
        left = resoToPixel(bpResos.get(selRange_-1))+2;
        right = rangePanel_.getWidth() - arrowW_;
      }
      else {
        left = resoToPixel(bpResos.get(selRange_-1))+2;
        right = resoToPixel(bpResos.get(selRange_))-2;
      }

      Polygon poly = new Polygon();
      poly.addPoint(left, 0);
      poly.addPoint(right, 0);
      poly.addPoint(getWidth(), getHeight());
      poly.addPoint(0, getHeight());
      g2d.setColor(Color.gray);
      g2d.fillPolygon(poly);

    }
  }

}
