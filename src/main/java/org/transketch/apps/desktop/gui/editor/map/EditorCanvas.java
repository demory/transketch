/*
 * Canvas.java
 * 
 * Created by demory on Mar 28, 2009, 12:34:51 PM
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

package org.transketch.apps.desktop.gui.editor.map;

import org.transketch.util.viewport.Viewport;
import org.transketch.apps.desktop.gui.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.transketch.util.viewport.MapCoordinates;
import org.transketch.apps.desktop.command.TSInvoker;
import org.transketch.apps.desktop.Editor;
import org.transketch.apps.desktop.command.network.CreateAnchorPointCommand;
import org.transketch.apps.desktop.command.network.CreateCorridorCommand;
import org.transketch.apps.desktop.command.network.DeleteAnchorPointCommand;
import org.transketch.apps.desktop.command.network.DeleteCorridorCommand;
import org.transketch.apps.desktop.command.network.AddCorridorsToLineCommand;
import org.transketch.apps.desktop.command.network.MergeAnchorPointCommand;
import org.transketch.apps.desktop.command.network.MoveAnchorPointCommand;
import org.transketch.apps.desktop.command.network.RemoveCorridorsFromLineCommand;
import org.transketch.apps.desktop.gui.editor.EditorToolbar;
import org.transketch.core.network.AnchorPoint;
import org.transketch.core.network.Bundler;
import org.transketch.core.network.corridor.Corridor;
import org.transketch.core.network.Line;
import org.transketch.core.network.PreviewAnchorPoint;
import org.transketch.core.network.corridor.PreviewCorridor;
import org.transketch.core.network.line.LineLayerIndexComparator;
import org.transketch.core.network.stop.Stop;

/**
 *
 * @author demory
 */
public class EditorCanvas extends Viewport {
  private final static Logger logger = Logger.getLogger(EditorCanvas.class);

  private Editor ed_;
  private TSInvoker invoker_;

  private DragMode dragMode_;
  private AnchorPoint draggingPoint_;
  private Point2D.Double oldDragPtLoc_; // the original location of a dragged point (for undo purposes)

  private AnchorPoint fromAnchor_;
  private PreviewAnchorPoint previewAnchor_ = null;
  private PreviewCorridor previewCorr_ = null;
  private List<Corridor> lineCorridors_ = null, lineAdditionPath_;
  
  private Set<Drawable> highlightedItems_;

  private Drawable hoverItem_ = null;
  private Set<Drawable.Type> hoverableTypes_ = new HashSet<Drawable.Type>();

  private AnchorPointMenu anchorPointMenu_;
  private CorridorMenu corridorMenu_;
  private StopMenu stopMenu_;
  private LineMenu lineMenu_;

  private boolean initialized_ = false;
  
  int clickTol_ = 5; // click tolerance in pixels

  enum DragMode { NONE, PANNING, DRAG_POINT }

  public EditorCanvas(TSInvoker invoker, Editor ed,  MapCoordinates coords, TranSketchGUI gui) {
    super(ed.getDocument(), gui, coords);
    invoker_ = invoker;
    ed_ = ed;
   
    dragMode_ = DragMode.NONE;

    highlightedItems_ = new HashSet<Drawable>();

    anchorPointMenu_ = new AnchorPointMenu(invoker, ed);
    corridorMenu_ = new CorridorMenu(invoker, ed);
    stopMenu_ = new StopMenu(invoker, ed);
    lineMenu_ = new LineMenu(invoker, ed);

  }

  @Override
  protected void mouseDown() {
    if(mButton_ == MouseEvent.BUTTON1) {
      AnchorPoint pt = doc_.getNetwork().getPointAtXY(coords_.xToWorld(mx_), coords_.yToWorld(my_), coords_.dxToWorld(clickTol_));
      if(pt != null) {
        dragMode_ = DragMode.DRAG_POINT;
        draggingPoint_ = pt;
        oldDragPtLoc_ = new Point2D.Double(pt.getX(), pt.getY());
      }
    }
    ed_.getPane().getToolbar().clearMenu();
  }

