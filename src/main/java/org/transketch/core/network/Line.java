/*
 * Line.java
 * 
 * Created by demory on Mar 30, 2009, 8:54:35 PM
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.apps.desktop.gui.editor.map.Drawable;
import org.transketch.apps.desktop.gui.editor.map.EditorCanvas;
import org.transketch.core.NamedItem;
import org.transketch.core.network.corridor.Corridor;
import org.transketch.core.network.corridor.CorridorComponent;
import org.transketch.util.FPUtil;
import org.transketch.util.viewport.MapCoordinates;

/**
 *
 * @author demory
 */
public class Line implements Drawable, NamedItem {
  private final static Logger logger = Logger.getLogger(Line.class);

  private int id_;
  private double layerIndex_ = 0;
  private int openMonth_, openYear_;

  private String name_;

  private LineStyle style_;
  private Map<String, Color> styleColors_;

  private List<Corridor> corridors_;
  private Map<Integer, CorridorInfo> corridorInfo_;

  private boolean enabled_, highlighted_, bundled_ = true;


  public Line(int id, String name) {//, Color color, int width, int offset) {
    this(id, name, new LineStyle(LineStyle.Preset.DEFAULT));
  }
  
  public Line(int id, String name, LineStyle style) {
    id_ = id;
    name_ = name;
    corridors_ = new ArrayList<Corridor>();
    corridorInfo_ = new HashMap<Integer, CorridorInfo>();
    style_ = style;
    styleColors_ = new HashMap<String, Color>();
    enabled_ = true;
    highlighted_ = false;
  }


  public int getID() {
    return id_;
  }
  
  public String getName() {
    return name_;
  }

  public double getLayerIndex() {
    return layerIndex_;
  }

  public void setLayerIndex(double li) {
    layerIndex_ = li;
  }

  public Rectangle2D getBoundingBox() {
    Rectangle2D bbox = new Rectangle2D.Double();
    for(Corridor corr : corridors_) {
      bbox.add(corr.fPoint().getPoint2D());
      bbox.add(corr.tPoint().getPoint2D());
    }
    return bbox;
  }

  @Override
  public String toString() {
    return name_;
  }

  public boolean isEnabled() {
    return enabled_;
  }

  public void setEnabled(boolean enabled) {
    enabled_ = enabled;
  }

  public boolean isBundled() {
    return bundled_;
  }
  
  public void setBundled(boolean bundled) {
    bundled_ = bundled;
  }

  public int getOpenMonth() {
    return openMonth_;
  }

  public void setOpenMonth(int openMonth) {
    openMonth_ = openMonth;
  }

  public int getOpenYear() {
    return openYear_;
  }

  public void setOpenYear(int openYear) {
    openYear_ = openYear;
  }

  public Date getOpenDate() {
    Calendar cal = Calendar.getInstance();
    if(openYear_ == 0) {
      cal.set(1, 1, 1);
      return cal.getTime();
    }
    cal.set(openYear_, openMonth_-1, 1);
    return cal.getTime();
  }

  public LineStyle getStyle() {
    return style_;
  }

  public void setStyle(LineStyle style) {
    style_ = style;
  }

  public Color getStyleColor(String key) {
    if(!styleColors_.containsKey(key)) return Color.GRAY;
    return styleColors_.get(key);
  }

  public void setStyleColor(String key, Color color) {
    styleColors_.put(key, color);
  }

  public AnchorPoint startPoint() {
    if(corridors_.size() == 0) return null;
    if(corridors_.size() == 1) return corridors_.get(0).fPoint();

    Corridor first = corridors_.get(0), second = corridors_.get(1);
    if(first.tPoint() == second.fPoint() || first.tPoint() == second.tPoint())
      return first.fPoint();
    if(first.fPoint() == second.fPoint() || first.fPoint() == second.tPoint())
      return first.tPoint();

    return null;
  }

  public AnchorPoint endPoint() {
    if(corridors_.size() == 0) return null;
    if(corridors_.size() == 1) return corridors_.get(0).tPoint();

    Corridor last = corridors_.get(corridors_.size()-1), previous = corridors_.get(corridors_.size()-2);
    if(last.tPoint() == previous.fPoint() || last.tPoint() == previous.tPoint())
      return last.fPoint();
    if(last.fPoint() == previous.fPoint() || last.fPoint() == previous.tPoint())
      return last.tPoint();

    return null;
  }

  public Corridor firstCorridor() {
    if(corridors_.size() == 0) return null;
    return corridors_.get(0);
  }

