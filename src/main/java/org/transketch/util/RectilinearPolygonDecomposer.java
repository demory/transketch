/*
 * RectilinearPolygonDecomposer.java
 * 
 * Created by demory on Aug 12, 2009, 10:44:30 PM
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

package org.transketch.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.PolygonExtracter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author demory
 */
public class RectilinearPolygonDecomposer {

  public static Set<Envelope> decompose(Polygon poly) {

    if(poly.isRectangle()) return Collections.singleton(poly.getEnvelopeInternal());

    Set<Envelope> results = new HashSet<Envelope>();

    List<Vertex> vertices = new LinkedList<Vertex>();

    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

    for(int v=0; v < poly.getNumPoints()-1; v++) {
      Coordinate c = poly.getCoordinates()[v];
      minX = Math.min(minX, c.x);
      minY = Math.min(minY, c.y);
      maxY = Math.max(maxY, c.y);
      vertices.add(new Vertex(c));
    }

    Collections.sort(vertices);

    //Vertex v = vertices.get(0);
    int i = 0;
    while(vertices.get(i).getX() == vertices.get(i+1).getX()) i++;

    double curX = minX; //vertices.get(i).getX();
    //results.add(createRect(minX, minY, curX, maxY).intersection(poly));

    for(int vi = 1; vi < vertices.size(); vi++) {
      if(vertices.get(vi).getX() == curX) continue;
      double nextX = vertices.get(vi).getX();
      Geometry slice = createRect(curX, minY, nextX, maxY).intersection(poly);
      List<Polygon> polys = PolygonExtracter.getPolygons(slice);
      for(Polygon p : polys)
        results.add(p.getEnvelopeInternal());
      curX = nextX;
    }

    return results;
  }

  /*private static Polygon createRect(Envelope e) {
    return createRect(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
  }*/

  private static Polygon createRect(double x1, double y1, double x2, double y2) {
    GeometryFactory gf = new GeometryFactory();

    Coordinate coords[] = new Coordinate[5];
    coords[0] = new Coordinate(x1, y1);
    coords[1] = new Coordinate(x1, y2);
    coords[2] = new Coordinate(x2, y2);
    coords[3] = new Coordinate(x2, y1);
    coords[4] = new Coordinate(x1, y1);

    LinearRing shell = gf.createLinearRing(coords);
    Polygon rect = gf.createPolygon(shell, null);

    return rect;
  }

  private static class Vertex implements Comparable {

    private Coordinate coord_;

    public Vertex(Coordinate coord) {
      coord_ = coord;
    }

    public double getX() { return coord_.x; }

    public double getY() { return coord_.y; }

    public int compareTo(Object o) {
      return new Double(this.coord_.x).compareTo(((Vertex) o).coord_.x);
    }

  }

}
