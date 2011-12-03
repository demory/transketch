/*
 * StopRendererTemplate.java
 * 
 * Created by demory on Feb 28, 2011, 10:01:42 AM
 * 
 * Copyright (C) 2011 David D. Emory
 * 
 * This file is part of Transit Sketchpad. See <http://www.transketch.org>
 * for additional information regarding the project.
 * 
 * Transit Sketchpad is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

package org.transketch.core.network.stop;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author demory
 */
public abstract class StopRendererTemplate implements Cloneable {
  private final static Logger logger = Logger.getLogger(StopRendererTemplate.class);

  protected Set<RendererProperty> props_ = new HashSet<RendererProperty>();

  public enum Type {
    BLANK, FILLEDBORDEREDSHAPE;
  }
  
  public abstract Type getType();

  public Collection<RendererProperty> getProperties() {
    return props_;
  }

  protected IntegerProperty createIntegerProperty(String key, String name, int defaultVal) {
    IntegerProperty prop = new IntegerProperty(key, name, defaultVal);
    props_.add(prop);
    return prop;
  }

  protected ColorProperty createColorProperty(String key, String name, Color defaultVal) {
    ColorProperty prop = new ColorProperty(key, name, defaultVal);
    props_.add(prop);
    return prop;
  }
  
  @Override
  public abstract Object clone();

  public void print() {
    for(RendererProperty prop : props_) {
      logger.debug("- "+prop.getName()+"="+prop.getValue()+" obj="+prop);
    }
  }
}
