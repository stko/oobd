package org.oobd.mobile;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import se.krka.kahlua.vm.JavaFunction;

/**
 *
 * @author steffen
 */
/*
 public interface ScriptFunction {
    public int call(Object callFrame, int nArguments);

}
 */
 public interface ScriptFunction extends JavaFunction{
    public int call(Object callFrame, int nArguments);
     
 }

