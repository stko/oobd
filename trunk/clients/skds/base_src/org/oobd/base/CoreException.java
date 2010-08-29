
package org.oobd.base;

/**
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

