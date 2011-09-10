/*
 * LineStyles.java
 * 
 * Created by demory on Jan 29, 2010, 8:44:04 PM
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.transketch.util.FPUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author demory
 */

public class LineStyles {

  private Map<Integer, LineStyle> styles_;

  private int lastID_;

  public LineStyles() {
    styles_ = new HashMap<Integer, LineStyle>();
    lastID_ = 0;
  }

  public void addStyle(LineStyle style) {
    int id = ++lastID_;
    styles_.put(id, style);
    style.setID(id);
  }

  public LineStyle getStyle(int id) {
    return styles_.get(id);
  }

  public List<LineStyle> getList() {
    List<LineStyle> list = new LinkedList<LineStyle>(styles_.values());
    Collections.sort(list);
    return list;
  }

  public void removeStyle(LineStyle style) {
    styles_.remove(style.getID());
  }

  public void readFromXML(Node tsNode) {
    NodeList topNodes = tsNode.getChildNodes();
    for (int i = 0; i < topNodes.getLength(); i++) {
      if(topNodes.item(i).getNodeName().equals("linestyles")) {
        NodeList stylesNodes = topNodes.item(i).getChildNodes();
        int maxID = 0;
        for (int is = 0; is < stylesNodes.getLength(); is++) {
          if(stylesNodes.item(is).getNodeName().equals("style")) {
            Node styleNode = stylesNodes.item(is);
            NamedNodeMap attributes = styleNode.getAttributes();
            int id = new Integer(attributes.getNamedItem("id").getNodeValue()).intValue();
            if(id > maxID) maxID = id;
            String name = attributes.getNamedItem("name").getNodeValue();

            System.out.println("found style id="+id);
            LineStyle style = new LineStyle(id, name);

            NodeList styleChildNodes = stylesNodes.item(is).getChildNodes();
            for(int isc = 0; isc < styleChildNodes.getLength(); isc++) {

              // look for the <substyles> tag
              if(styleChildNodes.item(isc).getNodeName().equals("substyles")) {
                Node substylesNode = styleChildNodes.item(isc);

                // process the "bounds" field
                attributes = substylesNode.getAttributes();
                String bpCSV = attributes.getNamedItem("breakpoints").getNodeValue();
                String bpStrArr[] = bpCSV.split(",");
                List<Double> bpList = new ArrayList<Double>();
                for(String bpStr : bpStrArr) {
                  if(FPUtil.isDouble(bpStr)) bpList.add(Double.parseDouble(bpStr));
                }
                style.setBreakpoints(bpList);

                // parse the actual SubStyles
                NodeList subStyleNodes = substylesNode.getChildNodes();
                for(int iss = 0; iss < subStyleNodes.getLength(); iss++) {
                  if(subStyleNodes.item(iss).getNodeName().equals("substyle")) {
                    LineSubStyle sub = new LineSubStyle();

                    attributes = subStyleNodes.item(iss).getAttributes();
                    if(attributes.getNamedItem("envelope") != null) {
                      String envStr = attributes.getNamedItem("envelope").getNodeValue();
                      if(FPUtil.isInteger(envStr)) sub.setEnvelope(Integer.parseInt(envStr));
                    }
                    
                    // parse this SubStyle's layer(s)
                    NodeList layerNodes = subStyleNodes.item(iss).getChildNodes();
                    for (int isl = 0; isl < layerNodes.getLength(); isl++) {
                      if(layerNodes.item(isl).getNodeName().equals("layer")) {
                        //Node subNode = subsNodes.item(iss);
                        attributes = layerNodes.item(isl).getAttributes();
                        int width = new Integer(attributes.getNamedItem("width").getNodeValue()).intValue();
                        float dash[] = null;
                        try {
                          String dashStr = attributes.getNamedItem("dash").getNodeValue();
                          dash = LineStyleLayer.parseDash(dashStr);
                        } catch(Exception ex) { }

                        String colorStr = attributes.getNamedItem("color").getNodeValue();
                        if(colorStr.startsWith("$")) {
                          String colorKey = colorStr.substring(1);
                          sub.addLayer(new LineStyleLayer(width, colorKey, dash));
                        }
                        else if(FPUtil.isInteger(colorStr)) {
                          Color color = new Color(new Integer(colorStr).intValue());
                          sub.addLayer(new LineStyleLayer(width, color, dash));
                        }
                      }
                    }
                    style.addSubStyle(sub);
                  }
                }
              }
            }
            styles_.put(id, style);
          }
        }
        lastID_ = maxID;
      }
    }
  }

  public boolean updateAll(double reso) {
    boolean anyChanged = false; // keep track of whether any active SubStyles changed
    for(LineStyle style : getList())
      anyChanged = anyChanged || style.updateActiveSubStyle(reso);
    return anyChanged;
  }
  
  public String getXML(String indent) {
    String xml = "";
    xml += indent+"<linestyles>\n";
    for(LineStyle style : styles_.values()) {
      xml += style.getXML(indent+"  ");
    }
    xml += indent+"</linestyles>\n";

    return xml;
  }

}
