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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentripplanner.util.PolylineEncoder;
import org.opentripplanner.util.model.EncodedPolylineBean;
import org.transketch.apps.desktop.Editor;
import org.transketch.core.network.*;
import org.transketch.core.network.corridor.Corridor;

/**
 *
 * @author demory
 */
public class OTPImporter {
  
  private TSNetwork network_;
  
  public OTPImporter(Editor ed) {
    network_ = ed.getDocument().getNetwork();
  }  
  
  public void importFromURL(URL url) {
    try {
      URL otp = new URL("file:///home/demory/otp/temp/pdxroutes.json");

      URLConnection conn = otp.openConnection();
      new BufferedReader(new InputStreamReader(conn.getInputStream()));
    }
    catch(Exception e) {
      
    }
  }
  
  public void importFromFile(File file) {
    try {
      
      BufferedReader br = new BufferedReader(new FileReader(file));
      runImporter(br);
      br.close();
      
    } catch (Exception ex) {
      Logger.getLogger(OTPImporter.class.getName()).log(Level.SEVERE, null, ex);
    }    
  }
  
  public void runImporter(BufferedReader reader) {
    System.out.println("reading otp...");
    try {
      
      Gson gson = new Gson();
      OTPData data = gson.fromJson(reader, OTPData.class);

      System.out.println("edges: "+data.edges.size());
      System.out.println("variantNames: "+data.variantNames.size());
      System.out.println("variantSets: "+data.variantSets.size());
      System.out.println("variantSetsByEdge: "+data.variantSetsByEdge.size());
      System.out.println("edgesByVariant: "+data.edgesByVariant.size());
      
      Set<Point2D> vertices = new HashSet<Point2D>();
      
      List<OTPEdge> edges = new ArrayList<OTPEdge>();
      
      for(EncodedPolylineBean bean : data.edges) {
        edges.add(new OTPEdge(bean));
      }

      for(OTPEdge edge: edges) {
        vertices.add(edge.from_);
        vertices.add(edge.to_);                
      }
      
      System.out.println("vertices: "+vertices.size());
      
      Map<Point2D, AnchorPoint> anchorLookup = new HashMap<Point2D, AnchorPoint>();
      int id = 1;
      
      double totalX = 0, totalY = 0;
      for(Point2D pt : vertices) {
        totalX += pt.getX();
        totalY += pt.getY();
      }
      double meanX = totalX/vertices.size(), meanY=totalY/vertices.size();
      for(Point2D pt : vertices) {
        int mult = 100;
        AnchorPoint anchor = new AnchorPoint(id++, (pt.getY()-meanY)*mult, (pt.getX()-meanX)*mult);
        network_.addAnchorPoint(anchor);
        anchorLookup.put(pt, anchor);
      }
            
      Map<Integer, Corridor> corridorLookup = new HashMap<Integer, Corridor>();
      Map<String, Corridor> corrStrMap = new HashMap<String, Corridor>();
 
      int i=0;
      for(OTPEdge edge : edges) {
        AnchorPoint from = anchorLookup.get(edge.from_);
        AnchorPoint to = anchorLookup.get(edge.to_);
        
        String edgeKey = Math.min(from.getID(), to.getID())+"_"+Math.max(from.getID(), to.getID());
        
        /*if(corrStrMap.containsKey(edgeKey)) {
          corridorLookup.put(i, corrStrMap.get(edgeKey));
          i++;
          continue;
        }*/
        

        if(from == to) {
          System.out.println("corridor from=to!");
          //i++;
          //continue;
        }
        Corridor corr = new Corridor(i+1, from, to, false);
        corr.setElbowAngle(0);
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
      for(List<Integer> edgeList : data.edgesByVariant) {
        String variantName = data.variantNames.get(i);
        i++;
        if(!variantName.startsWith("152 ")) continue;
        System.out.println("variant: "+variantName);
        Line line = new Line(id+1, variantName);
        id++;
        
        Corridor lastCorr = null;
        for(Integer edgeIndex : edgeList) {
          Corridor corr = corridorLookup.get(edgeIndex);
          System.out.println(" - adding corr "+corr+" (index "+edgeIndex+")");
          if(corr == null) {
            System.out.println("    ** null corridor!");
            continue;
          }
          if(lastCorr != null && !corr.adjacentTo(lastCorr)) {
            System.out.println("    ** non-adjacency!");
          }
          line.initCorridor(corr);
          lastCorr = corr;
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
