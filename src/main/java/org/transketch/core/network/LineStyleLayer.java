package org.transketch.core.network;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

public class LineStyleLayer {

  private int width_;
  private Color color_;
  private String colorKey_;
  private ColorMode colorMode_;
  private float[] dash_;

  public enum ColorMode { HARD_CODED, KEY_SPECIFIED; }


  public LineStyleLayer(int width, Color color) {
    this(width, color, null);
  }

  public LineStyleLayer(int width, Color color, float[] dash) {
    super();
    width_ = width;
    color_ = color;
    dash_ = dash;
    colorMode_ = ColorMode.HARD_CODED;
  }

  public LineStyleLayer(int width, String colorKey, float[] dash) {
    super();
    width_ = width;
    colorKey_ = colorKey;
    dash_ = dash;
    colorMode_ = ColorMode.KEY_SPECIFIED;
  }

  public Stroke getStroke() {
    return new BasicStroke(width_, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, dash_, 0);
  }

  public Color getColor() {
    return colorMode_ == ColorMode.HARD_CODED ? color_ : Color.gray;
  }

  public Color getColor(Line line) {
    if(colorMode_ == ColorMode.HARD_CODED) return color_;
    else if(colorMode_ == ColorMode.KEY_SPECIFIED) {
      if(line == null) return Color.GRAY;
      return line.getStyleColor(colorKey_);
    }
    return null;
  }

  public void setColor(Color color) {
    color_ = color;
    colorKey_ = null;
    colorMode_ = ColorMode.HARD_CODED;
  }

  public String getColorKey() {
    return colorKey_;
  }

  public void setColorKey(String key) {
    color_ = null;
    colorKey_ = key;
    colorMode_ = ColorMode.KEY_SPECIFIED;
  }

  public ColorMode getColorMode() {
    return colorMode_;
  }

  public int getWidth() {
    return width_;
  }

  public void setWidth(int width) {
    width_ = width;
  }

  public static float[] parseDash(String dash) throws NumberFormatException {
    String strArr[] = dash.split(",");
    float dashArr[] = new float[strArr.length];
    for(int i=0; i < strArr.length; i++) {
      dashArr[i] = Float.parseFloat(strArr[i]);
    }
    return dashArr;
  }

  public boolean setDash(String dash) {
    try {
      setDash(parseDash(dash));
      return true;
    }
    catch(NumberFormatException ex) {
      return false;
    }
  }

  public void setDash(float[] dash) {
    dash_ = dash;
  }

  public float[] getDash() {
    return dash_;
  }

  public String getDashStr() {
    return getDashStr(dash_);
  }

  public static String getDashStr(float[] dash) {
    if(dash == null) return "";
    String dashStr = "";
    for(int i = 0; i < dash.length; i++) {
      dashStr += dash[i] + (i < dash.length-1 ? "," : "");
    }
    return dashStr;
  }

}
