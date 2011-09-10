/*
 * TSDocument.java
 * 
 * Created by demory on Mar 1, 2010, 8:45:04 PM
 * 
 * Copyright 2010 David D. Emory
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

package org.transketch.apps.desktop;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.transketch.apps.desktop.gui.DocumentFrame;
import org.transketch.core.network.TSNetwork;
import org.transketch.core.network.Line;
import org.transketch.core.network.LineStyles;
import org.transketch.core.network.line.LineLayerIndexComparator;
import org.transketch.core.network.stop.Stop;
import org.transketch.core.network.stop.StopStyles;
import org.transketch.util.viewport.MapCoordinates;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author demory
 */
public class TSDocument {

  private int id_;

  private TSNetwork network_;
  private LineStyles lineStyles_ = new LineStyles();
  private StopStyles stopStyles_ = new StopStyles();

  private File activeFile_;

  private Editor ed_;

  private DocumentFrame frame_;

  public TSDocument(int id) {
    id_ = id;
    network_ = new TSNetwork(this);
  }

  public DocumentFrame getFrame() {
    return frame_;
  }

  public void setFrame(DocumentFrame frame) {
    frame_ = frame;
  }

  public String getWorkingTitle() {
    return hasActiveFile() ? getActiveFile().getName() : "Untitled-"+id_;
  }

  public TSNetwork getNetwork() {
    return network_;
  }

  public LineStyles getLineStyles() {
    return lineStyles_;
  }

  public StopStyles getStopStyles() {
    return stopStyles_;
  }

  public Color getBGColor() {
    return Color.white;
  }

  // FILE I/O //
  
  public boolean hasActiveFile() {
    return activeFile_ != null;
  }

  public File getActiveFile() {
    return activeFile_;
  }

  public void readXMLFile(File file) {
    System.out.println("Loading transit data from XML file");

    try {
      DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = docBuilder.parse(file);
      NodeList docNodes = doc.getChildNodes();
      Node thisNode = docNodes.item(0);
      if (!thisNode.getNodeName().equals("transketch")) {
        System.out.println("Not a valid Transit Sketchpad data file");
        return;
      }

      lineStyles_.readFromXML(thisNode);
      stopStyles_.readFromXML(thisNode);
      network_.readFromXML(thisNode);
      
      activeFile_ = file;
      //System.out.println("af="+activeFile_);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void writeXMLFile() {
    if(activeFile_ == null) return;
    writeXMLFile(activeFile_);
  }

  public void writeXMLFile(File file) {

    try {
      FileWriter writer = new FileWriter(file);
      writer.write("<?xml version=\"1.0\"?>\n");
      writer.write("<transketch>\n");
      writer.write(network_.getXML("  "));
      writer.write(lineStyles_.getXML("  "));
      writer.write(stopStyles_.getXML("  "));
      writer.write("</transketch>\n");
      writer.close();
      activeFile_ = file;

    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void writePNGFile(File file, MapCoordinates coords) {

    Rectangle2D bbox = network_.getBoundingBox();
    System.out.println("dimW: "+bbox.getWidth()+" x "+bbox.getHeight());
    int w = coords.distToScreen(bbox.getWidth()), h = coords.distToScreen(bbox.getHeight());
    System.out.println("dimP: "+w+" x "+h);

    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = (Graphics2D) img.getGraphics();

    RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHints(renderHints);

    g2d.setColor(Color.white);
    g2d.fillRect(0, 0, w, h);

    MapCoordinates c2 = new MapCoordinates(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), w, h);
    
    ImageCanvas imgCanvas = new ImageCanvas(g2d, c2);

    for(Line line : ed_.getDocument().getNetwork().getLines(new LineLayerIndexComparator())) {
      if(line.isEnabled())
        line.draw(imgCanvas);
    }

    if(ed_.getBoolProperty(Editor.Property.SHOW_STOPS))
      for(Stop stop : ed_.getDocument().getNetwork().getStops()) stop.draw(imgCanvas);
    
    try {
      ImageIO.write(img, "png", file);
    } catch (IOException ex) {
      Logger.getLogger(TSDocument.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void writeSVGFile(File file, MapCoordinates coords) {

    Rectangle2D bbox = network_.getBoundingBox();
    int w = coords.distToScreen(bbox.getWidth()), h = coords.distToScreen(bbox.getHeight());
    MapCoordinates c2 = new MapCoordinates(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), w, h);

    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    Document doc = domImpl.createDocument(null, "svg", null);
    SVGGraphics2D svg = new SVGGraphics2D(doc);
    ImageCanvas imgCanvas = new ImageCanvas(svg, c2);

    for(Line line : ed_.getDocument().getNetwork().getLines(new LineLayerIndexComparator())) {
      if(line.isEnabled())
        line.draw(imgCanvas);
    }

    if(ed_.getBoolProperty(Editor.Property.SHOW_STOPS))
      for(Stop stop : ed_.getDocument().getNetwork().getStops()) stop.draw(imgCanvas);

    try {
      svg.stream(new FileWriter(file), false);
    } catch (IOException ex) {
      Logger.getLogger(TSDocument.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public class ImageCanvas implements TSCanvas {

    private Graphics2D g2d_;
    private MapCoordinates coords_;

    public ImageCanvas(Graphics2D g2d, MapCoordinates coords) {
      g2d_ = g2d;
      coords_ = coords;
    }

    public Graphics2D getGraphics2D() {
      return g2d_;
    }

    public MapCoordinates getCoordinates() {
      return coords_;
    }

  }


  public Editor getEditor() {
    return ed_;
  }

  public void setEditor(Editor ed) {
    ed_ = ed;
  }
}