  @Override
  protected void mouseUp() {
    if(draggingPoint_ != null && (draggingPoint_.getX()-oldDragPtLoc_.x != 0 || draggingPoint_.getY()-oldDragPtLoc_.y != 0)) {
      Point2D.Double toPoint = new Point2D.Double(draggingPoint_.getX(), draggingPoint_.getY());
      invoker_.doCommand(new MoveAnchorPointCommand(ed_, draggingPoint_, oldDragPtLoc_, toPoint));
    }
    dragMode_ = DragMode.NONE;
    draggingPoint_ = null;
  }

  @Override
  protected void mouseMoved(int x, int y, boolean shift, boolean control) {
    //double wx = coords_.xToWorld(e.getX()), wy = coords_.yToWorld(e.getY());
    double wx = coords_.xToWorld(x), wy = coords_.yToWorld(y);
    ed_.getPane().setCoords(wx, wy);

    boolean repaint = false;


    // UPDATE HOVER ITEM (IF APPLICABLE)

    Drawable hoverItem = ed_.getDrawableAtXY(wx, wy, hoverableTypes_);

    // hover item is unchanged; no update necessary:
    if(hoverItem_ != hoverItem) {

      // check that this is a "hoverable" item according to currently allowed types:
      if(hoverItem != null && !hoverableTypes_.contains(hoverItem.getDrawableType())) {
        hoverItem_ = null;
        ed_.getPane().setStatusText("");
      }
      else {
        hoverItem_ = hoverItem;
        ed_.getPane().setStatusText(hoverItem == null ? "" : hoverItem.toString());
      }
      repaint = true;
    }


    EditorToolbar.Action toolbarAction = ed_.getPane().getToolbar().getSelectedAction();

    // UPDATE PREVIEW ANCHOR (IF APPLICABLE)

    if(toolbarAction == EditorToolbar.Action.CREATE_ANCHOR_POINT || toolbarAction == EditorToolbar.Action.DRAW_NETWORK) {
      if(hoverItem != null && hoverItem.getDrawableType() == Drawable.Type.ANCHOR_POINT) {
        previewAnchor_ = null;
      }
      else {
        Point2D snapped = getWorldCoords(x, y, true);
        previewAnchor_ = new PreviewAnchorPoint(snapped.getX(), snapped.getY());
      }
      repaint = true;
    }

    // UPDATE PREVIEW CORRIDOR (IF APPLICABLE)

    if((toolbarAction == EditorToolbar.Action.CREATE_CORRIDOR || toolbarAction == EditorToolbar.Action.DRAW_NETWORK)
            && fromAnchor_ != null) {

      if(hoverItem != null && hoverItem.getDrawableType() == Drawable.Type.ANCHOR_POINT) {
        Point2D.Double hoverPoint = ((AnchorPoint) hoverItem).getPoint2D();
        if(previewCorr_ == null)
          previewCorr_ = new PreviewCorridor(fromAnchor_.getPoint2D(), hoverPoint, Color.gray);
        else {
          if(corrFlipped_) previewCorr_.setFPoint(hoverPoint);
          else previewCorr_.setTPoint(hoverPoint);
          previewCorr_.updateGeometry();
        }
      }
      else {
        Point2D.Double mousePoint = getWorldCoords(x, y, toolbarAction == EditorToolbar.Action.DRAW_NETWORK);
        if(previewCorr_ == null)
          previewCorr_ = new PreviewCorridor(fromAnchor_.getPoint2D(), mousePoint, Color.gray);
        else {
          if(corrFlipped_) previewCorr_.setFPoint(mousePoint);
          else previewCorr_.setTPoint(mousePoint);
          previewCorr_.updateGeometry();
        }
      }

      repaint = true;
    }
    else {
      if(previewCorr_ != null) {
        previewCorr_ = null;
        repaint = true;
      }
    }

    // UPDATE LINE PREVIEW (IF APPLICABLE)

    if(toolbarAction == EditorToolbar.Action.MODIFY_LINE && ed_.getSelectedLine() != null) {
      Line line = ed_.getSelectedLine();
      if(hoverItem_ != null && hoverItem_.getDrawableType() == Drawable.Type.CORRIDOR) {
        Corridor corr = (Corridor) hoverItem_;
        if(line.size() == lineCorridors_.size() && !line.contains(corr)) {
          //logger.debug("adding corr "+((Corridor) hoverItem_).getID());
          if(line.addCorridor(corr)) { // try adding single, presumably adjacent corridor
            logger.debug("single addition success");
            lineAdditionPath_ = Collections.singletonList(corr);
            new Bundler(doc_.getNetwork());
            repaint = true;
          }
          else { // if adjacent corridor addition failed, look for path through network
            lineAdditionPath_ = doc_.getNetwork().findPathToLine(corr, line, 5);
            boolean success = false;
            if(lineAdditionPath_ != null) {
              lineAdditionPath_.add(corr);
              success = true;
              for(Corridor c : lineAdditionPath_) success = success & line.addCorridor(c);
            }
            if(success) {
              new Bundler(doc_.getNetwork());
              repaint = true;
            }
            else lineAdditionPath_ = null;
          }
        }
      }
      else if(line.size() != lineCorridors_.size()) {
        lineAdditionPath_ = null;
        restoreLine(line);
        new Bundler(doc_.getNetwork());
        repaint = true;
      }
    }

    if(repaint) repaint();

  }

