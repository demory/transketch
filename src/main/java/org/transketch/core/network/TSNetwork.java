/*
 * TSNetwork.java
 * 
 * Created by demory on Mar 28, 2009, 1:33:15 PM
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

package org.transketch.core.network;

import org.transketch.core.network.corridor.Corridor;
import java.awt.Color;
import org.transketch.core.network.stop.Stop;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.core.network.stop.AnchorBasedStop;
import org.transketch.util.FPUtil;
import org.jgrapht.graph.Pseudograph;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author demory
 */
public class TSNetwork {
  private final static Logger logger = Logger.getLogger(TSNetwork.class);

  private TSDocument doc_;

  private Pseudograph<AnchorPoint, Corridor> graph_;

  private Map<Integer, AnchorPoint> points_;
  private Map<Integer, Corridor> corridors_;
  private Map<Integer, Line> lines_;
  private Map<Integer, Stop> stops_;

  private int maxPointID_, maxCorridorID_, maxLineID_, maxStopID_;

  public TSNetwork(TSDocument doc) {
    doc_ = doc;
    clear();
  }

  public void clear() {
    graph_= new Pseudograph<AnchorPoint, Corridor>(Corridor.class);
    points_ = new HashMap<Integer, AnchorPoint>();
    corridors_ = new HashMap<Integer, Corridor>();
    lines_ = new HashMap<Integer, Line>();
    stops_ = new HashMap<Integer, Stop>();
    maxPointID_ = maxCorridorID_ = maxLineID_= 0;
  }
  
  public void clearBundlerData() {
    for(AnchorPoint pt : getAnchorPoints()) 
      pt.clearBundlerData();
    for(Line line : getLines())
      line.clearOffsets();
    
  }

  public Rectangle2D.Double getBoundingBox() {
    double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE;
    double maxx = -Double.MAX_VALUE, maxy = -Double.MAX_VALUE;
    for(AnchorPoint pt : points_.values()) {
      minx = Math.min(minx, pt.getX());
      miny = Math.min(miny, pt.getY());
      maxx = Math.max(maxx, pt.getX());
      maxy = Math.max(maxy, pt.getY());
    }
    return new Rectangle2D.Double(minx, miny, maxx-minx, maxy-miny);
  }
  
  // ANCHOR POINT METHODS

  public Collection<AnchorPoint> getAnchorPoints() {
    return points_.values();
  }

  public AnchorPoint getAnchorPoint(int id) {
    return points_.get(id);
  }

  public void addAnchorPoint(AnchorPoint point) {
    graph_.addVertex(point);
    points_.put(point.getID(), point);
  }

  public int newAnchorPointID() {
    return ++maxPointID_;
  }

  public void deleteAnchorPoint(AnchorPoint point) {
    deleteAnchorPoint(point, true);
  }

  public void deleteAnchorPoint(AnchorPoint point, boolean deleteCorridors) {
    if(deleteCorridors) {
      for(Corridor c : new HashSet<Corridor>(point.getCorridors())) {
        deleteCorridor(c);
      }
    }
    graph_.removeVertex(point);
    points_.remove(point.getID());
  }

  public AnchorPoint getPointAtXY(double x, double y, double tol) {
    AnchorPoint best = null;

    double minDist = tol;
    for(AnchorPoint pt : getAnchorPoints()) {
      double dist = FPUtil.magnitude(x, y, pt.getX(), pt.getY());
      if(dist < minDist) {
        best = pt;
        minDist = dist;
      }
    }

    return best;
  }
  
  public boolean pointExistsAt(double x, double y) {
    for(AnchorPoint pt : getAnchorPoints())
      if(pt.getX() == x && pt.getY() == y) return true;
    return false;
  }

  public void realignCorridorFrom(Corridor corr, AnchorPoint newFrom) {
    AnchorPoint toPt = corr.tPoint();
    graph_.removeEdge(corr);
    graph_.addEdge(newFrom, toPt, corr);
    corr.setFromPoint(newFrom);
  }

  public void realignCorridorTo(Corridor corr, AnchorPoint newTo) {
    AnchorPoint fromPt = corr.fPoint();
    graph_.removeEdge(corr);
    graph_.addEdge(fromPt, newTo, corr);
    corr.setToPoint(newTo);
  }

  // CORRIDOR METHODS

