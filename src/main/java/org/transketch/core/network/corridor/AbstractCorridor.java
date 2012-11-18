/*
 * AbstractCorridor.java
 * 
 * Created by demory on Jan 28, 2011, 9:48:43 PM
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

package org.transketch.core.network.corridor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.apps.desktop.gui.editor.map.Drawable;
import org.transketch.core.network.LineStyleLayer;
import org.transketch.util.FPUtil;
import org.transketch.util.viewport.MapCoordinates;

/**
 *
 * @author demory
 */
public abstract class AbstractCorridor implements Drawable {
  private final static Logger logger = Logger.getLogger(AbstractCorridor.class);

  protected double thetaR_ = (3.0/4.0)*Math.PI;
  protected double radiusW_ = 2;

  protected Point2D.Double defaultElbow_;
  protected Arc2D.Double defaultArcFW_, defaultArcBW_;
  
  protected double avgOffset_;

  public abstract double x1();
  public abstract double y1();
  public abstract double x2();
  public abstract double y2();

  public Type getDrawableType() {
    return Type.CORRIDOR;
  }

  public void setElbowAngle(double thetaR) {
    thetaR_ = thetaR;
    updateGeometry();
  }

  public double getElbowAngle() {
    return thetaR_;
  }

  public Point2D.Double getElbow() {
    //if(defaultElbow_ == null) return constructElbow(x1(), y1(), x2(), y2());
    return defaultElbow_;
  }

  public Line2D getFromTangent(double offsetW) {
    double xEnd = x1(), yEnd = y1();

    double xElb = (defaultElbow_ != null) ? defaultElbow_.x : x2();
    double yElb = (defaultElbow_ != null) ? defaultElbow_.y : y2();

    Line2D.Double vec = FPUtil.createNormalizedVector(new Point2D.Double(xEnd, yEnd), new Point2D.Double(xElb, yElb), 1);
    double dx = vec.x2 - vec.x1, dy = vec.y2-vec.y1;
    AffineTransform rot = AffineTransform. getRotateInstance(-Math.PI/2);
    Point2D.Double src = new Point2D.Double(dx, dy), dest = new Point2D.Double();
    rot.transform(src, dest);
    xEnd += dest.x*offsetW; yEnd += dest.y*offsetW;
    xElb += dest.x*offsetW; yElb += dest.y*offsetW;
    
    return new Line2D.Double(xEnd, yEnd, xElb, yElb);
  }

  public Line2D getToTangent(double offsetW) {
    double xEnd = x2(), yEnd = y2();

    double xElb = (defaultElbow_ != null) ? defaultElbow_.x : x1();
    double yElb = (defaultElbow_ != null) ? defaultElbow_.y : y1();

    Line2D.Double vec = FPUtil.createNormalizedVector(new Point2D.Double(xElb, yElb), new Point2D.Double(xEnd, yEnd), 1);
    double dx = vec.x2 - vec.x1, dy = vec.y2-vec.y1;
    AffineTransform rot = AffineTransform. getRotateInstance(-Math.PI/2);
    Point2D.Double src = new Point2D.Double(dx, dy), dest = new Point2D.Double();
    rot.transform(src, dest);
    xElb += dest.x*offsetW; yElb += dest.y*offsetW;
    xEnd += dest.x*offsetW; yEnd += dest.y*offsetW;

    return new Line2D.Double(xEnd, yEnd, xElb, yElb);
  }

  @Override
  public void draw(TSCanvas canvas) {
    draw(canvas.getGraphics2D(), canvas.getCoordinates(), new LineStyleLayer(2, Color.gray));
  }

  public void draw(TSCanvas canvas, LineStyleLayer sstyle) {
    draw(canvas.getGraphics2D(), canvas.getCoordinates(), sstyle);
  }

