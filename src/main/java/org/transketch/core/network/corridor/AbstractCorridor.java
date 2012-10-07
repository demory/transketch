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
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.apps.desktop.gui.editor.map.Drawable;
import org.transketch.core.network.Bundler;
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


  /*public Line2D getToTangent() {
    if(defaultElbow_ == null) return new Line2D.Double(x2(), y2(), x1(), y1());
    return new Line2D.Double(x2(), y2(), defaultElbow_.x, defaultElbow_.y);
  }*/

  public void draw(TSCanvas canvas) {
    draw(canvas, 0, 0, new LineStyleLayer(2, Color.gray));
    //System.out.println("draw corr "+((Corridor) this).getID());
  }

  public void draw(TSCanvas canvas, int offsetFrom, int offsetTo, LineStyleLayer sstyle) { //int width, int offsetFrom, int offsetTo, Color color) {
    draw(canvas.getGraphics2D(), canvas.getCoordinates(), offsetFrom, offsetTo, sstyle); //2, color);
  }

  public void draw(Graphics2D g2d, MapCoordinates coords, int offsetFrom, int offsetTo, LineStyleLayer sstyle) {
    Path2D path = getPath(offsetFrom, offsetTo, null, null, coords, false);
    g2d.setColor(sstyle.getColor());
    g2d.setStroke(sstyle.getStroke());
    g2d.draw(path);
    //if(((Corridor) this).getID()==13) System.out.println("draw corr 13 - "+path.getCurrentPoint());
  }

  public void drawHighlight(TSCanvas canvas, Color color) {
    draw(canvas, 0, 0, new LineStyleLayer(6, color));
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

  public Path2D getPath(int offsetFrom, int offsetTo, Line2D prevLine, Line2D nextLine, MapCoordinates coords, boolean reverse) {

    if(reverse) return getPathBW(offsetFrom, offsetTo, prevLine, nextLine, coords);
    else return getPathFW(offsetFrom, offsetTo, prevLine, nextLine, coords);
    
    /*Path2D path = new Path2D.Double();
    //boolean reverse = (fromPt != this.fPoint_);

    Point2D fPoint = new Point2D.Double(x1(), y1());
    Point2D tPoint = new Point2D.Double(x2(), y2());

    // apply offsets, if applicable
    if(offsetFrom != 0 || offsetTo != 0) {
      applyOffsets(fPoint, tPoint, offsetFrom, offsetTo, coords);
    } // end of apply offsets block

    double x1 = fPoint.getX(), y1 = fPoint.getY();
    double x2 = tPoint.getX(), y2 = tPoint.getY();

    if(isStraight()) {

      double tol = .0001;

      Line2D thisLine = new Line2D.Double(x1, y1, x2, y2);
      double thisTheta = FPUtil.getTheta(thisLine);
      if(thisTheta >= Math.PI) thisTheta -= Math.PI;
      //logger.debug("thisLine "+thisLine.getP1() +" to "+thisLine.getP2());

      if(prevLine != null) {
        //logger.debug(" prevLine "+prevLine.getP1() +" to "+prevLine.getP2());
        double prevTheta = FPUtil.getTheta(prevLine);
        if(prevTheta >= Math.PI) prevTheta -= Math.PI;
        if(Math.abs(prevTheta-thisTheta) >= tol) {
          Point2D isect = FPUtil.lineLineIntersection(prevLine, thisLine);
          if(isect != null) {
            //logger.debug(" prevLine isect "+isect);
            if(reverse) {
              x2 = isect.getX();
              y2 = isect.getY();
            }
            else {
              x1 = isect.getX();
              y1 = isect.getY();
            }
          }
        }
      }

      if(nextLine != null) {
        //logger.debug(" nextLine "+nextLine.getP1() +" to "+nextLine.getP2());
        
        double nextTheta = FPUtil.getTheta(nextLine);
        if(nextTheta >= Math.PI) nextTheta -= Math.PI;
        if(Math.abs(nextTheta-thisTheta) >= tol) {

          Point2D isect = FPUtil.lineLineIntersection(nextLine, thisLine);

          if(isect != null) {
            //logger.debug("  nextLine isect "+isect);
            if(reverse) {
              x1 = isect.getX();
              y1 = isect.getY();
            }
            else {
              x2 = isect.getX();
              y2 = isect.getY();
            }
          }
        }
      }

      if(reverse) {
        //logger.debug("gP str rev");
        path.moveTo(x2, y2);
        path.lineTo(x1, y1);
        //logger.debug("lineto " +x1+","+y1);
      }

      else {
        //logger.debug("gP str fwd");
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
      }


      path.transform(coords.getScaleTransform());
      path.transform(coords.getTranslateTransform());
      return path;
    } // end straight case

    if(reverse) path.moveTo(x2, y2);
    else path.moveTo(x1, y1);

    if(offsetFrom == 0 && offsetTo == 0) {
      if(!reverse) {
        //path.lineTo(defaultArcFW_.getStartPoint().getX(), defaultArcFW_.getStartPoint().getY());
        path.append(defaultArcFW_, true);
        path.lineTo(x2, y2);
      }
      else {
        //path.lineTo(defaultArcBW_.getStartPoint().getX(), defaultArcBW_.getStartPoint().getY());
        path.append(defaultArcBW_, true);
        path.lineTo(x1, y1);
      }
    }
    else { // offsets do apply

      Point2D.Double e = constructElbow(x1, y1, x2, y2);
      double l = radiusW_ / Math.tan(thetaR_/2);
      double rw = radiusW_;

      double shortest = Math.min(FPUtil.magnitude(x1, y1, e.x, e.y), FPUtil.magnitude(x2, y2, e.x, e.y));
      if( shortest < l) {
        l = shortest;
        rw = l * Math.tan(thetaR_/2);
      }

      //Line2D.Double v1 = FPUtil.createNormalizedVector(e, new Point2D.Double(x1, y1), l);
      //Line2D.Double v2 = FPUtil.createNormalizedVector(e, new Point2D.Double(x2, y2), l);

      if(!reverse) {
        //path.lineTo(v1.x2, v1.y2);

        Arc2D arc = constructArcFW(x1, y1, x2, y2, e, l, rw);
        //path.lineTo(arc.getStartPoint().getX(), arc.getStartPoint().getY());
        path.append(arc, true);
        path.lineTo(x2, y2);
      }
      else {
        //path.lineTo(v2.x2, v2.y2);

        Arc2D arc = constructArcBW(x1, y1, x2, y2, e, l, rw);
        path.append(arc, true);
        path.lineTo(x1, y1);
      }

    }

    // convert from world to screen coordinates:
    path.transform(coords.getScaleTransform());
    path.transform(coords.getTranslateTransform());

    return path;*/
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
      Point2D.Double e = constructElbow(x1, y1, x2, y2);
      double l = radiusW_ / Math.tan(thetaR_/2);
      double rw = radiusW_;

      double shortest = Math.min(FPUtil.magnitude(x1, y1, e.x, e.y), FPUtil.magnitude(x2, y2, e.x, e.y));
      if( shortest < l) {
        l = shortest;
        rw = l * Math.tan(thetaR_/2);
      }

      Arc2D arc = fw ? constructArcFW(x1, y1, x2, y2, e, l, rw) : constructArcBW(x1, y1, x2, y2, e, l, rw);

      comps.add(new SegmentComponent(fw ? fPoint : tPoint, fw ? arc.getStartPoint() : arc.getStartPoint()));
      comps.add(new ArcComponent(arc));
      comps.add(new SegmentComponent(fw ? arc.getEndPoint() : arc.getEndPoint(), fw ? tPoint : fPoint));
    }

    return comps;
  }

  public Path2D getPathFW(int offsetFrom, int offsetTo, Line2D prevLine, Line2D nextLine, MapCoordinates coords) {

    double tol = .0001;
    Path2D path = new Path2D.Double();

    Point2D fPoint = new Point2D.Double(x1(), y1()), tPoint = new Point2D.Double(x2(), y2());

    // apply offsets, if applicable
    if(offsetFrom != 0 || offsetTo != 0)
      applyOffsets(fPoint, tPoint, offsetFrom, offsetTo, coords);

    double x1 = fPoint.getX(), y1 = fPoint.getY();
    double x2 = tPoint.getX(), y2 = tPoint.getY();

    if(isStraight()) {

      Line2D thisLine = new Line2D.Double(x1, y1, x2, y2);
      double thisTheta = FPUtil.getTheta(thisLine);
      if(thisTheta >= Math.PI) thisTheta -= Math.PI;

      if(prevLine != null) {
        double prevTheta = FPUtil.getTheta(prevLine);
        if(prevTheta >= Math.PI) prevTheta -= Math.PI;
        if(Math.abs(prevTheta-thisTheta) >= tol) {
          Point2D isect = FPUtil.lineLineIntersection(prevLine, thisLine);
          if(isect != null) {
            x1 = isect.getX();
            y1 = isect.getY();
          }
        }
      }

      if(nextLine != null) {
        double nextTheta = FPUtil.getTheta(nextLine);
        if(nextTheta >= Math.PI) nextTheta -= Math.PI;
        if(Math.abs(nextTheta-thisTheta) >= tol) {
          Point2D isect = FPUtil.lineLineIntersection(nextLine, thisLine);
          if(isect != null) {
            x2 = isect.getX();
            y2 = isect.getY();
          }
        }
      }

      path.moveTo(x1, y1);
      path.lineTo(x2, y2);

      path.transform(coords.getScaleTransform());
      path.transform(coords.getTranslateTransform());
      return path;
    } // end straight case

    else path.moveTo(x1, y1);

    if(offsetFrom == 0 && offsetTo == 0) {
      path.append(defaultArcFW_, true);
      path.lineTo(x2, y2);
    }
    else { // offsets do apply

      Point2D.Double e = constructElbow(x1, y1, x2, y2);
      double l = radiusW_ / Math.tan(thetaR_/2);
      double rw = radiusW_;

      double shortest = Math.min(FPUtil.magnitude(x1, y1, e.x, e.y), FPUtil.magnitude(x2, y2, e.x, e.y));
      if( shortest < l) {
        l = shortest;
        rw = l * Math.tan(thetaR_/2);
      }

      Arc2D arc = constructArcFW(x1, y1, x2, y2, e, l, rw);
      path.append(arc, true);
      path.lineTo(x2, y2);
    }

    // convert from world to screen coordinates:
    path.transform(coords.getScaleTransform());
    path.transform(coords.getTranslateTransform());

    return path;
  }

  public Path2D getPathBW(int offsetFrom, int offsetTo, Line2D prevLine, Line2D nextLine, MapCoordinates coords) {

    double tol = .0001;
    Path2D path = new Path2D.Double();

    Point2D fPoint = new Point2D.Double(x1(), y1()), tPoint = new Point2D.Double(x2(), y2());

    // apply offsets, if applicable
    if(offsetFrom != 0 || offsetTo != 0)
      applyOffsets(fPoint, tPoint, offsetFrom, offsetTo, coords);

    double x1 = fPoint.getX(), y1 = fPoint.getY();
    double x2 = tPoint.getX(), y2 = tPoint.getY();

    if(isStraight()) {

      Line2D thisLine = new Line2D.Double(x1, y1, x2, y2);
      double thisTheta = FPUtil.getTheta(thisLine);
      if(thisTheta >= Math.PI) thisTheta -= Math.PI;

      if(prevLine != null) {
        double prevTheta = FPUtil.getTheta(prevLine);
        if(prevTheta >= Math.PI) prevTheta -= Math.PI;
        if(Math.abs(prevTheta-thisTheta) >= tol) {
          Point2D isect = FPUtil.lineLineIntersection(prevLine, thisLine);
          if(isect != null) {
            x2 = isect.getX();
            y2 = isect.getY();
          }
        }
      }

      if(nextLine != null) {
        double nextTheta = FPUtil.getTheta(nextLine);
        if(nextTheta >= Math.PI) nextTheta -= Math.PI;
        if(Math.abs(nextTheta-thisTheta) >= tol) {
          Point2D isect = FPUtil.lineLineIntersection(nextLine, thisLine);
          if(isect != null) {
            x1 = isect.getX();
            y1 = isect.getY();
          }
        }
      }

      path.moveTo(x2, y2);
      path.lineTo(x1, y1);
      path.transform(coords.getScaleTransform());
      path.transform(coords.getTranslateTransform());
      return path;
    } // end straight case

    path.moveTo(x2, y2);

    if(offsetFrom == 0 && offsetTo == 0) {
      path.append(defaultArcBW_, true);
      path.lineTo(x1, y1);
    }
    else { // offsets do apply

      Point2D.Double e = constructElbow(x1, y1, x2, y2);
      double l = radiusW_ / Math.tan(thetaR_/2);
      double rw = radiusW_;

      double shortest = Math.min(FPUtil.magnitude(x1, y1, e.x, e.y), FPUtil.magnitude(x2, y2, e.x, e.y));
      if( shortest < l) {
        l = shortest;
        rw = l * Math.tan(thetaR_/2);
      }

      Arc2D arc = constructArcBW(x1, y1, x2, y2, e, l, rw);
      path.append(arc, true);
      path.lineTo(x1, y1);

    }

    // convert from world to screen coordinates:
    path.transform(coords.getScaleTransform());
    path.transform(coords.getTranslateTransform());

    return path;
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
