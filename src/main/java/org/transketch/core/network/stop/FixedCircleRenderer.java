/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.transketch.core.network.stop;

/**
 *
 * @author demory
 */
public class FixedCircleRenderer extends CircleRenderer {

  private RendererProperty<Integer> radiusProp_;
  
  public FixedCircleRenderer() {
    super();
    radiusProp_ = new IntegerProperty("radius", "Radius", 5);
    addProperty(radiusProp_);
  }

  @Override
  public Type getType() {
    return Type.FIXED_CIRCLE;
  }
  
  @Override
  public double getRadius(Stop stop) {
    return radiusProp_.getValue();
  }

  protected void copyProperties(FixedCircleRenderer source) {
    super.copyProperties(source);
    System.out.println("copying radius, val="+source.radiusProp_.value_);
    radiusProp_ = createIntegerProperty(source.radiusProp_.key_, source.radiusProp_.name_, source.radiusProp_.value_);
  }
  
  @Override
  public StopRenderer getCopy() {
    System.out.println("FCR getCopy");
    FixedCircleRenderer clone = new FixedCircleRenderer();
    clone.copyProperties(this);
    return clone;
  }    
}
