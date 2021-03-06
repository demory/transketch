/*
 * CircleRendererTemplate.java
 * 
 * Created by demory on Feb 28, 2011, 10:23:34 AM
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
import java.util.Map;

/**
 *
 * @author demory
 */
public abstract class ShapeRenderer extends StopRenderer {

  private IntegerProperty borderWeight_;
  private ColorProperty fillColor_, borderColor_;

  public ShapeRenderer() {
    super();
    borderWeight_ = createIntegerProperty("bweight", "Border Weight", 2);
    fillColor_ = createColorProperty("fcolor", "Fill Color", Color.white);
    borderColor_ = createColorProperty("bcolor", "Border Color", Color.black);
  }

  public ShapeRenderer(ShapeRenderer copy) {
    borderWeight_ = createIntegerProperty(copy.borderWeight_.key_, copy.borderWeight_.name_, copy.borderWeight_.value_);
    fillColor_ = createColorProperty(copy.fillColor_.key_, copy.fillColor_.name_, copy.fillColor_.value_);
    borderColor_ = createColorProperty(copy.borderColor_.key_, copy.borderColor_.name_, copy.borderColor_.value_);
  }

  ShapeRenderer(Map<String, Object> keyValueMap_) {
    borderWeight_ = createIntegerProperty("bweight", "Border Weight", (Integer) keyValueMap_.get("bweight"));
    fillColor_ = createColorProperty("fcolor", "Fill Color", (Color) keyValueMap_.get("fcolor"));
    borderColor_ = createColorProperty("bcolor", "Border Color", (Color) keyValueMap_.get("bcolor"));
  }

  public int getBorderWeight() {
    return borderWeight_.getValue();
  }

  public Color getBorderColor() {
    return borderColor_.getValue();
  }

  public Color getFillColor() {
    return fillColor_.getValue();
  }

  protected void copyProperties(ShapeRenderer source) {
    borderWeight_ = createIntegerProperty(source.borderWeight_.key_, source.borderWeight_.name_, source.borderWeight_.value_);
    fillColor_ = createColorProperty(source.fillColor_.key_, source.fillColor_.name_, source.fillColor_.value_);
    borderColor_ = createColorProperty(source.borderColor_.key_, source.borderColor_.name_, source.borderColor_.value_);
  }
}
