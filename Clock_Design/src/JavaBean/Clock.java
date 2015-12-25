/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaBean;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.beans.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author naina
 */
public class Clock extends Canvas implements Runnable{

  // state & properties
    
  private transient Image     offImage;
  private transient Graphics  offGrfx;
  private transient Thread    clockThread;
  private boolean             raised;
  private boolean             digital;
  
  final int spacing = 10;

  // Constructors
  public Clock() {
    this(false, false);
  }

  public Clock(boolean r, boolean d) {
    // Allow the superclass constructor to do its thing
    super();

    // Set properties
    raised = r;
    digital = d;
    
   // Set BackGround and default size
    this.setBackground(Color.lightGray);
    this.setSize(400, 400);
    
    this.start();
    
  }
  
  
  // Accessor methods
  public boolean isRaised() {
    return raised;
  }

  public void setRaised(boolean r) {
    raised = r;
    repaint();
  }

  public boolean isDigital() {
    return digital;
  }

  public void setDigital(boolean d) {
    digital = d;
    repaint();
  }

  // Other public methods
  public void start()
  {
     if(clockThread == null)
     {
        clockThread = new Thread(this);    
        clockThread.start();
     }
  }
  
  public void stop()
  {
       clockThread = null;   
  }
  
  @Override
  public void run() {
      
     while(clockThread != null)
     {
         try {
             Thread.sleep(100);
         } catch (InterruptedException ex) {
             
         }
         repaint();
     }
     
       clockThread = null;
  }
  
  @Override
  public void update(Graphics g) {
    paint(g);
  }

