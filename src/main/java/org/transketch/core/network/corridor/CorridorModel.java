/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.core.network.corridor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import org.transketch.apps.desktop.TSCanvas;
import org.transketch.core.network.LineStyleLayer;
import org.transketch.util.FPUtil;
import org.transketch.util.viewport.MapCoordinates;

/**
 *
 * @author demory
 */
public abstract class CorridorModel {
  
  protected Corridor corr_;

  public enum Type { STYLIZED, GEOGRAPHIC }
  
  public CorridorModel(Corridor corr) {
    corr_ = corr;
  }
  
  protected double x1() {
    return corr_.x1();
  }

  protected double y1() {
    return corr_.y1();
  }
  
  protected double x2() {
    return corr_.x2();
  }
  
  protected double y2() {
    return corr_.y2();
  }
  
  public abstract Type getType();

  public abstract boolean isStraight();
    
  public abstract int getFromAngle();
    
  public abstract int getToAngle();

  public double getStraightTheta() {
    //logger.debug("gST "+(x2()-x1())+","+(y2()-y1()));
    double theta = FPUtil.getTheta(x2()-x1(), y2()-y1());
    if(theta >= Math.PI) theta -= Math.PI;
    return theta;
  }
  
  public abstract Line2D getFromTangent(double offsetW);

  public abstract Line2D getToTangent(double offsetW);
  
  public abstract Point2D.Double getNextInternalFrom();

  public abstract Point2D.Double getNextInternalTo();

  public abstract void updateGeometry();

  public abstract Path2D getPath(int offsetFrom, int offsetTo, Line2D prevLine, Line2D nextLine, MapCoordinates coords, boolean reverse);

  public abstract List<CorridorComponent> getOffsetComponents(int offsetFrom, int offsetTo, MapCoordinates coords, boolean fw);
  
  public void draw(TSCanvas canvas) {
    draw(canvas, 0, 0, new LineStyleLayer(2, Color.lightGray));
  }

  public void draw(TSCanvas canvas, int offsetFrom, int offsetTo, LineStyleLayer sstyle) { //int width, int offsetFrom, int offsetTo, Color color) {
    draw(canvas.getGraphics2D(), canvas.getCoordinates(), offsetFrom, offsetTo, sstyle); //2, color);
  }

  public void draw(Graphics2D g2d, MapCoordinates coords, int offsetFrom, int offsetTo, LineStyleLayer sstyle) {
    Path2D path = getPath(offsetFrom, offsetTo, null, null, coords, false);
    g2d.setColor(sstyle.getColor());
    g2d.setStroke(sstyle.getStroke());
    g2d.draw(path);
  }

  public void drawHighlight(TSCanvas canvas, Color color) {
    draw(canvas, 0, 0, new LineStyleLayer(6, color));
  }
  
  public abstract Point2D.Double nearestPoint(double x, double y);
  
  public abstract double distanceTo(double x, double y);
  
}
