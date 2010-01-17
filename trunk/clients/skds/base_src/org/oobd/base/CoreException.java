/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base;

/**
 *
 * @author steffen
 * CoreException is just a generic exception to have a base super class for all exceptions thrown by the oobd core
 */
public class CoreException extends Exception {
  public CoreException()
  {

  }
  public CoreException(String s)
  {
    super(s);
  }
}

