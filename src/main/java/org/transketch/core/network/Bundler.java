/*
 * Bundler.java
 * 
 * Created by demory on Oct 2, 2009, 10:11:33 PM
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

import org.transketch.core.network.corridor.Corridor;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.transketch.core.network.stop.AnchorBasedStop;
import org.transketch.core.network.stop.Stop;
import org.transketch.util.FPUtil;
import org.transketch.util.viewport.MapCoordinates;

/**
 *
 * @author demory
 */
public class Bundler {
  
  private final static Logger logger = Logger.getLogger(Bundler.class);

  // maps anchor pts to collections of angle-bundle pairs
  //private Map<AnchorPoint, Map<Integer, Bundle>> bundles_;

  private Set<Comparison> comparisons_;
  
  private Set<String> assignedOffsets_; // format: "[lineid]_[anchorid]_[theta]"

  private Set<Straightaway> straightaways_;

  public Bundler(TSNetwork network) {
    network.clearBundlerData();
    initBundles(network);
    initComparisons(network);
    assignOffsets(network);
    constructStraightaways(network);
    processStraightaways(network);
    processAnchors(network);
  }

  public void initBundles(TSNetwork network) {

    //bundles_ = new HashMap<AnchorPoint, Map<Integer, Bundle>>();
    for(AnchorPoint pt : network.getAnchorPoints())
      pt.setBundles(new HashMap<Integer, Bundle>()); //bundles_.put(pt, new HashMap<Integer, Bundle>());

    for(Line line : network.getLines()) {

      if(!line.isBundled()) continue;
      
      AnchorPoint from = line.startPoint(), to;
      for(Corridor c : line.getCorridors()) {
        to = (from != c.fPoint()) ? c.fPoint() : c.tPoint();
        
        int theta;
        Map<Integer, Bundle> bundlesAtPt;
        Bundle bundle;

        //logger.debug("corr "+c.getID());
        //logger.debug("from "+from.getID()+" to "+to.getID());

        // handle segment in the "forward" direction (starting with "from" point)
        if(c.isStraight()) theta = (int) Math.toDegrees(FPUtil.getTheta(to.getX()-from.getX(), to.getY()-from.getY()));
        else {
          Point2D.Double e = c.getElbow();
          theta = (int) Math.toDegrees(FPUtil.getTheta(e.x - from.getX(), e.y - from.getY()));
        }
        //logger.debug("fw theta = "+theta);
        bundlesAtPt = from.getBundles(); //bundles_.get(from);
        if(!bundlesAtPt.containsKey(theta)) bundlesAtPt.put(theta, new Bundle(from, theta));
        bundle = bundlesAtPt.get(theta);
        bundle.addLine(new LineInfo(line, LineInfo.Direction.FORWARD, c));

        // handle segment in the "backward" direction (starting with "to" point)
        if(c.isStraight()) theta = (int) Math.toDegrees(FPUtil.getTheta(from.getX()-to.getX(), from.getY()-to.getY()));
        else {
          Point2D.Double e = c.getElbow();
          theta = (int) Math.toDegrees(FPUtil.getTheta(e.x - to.getX(), e.y - to.getY()));
        }
        //logger.debug("bw theta = "+theta);
        bundlesAtPt = to.getBundles(); //bundles_.get(to);
        if(!bundlesAtPt.containsKey(theta)) bundlesAtPt.put(theta, new Bundle(to, theta));
        bundle = bundlesAtPt.get(theta);
        bundle.addLine(new LineInfo(line, LineInfo.Direction.BACKWARD, c));

        from = to;
      }
    }

    for(AnchorPoint pt : network.getAnchorPoints()) pt.initBundleAxes();

  }