  public void draw(Graphics2D g2d, MapCoordinates coords, LineStyleLayer sstyle) {
    Path2D path = getPath(coords, false);
    g2d.setColor(sstyle.getColor());
    g2d.setStroke(sstyle.getStroke());
    g2d.draw(path);
  }

  @Override
  public void drawHighlight(TSCanvas canvas, Color color) {
    draw(canvas, new LineStyleLayer(6, color));
  }

  public void updateGeometry() {
    if(isStraight()) {
      defaultElbow_ = null;
      defaultArcFW_ = defaultArcBW_ = null;
      return;
    }

    defaultElbow_ = constructElbow(x1(), y1(), x2(), y2());
    double l = radiusW_ / Math.tan(thetaR_/2);
    double rw = radiusW_;
    double shortest = Math.min(FPUtil.magnitude(x1(), y1(), defaultElbow_.x, defaultElbow_.y),
            FPUtil.magnitude(x2(), y2(), defaultElbow_.x, defaultElbow_.y));
    if( shortest < l) {
      l = shortest;
      rw = l * Math.tan(thetaR_/2);
    }

    defaultArcFW_ = constructArcFW(x1(), y1(), x2(), y2(), defaultElbow_, l, rw);
    defaultArcBW_ = constructArcBW(x1(), y1(), x2(), y2(), defaultElbow_, l, rw);
  }

  public Path2D getPath(MapCoordinates coords, boolean reverse) {
    if(reverse) return getPathBW(coords);
    else return getPathFW(coords);    
  }
  
  public Path2D getPathFW(MapCoordinates coords) {

    Path2D path = new Path2D.Double();

    path.moveTo(x1(), y1());
    if(isStraight()) {
      path.lineTo(x2(), y2());
    }
    else {
      path.append(defaultArcFW_, true);
      path.lineTo(x2(), y2());
    }
    
    // convert from world to screen coordinates:
    path.transform(coords.getScaleTransform());
    path.transform(coords.getTranslateTransform());

    return path;
  }

  public Path2D getPathBW(MapCoordinates coords) {

    Path2D path = new Path2D.Double();

    path.moveTo(x2(), y2());
    if(isStraight()) {
      path.lineTo(x1(), y1());
    }
    else {
      path.append(defaultArcBW_, true);
      path.lineTo(x1(), y1());
    }

    // convert from world to screen coordinates:
    path.transform(coords.getScaleTransform());
    path.transform(coords.getTranslateTransform());

    return path;
  }
  