  public Collection<Corridor> getCorridors() {
    return corridors_.values();
  }
  
  public Corridor getCorridor(int id) {
    return corridors_.get(id);
  }

  public Collection<Corridor> incidentCorridors(AnchorPoint point) {
    return graph_.edgesOf(point);
  }

  public int newCorridorID() {
    return ++maxCorridorID_;
  }

  public void addCorridor(Corridor corr) {
    graph_.addEdge(corr.fPoint(), corr.tPoint(), corr);
    corridors_.put(corr.getID(), corr);
  }

  public void deleteCorridor(Corridor corr) {
    corr.unregisterFromEndpoints();
    graph_.removeEdge(corr);
    corridors_.remove(corr.getID());
  }
  
  public Corridor getCorridorAtXY(double x, double y, double tol) {
    Corridor best = null;
    double minDist = tol;
    for(Corridor c : getCorridors()) {
      if(c.isStraight()) {
        double dist = FPUtil.distToSegment(x, y, c.x1(), c.y1(), c.x2(), c.y2());
        if(dist < minDist) {
          minDist = dist;
          best = c;
        }
      }
      else { // "bent" corridor
        Point2D.Double e = c.getElbow();
        double d1 = FPUtil.distToSegment(x, y, c.x1(), c.y1(), e.x, e.y);
        double d2 = FPUtil.distToSegment(x, y, e.x, e.y, c.x2(), c.y2());
        double dist = Math.min(d1, d2);
        if(dist < minDist) {
          minDist = dist;
          best = c;
        }
      }
    }
    return best;
  }

  public Corridor getStraightExtension(Corridor corr, AnchorPoint pt) {
    double tol = .001;
    double corrST = corr.getStraightTheta();
    for(Corridor c : incidentCorridors(pt)) {
      if(c.sharesEndpointsWith(corr)) continue;
      //logger.debug(c.getStraightTheta() + " vs "+corr.getStraightTheta());
      if(c.isStraight() && Math.abs(c.getStraightTheta() - corrST) < tol) //c.isVertical() && corr.isVertical() || c.isHorizontal() && corr.isHorizontal())
        return c;
    }
    return null;
  }

  // LINE METHODS

  public Collection<Line> getLines() {
    return lines_.values();
  }

  public Collection<Line> getLines(Comparator<Line> c) {
    List<Line> sorted = new LinkedList<Line>(lines_.values());// TreeSet<Line>(c);
    Collections.sort(sorted, c);
    //sorted.addAll(lines_.values());
    return sorted;
  }

  public int newLineID() {
    return ++maxLineID_;
  }

  public void addLine(Line line) {
    lines_.put(line.getID(), line);
  }

  public void deleteLine(Line line_) {
    lines_.remove(line_.getID());
  }

  public List<Corridor> findPathToLine(Corridor corr, Line line, int maxSteps) {
    //logger.debug("fPTL: corr "+corr.getID()+" to "+line.startPoint().getID()+"/"+line.endPoint().getID());
    Collection<Corridor> dnt = new HashSet<Corridor>(line.getCorridors()); //Collections.singleton(corr);
    dnt.add(corr);
    List<Corridor> shortest = null;

    List<Corridor> fs = findPathBetweenPoints(corr.fPoint(), line.startPoint(), dnt, maxSteps, " fs> ");
    //logger.debug(" fs: "+(fs == null ? "none" : "len="+fs.size()));
    if(fs != null && (shortest == null || fs.size() < shortest.size())) shortest = fs;

    List<Corridor> fe = findPathBetweenPoints(corr.fPoint(), line.endPoint(), dnt, maxSteps, " fe> ");
    //logger.debug(" fe: "+(fe == null ? "none" : "len="+fe.size()));
    if(fe != null && (shortest == null || fe.size() < shortest.size())) shortest = fe;

    List<Corridor> ts = findPathBetweenPoints(corr.tPoint(), line.startPoint(), dnt, maxSteps, " ts> ");
    //logger.debug(" ts: "+(ts == null ? "none" : "len="+ts.size()));
    if(ts != null && (shortest == null || ts.size() < shortest.size())) shortest = ts;

    List<Corridor> te = findPathBetweenPoints(corr.tPoint(), line.endPoint(), dnt, maxSteps, " te> ");
    //logger.debug(" te: "+(te == null ? "none" : "len="+te.size()));
    if(te != null && (shortest == null || te.size() < shortest.size())) shortest = te;

    return shortest;
  }