  public void initComparisons(TSNetwork network) {

    comparisons_ = new HashSet<Comparison>();

    // initialize diversion-based comparisons:
    for(AnchorPoint pt : network.getAnchorPoints()) { //Map.Entry<AnchorPoint, Map<Integer, Bundle>> entry : bundles_.entrySet()) {
      for(Bundle b : pt.getBundles().values()) { //entry.getValue().values()) {
        LineInfo lines[] = (LineInfo[]) b.lines_.toArray(new LineInfo[b.lines_.size()]);
        for(int i = 0; i < lines.length; i++) {
          for(int j = i+1; j < lines.length; j++) {
            //logger.debug("init comp "+lines[i]+" & "+lines[j]);
            initComparison(b, lines[i], lines[j]);
          }
        }
      }
    }

    //logger.debug(comparisons_.size()+" comparisons after diversion phase");

    // initialize containment-based comparisons:
    Line lines[] = (Line[]) network.getLines().toArray(new Line[network.getLines().size()]);
    for(int i = 0; i < lines.length; i++) {
      for(int j = i+1; j < lines.length; j++) {
        //if(lines[i].contains(lines[j])) {
        switch(lines[i].containsInOrder(lines[j])) {
          case 1:
            comparisons_.add(new Comparison(new LineInfo(lines[i], LineInfo.Direction.FORWARD, null), new LineInfo(lines[j], LineInfo.Direction.FORWARD, null)));
            comparisons_.add(new Comparison(new LineInfo(lines[j], LineInfo.Direction.BACKWARD, null), new LineInfo(lines[i], LineInfo.Direction.BACKWARD, null)));
            continue;
          case -1:
            comparisons_.add(new Comparison(new LineInfo(lines[j], LineInfo.Direction.FORWARD, null), new LineInfo(lines[i], LineInfo.Direction.BACKWARD, null)));
            comparisons_.add(new Comparison(new LineInfo(lines[i], LineInfo.Direction.FORWARD, null), new LineInfo(lines[j], LineInfo.Direction.BACKWARD, null)));
            continue;
        }
        switch(lines[j].containsInOrder(lines[i])) {
          case 1:
            comparisons_.add(new Comparison(new LineInfo(lines[j], LineInfo.Direction.FORWARD, null), new LineInfo(lines[i], LineInfo.Direction.FORWARD, null)));
            comparisons_.add(new Comparison(new LineInfo(lines[i], LineInfo.Direction.BACKWARD, null), new LineInfo(lines[j], LineInfo.Direction.BACKWARD, null)));
            continue;
          case -1:
            comparisons_.add(new Comparison(new LineInfo(lines[i], LineInfo.Direction.BACKWARD, null), new LineInfo(lines[j], LineInfo.Direction.FORWARD, null)));
            comparisons_.add(new Comparison(new LineInfo(lines[j], LineInfo.Direction.BACKWARD, null), new LineInfo(lines[i], LineInfo.Direction.FORWARD, null)));
            continue;
        }
      }
    }

    //logger.debug(comparisons_.size()+" comparisons after containment` phase");

    /*for(Comparison c: comparisons_) {
      logger.debug(" "+c.lesser_.toString()+ " < "+c.greater_.toString());
    }*/
  }

