/*
 * Stop.java
 * 
 * Created by demory on Apr 11, 2010, 1:20:50 PM
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

package org.transketch.core.network.stop;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.apps.desktop.gui.editor.map.Drawable;
import org.transketch.core.network.TSNetwork;
import org.transketch.core.network.corridor.Corridor;
import org.transketch.core.network.LabelStyle;
import org.transketch.core.network.Line;
import org.transketch.util.FPUtil;
import org.transketch.util.viewport.MapCoordinates;

/**
 *
 * @author demory
 */
public abstract class Stop implements Drawable {
  private final static Logger logger = Logger.getLogger(Stop.class);

  // core fields (i.e. stored in the .fpc file)

  protected int id_;

  protected String name_;

  protected StopStyle stopStyle_;

  protected LabelStyle labelStyle_;
  protected boolean showLabel_ = true;

  protected double labelAngleR_ = 0;


  // other fields

  protected StopRenderer renderer_; // created dynamically based on settings in StopStyle

  protected Map<Line, LineStopInfo> lineInfo_;

  public enum LineStopInfo {NEVER_STOPS, LIMITED_STOPS, ALWAYS_STOPS};

  public enum Type {
    ANCHORBASED("org.transketch.core.network.stop.AnchorBasedStop");

    public String className_;

    Type(String className) {
      className_ = className;
    }
  }

  public Stop(int id, String name) {
    id_ = id;
    name_ = name;
    stopStyle_ = new StopStyle(StopStyle.Preset.DEFAULT);
    labelStyle_ = new LabelStyle();

    updateRenderer();
  }

  public void applyStopProperties(Map<String, String> properties) {
    if(properties.containsKey("labelangle")) {
      String angleStr = properties.get("labelangle");
      if(FPUtil.isDouble(angleStr)) labelAngleR_ = new Double(angleStr);
    }
  }

  public abstract void applyTypeProperties(Map<String, String> properties,  TSNetwork network);

  public Drawable.Type getDrawableType() {
    return Drawable.Type.STOP;
  }

  public int getID() {
    return id_;
  }

  public String getName() {
    return name_;
  }

  public void setName(String name) {
    name_ = name;
  }

  public abstract Type getType();

  public StopStyle getStyle() {
    return stopStyle_;
  }

  public void setStyle(StopStyle style) {
    stopStyle_ = style;
    updateRenderer();
  }

  public StopRenderer getRenderer() {
    return renderer_;
  }
  
  public LabelStyle getLabelStyle() {
    return labelStyle_;
  }

  public double getLabelAngle() {
    return labelAngleR_;
  }

  public void setLabelAngle(double radians) {
    labelAngleR_ = radians;
  }

  public boolean getShowLabel() {
    return showLabel_;
  }

  public void setShowLabel(boolean showLabel) {
    showLabel_ = showLabel;
  }
  
  public abstract double getWorldX();

  public abstract double getWorldY();

  public abstract int getScreenX(MapCoordinates coords);

  public abstract int getScreenY(MapCoordinates coords);
  
  public abstract Point2D getScreenOffset();
  
  public abstract Collection<Corridor> getCorridors();

  public String getXML(String indent) {
    String xml = "";
    xml += indent + "<stop id=\""+id_+"\" name=\""+name_+"\">\n";

    if(labelAngleR_ != 0) {
      xml += indent + "  <property name=\"labelangle\" value=\""+labelAngleR_+"\" />\n";
    }
    //
    xml += indent + "  <type name=\""+getType().name()+"\">\n";
    xml += getTypePropertiesXML(indent+"     ");
    xml += indent + "  </type>\n";

    // stop style
    if(stopStyle_.getID() >= 0) xml += indent + "  <style id=\""+stopStyle_.getID()+"\"></style>\n";

    xml += indent + "</stop>\n";
    return xml;
  }

  public abstract String getTypePropertiesXML(String indent);

  public void updateRenderer() {

    try {
      //logger.debug("type="+stopStyle_.getRendererType());
      //logger.debug("style template="+stopStyle_.getTemplate());
      Class cl = stopStyle_.getRendererType().classObj_;
      Constructor co = cl.getConstructor(new Class[] {Stop.class, stopStyle_.getRendererType().templateClassObj_ } );
      renderer_ = (StopRenderer) co.newInstance(new Object[] { this, stopStyle_.getTemplate() } );
    } catch (Exception ex) {
      logger.error("error updating stop renderer", ex);
    }
  }

  @Override
  public void draw(TSCanvas c) {
    renderer_.initialize();
    renderer_.drawStop(c);
    if(showLabel_) renderer_.drawLabel(c);
  }

  @Override
  public void drawHighlight(TSCanvas canvas, Color color) {
    renderer_.initialize();
    renderer_.drawHighlight(canvas, color);
  }

}