  public Corridor lastCorridor() {
    if(corridors_.size() == 0) return null;
    return corridors_.get(corridors_.size()-1);
  }

  public AnchorPoint fromPoint(Corridor corr) {
    AnchorPoint from = startPoint(), to;
    for(Corridor c : corridors_) {
      to = c.opposite(from);
      if(c == corr) return from;
      from = to;
    }
    return null;
  }

  public AnchorPoint toPoint(Corridor corr) {
    AnchorPoint from = startPoint(), to;
    for(Corridor c : corridors_) {
      to = c.opposite(from);
      if(c == corr) return to;
      from = to;
    }
    return null;
  }

  public Corridor adjacent(Corridor corr, AnchorPoint anchor) {

    if(corr.fPoint() != anchor && corr.tPoint() != anchor) return null;
    if(anchor == startPoint() || anchor == endPoint()) return null;


    AnchorPoint from = startPoint(), to;
    int i = 0;
    for(Corridor c : corridors_) {
      to = c.opposite(from);
      if(c == corr) {
        if(from == anchor) return corridors_.get(i-1);
        if(to == anchor) return corridors_.get(i+1);
      }
      from = to;
      i++;
    }

    return null;
  }

  public int size() {
    return corridors_.size();
  }

  /*public int compareTo(Object o) {
    return name_.compareTo(((Line) o).getName());
  }*/

  public void initCorridor(Corridor corr) {
    initCorridor(corr, 0, 0, 0, 0);
  }
  
  public void initCorridor(Corridor corr, int offsetFrom, int offsetTo, int openMonth, int openYear) {
    corridors_.add(corr);
    corridorInfo_.put(corr.getID(), new CorridorInfo(offsetFrom, offsetTo, openMonth, openYear));
    corr.registerLine(this);
  }

  public boolean contains(Corridor c) {
    return corridors_.contains(c);
  }

  public boolean contains(Line l) {
    for(Corridor c : l.corridors_)
      if(!this.contains(c)) return false;
    return true;
  }

  /**
   *
   * @param line
   * @return 1 if in order, -1 if in reverse order, 0 if not contained in order
   */
  public int containsInOrder(Line line) {
    
    // special case for containment of single-corridor line:
    if(line.getCorridors().size() == 1) {
      Corridor corr = line.firstCorridor();
      if(this.contains(corr)) {
        if(this.fromPoint(corr) == corr.fPoint()) return 1;
        else return -1;
      }
      else return 0;
    }

    String thisStr = "", fwStr = "", bwStr = "";
    for(Corridor corr : corridors_) {
      thisStr += corr.getID() + "_";
    }
    for(Corridor corr : line.corridors_) {
      fwStr += corr.getID() + "_";
      bwStr = corr.getID() + "_" + bwStr;
    }

    if(thisStr.contains(fwStr)) return 1;
    if(thisStr.contains(bwStr)) return -1;
    return 0;
  }

  public void initBaseOffset(int offset) {
    AnchorPoint a = startPoint();
    for(Corridor c : corridors_) {
      CorridorInfo ci = corridorInfo_.get(c.getID());
      if(a != c.fPoint()) {
        ci.offsetFrom_ = -offset;
        ci.offsetTo_ = -offset;
        a = c.fPoint();
      }
      else {
        ci.offsetFrom_ = offset;
        ci.offsetTo_ = offset;
        a = c.tPoint();
      }
    }
  }

  public boolean addCorridor(Corridor corr) {
    if(corridors_.contains(corr)) return false;
    if(corridors_.isEmpty()) {
      corridors_.add(corr);
      corridorInfo_.put(corr.getID(), new CorridorInfo(0, 0, 0, 0));
      corr.registerLine(this);
      return true;
    }
    if(corr.adjacentTo(startPoint())) {
      corridors_.add(0, corr);
      corridorInfo_.put(corr.getID(), new CorridorInfo(0, 0, 0, 0));
      corr.registerLine(this);
      return true;
    }
    if(corr.adjacentTo(endPoint())) {
      corridors_.add(corridors_.size(), corr);
      corridorInfo_.put(corr.getID(), new CorridorInfo(0, 0, 0, 0));
      corr.registerLine(this);
      return true;
    }
    return false;
  }

  public boolean addCorridors(Collection<Corridor> corrs) {
    for(Corridor corr : corrs)
      if(!addCorridor(corr)) return false;
    return true;
  }
  
  public boolean removeCorridor(Corridor corr) {
    return removeCorridor(corr, true);
  }