  //private void initComparison(AnchorPoint anchor, LineInfo line1, LineInfo line2) {
  private void initComparison(Bundle bundle, LineInfo line1, LineInfo line2) {
    //logger.debug("--IC--");
    //logger.debug("anchor="+bundle.anchor_);
    AnchorPoint anchor = bundle.anchor_;
    AnchorPoint opp1 = line1.corr_.opposite(anchor);
    AnchorPoint opp2 = line2.corr_.opposite(anchor);
    //logger.debug("opp1="+opp1+", opp2="+opp2);


    Corridor c1 = line1.corr_;
    Corridor c2 = line2.corr_;

    Corridor adj1 = line1.line_.adjacent(line1.corr_, anchor);
    Corridor adj2 = line2.line_.adjacent(line2.corr_, anchor);
    //logger.debug("adj1="+adj1+", adj2="+adj2);

    // try comparing opposite points first
    if(opp1 != opp2) {
      
      Point2D.Double pt = null;

      if(line1.corr_.isStraight() && !line2.corr_.isStraight())
        pt = line2.corr_.getElbow();
      else if(!line1.corr_.isStraight() && line2.corr_.isStraight())
        pt = line1.corr_.getElbow();
      else if(!line1.corr_.isStraight() && !line2.corr_.isStraight()) {
        Point2D.Double e1 = line1.corr_.getElbow(), e2 = line2.corr_.getElbow();
        double dist1 = FPUtil.magnitude(anchor.getX(), anchor.getY(), e1.x, e1.y);
        double dist2 = FPUtil.magnitude(anchor.getX(), anchor.getY(), e2.x, e2.y);
        if(dist1 < dist2) pt = e1;
        else pt = e2;
      }
      else // both are straight
        return;

      //logger.debug(" ("+opp1.getX()+", "+opp1.getY()+")");
      //logger.debug(" ("+pt.x+", "+pt.y+")");
      //logger.debug(" ("+opp2.getX()+", "+opp2.getY()+")");

      int ccw = Line2D.relativeCCW(opp1.getX(), opp1.getY(), opp2.getX(), opp2.getY(), pt.x, pt.y);
      //logger.debug(" ccw = "+ccw);
      if(ccw == 1) { // a counterclockwise or "right" turn: line2 bundled before line1
        //logger.debug(" right turn, 2 < 1");
        comparisons_.add(new Comparison(line1, line2));
        comparisons_.add(new Comparison(line2.getReverse(), line1.getReverse()));
      }
      else if(ccw == -1) { // a clockwise or "left" turn: line2 bundled before line1
        //logger.debug(" left turn, 1 < 2");
        comparisons_.add(new Comparison(line2, line1));
        comparisons_.add(new Comparison(line1.getReverse(), line2.getReverse()));
      }
      else {
        int ccw2 = Line2D.relativeCCW(anchor.getX(), anchor.getY(), opp1.getX(), opp1.getY(), pt.x, pt.y);
        if(ccw2 == -1) {
          comparisons_.add(new Comparison(line2, line1));
          comparisons_.add(new Comparison(line1.getReverse(), line2.getReverse()));
        }
        else {
          comparisons_.add(new Comparison(line1, line2));
          comparisons_.add(new Comparison(line2.getReverse(), line1.getReverse()));
        }
      }

      /*int ccw = Line2D.relativeCCW(opp1.getX(), opp1.getY(), pt.x, pt.y, opp2.getX(), opp2.getY());
      logger.debug(" ccw = "+ccw);
      if(ccw == 1) { // a counterclockwise or "right" turn: line2 bundled before line1
        //logger.debug(" right turn, 2 < 1");
        comparisons_.add(new Comparison(line2, line1));
        comparisons_.add(new Comparison(line1.getReverse(), line2.getReverse()));
      }
      if(ccw == -1 || ccw == 0) { // a clockwise or "left" turn: line2 bundled before line1
        //logger.debug(" left turn, 1 < 2");
        comparisons_.add(new Comparison(line1, line2));
        comparisons_.add(new Comparison(line2.getReverse(), line1.getReverse()));
      }*/
    }

    // if comparing opposite points is unhelpful, look at adjacent corridors on other side of root point
    else if(adj1 != null && adj2 != null && adj1 != adj2) {

      Point2D.Double pt1 = adj1.isStraight() ?
        adj1.opposite(anchor).getPoint2D() :
        adj1.getElbow();

      Point2D.Double pt2 = adj2.isStraight() ?
        adj2.opposite(anchor).getPoint2D() :
        adj2.getElbow();

      int theta1 = (int) Math.toDegrees(FPUtil.getTheta(pt1.x - anchor.getX(), pt1.y - anchor.getY()));
      int theta2 = (int) Math.toDegrees(FPUtil.getTheta(pt2.x - anchor.getX(), pt2.y - anchor.getY()));

      int dt1 = theta1 - bundle.theta_;
      if(dt1 < 0) dt1 += 360;
      int dt2 = theta2 - bundle.theta_;
      if(dt2 < 0) dt2 += 360;
      //logger.debug("dt1="+dt1+", dt2="+dt2);

      if(dt1 < dt2) { // line1 bundled before line1
        //logger.debug(" 1 < 2");
        comparisons_.add(new Comparison(line1, line2));
        comparisons_.add(new Comparison(line2.getReverse(), line1.getReverse()));
      }
      else if(dt2 < dt1) { // line2 bundled before line1
        //logger.debug(" 2 < 1");
        comparisons_.add(new Comparison(line2, line1));
        comparisons_.add(new Comparison(line1.getReverse(), line2.getReverse()));
      }
    }

  }

  
  public void assignOffsets(TSNetwork network) {
    //logger.debug("--aO--");
    for(Line line : network.getLines())
      line.clearOffsets();

    List<Bundle> sortedBundles = new LinkedList<Bundle>();//new AnchorBundleSizeComparator());
    assignedOffsets_ = new HashSet<String>();

    // populate the collection of bundles and sort by size (i.e. no. of lines in each)
    for(AnchorPoint pt : network.getAnchorPoints()) { //Map.Entry<AnchorPoint, Map<Integer, Bundle>> entry : bundles_.entrySet()) {
      for(Bundle b : pt.getBundles().values()) { //entry.getValue().values()) {
        sortedBundles.add(b);
      }
    }
    Collections.sort(sortedBundles);

    //logger.debug("sortedBundles size="+sortedBundles.size());

    while(!sortedBundles.isEmpty()) {
      // get the largest remaining bundle:
      Bundle b = sortedBundles.get(sortedBundles.size()-1);
      if(b.size() == 1) break;

      // sort the bundle itself:
      //logger.debug("sorting bundle "+b.anchor_.getID()+":"+b.theta_+" size="+b.lines_.size());
      List<LineInfo> bundledLines = new LinkedList<LineInfo>();
      for(LineInfo li : b.lines_) bundledLines.add(li);
      Collections.sort(bundledLines, new BundledLineComparator());

      int offset = -b.getWidth()/2;// + bundledLines.get(0).line_.getStyle().getWidth()/2;
      //int offset = -((b.size()-1)*10)/2;
      for(LineInfo li : bundledLines) {
        //offset += li.line_.getStyle().getActiveSubStyle().getMaxLayerWidth()/2;
        offset += li.line_.getStyle().getActiveSubStyle().getEnvelope()/2;

        assignOffset(li.line_, li.corr_, b.anchor_, b.theta_, offset);
        //extendOffset(li.line_, li.corr_, b.anchor_, b.theta_, offset);

        Corridor oppCorr = li.line_.adjacent(li.corr_, b.anchor_);
        
        if(oppCorr != null && Math.abs(oppCorr.getAngle(b.anchor_)-b.theta_) == 180) {

          assignOffset(li.line_, oppCorr, b.anchor_, oppositeTheta(b.theta_), -offset);
          //extendOffset(li.line_, oppCorr, b.anchor_, oppositeTheta(b.theta_), -offset);
        }


        offset += li.line_.getStyle().getActiveSubStyle().getEnvelope()/2; // getMaxLayerWidth()/2; //10;
      }

      sortedBundles.remove(b);
    }
  }

