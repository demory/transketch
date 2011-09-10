/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.core.network.line;

import java.util.Comparator;
import org.transketch.core.network.Line;

/**
 *
 * @author demory
 */

public class LineLayerIndexComparator implements Comparator<Line> {

  public int compare(Line o1, Line o2) {
    return new Double(o1.getLayerIndex()).compareTo(o2.getLayerIndex());
  }

}
