/*
 * Application.java
 *
 * Created on September 27, 2004, 9:32 PM
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
package org.transketch.apps;

//import org.fpdev.core.*;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.transketch.util.FPUtil;
import org.transketch.util.SysProps;

/**
 * Superclass for any executable application based on the FPEngine core.
 * 
 * @author demory
 */
public class Application {
  private static final Logger logger = Logger.getLogger(Application.class);
  
  //protected FPEngine engine_;

  protected Properties sysProps_;

  public Application(String appMode, boolean initEngine) {
    BasicConfigurator.configure();

    // get the home directory:
    String cdir = "";
    try {
      File cdirfile = new File(".");
      cdir = cdirfile.getCanonicalPath();
      logger.info("Current home dir: " + cdir);
    } catch (Exception e) {
      logger.error("Error initializing home directory", e);
    }
   
    // initialize the system properties
    sysProps_ = new Properties();
   
    String fs = File.separator;
    sysProps_.setProperty(SysProps.APP_MODE, appMode);
    sysProps_.setProperty(SysProps.FP_HOME, cdir + fs);
    sysProps_.setProperty(SysProps.DERBY_HOME, cdir + fs + "derby" + fs);

    String filename = cdir + fs +"conf" + fs + appMode +".xml";
    if(new File(filename).exists())
      FPUtil.readPropertiesFile(sysProps_, filename);

    
    // run any pre-engine operations
    runPreEngineOps(sysProps_);
    
    // finally, create the engine
    //if(initEngine)
      //engine_ = new FPEngine(sysProps_);
  }
  
  public void runPreEngineOps(Properties sysProps) { }

  /*public void reinitializeEngine(Properties props) {
    engine_ = new FPEngine(props);
  }

  public FPEngine getEngine() {
    return engine_;
  }*/

  public Properties getProperties() {
    return sysProps_;
  }

  public void writeProperties() {
    try {

      Set<String> toExclude = new HashSet<String>();
      toExclude.add(SysProps.APP_MODE);
      toExclude.add(SysProps.FP_HOME);
      toExclude.add(SysProps.DERBY_HOME);

      String filename = sysProps_.getProperty(SysProps.FP_HOME) + "conf" + File.separator + sysProps_.getProperty(SysProps.APP_MODE) + ".xml";
      if(!new File(filename).exists()) return;
      FileWriter writer = new FileWriter(filename);

      writer.write("<?xml version=\"1.0\"?>\n");
      writer.write("<properties>\n");
      for(Map.Entry entry : sysProps_.entrySet()) {
        String name = entry.getKey().toString();
        if(toExclude.contains(name)) continue;
        writer.write("  <property name=\""+name+"\">"+entry.getValue().toString()+"</property>\n");
      }

      writer.write("</properties>\n");
      writer.close();

    } catch(Exception ex) {
      logger.error("error writing properties", ex);
    }

  }
}