  private void assignOffset(Line line, Corridor corr, AnchorPoint anchor, int theta, int offset) {
    String str = line.getID()+"_"+anchor.getID()+"_"+theta;
    if(assignedOffsets_.contains(str)) return;
   
    //if(anchor.getID() == 7) logger.debug(" assigning offset "+offset+" to "+line.getName()+"_"+anchor.getID()+"_"+theta);

    Line.CorridorInfo ci = line.getCorridorInfo(corr);
    if(anchor == corr.fPoint()) ci.offsetFrom_ = offset;
    if(anchor == corr.tPoint()) ci.offsetTo_ = -offset;

    assignedOffsets_.add(str);
    
    //double thetaR = Math.toRadians(theta) + Math.PI/2;
    //anchor.applyBundleOffset(Math.cos(thetaR)*offset, Math.sin(thetaR)*offset);
  }

  /* no longer needed since Straightaways introduced
  
  private void extendOffset(Line line, Corridor corr, AnchorPoint anchor, int theta, int offset) {

    if(!corr.isStraight()) return;

    while(corr.isStraight()) {
      anchor = corr.opposite(anchor);
      assignOffset(line, corr, anchor, oppositeTheta(theta), -offset);
      Corridor adj = line.adjacent(corr, anchor);
      if(adj == null || Math.abs(adj.getAngle(anchor)-corr.getAngle(anchor)) != 180) return;
      assignOffset(line, adj, anchor, theta, offset);
      corr = adj;
    }
  }*/

  public static int oppositeTheta(int theta) {
    theta += 180;
    if(theta >= 360) theta -= 360;
    return theta;
  }

  public void print() {
    /*logger.debug("-- PRINTING BUNDLES --");
    for(AnchorPoint pt : bundles_.keySet()) {
      logger.debug("Point "+pt.getID());
      for(Map.Entry<Integer, Bundle> entry : bundles_.get(pt).entrySet()) {
        logger.debug("  Bundle at "+entry.getKey()+" degrees:");
        for(LineInfo line : entry.getValue().lines_) {
          logger.debug("    "+line.toString());
        }
      }
    }
    logger.debug("-- END BUNDLES --");*/
  }

  
  
  public void constructStraightaways(TSNetwork network) {
    Set<Corridor> corrs = new HashSet<Corridor>(network.getCorridors());
    straightaways_ = new HashSet<Straightaway>();
    while(!corrs.isEmpty()) {
      //logger.debug("corrs size="+corrs.size());
      Corridor corr = corrs.iterator().next();
      //logger.debug("considering: "+corr);

      /*if(corr.isVertical()) {
        //logger.debug(" vert");
      }
      else if(corr.isHorizontal()) {
        //logger.debug(" horiz");
      }*/
      if(corr.isStraight()) {

      }
      else {
        corrs.remove(corr);
        continue;
      }

      List<Corridor> saCorrs = new ArrayList<Corridor>();
      saCorrs.add(corr);
      corrs.remove(corr);

      // attempt to extend in the "from" direction:
      Corridor next = network.getStraightExtension(corr, corr.fPoint());
      AnchorPoint curPt = corr.fPoint();
      while(next != null) {
        //logger.debug("  next="+corr.getID());
        saCorrs.add(0, next);
        corrs.remove(next);
        curPt = next.opposite(curPt);
        next = network.getStraightExtension(next, curPt);
      }

      //attempt to extend in the "to" direction:
      next = network.getStraightExtension(corr, corr.tPoint());
      curPt = corr.tPoint();
      while(next != null) {
        saCorrs.add(saCorrs.size(), next);
        corrs.remove(next);
        curPt = next.opposite(curPt);
        next = network.getStraightExtension(next, curPt);
      }

      Straightaway sa = new Straightaway(saCorrs);
      //logger.debug("new SA: "+sa.toString());
      //logger.debug(" left: "+corrs.toString());
      straightaways_.add(sa);
    }

    //logger.debug("constructed "+straightaways_.size()+" straightaways");
  }

