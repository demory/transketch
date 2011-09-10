/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.core.network.corridor;

import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 *
 * @author demory
 */
public abstract class CorridorComponent {

  private boolean isFirst_, isLast_;

  public enum Type {
    SEGMENT,
    ARC
  }

  public boolean isFirst() {
    return isFirst_;
  }

  public void setIsFirst(boolean isFirst) {
    isFirst_ = isFirst;
  }

  public boolean isLast() {
    return isLast_;
  }

  public void setIsLast(boolean isLast) {
    isLast_ = isLast;
  }
  
  public abstract Type getType();

  public abstract Shape getShape();

  public abstract Shape truncateTo(CorridorComponent comp);

  public abstract Shape truncateFrom(CorridorComponent comp);

  public abstract Point2D intersect(CorridorComponent comp);

}
