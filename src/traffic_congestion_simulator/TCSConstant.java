/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic_congestion_simulator;

/**
 * TSConstant interface will act as a database.
 * It contains all the numbers the 
 *
 * @author chenhanxi
 */
//Stores all the contants that this simulation needs.
public interface TCSConstant {
    
    public final double TIMEINCREMENTS = 0.01; //milliseconds in unit second, 1 millisecond = 0.001 second
    
    //Vehicle classes constants
    public final double BUFFER = 3;
    
    public final int ROUNDEDDECPOS = 4;
            
    public final double ACCELERATIONAVG = 3.5;
    
    public final double ACCELERATIONAVGMAX = 4.1;
            
    public final double DECELERATIONAVG = -2;
    
    public final double DECELERATIONAVGMAX = -3;
            
    public final double REACTIONTIMEAVG = 2;
    
    
    public final int FRAMEWIDTH = 600;
    
    public final int FRAMEHEIGHT = 600;
    
    public final int DRAWINGWIDTH = 400;
    
    public final int DRAWINGHEIGHT = 400;
    
    /*
    public final int LANELENGTH = 150;
    
    public final int LANEKWIDTH = 150;
    */
    // Need to modify
    public final int STARTINGPOSX = 0;
    
    public final int STARTINGPOSY = 0;
    
    public final int TESTSIZEX = 50;
    
    public final int TESTSIZEY = 30;
    
    public final int LIGHTPOSX = 150;
    
    public final int LIGHTPOSY = 150;
    
    // This value will be changed.
    public final int LIGHTCYCLER = 10;
    
    // This value will be changed.
    public final int LIGHTCYCLEG = 7;
    
    // This value will be changed.
    public final int LIGHTCYCLEY = 1;
    
    // We will fix this later.
    public final double[][] LIGHTPOSITION = {};
    
    public final double[][][] LANEPOSITION = {};

    public final double[][][] LANESIZE = {{{280, 10}}};
    
    public final double[] LSIZE = {280, 10};
    
    public final double[][] LANEDIRECTION = {};
    
    public final int[] NUMOFLANE = {}; // NUMOFLANE can also be an indicator of number of light at a specific intersection.
    
    public final int NUMOFINTERSECTION = 0;
}