  public void processStraightaways(TSNetwork network) {
    for(Straightaway sa : straightaways_) {
      boolean needsAttention = false;
      for(Corridor c : sa.corridors_) {
        for(Line l : c.getLines()) {
          Line.CorridorInfo ci = l.getCorridorInfo(c);
          if(ci.offsetFrom_ != ci.offsetTo_)
            needsAttention = true;
        }
      }
      if(needsAttention) {
        logger.debug(sa.toString()+ " needs attention");
        if(sa.corridors_.size() == 1) {
          fixCorridor(sa.corridors_.get(0), sa.corridors_.get(0).fPoint());
        }
        else {
          Corridor c0 = sa.corridors_.get(0), c1 = sa.corridors_.get(1);
          AnchorPoint startPt = null;
          if(c1.adjacentTo(c0.tPoint())) startPt = c0.fPoint();
          if(c1.adjacentTo(c0.fPoint())) startPt = c0.tPoint();

          AnchorPoint pt = startPt;
          for(Corridor c : sa.corridors_) {
            fixCorridor(c, pt);
            pt = c.opposite(pt);
          }

          double offsetEnv[] = sa.getOffsetEnvelope();
          logger.debug("  Final range: "+offsetEnv[0]+" to "+offsetEnv[1]);
          double adjustment = -(offsetEnv[0]+offsetEnv[1])/2;
          if(adjustment != 0) {
            logger.debug("  Adjusting by "+adjustment);
            pt = startPt;
            for(Corridor c : sa.corridors_) {
              bumpPointOut(pt, c, adjustment);
              pt = c.opposite(pt);
            }
            bumpPointIn(pt, sa.corridors_.get(sa.corridors_.size()-1), adjustment);
          }


        }


      }
    }
  }

  private void fixCorridor(Corridor c, AnchorPoint start) {
    //logger.debug(" fixing corr "+c.getID() + " from pt "+start.getID());
    Set<Double> deltas = new HashSet<Double>();
    for(Line l : c.getLines()) {
      Line.CorridorInfo ci = l.getCorridorInfo(c);
      double delta = -(ci.offsetTo_-ci.offsetFrom_);
      //if(c.fPoint() != start) delta = -delta;
      deltas.add(delta);
      //logger.debug("  l="+l.getName()+" d="+delta);
    }

    if(deltas.size() == 1) {
      double delta = deltas.iterator().next();
      if(delta == 0) {
        //logger.debug("  does not need fixing");
      }
      else {
        bumpPointIn(c.opposite(start), c, delta);
        //logger.debug("  clean fix");
      }
    }
    else if(deltas.size() > 1) {
      logger.debug("  fixCorridor special case!! c="+c.getID());
    }
  }

  private void bumpPointIn(AnchorPoint pt, Corridor inCorr, double delta) {

    AnchorPoint opp = inCorr.opposite(pt);
    int inAngle = (int) Math.toDegrees(FPUtil.getTheta(opp.getX()-pt.getX(), opp.getY()-pt.getY()));
    int outAngle = oppositeTheta(inAngle);
    
    Bundle inBundle = pt.getBundles().get(inAngle);
    Bundle outBundle = pt.getBundles().get(outAngle);

    bumpBundles(pt, inBundle, outBundle, delta);
  }

  private void bumpPointOut(AnchorPoint pt, Corridor outCorr, double delta) {

    AnchorPoint opp = outCorr.opposite(pt);
    int outAngle = (int) Math.toDegrees(FPUtil.getTheta(opp.getX()-pt.getX(), opp.getY()-pt.getY()));
    int inAngle = oppositeTheta(outAngle);

    Bundle inBundle = pt.getBundles().get(inAngle);
    Bundle outBundle = pt.getBundles().get(outAngle);

    bumpBundles(pt, inBundle, outBundle, delta);
  }