  public List<Corridor> findPathBetweenPoints(AnchorPoint from, AnchorPoint to, Collection<Corridor> doNotTraverse, int maxSteps, String indent) {
    Queue<AnchorPoint> q = new LinkedList<AnchorPoint>();
    Map<AnchorPoint, Corridor> visited = new HashMap<AnchorPoint, Corridor>();
    List<Corridor> result = new LinkedList<Corridor>();

    q.add(from);
    visited.put(from, null);

    while(!q.isEmpty()) {
      AnchorPoint pt = q.poll();
      for(Corridor corr : incidentCorridors(pt)) {
        if(doNotTraverse.contains(corr)) continue;
        AnchorPoint opp = corr.opposite(pt);
        if(opp == to) {
          List<Corridor> path = new LinkedList<Corridor>();
          path.add(corr);
          AnchorPoint p = pt;
          while(true) {
            Corridor c = visited.get(p);
            if(c == null) break;
            path.add(c);
            p = c.opposite(p);
          }
          return path;
        }
        if(!visited.containsKey(opp)) {
          visited.put(opp, corr);
          q.add(opp);
        }
      }
    }

    return null;
  }

  boolean bundlerEnabled_ = false;

  public void rebundle() {
    //if(bundlerEnabled_)
      new Bundler(this);
  }

  public void enableBundler() {
    bundlerEnabled_ = true;
  }
  
  // STOP METHODS

  public Collection<Stop> getStops() {
    return stops_.values();
  }

  public Collection<Stop> getStops(AnchorPoint pt) {
    Set<Stop> stops = new HashSet<Stop>();
    for(Stop stop : stops_.values()) {
      if(stop.getType() == Stop.Type.ANCHORBASED && ((AnchorBasedStop) stop).getAnchorPoint() == pt) stops.add(stop);
    }
    return stops;
  }

  public int newStopID() {
    return ++maxStopID_;
  }

  public void addStop(Stop stop) {
    stops_.put(stop.getID(), stop);
    //logger.debug("# stops = "+stops_.size());
  }

  public void deleteStop(Stop stop) {
    stops_.remove(stop.getID());
  }

  public void updateStopRenderers() {
    for(Stop stop : stops_.values())
      stop.updateRenderer();
  }


  // FILE I/O METHODS

