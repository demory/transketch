/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package org.transketch.apps.desktop.command.viewport;

import java.awt.geom.Rectangle2D;
import org.transketch.apps.desktop.TranSketch;
import org.transketch.apps.desktop.command.AbstractCommand;
import org.transketch.util.viewport.Viewport;

/**
 *
 * @author demory
 */
public class ZoomToExtentsCommand extends AbstractCommand {

  private Viewport viewport_;
  private Rectangle2D rect_;

  public ZoomToExtentsCommand(Viewport viewport, Rectangle2D rect) {
    viewport_ = viewport;
    rect_ = rect;
  }

  public boolean doThis(TranSketch ts) {
    System.out.println(rect_.toString());
    viewport_.zoomRange(rect_);
    return true;
  }
}