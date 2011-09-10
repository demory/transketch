/*
 * MovieRecorder.java
 * 
 * Created by demory on Sep 8, 2009, 1:25:55 PM
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

package org.transketch.apps.desktop.movie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.media.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.datasink.*;
import javax.media.format.VideoFormat;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author demory
 */
public class MovieRecorder implements ControllerListener, DataSinkListener {

  private CanvasPanel canvas_;
  private int firstYear_, lastYear_, stepDays_;

  private Set<RenderedMap> maps_;

  public MovieRecorder() {
    firstYear_ = 1979;
    lastYear_ = 2020;
    stepDays_ = 15;

    maps_ = new HashSet<RenderedMap>();
    maps_.add(new RenderedMap(20, 20, 300, 300, "ATLANTA", "/home/demory/railanim/marta.fpc"));
    maps_.add(new RenderedMap(340, 20, 300, 300, "DALLAS", "/home/demory/railanim/dallas.fpc"));
    maps_.add(new RenderedMap(660, 20, 300, 300, "DENVER", "/home/demory/railanim/denver.fpc"));
    maps_.add(new RenderedMap(20, 340, 300, 300, "LOS ANGELES", "/home/demory/railanim/la.fpc"));
    maps_.add(new RenderedMap(340, 340, 300, 300, "PORTLAND", "/home/demory/railanim/portland.fpc"));
    maps_.add(new RenderedMap(660, 340, 300, 300, "SEATTLE", "/home/demory/railanim/seattle.fpc"));
  }

  public void run() {
    int width = 990, height = 740, frameRate = 24;

    int frameCount = 365*(lastYear_-firstYear_+1) / stepDays_;
    System.out.println("frameCount="+frameCount);

    MediaLocator oml;
    String outputURL = "file:/home/demory/working/test.mov";
    if ((oml = new MediaLocator(outputURL)) == null) {
	    System.err.println("Cannot build media locator from: " + outputURL);
	    return;
    }

    // create the canvas panel & frame

    canvas_ = new CanvasPanel(height, width);
    JFrame display = new JFrame();
    display.add(canvas_);
    display.setSize(width, height);
    display.setLocationRelativeTo(null);
    display.setVisible(true);


    ImageDataSource ids = new ImageDataSource(width, height, frameRate, frameCount);

    Processor p;

    try {
      System.err.println("- create processor for the image datasource ...");
      p = Manager.createProcessor(ids);
    } catch (Exception e) {
      System.err.println("Yikes!  Cannot create a processor from the data source.");
      return;
    }

    p.addControllerListener(this);

    // Put the Processor into configured state so we can set
    // some processing options on the processor.
    p.configure();
    if (!waitForState(p, p.Configured)) {
      System.err.println("Failed to configure the processor.");
      return;
    }

    // Set the output content descriptor to QuickTime.
    p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

    // Query for the processor for supported formats.
    // Then set it on the processor.
    TrackControl tcs[] = p.getTrackControls();
    Format f[] = tcs[0].getSupportedFormats();
    if (f == null || f.length <= 0) {
      System.err.println("The mux does not support the input format: " + tcs[0].getFormat());
      return;
    }

    tcs[0].setFormat(f[0]);

    System.err.println("Setting the track format to: " + f[0]);

    // We are done with programming the processor.  Let's just
    // realize it.
    p.realize();
    if (!waitForState(p, p.Realized)) {
      System.err.println("Failed to realize the processor.");
      return;
    }

    // Now, we'll need to create a DataSink.
    DataSink dsink;
    if ((dsink = createDataSink(p, oml)) == null) {
      System.err.println("Failed to create a DataSink for the given output MediaLocator: " + oml);
      return;
    }

    dsink.addDataSinkListener(this);
    fileDone = false;

    System.err.println("start processing...");

    // OK, we can now start the actual transcoding.
    try {
      p.start();
      dsink.start();
    } catch (IOException e) {
      System.err.println("IO error during processing");
      return;
    }

    // Wait for EndOfStream event.
    waitForFileDone();

    // Cleanup.
    try {
      dsink.close();
    } catch (Exception e) {
    }
    p.removeControllerListener(this);

    System.err.println("...done processing.");
  }

  /**
   * Create the DataSink.
   */
  DataSink createDataSink(Processor p, MediaLocator outML) {

    DataSource ds;

    if ((ds = p.getDataOutput()) == null) {
      System.err.println("Something is really wrong: the processor does not have an output DataSource");
      return null;
    }

    DataSink dsink;

    try {
      System.err.println("- create DataSink for: " + outML);
      dsink = Manager.createDataSink(ds, outML);
      dsink.open();
    } catch (Exception e) {
      System.err.println("Cannot create the DataSink: " + e);
      return null;
    }

    return dsink;
  }
  Object waitSync = new Object();
  boolean stateTransitionOK = true;