  @Override
  public synchronized void paint(Graphics g) {
    Dimension d;
      d = getSize();
      
    // Create the offscreen graphics context
     if (offGrfx == null 
             || (offImage.getWidth(this) != d.getWidth()) 
             || (offImage.getHeight(this) != d.getHeight()) )
     {
      offImage = createImage(d.width, d.height);
      offGrfx = offImage.getGraphics();
     }
     
     
    // Paint the background with 3D effects
      offGrfx.setColor(getBackground());
      offGrfx.fillRect(1, 1, d.width - 2, d.height - 2);
      offGrfx.draw3DRect(0, 0,d.width - 1, d.height - 1, raised);
      offGrfx.setColor(getForeground());
      

    // Paint the clock
    if (digital)
    {
        try {
            drawDigitalClock(offGrfx);
        } catch (FontFormatException ex) {
            Logger.getLogger(Clock.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Clock.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    else
    {
    	drawAnalogClock(offGrfx);
    }
    
    //Paint the image onto the screen
    g.drawImage(offImage, 0, 0, this);
  }
  
  // Private support methods
  private void drawAnalogClock(Graphics g2) {
      
    Graphics2D g = (Graphics2D) g2;
    
    int width = getWidth();
    int height = getHeight();
    int xcentre = width/2;
    int ycentre = height/2;
    int size = width/2 - spacing;
    int radius = size/2;
    int tickStart;
    
    //Calculate the current hour,minute and second
    Calendar now = Calendar.getInstance();
  
    int hour = (int)now.get(Calendar.HOUR);
    int minute = (int)now.get(Calendar.MINUTE);
    int second = (int)now.get(Calendar.SECOND);
      
    //Calculate the radius of the second hand,minute hand and hour hand
    float sec_radius = size/2 - spacing; 
    float min_radius = (float) (sec_radius*(3/4.0));
    float hour_radius = (float) (sec_radius*(1/2.0));
     
          
    // Draw the clock shape
    g.setColor(Color.white);
    g.fillOval(xcentre - radius,ycentre - radius, size,size);
    g.setStroke(new BasicStroke(4));
    g.setColor(Color.black);
    g.drawOval(xcentre - (radius + 2),ycentre - (radius + 2), size + 3,size + 3);
    

    // Draw the hour marks and numbers 
    
   for(int sec = 0; sec < 60; sec++)
   {
       
    if(sec%5 == 0)                    //Tick for hours
    {
       tickStart = radius - 10;
    }
    else                                //Tick for minutes
    {
       tickStart = radius - 5;
    } 
    
    g.setColor(Color.RED);
    g.setStroke(new BasicStroke(3));
    
    //Determine the positions of the tick marks
    int txsecond = (int) (Math.cos(sec * 3.14f / 30 - 3.14f / 2) * radius + xcentre);
    int tysecond = (int) (Math.sin(sec * 3.14f / 30 - 3.14f / 2) * radius  + ycentre);
    int tx1second = (int) (Math.cos(sec * 3.14f / 30 - 3.14f / 2) * tickStart + xcentre);
    int ty1second = (int) (Math.sin(sec * 3.14f / 30 - 3.14f / 2) * tickStart + ycentre);
    //Draw the tick marks 
    g.drawLine(tx1second, ty1second, txsecond, tysecond);             //Tick mark Lines   
 
   }
   
   for(int num = 1; num < 13; num++)
   {     
    // Calculate the string width and height so we can center it properly
    // Center point   
    double cX = getWidth()/2;
    double cY = getHeight()/2;   
    String numStr = ""+num;
    int charWidth = 5;
    int charHeight = 4;

    double di = (double)num;  // number as double for easier math

    // Calculate the position along the edge of the clock where the number should
    // be drawn
     // Get the angle from 12 O'Clock to this tick (radians)
    double angleFrom12 = (di/12.0*2.0*Math.PI);

    // Get the angle from 3 O'Clock to this tick
        // Note: 3 O'Clock corresponds with zero angle in unit circle
        // Makes it easier to do the math.
    double angleFrom3 = (Math.PI/2.0-angleFrom12);

    // Get diff between number position and clock center
    int tx = (int)(Math.cos(angleFrom3)*(radius - 21));
    int ty = (int)(-Math.sin(angleFrom3)*(radius - 21));

    // For 6 and 12 we will shift number slightly so they are more even
    if ( num == 6 ){
        ty -= charHeight/2;
    } else if ( num == 12 ){
        ty += charHeight/2;
    }

    // Translate the graphics context by delta between clock center and
    // number position
    g.translate(
            tx,
            ty
    );

    // Draw number at clock center.
    g.drawString(numStr, (int)cX-charWidth/2, (int)cY-charHeight/2);

    // Undo translation
    g.translate(-tx, -ty);

   }

   
    //Determind the positions of the hour, minute and second hands 
    int xsecond = (int) (Math.cos(second * 3.14f / 30 - 3.14f / 2) * sec_radius + xcentre);
    int ysecond = (int) (Math.sin(second * 3.14f / 30 - 3.14f / 2) * sec_radius + ycentre);
    int xminute = (int) (Math.cos(minute * 3.14f / 30 - 3.14f / 2) * min_radius + xcentre);
    int yminute = (int) (Math.sin(minute * 3.14f / 30 - 3.14f / 2) * min_radius + ycentre);
    int xhour = (int) (Math.cos((hour * 30 + minute / 2) * 3.14f / 180 - 3.14f / 2) * hour_radius + xcentre);
    int yhour = (int) (Math.sin((hour * 30 + minute / 2) * 3.14f / 180 - 3.14f / 2) * hour_radius + ycentre);

    //Draw the second hand
    g.setStroke(new BasicStroke(1));
    g.setColor(Color.RED);
    g.drawLine(xcentre, ycentre, xsecond, ysecond);

    //Draw the minute hand
    g.setColor(Color.BLACK);
    g.setStroke(new BasicStroke(2));
    g.drawLine(xcentre, ycentre - 1, xminute, yminute);
    g.drawLine(xcentre - 1, ycentre, xminute, yminute);

    //Draw the hour hand
    g.setColor(Color.BLACK);
    g.setStroke(new BasicStroke(4));
    g.drawLine(xcentre, ycentre - 1, xhour, yhour);
    g.drawLine(xcentre - 1 , ycentre, xhour, yhour);
    //g.drawLine(xcentre + 1, ycentre, xhour, yhour);
    //g.drawLine(xcentre, ycentre + 1, xhour, yhour);

    //center point
    g.setColor(Color.BLACK);
    g.fillOval(xcentre-3,ycentre-3,6,6);  
    
  }

  private void drawDigitalClock(Graphics g) throws FontFormatException, IOException {
    Dimension d = getSize();

    // Get the current time
    Calendar now = Calendar.getInstance();
   
    int hour   = (now.get(Calendar.HOUR));
    int minute = (now.get(Calendar.MINUTE));
    int second = (now.get(Calendar.SECOND));
    
    // Get the time in String format with leading zero
    String time = zero(hour) +":"+ zero(minute) +":"+ zero(second) ;
     
    //Draw the rectangle
    int xcentre = getWidth()/2;
    int ycentre = getHeight()/2;
     
    g.setColor(Color.BLACK);
    g.drawRect(xcentre - getWidth()/4,ycentre - getHeight()/6,getWidth()/2,getHeight()/4);
    g.setColor(Color.blue);
    g.fill3DRect(xcentre - getWidth()/4,ycentre - getHeight()/6,getWidth()/2,getHeight()/4,true);
    

    Font myFont = new Font("SansSerif" ,Font.BOLD,32);
    
    g.setFont(myFont);
    g.setColor(Color.white);
    g.drawString(time,( xcentre - getWidth()/4) + 30,(ycentre - getHeight()/6) + 60);
  }
    
  public String zero(int num)
     {
       String number=( num < 10) ? ("0"+num) : (""+num);
       return number;                                    //Add leading zero if needed 
     }
      
}