  public void readFromXML(Node tsNode) {

    //try {
    clear();

    NodeList docNodes = tsNode.getChildNodes();
    for (int i = 0; i < docNodes.getLength(); i++) {
      if(docNodes.item(i).getNodeName().equals("points")) {
        NodeList pointNodes = docNodes.item(i).getChildNodes();
        int maxID = 0;
        for (int ip = 0; ip < pointNodes.getLength(); ip++) {
          if(pointNodes.item(ip).getNodeName().equals("point")) {
            Node pointNode = pointNodes.item(ip);
            NamedNodeMap attributes = pointNode.getAttributes();
            int id = new Integer(attributes.getNamedItem("id").getNodeValue()).intValue();
            if(id > maxID) maxID = id;
            double x = new Double(attributes.getNamedItem("x").getNodeValue()).doubleValue();
            double y = new Double(attributes.getNamedItem("y").getNodeValue()).doubleValue();
            addAnchorPoint(new AnchorPoint(id, x, y));
          }
        }
        maxPointID_ = maxID;
      }
      if(docNodes.item(i).getNodeName().equals("corridors")) {
        NodeList corrNodes = docNodes.item(i).getChildNodes();
        int maxID = 0;
        for (int ic = 0; ic < corrNodes.getLength(); ic++) {
          if(corrNodes.item(ic).getNodeName().equals("corridor")) {
            Node corrNode = corrNodes.item(ic);
            NamedNodeMap attributes = corrNode.getAttributes();
            int id = new Integer(attributes.getNamedItem("id").getNodeValue()).intValue();
            if(id > maxID) maxID = id;
            int fpoint = new Integer(attributes.getNamedItem("fpoint").getNodeValue()).intValue();
            int tpoint = new Integer(attributes.getNamedItem("tpoint").getNodeValue()).intValue();
            Corridor corr = new Corridor(id, points_.get(fpoint), points_.get(tpoint), false);
            addCorridor(corr);

            if(attributes.getNamedItem("theta") != null)
              corr.setElbowAngle(Double.parseDouble(attributes.getNamedItem("theta").getNodeValue()));
          }
        }
        maxCorridorID_ = maxID;
      }
      if(docNodes.item(i).getNodeName().equals("lines")) {
        NodeList lineNodes = docNodes.item(i).getChildNodes();
        int maxID = 0;
        for (int il = 0; il < lineNodes.getLength(); il++) {
          if(lineNodes.item(il).getNodeName().equals("line")) {
            Node lineNode = lineNodes.item(il);
            NamedNodeMap attributes = lineNode.getAttributes();
            int id = new Integer(attributes.getNamedItem("id").getNodeValue()).intValue();
            //logger.debug("loading line "+id);
            if(id > maxID) maxID = id;
            String name = attributes.getNamedItem("name").getNodeValue();

            Line line = new Line(id, name);

            if(attributes.getNamedItem("bundled") != null) {
              boolean bundled = Boolean.parseBoolean(attributes.getNamedItem("bundled").getNodeValue());
              line.setBundled(bundled);
            }

            if(attributes.getNamedItem("layerindex") != null) {
              double layerIndex = Double.parseDouble(attributes.getNamedItem("layerindex").getNodeValue());
              line.setLayerIndex(layerIndex);
            }

            // initialize the corridors
            if(attributes.getNamedItem("corridors") != null) {
              String[] corrIDs = attributes.getNamedItem("corridors").getNodeValue().split(",");
              for(String corrID : corrIDs) {
                Corridor corr = corridors_.get(new Integer(corrID));
                line.initCorridor(corr);
                //logger.debug(" added corr "+corrID);
              }
            }

            /*if(attributes.getNamedItem("style") != null) {
              String styleID = attributes.getNamedItem("style").getNodeValue();
              if(FPUtil.isInteger(styleID)) {
                line.setStyle(doc_.getStyles().getStyle(new Integer(styleID)));
              }
              else {
                line.setStyle(new LineStyle(LineStyle.Preset.valueOf(styleID)));
              }
            }*/

            NodeList lineSubNodes = lineNode.getChildNodes();
            for (int ils = 0; ils < lineSubNodes.getLength(); ils++) {

              if(lineSubNodes.item(ils) == null) continue;

              // process a top-level line property node (not currently used)
              /*if(stopSubNodes.item(iss).getNodeName().equals("property")) {
                Node propNode = stopSubNodes.item(iss);
                String propName = propNode.getAttributes().getNamedItem("name").getNodeValue();
                String propValue = propNode.getAttributes().getNamedItem("value").getNodeValue();
                // do something with values
              }*/

              // process the line style block
              if(lineSubNodes.item(ils).getNodeName().equals("style")) {
                Node styleNode = lineSubNodes.item(ils);
                NamedNodeMap styleAttrs = styleNode.getAttributes();
                String styleID = styleAttrs.getNamedItem("id").getNodeValue();
                if(FPUtil.isInteger(styleID))
                  line.setStyle(doc_.getLineStyles().getStyle(new Integer(styleID)));
                else
                  line.setStyle(new LineStyle(LineStyle.Preset.valueOf(styleID)));

                // process any line-specified color definitions
                NodeList colorNodes = styleNode.getChildNodes();
                for (int ilc = 0; ilc < colorNodes.getLength(); ilc++) {
                  if(colorNodes.item(ilc) != null && colorNodes.item(ilc).getNodeName().equals("colorkey")) {
                    Node colorNode = colorNodes.item(ilc);
                    String colorKey = colorNode.getAttributes().getNamedItem("key").getNodeValue();
                    String colorValue = colorNode.getAttributes().getNamedItem("color").getNodeValue();
                    Color color = new Color(new Integer(colorValue).intValue());
                    line.setStyleColor(colorKey, color);
                  }
                }
              }

            }

            /*int openMonth = 0, openYear = 0;
            if(attributes.getNamedItem("opendate") != null) {
              String opendate = attributes.getNamedItem("opendate").getNodeValue();
              if(opendate.length() == 6) {
                openMonth = Integer.parseInt(opendate.substring(0, 2));
                openYear = Integer.parseInt(opendate.substring(2, 6));
              }
            }*/

            //line.initBaseOffset(offset);

            addLine(line);
          }
        }
        maxLineID_ = maxID;
      }
      if(docNodes.item(i).getNodeName().equals("stops")) {
        NodeList stopNodes = docNodes.item(i).getChildNodes();
        int maxID = 0;
        for (int is = 0; is < stopNodes.getLength(); is++) {
          if(stopNodes.item(is).getNodeName().equals("stop")) {
            Node stopNode = stopNodes.item(is);
            NamedNodeMap attributes = stopNode.getAttributes();
            int stopID = new Integer(attributes.getNamedItem("id").getNodeValue()).intValue();
            if(stopID > maxID) maxID = stopID;
            String stopName = attributes.getNamedItem("name").getNodeValue();

            String typeName  = null;
            Map<String, String> stopProperties = new HashMap<String, String>();
            Map<String, String> typeProperties = new HashMap<String, String>();
            int stopStyleID = 0;

            NodeList stopSubNodes = stopNode.getChildNodes();
            for (int iss = 0; iss < stopSubNodes.getLength(); iss++) {

              if(stopSubNodes.item(iss) == null) continue;
              
              // process a top-level stop property
              if(stopSubNodes.item(iss).getNodeName().equals("property")) {
                Node propNode = stopSubNodes.item(iss);
                String propName = propNode.getAttributes().getNamedItem("name").getNodeValue();
                String propValue = propNode.getAttributes().getNamedItem("value").getNodeValue();
                stopProperties.put(propName, propValue);
              }

              // process the type block
              if(stopSubNodes.item(iss).getNodeName().equals("type")) {
                Node typeNode = stopSubNodes.item(iss);
                NamedNodeMap typeAttrs = typeNode.getAttributes();
                typeName = typeAttrs.getNamedItem("name").getNodeValue();
                NodeList typePropNodes = typeNode.getChildNodes();
                for (int istp = 0; istp < stopSubNodes.getLength(); istp++) {
                  if(typePropNodes.item(istp) != null && typePropNodes.item(istp).getNodeName().equals("property")) {
                    Node typePropNode = typePropNodes.item(istp);
                    String propName = typePropNode.getAttributes().getNamedItem("name").getNodeValue();
                    String propValue = typePropNode.getAttributes().getNamedItem("value").getNodeValue();
                    typeProperties.put(propName, propValue);
                  }
                }
              }

              // process the stop style block
              if(stopSubNodes.item(iss).getNodeName().equals("style")) {
                Node styleNode = stopSubNodes.item(iss);
                NamedNodeMap styleAttrs = styleNode.getAttributes();
                stopStyleID = new Integer(styleAttrs.getNamedItem("id").getNodeValue());
              }

              /*// process the label style block
              if(stopSubNodes.item(iss).getNodeName().equals("labelstyle")) {
              }*/
            }

            // create the stop
            Stop stop = null;
            try {
              //logger.debug("typeName="+typeName);
              Class cl = Class.forName(Stop.Type.valueOf(typeName).className_);
              Constructor co = cl.getConstructor(new Class[] {Integer.class, String.class} );
              stop = (Stop) co.newInstance(new Object[] {stopID, stopName} );
              stop.applyStopProperties(stopProperties);
              stop.applyTypeProperties(typeProperties, this);
              if(stopStyleID > 0) stop.setStyle(doc_.getStopStyles().getStyle(stopStyleID));
              addStop(stop);
            } catch (Exception ex) {
              logger.error("error creating stop", ex);
            }

          }

        }
        maxStopID_ = maxID;
      } // end of stop processing block

    }
  }
  
  public String getXML(String indent) {
    String xml = "";

    xml += indent+"<points>\n";
    for(AnchorPoint ap : getAnchorPoints()) {
      xml += indent+"  "+ap.getXML();
    }
    xml += indent+"</points>\n";
    xml += indent+"<corridors>\n";
    for(Corridor corr : getCorridors()) {
      xml += indent+"  "+corr.getXML();
    }
    xml += indent+"</corridors>\n";

    xml += indent+"<lines>\n";
    for(Line line : getLines()) {
      xml += line.getXML(indent+"  ");
    }
    xml += indent+"</lines>\n";

    xml += indent+"<stops>\n";
    for(Stop stop : getStops()) {
      xml += stop.getXML(indent+"  ");
    }
    xml += indent+"</stops>\n";

    return xml;
  }


}
