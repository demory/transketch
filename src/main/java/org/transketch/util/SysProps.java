/*
 * SysProps.java
 *
 * Created on September 27, 2004, 10:38 PM
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

/**
 * A class to keep track of system property keys.
 * 
 * Properties currently in use:
 * 
 * KEY          VALUE
 * APP_MODE     application mode: "viewer" or "server"
 * FP_HOME      main 5p install directory, including trailing separator
 * DERBY_HOME   Derby data storage directory, including trailing separator
 * FILE_DIR     Most recent directory used for file selection
 * RECENT_FILES Semicolon-separated list of recently-used filenames
 * INIT_DP      name of startup data package
 * INIT_X       initial x-coord for viewer display (floating-point number)
 * INIT_Y       initial y-coord for viewer display (floating-point number)
 * INIT_RES     initial resolution for viewer display (floating-point number)
 * dbUsername   database user (not necessary for derby)
 * dbPassword   database pw (not necessary for derby)
 * 
 * @author demory
 */
public class SysProps {
  
  public static final String APP_MODE = "mode";
  public static final String FP_HOME = "5pHome";
  public static final String DERBY_HOME = "derbyHome";
  public static final String FILE_DIR = "fileDir";
  public static final String RECENT_FILES = "recentFiles";
  public static final String INIT_DP = "initDP";
  public static final String INIT_X = "initX";
  public static final String INIT_Y = "initY";
  public static final String INIT_RES = "initRes";
  public static final String INIT_SHP = "initShp";
  public static final String INIT_OTP = "initOTP";

}