  private void bumpBundles(AnchorPoint pt, Bundle inBundle, Bundle outBundle, double delta) {
    
    String inTheta = (inBundle != null) ? ""+inBundle.theta_ : "n/a";
    String outTheta = (outBundle != null) ? ""+outBundle.theta_ : "n/a";
    
    if(pt.getID() == 3) logger.debug("bb "+pt.getID()+" "+inTheta+"-"+outTheta+" d="+delta);
    
    if(inBundle != null) {
      for(LineInfo l : inBundle.lines_) {
        //logger.debug("      * "+l.toString()+ "; offsets: "+ci.offsetFrom_+"/"+ci.offsetTo_);
        if(l.dir_ == LineInfo.Direction.BACKWARD) {
          if(l.corr_.fPoint() == pt) l.line_.getCorridorInfo(l.corr_).offsetFrom_ -=delta;
          else if(l.corr_.tPoint() == pt) l.line_.getCorridorInfo(l.corr_).offsetTo_ +=delta;
        }
        else if(l.dir_ == LineInfo.Direction.FORWARD) {
          if(l.corr_.fPoint() == pt) l.line_.getCorridorInfo(l.corr_).offsetFrom_ -=delta;
          else if(l.corr_.tPoint() == pt) l.line_.getCorridorInfo(l.corr_).offsetTo_ +=delta;
        }
      }
    }
    if(outBundle != null) {
      //logger.debug("   - out:");
      for(LineInfo l : outBundle.lines_) {
        //logger.debug("      * "+l.toString()+ "; offsets: "+ci.offsetFrom_+"/"+ci.offsetTo_);
        if(l.dir_ == LineInfo.Direction.BACKWARD) {
          if(l.corr_.fPoint() == pt) l.line_.getCorridorInfo(l.corr_).offsetFrom_ += delta;
          if(l.corr_.tPoint() == pt) l.line_.getCorridorInfo(l.corr_).offsetTo_ -= delta;
        }
        if(l.dir_ == LineInfo.Direction.FORWARD) {
          if(l.corr_.fPoint() == pt) l.line_.getCorridorInfo(l.corr_).offsetFrom_ += delta;
          if(l.corr_.tPoint() == pt) l.line_.getCorridorInfo(l.corr_).offsetTo_ -= delta;
        }
      }
    }
    
    //logger.debug("   - bumped");
    //bumped_++;
  }
   
  private static double[] checkPointOffsetsOut(AnchorPoint pt, Corridor out) {
    //logger.debug("**  check pt "+pt.getID()+", out "+out.getID());
    AnchorPoint opp = out.opposite(pt);
    int outAngle = (int) Math.toDegrees(FPUtil.getTheta(opp.getX()-pt.getX(), opp.getY()-pt.getY()));
    int inAngle = oppositeTheta(outAngle);

    Bundle outBundle = pt.getBundles().get(outAngle);
    Bundle inBundle = pt.getBundles().get(inAngle);

    return checkPointOffsets(pt, inBundle, outBundle);
  }

  private static double[] checkPointOffsetsIn(AnchorPoint pt, Corridor in) {
    //logger.debug("**  check pt "+pt.getID()+", out "+out.getID());
    AnchorPoint opp = in.opposite(pt);
    int inAngle = (int) Math.toDegrees(FPUtil.getTheta(opp.getX()-pt.getX(), opp.getY()-pt.getY()));
    int outAngle = oppositeTheta(inAngle);

    Bundle outBundle = pt.getBundles().get(outAngle);
    Bundle inBundle = pt.getBundles().get(inAngle);

    return checkPointOffsets(pt, inBundle, outBundle);
  }

  private static double[] checkPointOffsets(AnchorPoint pt, Bundle inBundle, Bundle outBundle) {
    if(outBundle == null && inBundle == null) return null;

    double max = -Double.MAX_VALUE, min = Double.MAX_VALUE;
    if(outBundle != null) {
      for(LineInfo li : outBundle.lines_) {
        double offset = 0;
        if(li.corr_.fPoint() == pt) offset = li.line_.getCorridorInfo(li.corr_).offsetFrom_;
        else offset = -li.line_.getCorridorInfo(li.corr_).offsetTo_;
        //logger.debug("**   o-line "+li.line_.getName()+" (corr "+li.corr_ +"), offset "+offset);
        max = Math.max(max, offset);
        min = Math.min(min, offset);
      }
    }

    if(inBundle != null) {
      for(LineInfo li : inBundle.lines_) {
        double offset = 0;
        if(li.corr_.fPoint() == pt) offset = -li.line_.getCorridorInfo(li.corr_).offsetFrom_;
        else offset = li.line_.getCorridorInfo(li.corr_).offsetTo_;
        //logger.debug("**   i-line "+li.line_.getName()+" (corr "+li.corr_ +"), offset "+offset);
        max = Math.max(max, offset);
        min = Math.min(min, offset);
      }
    }
    return new double[] {min, max};    
  }
  
  public void computeInitialStopOffsets(TSNetwork net) {
    for(Stop stop : net.getStops()) {
      if(stop.getType() == Stop.Type.ANCHORBASED) {
        AnchorPoint pt = ((AnchorBasedStop) stop).getAnchorPoint();
        logger.debug("abStop at "+pt.getID());
        Set<Bundle> visited = new HashSet<Bundle>();
        for(Map.Entry<Integer, Bundle> e : pt.getBundles().entrySet()) {
          int theta = e.getKey();
          Bundle b = e.getValue();
          if(visited.contains(b)) continue;
          Bundle oppBundle = pt.getBundles().get(oppositeTheta(theta));
          double[] env = checkPointOffsets(pt, b, oppBundle);
          logger.debug("  - th "+theta+", "+env[0]+" to "+env[1]);
        }
      }
    }
  }
  
