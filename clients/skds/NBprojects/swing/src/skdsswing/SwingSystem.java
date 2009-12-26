/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package skdsswing;

import org.oobd.base.*;
/**
 * This class is the connection between the generic oobd system and the enviroment for e.g. IO operations
 * @author steffen
 */
public class SwingSystem implements IFsystem{

    Core core;

   public void registerOobdCore(Core thisCore){
       core=thisCore;
       core.register("Moin vom System");
   }
}
