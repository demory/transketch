/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.core.network.corridor;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

/**
 *
 * @author demory
 */
public class ArcComponent extends CorridorComponent {

  //public Point2D center_;
  //double radius_, startThetaR_, deltaThetaR_;
  private Arc2D arc_;

  public ArcComponent(Arc2D arc) {
    arc_ = arc;
  }

  public Type getType() {
    return Type.ARC;
  }

  public Arc2D getArc() {
    return arc_;
  }

  public Shape getShape() {
    return getArc();
  }

  public Point2D intersect(CorridorComponent comp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Shape truncateTo(CorridorComponent comp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Shape truncateFrom(CorridorComponent comp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
