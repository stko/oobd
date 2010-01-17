/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base.support;

import org.oobd.base.CoreException;


/**
 * OnionNoEntryException is thrown when a requested object is not the requested type
 * @author steffen
 */
public class OnionWrongTypeException extends CoreException{
  public OnionWrongTypeException()
  {

  }
  public OnionWrongTypeException(String s)
  {
    super(s);
  }

}