  public List<CorridorComponent> getOffsetComponents(int offsetFrom, int offsetTo, MapCoordinates coords, boolean fw) {

    List<CorridorComponent> comps = new LinkedList<CorridorComponent>();

    Point2D fPoint = new Point2D.Double(x1(), y1()), tPoint = new Point2D.Double(x2(), y2());

    // apply offsets, if applicable
    if(offsetFrom != 0 || offsetTo != 0)
      applyOffsets(fPoint, tPoint, offsetFrom, offsetTo, coords);

    double x1 = fPoint.getX(), y1 = fPoint.getY();
    double x2 = tPoint.getX(), y2 = tPoint.getY();

    if(isStraight()) { // straight corridor, w/ or w/o offsets
      Point2D mid = new Point2D.Double((fPoint.getX()+tPoint.getX())/2, (fPoint.getY()+tPoint.getY())/2);
      if(fw) {
        comps.add(new SegmentComponent(fPoint, mid));
        comps.add(new SegmentComponent(mid, tPoint));
      }
      else {
        comps.add(new SegmentComponent(tPoint, mid));
        comps.add(new SegmentComponent(mid, fPoint));
      }
    }
    else if(offsetFrom == 0 && offsetTo == 0) { // bent, no offsets
      comps.add(new SegmentComponent(fw ? fPoint : tPoint, fw ? defaultArcFW_.getStartPoint() : defaultArcBW_.getStartPoint()));
      comps.add(new ArcComponent(fw ? defaultArcFW_ : defaultArcBW_));
      comps.add(new SegmentComponent(fw ? defaultArcFW_.getEndPoint() : defaultArcBW_.getEndPoint(), fw ? tPoint : fPoint));
    }
    else { // bent, w/ offsets
      
      // start with the default arc
      Arc2D defArc = fw ? defaultArcFW_ : defaultArcBW_;
      
      // construct offset elbow and determine orientation
      Point2D.Double e = constructElbow(x1, y1, x2, y2);      
      int ccw = Line2D.relativeCCW( x1, y1, e.x, e.y, x2, y2);
      
      // compute the radius (assuming corridor is unconstrained)
      double combinedOffset = (offsetFrom+offsetTo)/2; 
      double rw = radiusW_ - ccw*coords.dxToWorld(combinedOffset-this.avgOffset_);
      
      // adjust radius down if corridor is constrianed
      double halfTheta = thetaR_/2;
      double l = radiusW_ / Math.tan(halfTheta);
      double shortest = Math.min(FPUtil.magnitude(x1, y1, e.x, e.y), FPUtil.magnitude(x2, y2, e.x, e.y));
      if( shortest < l) rw = shortest * Math.tan(halfTheta);
      
      // compute the center of the arc 
      double h = rw / Math.sin(halfTheta);
      double hAngle = FPUtil.getTheta(x2-e.x, y2-e.y) - ccw*halfTheta;
      double cx = e.x+ h * Math.cos(hAngle);
      double cy = e.y+ h * Math.sin(hAngle);
      
      // construct the arc
      Arc2D arc = new Arc2D.Double(cx-rw, cy-rw, rw*2, rw*2, defArc.getAngleStart(), defArc.getAngleExtent(), Arc2D.OPEN);
      
      comps.add(new SegmentComponent(fw ? fPoint : tPoint, fw ? arc.getStartPoint() : arc.getStartPoint()));
      comps.add(new ArcComponent(arc));
      comps.add(new SegmentComponent(fw ? arc.getEndPoint() : arc.getEndPoint(), fw ? tPoint : fPoint));
    }

    return comps;
  }

  public void applyOffsets(Point2D fPoint, Point2D tPoint, double offsetFrom, double offsetTo, MapCoordinates coords) {
    // calculate offset distance in "world" units:
    double oFDistW = coords.dxToWorld(offsetFrom);
    double oTDistW = coords.dxToWorld(offsetTo);

    //logger.debug("offset "+offset+ " to " + distW);
    if(isStraight()) {

      // special case: to be handled later or (preferably) avoided completely?
      /*if(offsetFrom != offsetTo) {
        logger.debug("straight corridor with unequal offsets!");
        return path; // nothing drawn in this case (for now)
      }*/

      //logger.debug("applying straight offset");
      Line2D.Double vec = FPUtil.createNormalizedVector(fPoint, tPoint, 1);
      double dx = vec.x2 - vec.x1, dy = vec.y2-vec.y1;
      AffineTransform rot = AffineTransform. getRotateInstance(-Math.PI/2);
      Point2D.Double src = new Point2D.Double(dx, dy), dest = new Point2D.Double();
      rot.transform(src, dest);
      fPoint.setLocation(fPoint.getX() + dest.x*oFDistW, fPoint.getY() + dest.y*oFDistW);
      tPoint.setLocation(tPoint.getX() + dest.x*oTDistW, tPoint.getY() + dest.y*oTDistW);
    }
    else {
      //logger.debug("applying bent offset");
      Point2D.Double e = constructElbow(fPoint.getX(), fPoint.getY(), tPoint.getX(), tPoint.getY());
      Line2D.Double vec;
      double dx, dy;
      AffineTransform rot = AffineTransform. getRotateInstance(-Math.PI/2);
      Point2D.Double src, dest;

      // shift "from" point
      vec = FPUtil.createNormalizedVector(fPoint, e, 1);
      dx = vec.x2 - vec.x1; dy = vec.y2-vec.y1;
      src = new Point2D.Double(dx, dy);
      dest = new Point2D.Double();
      rot.transform(src, dest);
      fPoint.setLocation(fPoint.getX() + dest.x*oFDistW, fPoint.getY() + dest.y*oFDistW);

      // shift "to" point
      vec = FPUtil.createNormalizedVector(e, tPoint, 1);
      dx = vec.x2 - vec.x1; dy = vec.y2-vec.y1;
      src = new Point2D.Double(dx, dy);
      dest = new Point2D.Double();
      rot.transform(src, dest);
      tPoint.setLocation(tPoint.getX() + dest.x*oTDistW, tPoint.getY() + dest.y*oTDistW);
    }
  }

