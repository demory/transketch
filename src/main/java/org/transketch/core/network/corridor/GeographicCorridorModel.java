/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.core.network.corridor;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.transketch.util.FPUtil;
import org.transketch.util.viewport.MapCoordinates;

/**
 *
 * @author demory
 */
public class GeographicCorridorModel extends CorridorModel {

  private List<Point2D> internals_;
  
  private List<Line2D.Double> segments_;
          
  public GeographicCorridorModel(Corridor corr) {
    super(corr);
    internals_ = new LinkedList<Point2D>();
    updateGeometry();
  }
  
  public GeographicCorridorModel(Corridor corr, List<Point2D> internals) {
    super(corr);
    internals_ = internals;
    updateGeometry();
  }

  public void setInternals(List<Point2D> internals) {
    internals_ = internals;
    updateGeometry();
  }
  
  @Override
  public Type getType() {
    return Type.GEOGRAPHIC;
  }

  @Override
  public boolean isStraight() {
    return (internals_.isEmpty());
  }

  @Override
  public int getFromAngle() {
    Point2D.Double pt = getNextInternalFrom();
    return (int) (Math.toDegrees(FPUtil.getTheta(pt.x-x1(), pt.y-y1())) + 0.5);
  }

  @Override
  public int getToAngle() {
    Point2D.Double pt = getNextInternalTo();
    return (int) (Math.toDegrees(FPUtil.getTheta(pt.x-x2(), pt.y-y2())) + 0.5);
  }

  @Override
  public Line2D getFromTangent(double offsetW) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Line2D getToTangent(double offsetW) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Point2D.Double getNextInternalFrom() {
    return (Point2D.Double) segments_.get(0).getP2();
  }

  @Override
  public Point2D.Double getNextInternalTo() {
    return (Point2D.Double) segments_.get(segments_.size()-1).getP2();
  }

  @Override
  public void updateGeometry() {
    segments_ = getSegments();
  }

  @Override
  public Path2D getPath(int offsetFrom, int offsetTo, Line2D prevLine, Line2D nextLine, MapCoordinates coords, boolean reverse) {
    
    if(reverse) System.out.println("BARF!!");
    Path2D path = new Path2D.Double();
    path.moveTo(coords.xToScreen(corr_.x1()), coords.yToScreen(corr_.y1()));
    for(Point2D pt : internals_) {
      path.lineTo(coords.xToScreen(pt.getX()), coords.yToScreen(pt.getY()));
    }
    path.lineTo(coords.xToScreen(corr_.x2()), coords.yToScreen(corr_.y2()));
      
    /*for(Line2D segment : segments_) {
      path.append(segment, true);
    }*/
    return path;
  }

  @Override
  public List<CorridorComponent> getOffsetComponents(int offsetFrom, int offsetTo, MapCoordinates coords, boolean fw) {
    //System.out.println("gOC id="+corr_.getID()+ " fw="+fw);
    List<CorridorComponent> comps = new LinkedList<CorridorComponent>();
    if(fw) {
      for(Line2D seg : segments_) {
        comps.add(new SegmentComponent(seg.getP1(), seg.getP2()));
        //System.out.println("   - seg="+seg.getP1()+" to "+seg.getP2());
      }
    }
    else {
      ListIterator<Line2D.Double> iter = segments_.listIterator(segments_.size());
      while(iter.hasPrevious()) {
        Line2D seg = iter.previous(); 
        comps.add(new SegmentComponent(seg.getP2(), seg.getP1()));
      }
    }
    return comps;
  }

  @Override
  public Point2D.Double nearestPoint(double x, double y) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public double distanceTo(double x, double y) {
    double minDist = Double.MAX_VALUE;
    for(Line2D segment : segments_) {
      double dist = segment.ptSegDist(x, y);
      minDist = Math.min(minDist, dist);
    }
    return minDist;
  }
  
  public List<Line2D.Double> getSegments() {
    if(internals_.isEmpty()) return Collections.singletonList(new Line2D.Double(corr_.x1(), corr_.y1(), corr_.x2(), corr_.y2()));
    
    List<Line2D.Double> segments = new LinkedList<Line2D.Double>();
    
    segments.add(new Line2D.Double(new Point2D.Double(corr_.x1(), corr_.y1()), internals_.get(0)));
    
    for(int i=0; i<internals_.size()-1; i++)
      segments.add(new Line2D.Double(internals_.get(i), internals_.get(i+1)));
      
    segments.add(new Line2D.Double(internals_.get(internals_.size()-1), new Point2D.Double(corr_.x2(), corr_.y2())));
    
    return segments;    
  }

}