  public boolean removeCorridor(Corridor corr, boolean unregister) {
    boolean success = corridors_.remove(corr);
    if(success) {
      corridorInfo_.remove(corr.getID());
      if(unregister) corr.unregisterLine(this);
    }
    return success;
  }

  public void removeAllCorridors() {
    while(size() > 0) removeCorridor(firstCorridor());
  }

  public void splitCorridor(Corridor old, Corridor new1, Corridor new2) {
    logger.debug("splitCorridor "+name_+" at "+old.toString());
    List<Corridor> newList = new ArrayList<Corridor>();

    // special case: we are splitting the one corridor in this line
    if(corridors_.size() == 1 && corridors_.get(0) == old) {
      newList.add(new1);
      newList.add(new2);
    }

    // the standard case:
    else {
     AnchorPoint pt = startPoint();
     for(Corridor c : corridors_) {
       if(c == old) {
         if(new1.adjacentTo(pt)) {
           newList.add(new1);
           newList.add(new2);
         }
         else if (new2.adjacentTo(pt)) {
           newList.add(new2);
           newList.add(new1);
         }
       }
       else {
         newList.add(c);
       }
       
       pt = c.opposite(pt);
     }
     
    }

    corridors_ = newList;
    CorridorInfo oldCI = corridorInfo_.get(old.getID());
    CorridorInfo newCI1 = new CorridorInfo(oldCI.offsetFrom_, new1.isStraight() ? oldCI.offsetFrom_ : oldCI.offsetTo_, oldCI.openMonth_, oldCI.openYear_);
    CorridorInfo newCI2 = new CorridorInfo(new2.isStraight() ? oldCI.offsetTo_ : oldCI.offsetFrom_, oldCI.offsetTo_, oldCI.openMonth_, oldCI.openYear_);
    corridorInfo_.put(new1.getID(), newCI1);
    corridorInfo_.put(new2.getID(), newCI2);
    corridorInfo_.remove(old.getID());
    new1.registerLine(this);
    new2.registerLine(this);
    old.unregisterLine(this);

  }

  public void unsplitCorridor(Corridor old, Corridor new1, Corridor new2) {
    logger.debug("splitCorridor "+name_+" at "+old.toString());
    List<Corridor> newList = new ArrayList<Corridor>();

    String str = "usC before:";
    for(Corridor c : corridors_) str+=" " + c.getID();
    logger.debug(str);

    for(int i = 0; i < corridors_.size()-1; i++) {
      if((corridors_.get(i) == new1 && corridors_.get(i+1) == new2) ||
         (corridors_.get(i) == new2 && corridors_.get(i+1) == new1)) {
        newList.add(old);
        i++;
      }
      else {
        newList.add(corridors_.get(i));
      }
      if(i+1 == corridors_.size()-1)
        newList.add(corridors_.get(i+1));
    }

    str = "usC after:";
    for(Corridor c : corridors_) str+=" " + c.getID();
    logger.debug(str);
    
    corridors_ = newList;
    CorridorInfo newCI1 = corridorInfo_.get(new1.getID());
    CorridorInfo newCI2 = corridorInfo_.get(new2.getID());
    CorridorInfo oldCI = new CorridorInfo(newCI1.offsetFrom_, newCI2.offsetTo_, newCI1.openMonth_, newCI1.openYear_);
    corridorInfo_.put(old.getID(), oldCI);
    corridorInfo_.remove(new1.getID());
    corridorInfo_.remove(new2.getID());
    new1.unregisterLine(this);
    new2.unregisterLine(this);
    old.registerLine(this);

  }

  public void clearOffsets() {
    for(CorridorInfo ci : corridorInfo_.values())
      ci.offsetFrom_ = ci.offsetTo_ = 0;
  }

  public Collection<Corridor> getCorridors() {
    return corridors_;
  }

  public CorridorInfo getCorridorInfo(Corridor corr) {
    return corridorInfo_.get(corr.getID());
  }

  public AlignmentSnapshot getAlignmentSnapshot() {
    return new AlignmentSnapshot();
  }

  public void restoreAlignment(AlignmentSnapshot alignment) {
    corridors_ = new LinkedList<Corridor>(alignment.corridors_);
    corridorInfo_ = new HashMap<Integer, CorridorInfo>(alignment.corridorInfo_);
    /*for(Map.Entry<Integer, CorridorInfo> entry : alignment.corridorInfo_.entrySet()) {
      corridorInfo_.put(entry.getKey(), new CorridorInfo(entry.getValue()));
    }*/
  }

