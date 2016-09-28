/*
 * Interface to have a generic interface for all kind of input file formats (normal, zipped, encrypted etc.)
 */
package org.oobd.core.archive;

import java.io.InputStream;

import org.oobd.core.Core;

/**
 *
 * @author steffen
 */
public interface Archive {
    
   
    /**
     * \brief return the inputstream of that file
     * @param innerPath internal path inside the file, e.g. if the file is an archive
     * @return the inputstream to read the given (inner) file
     */
    
     InputStream getInputStream(String innerPath);
     
     /**
      * \brief closes an opened inputstream
      */
     void closeInputStream(InputStream inStream);
     /**
      * \brief checks, if the file it belongs to is really the right filetype this Achive object is able to handle.
      * @param fileName
      * @return 
      */
     boolean bind(String filePath);
     
     /**
      * \brief releases the bind file again
      */
     void unBind();
     
    /**
      * \brief in case archive contains some lbcs, this functions load that particular manifest into the global manifest
      */
     void relocateManifest(String luaFileName);
     
   /**
      * \brief checks, if a file exists
      */
     boolean fileExist(String fileName);
     
     /**
      * \brief read properties, if the file contains a manifest. 
      * @param property
      * @param defaultValue
      * @return 
      */
    public String getProperty(String property, String defaultValue);
    
    @Override
     public String toString();
 
     public String getID();
    
    public String getFilePath();

    public String getFileName();
}