  public static class LineInfo {

    public enum Direction { 
      FORWARD("fw"),
      BACKWARD("bw");

      private String disp_;

      Direction(String disp) {
        disp_ = disp;
      }

      @Override
      public String toString() {
        return disp_;
      }
    };

    Line line_;
    Direction dir_;
    Corridor corr_;

    /*public LineInfo(Line line, Direction dir) {
      this(line, dir, null);
    }*/

    public LineInfo(Line line, Direction dir, Corridor corr) {
      line_ = line;
      dir_ = dir;
      corr_ = corr;
    }

    public LineInfo getReverse() {

      return new LineInfo(line_, (dir_ == Direction.FORWARD ? Direction.BACKWARD : Direction.FORWARD), corr_);
    }

    public double getOffset(AnchorPoint anchor) {
      if(corr_.fPoint() == anchor) return line_.getCorridorInfo(corr_).offsetFrom_;
      if(corr_.tPoint() == anchor) return -line_.getCorridorInfo(corr_).offsetTo_;
      return -1;
    }
    
    @Override
    public String toString() {
      return "line " + line_.getName() + "(#" + line_.getID() + " from "+line_.startPoint().getID()+") @ corr "+corr_.getID()+ " going " + dir_;
    }

    public int hash() {
      return (dir_ == Direction.FORWARD ? 1 : -1) * line_.hashCode();
    }
  }

  public static class Bundle implements Comparable {

    private AnchorPoint anchor_;
    private int theta_;
    private Set<LineInfo> lines_;

    public Bundle(AnchorPoint anchor, int theta) {
      anchor_ = anchor;
      theta_ = theta;
      lines_ = new HashSet<LineInfo>();
    }

    public void addLine(LineInfo line) {
      lines_.add(line);
    }

    public int size() {
      return lines_.size();
    }

    // Bundles are compared based on number of lines contained
    public int compareTo(Object o) {
      return new Integer(lines_.size()).compareTo(((Bundle) o).size());
    }

    public int getWidth() {
      int width = 0;
      for(LineInfo li : lines_) {
        width += li.line_.getStyle().getActiveSubStyle().getEnvelope(); //getMaxLayerWidth();
      }
      return width;
    }
  }

  public static class Comparison {

    LineInfo lesser_, greater_;

    public Comparison(LineInfo lesser, LineInfo greater) {
      lesser_ = lesser;
      greater_ = greater;
    }

    public boolean equals(Comparison c) {
      return lesser_.line_ == c.lesser_.line_ && lesser_.dir_ == c.lesser_.dir_ &&
             greater_.line_ == c.greater_.line_ && greater_.dir_ == c.greater_.dir_;
    }
  }

  public class BundledLineComparator implements Comparator<LineInfo> {

    public int compare(LineInfo o1, LineInfo o2) {
      //logger.debug("comparing "+o1.toString()+" and "+o2.toString());
      int ret = 0;
      Comparison lessThan = new Comparison(o1, o2); // assume o1 < o2
      Comparison greaterThan = new Comparison(o2, o1); // assume o1 > o2
      for(Comparison c : comparisons_) {
        if(c.equals(lessThan)) ret--;
        if(c.equals(greaterThan)) ret++;        
      }
      //logger.debug("blc ret="+ret);
      if(ret == 0) {
        ret = new Integer(o1.hash()).compareTo(o2.hash());
      }
      return ret;
    }
  }

  public static class Straightaway {
    List<Corridor> corridors_;
    //double angleR_;

    public Straightaway(List<Corridor> corrs) { //, double angleR) {
      corridors_ = corrs;
      //angleR_ = angleR;
    }

    @Override
    public String toString() {
      String str = "";
      for(Corridor c : corridors_) str += c.getID() + " ";
      return str;
    }

    public double[] getOffsetEnvelope() {
      Corridor c0 = corridors_.get(0);
      AnchorPoint from = null;
      double max = -Double.MAX_VALUE, min = Double.MAX_VALUE;
      if(corridors_.size() == 1) from = c0.fPoint();
      else {
        Corridor c1 = corridors_.get(1);
        if(c1.adjacentTo(c0.tPoint())) from = c0.fPoint();
        if(c1.adjacentTo(c0.fPoint())) from = c0.tPoint();
      }
      for(int i=0; i < corridors_.size(); i++) {
        Corridor c =corridors_.get(i);
        AnchorPoint to = c.opposite(from);
        //logger.debug("** corr "+c.getID());

        double[] outRange = checkPointOffsetsOut(from, c);
        if(outRange != null) {
          //logger.debug("   outrange: "+outRange[0]+" to "+outRange[1]);
          min = Math.min(min, outRange[0]);
          max = Math.max(max, outRange[1]);
        }

        if(i == corridors_.size()-1) {
          double[] inRange = checkPointOffsetsIn(to, c);
          if(inRange != null) {
            //logger.debug("   inrange: "+inRange[0]+" to "+inRange[1]);
            min = Math.min(min, inRange[0]);
            max = Math.max(max, inRange[1]);
          }
        }
        from = to;
      }
      return new double[] {min,max};
    }
  }
  
