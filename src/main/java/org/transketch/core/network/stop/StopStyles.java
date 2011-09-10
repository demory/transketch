/*
 * StopStyles.java
 * 
 * Created by demory on Feb 26, 2011, 8:29:48 PM
 * 
 * Copyright (C) 2011 David D. Emory
 * 
 * This file is part of Transit Sketchpad. See <http://www.transketch.org>
 * * for additional information regarding the project.
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

package org.transketch.core.network.stop;

import java.awt.Color;
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

public class StopStyles {

  private Map<Integer, StopStyle> styles_;

  private int lastID_;

  public StopStyles() {
    styles_ = new HashMap<Integer, StopStyle>();
    lastID_ = 0;
  }

  public void addStyle(StopStyle style) {
    int id = ++lastID_;
    styles_.put(id, style);
    style.setID(id);
  }

  public StopStyle getStyle(int id) {
    return styles_.get(id);
  }

  public List<StopStyle> getList() {
    List<StopStyle> list = new LinkedList<StopStyle>(styles_.values());
    //Collections.sort(list);
    return list;
  }

  public void removeStyle(StopStyle style) {
    styles_.remove(style.getID());
  }

  public void readFromXML(Node tsNode) {
    NodeList topNodes = tsNode.getChildNodes();
    for (int i = 0; i < topNodes.getLength(); i++) {
      if(topNodes.item(i).getNodeName().equals("stopstyles")) {
        NodeList stylesNodes = topNodes.item(i).getChildNodes();
        int maxID = 0;
        for (int is = 0; is < stylesNodes.getLength(); is++) {
          if(stylesNodes.item(is).getNodeName().equals("style")) {
            Node styleNode = stylesNodes.item(is);
            NamedNodeMap attributes = styleNode.getAttributes();
            int id = new Integer(attributes.getNamedItem("id").getNodeValue()).intValue();
            if(id > maxID) maxID = id;
            String name = attributes.getNamedItem("name").getNodeValue();

            //System.out.println("found style id="+id);
            StopStyle style = new StopStyle();
            style.setID(id);
            style.setName(name);

            String rendererTypeName = attributes.getNamedItem("renderertype").getNodeValue();
            style.setRendererType(StopRenderer.Type.valueOf(rendererTypeName));

            Map<String, Object> keyValueMap_ = new HashMap<String, Object>();

            NodeList propsNodes = stylesNodes.item(is).getChildNodes();
            for (int ip = 0; ip < propsNodes.getLength(); ip++) {
              if(propsNodes.item(ip).getNodeName().equals("intprop")) {
                NamedNodeMap propAttrs = propsNodes.item(ip).getAttributes();
                String key = propAttrs.getNamedItem("key").getNodeValue();
                Integer value = new Integer(propAttrs.getNamedItem("value").getNodeValue());
                keyValueMap_.put(key, value);
              }
              if(propsNodes.item(ip).getNodeName().equals("colorprop")) {
                NamedNodeMap propAttrs = propsNodes.item(ip).getAttributes();
                String key = propAttrs.getNamedItem("key").getNodeValue();
                Color value = new Color(Integer.parseInt(propAttrs.getNamedItem("value").getNodeValue()));
                keyValueMap_.put(key, value);
              }
            }

            String templateTypeName = attributes.getNamedItem("templatetype").getNodeValue();
            switch(StopRendererTemplate.Type.valueOf(templateTypeName)) {
              case BLANK:
                style.setTemplate(new BlankRendererTemplate());
                break;
              case FILLEDBORDEREDSHAPE:
                FilledBorderedShapeRendererTemplate template = new FilledBorderedShapeRendererTemplate(keyValueMap_);
                style.setTemplate(template);
                break;
            }

            styles_.put(id, style);
          }
        }
        lastID_ = maxID;
      }
    }
  }

  public String getXML(String indent) {
    String xml = "";
    xml += indent+"<stopstyles>\n";
    for(StopStyle style : styles_.values()) {
      xml += style.getXML(indent+"  ");
    }
    xml += indent+"</stopstyles>\n";

    return xml;
  }
}
