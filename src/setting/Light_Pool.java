/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package setting;

import java.awt.Color;
import traffic_congestion_simulator.Buffer;
import traffic_congestion_simulator.TCSConstant;

/**
 *
 * @author chenhanxi
 */
public class Light_Pool implements Runnable {

    Light_Set[] lightpool;

    final Buffer shared;

    public Light_Pool(Buffer shared) {

        lightpool = new Light_Set[TCSConstant.NUMOFINTERSECTION + 1];

        for (int i = 0; i < lightpool.length - 1; i++) {
            // I'm using TCSConstant for now. We will change to have a specific file for it.
            lightpool[i] = new Light_Set(TCSConstant.LIGHTPOSITION[i][0], TCSConstant.LIGHTPOSITION[i][1]);
        }
        // Export lane.
        lightpool[lightpool.length - 1] = null;

        this.shared = shared;
    }

    public Light_Set getLight_Set(int index) {
        return lightpool[index];
    }
    
    private void runCycleUnit() {
        for (Light_Set lightpool1 : lightpool) {
            lightpool1.runCycleUnit();
        }
    }

    // This is the thread
    @Override
    public void run() {

        while (true) {
            synchronized (shared) {
                if (shared.getState() != 1) {
                    try {
                        shared.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Light increment by 1 unit second");
                runCycleUnit();
                // Change to Lane
                shared.setState(2);
                shared.notifyAll();
            }
        }

    }
}
