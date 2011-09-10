package org.transketch.apps.desktop.gui.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import org.transketch.core.network.LineStyleView;
import org.transketch.core.network.LineStyleLayer;

public class LineStylePreviewPanel extends JPanel {

  private LineStyleView styleInfo_;

  private Color borderColor_;
  private int borderWidth_;
  private boolean antialias_;

  public LineStylePreviewPanel(LineStyleView style, int width, int height, Color borderColor, int borderWidth, boolean antialias) {
    super();
    styleInfo_ = style;
    borderColor_ = borderColor;
    borderWidth_ = borderWidth;
    antialias_ = antialias;
    setPreferredSize(new Dimension(width, height));
  }

  public void setStyleInfo(LineStyleView info) {
    styleInfo_ = info;
  }
  
  public void setAntialias(boolean antialias) {
    antialias_ = antialias;
  }
  
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    if (antialias_) {
      RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHints(renderHints);
    }
    g2d.setColor(borderColor_);
    g2d.fillRect(0, 0, getWidth(), getHeight());
    g2d.setColor(Color.WHITE);
    g2d.fillRect(borderWidth_, borderWidth_, getWidth() - 2*borderWidth_, getHeight() - 2*borderWidth_);
    List<LineStyleLayer> subStylesCopy = new LinkedList(styleInfo_.getLayers());

    Collections.reverse(subStylesCopy);
    int y = getHeight() / 2;
    for (LineStyleLayer ss : subStylesCopy) {
      g2d.setStroke(ss.getStroke());
      g2d.setColor(ss.getColor());
      g2d.drawLine(4, y, getWidth() - 4, y);
    }
  }
}