  public void setHighlighted(boolean highlighted) {
    highlighted_ = highlighted;
  }
  
  public void draw(TSCanvas canvas) {
    draw(canvas.getGraphics2D(), canvas.getCoordinates());
  }

  public void draw(Graphics2D g2d, MapCoordinates coords) {
    //logger.debug("draw line "+name_);
    AnchorPoint a = startPoint();
    
    /*if(highlighted_) {
      g2d.setColor(Color.YELLOW);
      if(a != null)
        g2d.drawOval(coords.xToScreen(a.getX())-5, coords.yToScreen(a.getY())-4, 8, 8);
    }*/

    Path2D path = getOffsetPath(coords);
    LineSubStyle subStyle = style_.getActiveSubStyle();
    ListIterator<LineStyleLayer> iter = subStyle.getLayers().listIterator(subStyle.getLayers().size());
    while(iter.hasPrevious()) {
      LineStyleLayer layer = iter.previous();
      g2d.setColor(layer.getColor(this));
      g2d.setStroke(layer.getStroke());
      g2d.draw(path);
    }

    /*Path2D path2 = getOffsetPath(coords, 5);
    g2d.setColor(Color.yellow);
    g2d.setStroke(new BasicStroke(2));
    g2d.draw(path2);*/
    
  }

  public void drawHighlight(TSCanvas canvas, Color color) {
    Graphics2D g2d = canvas.getGraphics2D();
    Path2D path = getOffsetPath(canvas.getCoordinates());
    g2d.setColor(color);
    g2d.setStroke(new BasicStroke(style_.getActiveSubStyle().getMaxLayerWidth()+5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
    g2d.draw(path);
  }

  public Type getDrawableType() {
    return Type.LINE;
  }

  public boolean containsPoint(EditorCanvas c, double wx, double wy) {
    Path2D path = getOffsetPath(c.getCoordinates());
    
    double mx = c.getCoordinates().xToScreen(wx);
    double my = c.getCoordinates().yToScreen(wy);
    double tol = style_.getActiveSubStyle().getMaxLayerWidth()/2;
    //logger.debug("MPt = "+mx+","+my);

    PathIterator pi = path.getPathIterator(null, 1);
    double pts[] = new double[6];
    Point2D pt1 = null, pt2 = null;
    while(!pi.isDone()) {

      pi.currentSegment(pts);
      //logger.debug("pt = "+pts[0]+","+pts[1]);

      if(pt1 == null) {

        pt1 = new Point2D.Double(pts[0], pts[1]);
      }
      else {
        pt2 = new Point2D.Double(pts[0], pts[1]);
        double dist = FPUtil.distToSegment(mx, my, pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
        //if(dist< 100) logger.debug("  dist = "+dist);
        if(dist < tol) return true;
        pt1 = pt2;
      }
      pi.next();
    }
    return false;
  }

  private Path2D getOffsetPath(MapCoordinates coords) {
    return getOffsetPath(coords, 0);
  }

  private Path2D getOffsetPath(MapCoordinates coords, int additionalOffset) {
    
    AnchorPoint fromPt = this.startPoint(), toPt;
    //CorridorComponent lastComp = null;

    List<CorridorComponent> allComps = new ArrayList<CorridorComponent>();
    for(int iCorr=0; iCorr < corridors_.size(); iCorr++) {
      Corridor corr = corridors_.get(iCorr);
      CorridorInfo ci = corridorInfo_.get(corr.getID());
      toPt = corr.opposite(fromPt);
      int offsetFrom = ci.offsetFrom_ + additionalOffset;
      int offsetTo = ci.offsetTo_ + additionalOffset;
      
      List<CorridorComponent> comps = corr.getOffsetComponents(offsetFrom, offsetTo, coords, (corr.fPoint() == fromPt));
      for(int iComp=0; iComp < comps.size(); iComp++) {
        CorridorComponent comp = comps.get(iComp);

        if(iComp == 0) comp.setIsFirst(true);
        if(iComp == comps.size()-1) comp.setIsLast(true);

        allComps.add(comp);
        //CorridorComponent comp = comps.get(iComp);
        //if(iComp == 0 && lastComp != null)
        
        //path.append(comp.getShape(), true);
        /*if(iComp == comps.size()-1) {

          lastComp = comp;
        }*/
      }
      
      fromPt = toPt;
    }

    Path2D path = new Path2D.Double();
    for(int i=0; i<allComps.size(); i++) {
      CorridorComponent comp = allComps.get(i);
      Shape shape;
      if(comp.isFirst() && i-1 >= 0)
        shape = comp.truncateFrom(allComps.get(i-1));
      else if(comp.isLast() && i+1 <= allComps.size()-1)
        shape = comp.truncateTo(allComps.get(i+1));
      else
        shape = comp.getShape();
      path.append(shape, true);
    }

    path.transform(coords.getScaleTransform());
    path.transform(coords.getTranslateTransform());

    return path;
    
    /*Path2D path = new Path2D.Double();
    AnchorPoint fromPt = this.startPoint(), toPt;
    for(int i=0; i < corridors_.size(); i++) { //Corridor c : corridors_) {
      Corridor c = corridors_.get(i);
      CorridorInfo ci = corridorInfo_.get(c.getID());
      toPt = c.opposite(fromPt);
      int offsetFrom = ci.offsetFrom_ + additionalOffset;
      int offsetTo = ci.offsetTo_ + additionalOffset;
      //logger.debug(" c="+c.getID()+" "+offsetFrom+","+offsetTo);
      Line2D prevLine = null;
      if(i-1 >= 0) {
        Corridor prevCorr = corridors_.get(i-1);
        prevLine = prevCorr.getTangent(fromPt, coords.dxToWorld(corridorInfo_.get(prevCorr.getID()).offsetTo_));
      }
      Line2D nextLine = null;
      if(i+1 < corridors_.size()) {
        Corridor nextCorr = corridors_.get(i+1);
        nextLine = nextCorr.getTangent(toPt, coords.dxToWorld(corridorInfo_.get(nextCorr.getID()).offsetFrom_));
      }
      path.append(c.getPath(fromPt, offsetFrom, offsetTo, prevLine, nextLine, coords), true);
      fromPt = toPt;
    }

    return path;*/
  }

  public String getXML(String prefix) {
    String corrIDs = "";
    for(Corridor corr : corridors_) {
      corrIDs += corr.getID()+",";
    }
    if(corrIDs.length() > 0) corrIDs = corrIDs.substring(0, corrIDs.length()-1); // chop off trailing comma


    String opendate = (openMonth_ < 10 ? "0" : "") + openMonth_ + openYear_;
    String xml = "";
    xml += prefix + "<line id=\""+id_+"\" name=\""+name_+"\" corridors=\""+corrIDs+"\" bundled=\""+bundled_+"\" layerindex=\""+layerIndex_+"\">\n";
    xml += prefix + "  <style id=\""+style_.getIDForFile()+"\">\n";
    for(String key : style_.getColorKeys()) {
      xml += prefix + "    <colorkey key=\""+key+"\" color=\""+this.getStyleColor(key).getRGB()+"\" />\n";
    }
    xml += prefix + "  </style>\n";   
    xml += prefix + "</line>\n";
    return xml;
  }


  /**
   * CorridorInfo contains corridor-specific information that is unique to this
   * Line -- e.g. display offsets.
   */

  public class CorridorInfo {

    int offsetFrom_, offsetTo_;
    int openMonth_, openYear_;

    public CorridorInfo(int offsetFrom, int offsetTo, int openMonth, int openYear) {
      offsetFrom_ = offsetFrom;
      offsetTo_ = offsetTo;
      openMonth_ = openMonth;
      openYear_ = openYear;
    }

    public CorridorInfo(CorridorInfo copy) {
      offsetFrom_ = copy.offsetFrom_;
      offsetTo_ = copy.offsetTo_;
      openMonth_ = copy.openMonth_;
      openYear_ = copy.openYear_;
    }
  }

  /**
   * AlignmentInfo is a "snapshot" of a Line's geometry and associated info --
   * i.e. the corridors_ and corridorInfo_ fields. Used by undo/redo commands
   * to save and restore line geometries.
   */

  public class AlignmentSnapshot {

    private List<Corridor> corridors_;
    private Map<Integer, CorridorInfo> corridorInfo_;

    public AlignmentSnapshot() {
      corridors_ = new LinkedList<Corridor>(Line.this.corridors_);
      corridorInfo_ = new HashMap<Integer, CorridorInfo>(Line.this.corridorInfo_);
      /*for(Map.Entry<Integer, CorridorInfo> entry : Line.this.corridorInfo_.entrySet()) {
        corridorInfo_.put(entry.getKey(), new CorridorInfo(entry.getValue()));
      }*/

    }
  }
}