  public Point2D.Double constructElbow(double fx, double fy, double tx, double ty) {

    double dx = tx - fx;
    double dy = ty - fy;

    double t2 = thetaR_ - Math.PI/2;


    if(Math.abs(dx) > Math.abs(dy) || Math.abs(x2()-x1()) == Math.abs(y2()-y1())) { // more horizontal, or square, bounding box
      double xi = Math.abs(dy) * Math.tan(t2);
      return new Point2D.Double(tx - (Math.abs(dx)/dx)*xi, fy);
      //return new Point2D.Double(fx + (Math.abs(dx)/dx)*xi, ty);
    }

    if(Math.abs(dx) < Math.abs(dy)) { // more vertical bounding box
      double yi = Math.abs(dx) * Math.tan(t2);
      return new Point2D.Double(fx, ty - (Math.abs(dy)/dy)*yi);
      //return new Point2D.Double(tx, fy + (Math.abs(dy)/dy)*yi);
    }

    return null;
  }

  public Arc2D.Double constructArcFW(double fx, double fy, double tx, double ty, Point2D.Double e, double l, double rw) {
    double cx=0, cy=0;
    int ccw = Line2D.relativeCCW(fx, fy, e.x, e.y, tx, ty);
    double startAngle = 0;
    if(e.x == fx) { // starting alignment is vertical
      cx = fx + rw*(tx-fx)/Math.abs(tx-fx);
      cy = fy + (Math.abs(e.y-fy)-l)*(ty-fy)/Math.abs(ty-fy);
      startAngle = 90 + 90 * (tx-fx)/Math.abs(tx-fx);
    }
    if(e.y == fy) { // starting alignment is horizontal
      cy = fy + rw*(ty-fy)/Math.abs(ty-fy);
      cx = fx + (Math.abs(e.x-fx)-l)*(tx-fx)/Math.abs(tx-fx);
      startAngle = 180 - 90 * (ty-fy)/Math.abs(ty-fy);
    }
    return new Arc2D.Double(cx-rw, cy-rw, rw*2, rw*2, startAngle, ccw*(180-Math.toDegrees(thetaR_)), Arc2D.OPEN);
  }

  public Arc2D.Double constructArcBW(double fx, double fy, double tx, double ty, Point2D.Double e, double l, double rw) {
    double cx=0, cy=0;
    int ccw = Line2D.relativeCCW(tx, ty, e.x, e.y, fx, fy);
    double startAngle = 0;
    double ysign = (ty-fy)/Math.abs(ty-fy);
    double xsign = (tx-fx)/Math.abs(tx-fx);

    if(e.x == fx) { // starting alignment is vertical
      cx = fx + rw*(tx-fx)/Math.abs(tx-fx);
      cy = fy + (Math.abs(e.y-fy)-l)*ysign;
      startAngle = 180 + 90*ysign - (Math.toDegrees(thetaR_)-90) * xsign * ysign;
    }
    if(e.y == fy) { // starting alignment is horizontal
      cy = fy + rw*(ty-fy)/Math.abs(ty-fy);
      cx = fx + (Math.abs(e.x-fx)-l)*(tx-fx)/Math.abs(tx-fx);
      startAngle = 90 - 90*xsign + (Math.toDegrees(thetaR_)-90) * ysign * xsign;
    }
    return new Arc2D.Double(cx-rw, cy-rw, rw*2, rw*2, startAngle, ccw*(180-Math.toDegrees(thetaR_)), Arc2D.OPEN);
  }

