/*
 * OTPImporter.java
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

package org.transketch.core.network.otp;

import com.google.gson.Gson;
import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import org.opentripplanner.util.PolylineEncoder;
import org.opentripplanner.util.model.EncodedPolylineBean;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.TSDocument;
import org.transketch.core.network.*;
import org.transketch.core.network.corridor.GeographicCorridorModel;
import org.transketch.core.network.corridor.NetworkCorridor;

/**
 *
 * @author demory
 */
public class OTPImporter {
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(OTPImporter.class);

  private TSNetwork network_;
  
  public OTPImporter(Editor ed) {
    network_ = ed.getDocument().getNetwork();
  }  
  
  public void importFromURL(URL url) {
    try {
      URLConnection conn = url.openConnection();
      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      runImporter(br);
      br.close();
       
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  
  public void importFromFile(File file) {
    try {
      
      BufferedReader br = new BufferedReader(new FileReader(file));
      runImporter(br);
      br.close();
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }    
  }
  
  public void runImporter(BufferedReader reader) {
    try {
      
      Gson gson = new Gson();
      OTPData data = gson.fromJson(reader, OTPData.class);

      logger.info("edges: n="+data.edges.size());
      logger.info("variantNames: n="+data.variantNames.size());
      logger.info("variantSets: n="+data.variantSets.size());
      logger.info("variantSetsByEdge: n="+data.variantSetsByEdge.size());
      logger.info("edgesByVariant: n="+data.edgesByVariant.size());
      
      Set<Point2D> vertices = new HashSet<Point2D>();
      
      List<OTPEdge> edges = new ArrayList<OTPEdge>();
      
      for(EncodedPolylineBean bean : data.edges) {
        edges.add(new OTPEdge(bean));
      }

      for(OTPEdge edge: edges) {
        vertices.add(edge.from_);
        vertices.add(edge.to_);                
      }
      
      logger.info("total unique vertices: "+vertices.size());
      
      // create AnchorPoints
      
      Map<Point2D, AnchorPoint> anchorLookup = new HashMap<Point2D, AnchorPoint>();
      int id = 1;
      
      double totalX = 0, totalY = 0;
      for(Point2D pt : vertices) {
        totalX += pt.getX();
        totalY += pt.getY();
      }
      double meanX = totalX/vertices.size(), meanY=totalY/vertices.size();
      int mult = 100;
      for(Point2D pt : vertices) {
        AnchorPoint anchor = new AnchorPoint(id++, (pt.getY()-meanY)*mult, (pt.getX()-meanX)*mult);
        network_.addAnchorPoint(anchor);
        anchorLookup.put(pt, anchor);
      }

      
      // create Corridors
      
      Map<Integer, NetworkCorridor> corridorLookup = new HashMap<Integer, NetworkCorridor>();
      Map<String, NetworkCorridor> corrStrMap = new HashMap<String, NetworkCorridor>();
 
      int i=0;
      boolean collapseEdges = false;
      for(OTPEdge edge : edges) {
        AnchorPoint from = anchorLookup.get(edge.from_);
        AnchorPoint to = anchorLookup.get(edge.to_);
        
        String edgeKey = Math.min(from.getID(), to.getID())+"_"+Math.max(from.getID(), to.getID());
        
        if(collapseEdges && corrStrMap.containsKey(edgeKey)) {
          corridorLookup.put(i, corrStrMap.get(edgeKey));
          i++;
          continue;
        }

        NetworkCorridor corr = new NetworkCorridor(i, from, to, false);
        
        List<Point2D> pts = new ArrayList<Point2D>();
        if(edge.coords_.size() > 2) {
          for(Coordinate c : edge.coords_.subList(1, edge.coords_.size()-1))
            pts.add(new Point2D.Double((c.y-meanY)*mult, (c.x-meanX)*mult));
        }
        corr.setModel(new GeographicCorridorModel(corr, pts));
        
        /*StylizedCorridorModel scm = new StylizedCorridorModel(corr);
        scm.setElbowAngle(0);
        corr.setModel(scm);*/
        
        network_.addCorridor(corr);
        corrStrMap.put(edgeKey, corr);
        corridorLookup.put(i, corr);
        i++;
      }
      System.out.println("corrStrMap count: "+corrStrMap.size());
      
      
      // create initial LineStyle
      
      LineStyle style = new LineStyle(1, "otp");
      LineSubStyle ss = new LineSubStyle();
      ss.addLayer(new LineStyleLayer(4, "color", null));
      ss.setEnvelope(8);
      style.addSubStyle(ss);
      
      // create Lines
      
      id=0;
      i=0;
      
      Set<String> routeIDs = new HashSet<String>();
      
      for(List<Integer> edgeList : data.edgesByVariant) {
        String variantName = data.variantNames.get(i);
        String routeID = variantName.split(" ")[0];
        i++;
        
        // read single variant for each route
        if(routeIDs.contains(routeID)) continue;
        routeIDs.add(routeID);
        
        //logger.debug("reading variant: "+variantName);
        
        Line line = new Line(id++, variantName);
        
        for(Integer edgeIndex : edgeList) {
          NetworkCorridor corr = corridorLookup.get(edgeIndex);
          System.out.println(" - adding corr "+corr+" (index "+edgeIndex+")");
          if(corr == null) {
            System.out.println("    ** null corridor!");
            continue;
          }
          if(line.size() > 0 && !corr.adjacentTo(line.endPoint()) && !corr.adjacentTo(line.startPoint())) {
            System.out.println("    ** non-adjacency!");
            break;
          }
          
          line.initCorridor(corr);
        }
        line.setStyle(style);
        line.setStyleColor("color", new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
        
        network_.addLine(line);
      }
      
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    System.out.println("done");
  }
  
 
  public class OTPData {
    List<EncodedPolylineBean> edges;
    List<String> variantNames;
    List<List<Integer>> variantSets;
    List<Integer> variantSetsByEdge; 
    List<List<Integer>> edgesByVariant;
  }
  
  public class OTPEdge {

    List<Coordinate> coords_;
    Point2D from_, to_;
    
    public OTPEdge(EncodedPolylineBean bean) {
      coords_ = PolylineEncoder.decode(bean);
      Coordinate start = coords_.get(0), end = coords_.get(coords_.size()-1);
      from_ = new Point2D.Double(round5(start.x), round5(start.y));
      to_ = new Point2D.Double(round5(end.x), round5(end.y));
      
    }
            
  }
  
  public static double round5(double d) {
    double r = d *100000;
    r = Math.round(r);
    return r/100000;    
  } 
  
 
}