  private void processAnchors(TSNetwork network) {
    //System.out.println("** PA **");
    for(AnchorPoint anchor : network.getAnchorPoints()) {
      /*if(anchor.getID() != 54) {
        anchor.addBundleOffset(new Point2D.Double(0,0));
        anchor.computeOffsetCenter();
        continue;
      }*/
      //System.out.println("anchor="+anchor.getID());
      Map<Integer, Set<Double>> offsets = new HashMap<Integer, Set<Double>>();

      for(Map.Entry<Integer, Bundle> entry : anchor.getBundles().entrySet()) {
        Bundle bundle = entry.getValue();
        //System.out.println(" b: "+bundle.theta_);
        int theta = entry.getKey();

        for(LineInfo li : bundle.lines_) {

          double offset = li.getOffset(anchor);
          int rotTheta = this.rotateTheta(theta, -90);//(li.corr_.fPoint()==anchor ? -90 : 90));
          //System.out.println("   * li: "+li.toString()+" offset="+offset+ " rt="+rotTheta);

          if(!offsets.containsKey(rotTheta)) {
            offsets.put(rotTheta, new HashSet<Double>());
          }
          offsets.get(rotTheta).add(offset);

        }
      }

      Map<Integer, Double> meanOffsets = new HashMap<Integer, Double>();
      List<Line2D> lines = new ArrayList<Line2D>();
      for(int rt : offsets.keySet()) {
        //System.out.println("rt="+rt);
        double maxOffset = -Double.MAX_VALUE, minOffset = Double.MAX_VALUE;

        for(double offset : offsets.get(rt)) {
          //System.out.println(" - "+offset);
          minOffset = Math.min(minOffset, offset);
          maxOffset = Math.max(maxOffset, offset);
        }
        double meanOffset = (minOffset+maxOffset)/2;
        //System.out.println(" * mean="+meanOffset);

        int oppTheta = this.oppositeTheta(rt);
        if(meanOffsets.containsKey(oppTheta) && -meanOffsets.get(oppTheta)==meanOffset) {
          //System.out.println("  > equivalent already exists");
        }
        else {
          meanOffsets.put(rt, meanOffset);

          double rtRad = Math.toRadians(rt);


          //double meanOffsetW = coords.dxToWorld(meanOffset);

          double x1 = anchor.getX()+meanOffset*Math.cos(rtRad);
          double y1 = anchor.getY()+meanOffset*Math.sin(rtRad);
          double x2 = x1 + Math.cos(rtRad+Math.PI/2);
          double y2 = y1 + Math.sin(rtRad+Math.PI/2);            

          Line2D line = new Line2D.Double(x1, y1, x2, y2);
          //System.out.println("  > creating line "+x1+","+y1+" to "+x2+","+y2);
          lines.add(line);
        }
      }
      anchor.clearBundleOffsets();
      if(lines.size()==1) {
        //System.out.println("adding offset: "+lines.get(0).getP1());
        anchor.addBundleOffset(new Point2D.Double(lines.get(0).getP1().getX()-anchor.getX(), lines.get(0).getP1().getY()-anchor.getY()));
      } 
      else {
        // compare each line-line pairing
        for(int a = 0; a < lines.size(); a++) {
          for(int b = a+1; b < lines.size(); b++) {
            //System.out.println("comparing "+a+" and "+b);
            Point2D isect = FPUtil.lineLineIntersection(lines.get(a), lines.get(b));
            //System.out.println(" isect="+isect.toString());
            if(isect != null) {
              Point2D.Double isectOffset = new Point2D.Double(isect.getX()-anchor.getX(), isect.getY()-anchor.getY());
              //System.out.println(" isectOffset="+isectOffset.toString());
              anchor.addBundleOffset(isectOffset);
            }
            else {
              //System.out.println(" non-intersection!");
              anchor.addBundleOffset(new Point2D.Double(lines.get(a).getP1().getX()-anchor.getX(), lines.get(a).getP1().getY()-anchor.getY()));
              anchor.addBundleOffset(new Point2D.Double(lines.get(b).getP1().getX()-anchor.getX(), lines.get(b).getP1().getY()-anchor.getY()));
            }
          }
        }
      }
      anchor.computeOffsetCenter();
    }
  }
  
  public int rotateTheta(int theta, int rotateBy) {
    theta += rotateBy;
    if(theta < 0) theta += 360;
    if(theta >= 360) theta -= 360;
    return theta;
  }
}