  public boolean isStraight() {
    double tol = .001;
    if(thetaR_ != Math.PI/2 && Math.abs(Math.abs(x2()-x1()) - Math.abs(y2()-y1())) < tol) return true;
    if(Math.abs(x1()-x2()) < tol || Math.abs(y1()-y2()) < tol) return true;
    return false;
  }

  public boolean isVertical() {
    return x1() - x2() == 0;
  }

  public boolean isHorizontal() {
    return y1() - y2() == 0;
  }

  public double getStraightTheta() {
    //if(!isStraight()) return -1;
    //logger.debug("gST "+(x2()-x1())+","+(y2()-y1()));
    double theta = FPUtil.getTheta(x2()-x1(), y2()-y1());
    if(theta >= Math.PI) theta -= Math.PI;
    return theta;
  }

  public Point2D.Double nearestPoint(double x, double y) {
    Point2D.Double e = getElbow();
    if(e == null) return FPUtil.closestPointOnSegment(x, y, x1(), y1(), x2(), y2());

    Point2D.Double p1 = FPUtil.closestPointOnSegment(x, y, x1(), y1(), e.x, e.y);
    Point2D.Double p2 = FPUtil.closestPointOnSegment(x, y, e.x, e.y, x2(), y2());

    double d1 = p1.distance(x, y);
    double d2 = p2.distance(x, y);

    if(FPUtil.magnitude(x, y, p1.x, p1.y) < FPUtil.magnitude(x, y, p2.x, p2.y))
      return p1;
    else
      return p2;
  }


  public double distanceTo(double x, double y) {
    if(defaultArcFW_ == null)
      return FPUtil.distToSegment(x, y, x1(), y1(), x2(), y2());

    double da = distToCircularArc(x, y, defaultArcFW_);
    if(da >= 0) return da;

    double d1 = FPUtil.distToSegment(x, y, x1(), y1(), defaultElbow_.x, defaultElbow_.y);
    double d2 = FPUtil.distToSegment(x, y, defaultElbow_.x, defaultElbow_.y, x2(), y2());

    return Math.min(d1, d2);
  }

  public static double distToCircularArc(double x, double y, Arc2D.Double arc) {

    double minAngle = Math.min(arc.getAngleStart(), arc.getAngleStart()+arc.getAngleExtent());
    if(minAngle < 0) minAngle += 360;
    double maxAngle = minAngle + Math.abs(arc.getAngleExtent());

    double theta = 360-Math.toDegrees(FPUtil.getTheta(x-arc.getCenterX(), y-arc.getCenterY()));

    //logger.debug("angles: "+minAngle+" to "+maxAngle+", "+theta);
    
    if((theta >= minAngle && theta <= maxAngle) || (theta+360 >= minAngle && theta+360 <= maxAngle)) {
      double d = Math.abs(FPUtil.magnitude(x, y, arc.getCenterX(), arc.getCenterY()) - arc.width/2);
      //return Math.abs(FPUtil.magnitude(x, y, arc.getCenterX(), arc.getCenterY()) - arc.width/2);
      //logger.debug("d="+d);
      return d;
    }

    /*if((arc.getAngleExtent() > 0 && theta > arc.getAngleStart() && theta < arc.getAngleStart()+arc.getAngleExtent()) ||
       (arc.getAngleExtent() < 0 && theta < arc.getAngleStart() && theta > arc.getAngleStart()+arc.getAngleExtent())) {
      return Math.abs(FPUtil.magnitude(x, y, arc.getCenterX(), arc.getCenterY()) - arc.width/2);
    }*/

    return -1; //Double.MAX_VALUE;
  }

}
