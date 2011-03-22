/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.bus;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.oobd.base.*;
import org.oobd.base.support.Onion;



/**
 *
 * @author steffen
 */
public class BusEcho extends OobdBus implements OOBDConstants{

	protected boolean keepRunning = true;
	
	
	
    public BusEcho() {
        Debug.msg("busecho",DEBUG_BORING,"Ich bin BusEchoAndroid...");

    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
        Debug.msg("busecho",DEBUG_BORING,"Core registered...");
    }

    @Override
    public String getPluginName() {
        return "b:Echo";
    }

    public void run() {
        
    	// TODO	uncomment again...
    	// throw new UnsupportedOperationException("Not supported yet.");
    	
    	System.out.println ("Bus Echo not supportded");
    		

        
    }
}