  /**
   * Block until the processor has transitioned to the given state.
   * Return false if the transition failed.
   */
  boolean waitForState(Processor p, int state) {
    synchronized (waitSync) {
      try {
        while (p.getState() < state && stateTransitionOK) {
          waitSync.wait();
        }
      } catch (Exception e) {
      }
    }
    return stateTransitionOK;
  }

  /**
   * Controller Listener.
   */
  public void controllerUpdate(ControllerEvent evt) {

    if (evt instanceof ConfigureCompleteEvent ||
            evt instanceof RealizeCompleteEvent ||
            evt instanceof PrefetchCompleteEvent) {
      synchronized (waitSync) {
        stateTransitionOK = true;
        waitSync.notifyAll();
      }
    } else if (evt instanceof ResourceUnavailableEvent) {
      synchronized (waitSync) {
        stateTransitionOK = false;
        waitSync.notifyAll();
      }
    } else if (evt instanceof EndOfMediaEvent) {
      evt.getSourceController().stop();
      evt.getSourceController().close();
    }
  }
  Object waitFileSync = new Object();
  boolean fileDone = false;
  boolean fileSuccess = true;

  /**
   * Block until file writing is done.
   */
  boolean waitForFileDone() {
    synchronized (waitFileSync) {
      try {
        while (!fileDone) {
          waitFileSync.wait();
        }
      } catch (Exception e) {
      }
    }
    return fileSuccess;
  }

  /**
   * Event handler for the file writer.
   */
  public void dataSinkUpdate(DataSinkEvent evt) {

    if (evt instanceof EndOfStreamEvent) {
      synchronized (waitFileSync) {
        fileDone = true;
        waitFileSync.notifyAll();
      }
    } else if (evt instanceof DataSinkErrorEvent) {
      synchronized (waitFileSync) {
        fileDone = true;
        fileSuccess = false;
        waitFileSync.notifyAll();
      }
    }
  }