  @Override
  protected void mouseDragged(int x, int y) {
    double dx = -coords_.dxToWorld(x - mx_);
    double dy = coords_.dyToWorld(y - my_);
    switch (dragMode_) {
      /*case PANNING:
        coords_.shiftRange(dx, dy);
        repaint();
        break;*/
      case DRAG_POINT:
        if(ed_.getBoolProperty(Editor.Property.SNAP_TO_GRID)) { //gui_.getControlPanel().getSnapToGrid()) {
          draggingPoint_.moveTo((double) Math.round(coords_.xToWorld(x)),
                                (double) Math.round(coords_.yToWorld(y)));
        }
        else
          draggingPoint_.moveBy(-dx, -dy);
        
        for(Corridor corr : doc_.getNetwork().incidentCorridors(draggingPoint_))
          corr.updateGeometry();

        doc_.getNetwork().rebundle();
        repaint();
        break;
    }
  }

  private KeyEventDispatcher keyED_;
  private boolean corrFlipped_ = false, corr90Deg_ = false;;

  public void activateKeyListener() {

    if(keyED_ == null) {
      keyED_ = new KeyEventDispatcher() {

        public boolean dispatchKeyEvent(KeyEvent e) {
          switch(e.getKeyCode()) {
            case KeyEvent.VK_SHIFT:
              if(previewCorr_ != null && e.getID() == KeyEvent.KEY_RELEASED) {
                previewCorr_.flip();
                corrFlipped_ = !corrFlipped_;
                repaint();
              }
              break;
            case KeyEvent.VK_CONTROL:
              if(previewCorr_ != null && e.getID() == KeyEvent.KEY_RELEASED) {
                corr90Deg_ = !corr90Deg_;
                previewCorr_.setElbowAngle(Math.toRadians(corr90Deg_ ? 90 : 135));
                repaint();
              }
              break;
            case KeyEvent.VK_ESCAPE:
              cancelActiveAction();
              if(ed_.getPane().getToolbar().getSelectedAction() == EditorToolbar.Action.MODIFY_LINE) {
                ed_.setSelectedLine(null);
                setHoverableTypes(new Drawable.Type[] { Drawable.Type.LINE });
              }
              break;
          }
          gui_.dispatchKeyEvent(e);
          return true;
        }
      };
    }

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyED_);

  }

  public void deactivateKeyListener() {
    if(keyED_ != null) KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyED_);
  }

  private void initCoords() {
    double yxRatio = (double) getHeight() / (double) getWidth();
    double initX = 10;
    coords_.updateRange(-initX, -yxRatio*initX, initX, yxRatio*initX);
    coords_.updateDim(getWidth(), getHeight());
  }

  public int getClickTolerance() {
    return clickTol_;
  }
  
  public double getClickToleranceW() {
    return coords_.dxToWorld(clickTol_);
  }

  public int getLastMX() {
    return mx_;
  }

  public int getLastMY() {
    return my_;
  }

  public double getLastWX() {
    return coords_.xToWorld(mx_);
  }

  public double getLastWY() {
    return coords_.yToWorld(my_);
  }

  public void clearHighlightedItems() {
    highlightedItems_ = new HashSet<Drawable>();
  }

  public void cancelActiveAction() {
    fromAnchor_ = null;
    previewAnchor_ = null;
    previewCorr_ = null;
    clearHighlightedItems();
    repaint();

  }

  public void toolbarActionChanged() {
    if(ed_.getPane() == null) return;
    switch(ed_.getPane().getToolbar().getSelectedAction()) {
      case MODIFY_LINE: 
        if(ed_.getSelectedLine() != null) startEditingLine(ed_.getSelectedLine());
        break;
    }
  }

  // LINE EDITING METHODS

  public void startEditingLine(Line line) {
    if(ed_.getSelectedLine() != line) ed_.setSelectedLine(line);
    lineCorridors_ = new LinkedList<Corridor>();
    lineCorridors_.addAll(line.getCorridors());
    Collections.reverse(lineCorridors_);
    clearHighlightedItems();
    highlightedItems_.add(line);
    hoverItem_ = null;
    hoverableTypes_.clear();
    hoverableTypes_.add(Drawable.Type.CORRIDOR);
    lineAdditionPath_ = null;
  }

  public void restoreLine(Line line) {
    line.removeAllCorridors();
    line.addCorridors(lineCorridors_);
  }

  // HIGHLIGHT METHODS

  public void addHighlightedItem(Drawable item) {
    highlightedItems_.add(item);
  }

  public void removeHighlightedItem(Drawable item) {
    highlightedItems_.remove(item);
  }

  public void deletingDrawable(Drawable d) {
    if(hoverItem_ == d) hoverItem_ = null;
  }

  public void setHoverableTypes(Drawable.Type[] types) {
    hoverableTypes_.clear();
    for(Drawable.Type type : types) hoverableTypes_.add(type);
  }

  public boolean isHoverable(Drawable.Type type) {
    return hoverableTypes_.contains(type);
  }

  // MOUSE INPUT METHODS

  @Override
  protected void leftClick(int mx, int my, int numClicks, boolean shift, boolean control) {

    if(numClicks == 2) {
      if(hoverItem_ != null && hoverItem_.getDrawableType() == Drawable.Type.LINE) {
        Line line = (Line) hoverItem_;
        logger.debug(line);
        //gui_.getControlFrameManager().getLinesFrame().selectRow(line);
      }
      return;
    }

    Point2D wXY; // world x,y coordinates

    switch(ed_.getPane().getToolbar().getSelectedAction()) {
      case DRAW_NETWORK:
        wXY = getWorldCoords(mx, my, true);
        if(hoverItem_ != null && hoverItem_.getDrawableType() == Drawable.Type.ANCHOR_POINT) {
          Point2D pt = ((AnchorPoint) hoverItem_).getPoint2D();
          if(fromAnchor_ != null) {
            corridorEndpointClicked(pt.getX(), pt.getY(), corrFlipped_, corr90Deg_);
            fromAnchor_ = (AnchorPoint) hoverItem_;
          }
          else {            
            corridorEndpointClicked(pt.getX(), pt.getY(), corrFlipped_, corr90Deg_);
          }
        }
        else {
          CreateAnchorPointCommand cmd = new CreateAnchorPointCommand(ed_, wXY.getX(), wXY.getY());
          invoker_.doCommand(cmd);
          corridorEndpointClicked(wXY.getX(), wXY.getY(), corrFlipped_, control);
          fromAnchor_ = cmd.getAnchorPoint();
        }
        break;

      case CREATE_ANCHOR_POINT:
        wXY = getWorldCoords(mx, my, true);
        invoker_.doCommand(new CreateAnchorPointCommand(ed_, wXY.getX(), wXY.getY()));
        break;

      case DELETE_ANCHOR_POINT:
        wXY = getWorldCoords(mx, my, false);
        invoker_.doCommand(new DeleteAnchorPointCommand(ed_, wXY.getX(), wXY.getY()));
        break;

      case MERGE_ANCHOR_POINT:
        wXY = getWorldCoords(mx, my, false);
        mergeEndpointClicked(wXY.getX(), wXY.getY());
        break;

      case CREATE_CORRIDOR:
        wXY = getWorldCoords(mx, my, false);
        corridorEndpointClicked(wXY.getX(), wXY.getY(), shift, control);
        break;

      case DELETE_CORRIDOR:
        wXY = getWorldCoords(mx, my, false);
        invoker_.doCommand(new DeleteCorridorCommand(ed_, wXY.getX(), wXY.getY()));
        break;

      case SPLIT_CORRIDOR:
        //ts_.getNetworkOps().splitCorridor(coords_.xToWorld(mx), coords_.yToWorld(my));
        //repaint();
        break;

      case MODIFY_LINE:
        if(hoverItem_ != null && hoverItem_.getDrawableType() == Drawable.Type.LINE) {
          Line line = (Line) hoverItem_;
          startEditingLine(line);
          repaint();
        }
        else {
          double wx = coords_.xToWorld(mx), wy = coords_.yToWorld(my);
          Line line = ed_.getSelectedLine();
          if(line == null) break;
          restoreLine(line);
          if(lineAdditionPath_ != null && lineAdditionPath_.size() > 0) {
            logger.debug("adding "+lineAdditionPath_.size()+" corrs");
            invoker_.doCommand(new AddCorridorsToLineCommand(ed_, line, lineAdditionPath_));
            //startEditingLine(line);
            repaint();
          }
          else {
            Corridor corr = doc_.getNetwork().getCorridorAtXY(wx, wy,
              ed_.getPane().getCanvas().getClickToleranceW());
            if(corr == line.firstCorridor() || corr == line.lastCorridor()) {
              invoker_.doCommand(new RemoveCorridorsFromLineCommand(ed_, line, corr));
              repaint();
            }
          }

        }
        break;
    }
  }

  @Override
  protected void rightClick(int mx, int my, int numClicks, boolean shift, boolean control) {
    if(ed_.getPane().getToolbar().getSelectedAction() != EditorToolbar.Action.SELECT) return;

    Drawable clickedItem = ed_.getDrawableAtXY(coords_.xToWorld(mx), coords_.yToWorld(my), hoverableTypes_);
    if(clickedItem == null) return;

    switch(clickedItem.getDrawableType()) {
      case ANCHOR_POINT:
        anchorPointMenu_.setAnchorPoint((AnchorPoint) clickedItem);
        anchorPointMenu_.show(this, mx, my);// setLocation(mx, my);
        break;
      case CORRIDOR:
        corridorMenu_.setCorridor((Corridor) clickedItem);
        corridorMenu_.show(this, mx, my);// setLocation(mx, my);
        break;
      case STOP:
        stopMenu_.setStop((Stop) clickedItem);
        stopMenu_.show(this, mx, my);
        break;
      case LINE:
        lineMenu_.setLine((Line) clickedItem);
        lineMenu_.show(this, mx, my);
        break;
    }
  }

  private Point2D.Double getWorldCoords(int mx, int my, boolean snapApplies) {
    double wx = coords_.xToWorld(mx), wy = coords_.yToWorld(my);

    if(snapApplies) {
      wx = ed_.getBoolProperty(Editor.Property.SNAP_TO_GRID) ? Math.round(wx) : wx;
      wy = ed_.getBoolProperty(Editor.Property.SNAP_TO_GRID) ? Math.round(wy) : wy;
    }

    return new Point2D.Double(wx, wy);
  }
  
  public void corridorEndpointClicked(double x, double y, boolean shift, boolean control) {
    AnchorPoint pt = doc_.getNetwork().getPointAtXY(x, y, getClickToleranceW());
    if(pt == null) return;

    if(fromAnchor_ == null) {
      fromAnchor_ = pt;
      addHighlightedItem(fromAnchor_);
      repaint();
      //gui_.msg("start point selected");
    }
    else {
      double theta = corr90Deg_ ? Math.PI/2 : 3*Math.PI/4;
      if(corrFlipped_) invoker_.doCommand(new CreateCorridorCommand(ed_, pt, fromAnchor_, theta, false));
      else invoker_.doCommand(new CreateCorridorCommand(ed_, fromAnchor_, pt, theta, false));
      removeHighlightedItem(fromAnchor_);
      previewCorr_ = null;
      fromAnchor_ = null;
      corrFlipped_ = corr90Deg_ = false;
    }
  }

  public void mergeEndpointClicked(double x, double y) {
    AnchorPoint pt = doc_.getNetwork().getPointAtXY(x, y, getClickToleranceW());
    if(pt == null) return;

    if(fromAnchor_ == null) {
      fromAnchor_ = pt;
      addHighlightedItem(fromAnchor_);
      repaint();
    }
    else {
      invoker_.doCommand(new MergeAnchorPointCommand(ed_, fromAnchor_, pt));
      removeHighlightedItem(fromAnchor_);
      fromAnchor_ = null;
    }
  }

  public void mergeStarted(AnchorPoint pt) {
    ed_.getPane().getToolbar().selectAction(EditorToolbar.Action.MERGE_ANCHOR_POINT);
    fromAnchor_ = pt;
    addHighlightedItem(fromAnchor_);
    repaint();
  }

  // KEYLISTENER METHODS

  public void keyTyped(KeyEvent e) {
  }

  public void keyPressed(KeyEvent e) {
    logger.debug("pressed "+e.getKeyChar());
    if(e.getKeyCode() == KeyEvent.VK_SHIFT) logger.debug("pressed");
  }

  public void keyReleased(KeyEvent e) {
    if(e.getKeyCode() == KeyEvent.VK_SHIFT) logger.debug("released");
  }


  // PAINT METHODS

  @Override
  protected void paintComponent(Graphics g) {    
    if(!initialized_) { //coords_.getWidth() == 0) {
      initCoords();
      initialized_ = true;
    }
    
    g2d_ = (Graphics2D) g;

    // setup antialiasing
    if(ed_.getBoolProperty(Editor.Property.USE_ANTIALIASING)) { //gui_.getControlPanel().getAntialiasingOn()) {
      RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d_.setRenderingHints(renderHints);
    }

    g2d_.setColor(doc_.getBGColor());
    g2d_.fillRect(0, 0, getWidth(), getHeight());

    Color highlightColor = Color.yellow;

    if(ed_.getBoolProperty(Editor.Property.SHOW_GRID)) drawGrid();
    
    if(ed_.getBoolProperty(Editor.Property.SHOW_CORRIDORS))
      for(Corridor corr : doc_.getNetwork().getCorridors()) {
        if(highlightedItems_.contains(corr)) corr.drawHighlight(this, highlightColor);
        corr.draw(this);
      }
    
    if(ed_.getBoolProperty(Editor.Property.SHOW_LINES)) {
      for(Line line : doc_.getNetwork().getLines(new LineLayerIndexComparator())) {
        if(line.isEnabled()) {
          if(highlightedItems_.contains(line)) line.drawHighlight(this, highlightColor);
          line.draw(this);
        }
      }
    }

    if(ed_.getBoolProperty(Editor.Property.SHOW_STOPS)) {
      for(Stop stop : doc_.getNetwork().getStops()) {
        if(highlightedItems_.contains(stop)) stop.drawHighlight(this, highlightColor);
        stop.draw(this);
      }
    }

    if(previewAnchor_ != null) previewAnchor_.draw(this);
    if(previewCorr_ != null) previewCorr_.draw(this);

    if(ed_.getBoolProperty(Editor.Property.SHOW_ANCHORS)) {
      for(AnchorPoint pt : doc_.getNetwork().getAnchorPoints()) {
        if(highlightedItems_.contains(pt)) pt.drawHighlight(this, highlightColor);
        pt.draw(this);
      }
    }

    if(hoverItem_ != null) {
      hoverItem_.drawHighlight(this, Color.cyan);
      hoverItem_.draw(this);
    }

  }

  private void drawGrid() {
    g2d_.setColor(Color.lightGray);
    g2d_.setStroke(new BasicStroke(1));

    for(double x = Math.floor(coords_.getX1()); x <= Math.ceil(coords_.getX2()); x++)
      drawLineW(x, coords_.getY1(), x, coords_.getY2());

    for(double y = Math.floor(coords_.getY1()); y <= Math.ceil(coords_.getY2()); y++)
      drawLineW(coords_.getX1(), y, coords_.getX2(), y);
  }

  /**
   * Draws a line on the canvas given "world," rather than screen, coordinates.
   * 
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   */
  public void drawLineW(double x1, double y1, double x2, double y2) {
    g2d_.drawLine(coords_.xToScreen(x1), coords_.yToScreen(y1),
                  coords_.xToScreen(x2), coords_.yToScreen(y2));
  }

  /*public void drawNetworkLines() {

  }*/

}
