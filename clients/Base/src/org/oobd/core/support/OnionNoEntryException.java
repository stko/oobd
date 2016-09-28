/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.core.support;

import org.oobd.core.CoreException;

/**
 *OnionNoEntryException is thrown when a requested object is not found in the onion
 *
 * @author steffen
 */
public class OnionNoEntryException extends CoreException {
  public OnionNoEntryException()
  {

  }
  public OnionNoEntryException(String s)
  {
    super(s);
  }
}