  private void refreshFrame(Date curTime) {
    try {
      System.out.println("refreshing frame");
      canvas_.setTime(curTime);
      canvas_.paintImmediately(0,0,canvas_.getWidth(), canvas_.getHeight());//repaint();
      BufferedImage img = new BufferedImage(canvas_.getWidth(), canvas_.getHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics2D g = img.createGraphics();
      canvas_.paintAll(g);
      ImageIO.write(img, "jpeg", new File("/home/demory/5p/temp/foo.jpg"));
      //return img;
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  ///////////////////////////////////////////////
  //
  // Inner classes.
  ///////////////////////////////////////////////

  class CanvasPanel extends JPanel {

    private Date time_;
    private long startTime_, timeSpan_;

    private DateFormat dispMonthDF_, dispYearDF_;

    public CanvasPanel(int height, int width) {
      super();
      setSize(width, height);
      dispMonthDF_ = new SimpleDateFormat("MMM");
      dispYearDF_ = new SimpleDateFormat("yyyy");
      Calendar cal = Calendar.getInstance();
      cal.set(firstYear_, 0, 1);
      startTime_ = cal.getTime().getTime();
      cal.set(lastYear_, 11, 31);
      timeSpan_ = cal.getTime().getTime() - startTime_;

    }

    public void setTime(Date time) {
      time_ = time;
    }

    @Override
    public void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;

      RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHints(renderHints);

      g2d.setColor(Color.WHITE);
      g2d.fillRect(0, 0, getWidth(), getHeight());

      // draw the progress bar and date label
      int leftPx = 20, rightPx = getWidth()-135, yMid = getHeight() - 30;
      g2d.setColor(Color.BLUE);
      g2d.fillRect(leftPx, yMid-1, rightPx-leftPx, 3);
      double timeAfter = time_.getTime()-startTime_;
      double pct = timeAfter / (double) timeSpan_;
      //System.out.println("pct="+pct+ "ta="+timeAfter+" span="+timeSpan_);
      double pctPx = pct * (rightPx-leftPx);
      g2d.fillRect(leftPx, yMid-5, (int) pctPx, 11);

      g2d.setColor(Color.BLACK);
      g2d.setFont(new Font("Arial", Font.BOLD, 24));
      g2d.drawString(dispMonthDF_.format(time_), rightPx+10, yMid+9);
      g2d.drawString(dispYearDF_.format(time_), rightPx+60, yMid+9);

      for(RenderedMap map : maps_) {
        map.renderMap(g2d, time_);
      }


    }
  }

  /**
   * A DataSource to read from a list of JPEG image files and
   * turn that into a stream of JMF buffers.
   * The DataSource is not seekable or positionable.
   */
  class ImageDataSource extends PullBufferDataSource {

    ImageSourceStream streams[];

    ImageDataSource(int width, int height, int frameRate, int frameCount) {
      streams = new ImageSourceStream[1];
      streams[0] = new ImageSourceStream(width, height, frameRate, frameCount);
    }

    public void setLocator(MediaLocator source) {
    }

    public MediaLocator getLocator() {
      return null;
    }

    /**
     * Content type is of RAW since we are sending buffers of video
     * frames without a container format.
     */
    public String getContentType() {
      return ContentDescriptor.RAW;
    }

    public void connect() {
    }

    public void disconnect() {
    }

    public void start() {
    }

    public void stop() {
    }

    /**
     * Return the ImageSourceStreams.
     */
    public PullBufferStream[] getStreams() {
      return streams;
    }

    /**
     * We could have derived the duration from the number of
     * frames and frame rate.  But for the purpose of this program,
     * it's not necessary.
     */
    public Time getDuration() {
      return DURATION_UNKNOWN;
    }

    public Object[] getControls() {
      return new Object[0];
    }

    public Object getControl(String type) {
      return null;
    }
  }

  /**
   * The source stream to go along with ImageDataSource.
   */
  class ImageSourceStream implements PullBufferStream {

    //private Visualizer viz_;
    //Vector images;
    private Calendar now_;
    private int frameCount_;
    private VideoFormat format;
    private int nextImage_ = 0;	// index of the next image to be read.
    private boolean ended_ = false;

    public ImageSourceStream(int width, int height, int frameRate, int frameCount) {
      //width_ = width;
      //height_ = height;
      //this.images = images;
      //curTime_ = startTime_ = startTime;
      //step_ = step;

      now_ = Calendar.getInstance();
      now_.set(firstYear_, 0, 1);

      frameCount_ = frameCount;

      format = new VideoFormat(VideoFormat.JPEG,
              new Dimension(width, height),
              Format.NOT_SPECIFIED,
              Format.byteArray,
              (float) frameRate);
    }

    /**
     * We should never need to block assuming data are read from files.
     */
    public boolean willReadBlock() {
      return false;
    }

    /**
     * This is called from the Processor to read a frame worth
     * of video data.
     */
    public void read(Buffer buf) throws IOException {

      // Check if we've finished all the frames.
      if (nextImage_ >= frameCount_) { //images.size()) {
        // We are done.  Set EndOfMedia.
        System.err.println("Done reading all images.");
        buf.setEOM(true);
        buf.setOffset(0);
        buf.setLength(0);
        ended_ = true;
        return;
      }

      //String imageFile = (String) images.elementAt(nextImage_);
      String imageFile = "/home/demory/5p/temp/foo.jpg";
      MovieRecorder.this.refreshFrame(now_.getTime());
      nextImage_++;

      //System.err.println("  - reading image file: " + imageFile);

      // Open a random access file for the next image.
      RandomAccessFile raFile;
      raFile = new RandomAccessFile(imageFile, "r");

      byte data[] = null;

      // Check the input buffer type & size.

      if (buf.getData() instanceof byte[]) {
        data = (byte[]) buf.getData();
      }

      // Check to see the given buffer is big enough for the frame.
      if (data == null || data.length < raFile.length()) {
        data = new byte[(int) raFile.length()];
        buf.setData(data);
      }

      // Read the entire JPEG image from the file.
      raFile.readFully(data, 0, (int) raFile.length());

      System.err.println("    read " + raFile.length() + " bytes.");

      buf.setOffset(0);
      buf.setLength((int) raFile.length());
      buf.setFormat(format);
      buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);

      // Close the random access file.
      raFile.close();

      now_.add(Calendar.DATE, stepDays_);
      //curTime_ += step_;
    }

    /**
     * Return the format of each video frame.  That will be JPEG.
     */
    public Format getFormat() {
      return format;
    }

    public ContentDescriptor getContentDescriptor() {
      return new ContentDescriptor(ContentDescriptor.RAW);
    }

    public long getContentLength() {
      return 0;
    }

    public boolean endOfStream() {
      return ended_;
    }

    public Object[] getControls() {
      return new Object[0];
    }

    public Object getControl(String type) {
      return null;
    }
  }
}
