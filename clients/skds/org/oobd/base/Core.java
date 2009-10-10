/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base;

import org.oobd.base.support.Onion;
        import java.lang.reflect.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;


/**
 * The interface for nearly all interaction between the generic oobd maschine and the different environments
 * @author steffen
 */
public class Core {

    IFui userInterface;
    IFsystem systemInterface;

    public Core(IFui myUserInterface, IFsystem mySystemInterface){
        userInterface= myUserInterface;
        systemInterface = mySystemInterface;
        //userInterface.sm("Moin");
        Onion testOnion=Onion.generate(null);
        testOnion.setValue("test", "moin");
        testOnion.setValue("test2", "moin2");
        testOnion.setValue("path/test3", "moin3");
        testOnion.setValue("path/test4", "moin4");
        testOnion.setValue("path/path2/test5", "moin5");
        Onion testOnion2=null;
        try{
            testOnion2=new Onion(testOnion.toString());
        }
        catch (org.json.JSONException e){

        }
        System.out.println(testOnion.toString());
        System.out.println(testOnion2.toString());
        systemInterface.register(this); //Anounce itself at the Systeminterface
         systemInterface.loadConnectors();
         try{
             HashMap<String,Object> busses=loadOobdClasses("/home/steffen/Desktop/workcopies/oobd/trunk/clients/skds/org/oobd/ui/swing/build/classes/org/oobd/base/support", "org.oobd.base.support.",Class.forName( "org.oobd.base.support.Onion"));
         }catch(ClassNotFoundException e){

         }
    }

    public void register(String msg){
        userInterface.sm(msg);
    }

    /**
     * loads different dynamic classes via an URLClassLoader.
     * Aa an URLClassloader is generic and can handle URLs, file systems and also jar files,
     * this loader is located in the core section of oobd. Just the information about the correct load path
     * is environment specific and needs to come from the systemInterface
     * @param path
     * @param classtype
     * @return
     */
    public HashMap loadOobdClasses(String path, String classPrefix,Class classType){
 // abgekuckt unter http://de.wikibooks.org/wiki/Java_Standard:_Class
        HashMap<String,Object> myInstances = new HashMap<String,Object>() ;
 /*
    // erste Möglichkeit - Klassen liegen in selbem Verzeichnis wie diese Klasse
    try {
       Class<?> klasse = Class.forName( "MyInterfaceImplementation" );
       MyInterface impl = ( MyInterface ) klasse.newInstance();
       System.out.println( impl.getName() );
    } catch ( ClassNotFoundException ex ) {
       System.out.println( ex.getMessage() );
    } catch ( InstantiationException ex ) {
       System.out.println( ex.getMessage() );
    } catch (IllegalAccessException ex) {
       // Wird geworfen, wenn man einen access-modifier nicht beachtet
       // Man kann mittels reflect die modifier aber auch ändern
       System.out.println( ex.getMessage() );
    }
 */
    //path = "C:\\Dokumente und Einstellungen\\wenGehtsWasAn\\Desktop";

    File directory = new File( path );
    System.out.println( directory.exists() );
    if( directory.exists() ) {
       File[] files = directory.listFiles();

       URL sourceURL = null;
       try {
          // Den Pfad des Verzeichnisses auslesen
          sourceURL = directory.toURI().toURL();
       } catch ( java.net.MalformedURLException ex ) {
          System.out.println( ex.getMessage() );
       }

       // Einen URLClassLoader für das Verzeichnis instanzieren
       URLClassLoader loader = new URLClassLoader(new java.net.URL[]{sourceURL}, Thread.currentThread().getContextClassLoader());

       // Für jeden File im Verzeichnis...
       for( int i=0; i<files.length; i++ ) {

       // Splittet jeden Dateinamen in Bezeichnung und Endung
       // siehe "regular expression" und String.split()
       String name[] = files[i].getName().split("\\.");

       // Nur Class-Dateien werden berücksichtigt
       if( name.length>1 && name[1].equals("class") ){

          try {
             // Die Klasse laden
             Class<?> source = loader.loadClass( classPrefix+name[0] );

             // Prüfen, ob die geladene Klasse das Interface implementiert
             // bzw. ob sie das Interface beerbt
             // Das Interface darf dabei natürlich nicht im selben Verzeichnis liegen
             // oder man muss prüfen, ob es sich um ein Interface handelt Class.isInterface()
             if( classType.isAssignableFrom( source ) ) {

                //classType implementation = ( classType ) source.newInstance();
                 myInstances.put(name[0],  source.newInstance());

/*
                Method method = source.getDeclaredMethod( "getName", new Class<?>[]{} );

                System.out.println( method.invoke( implementation, new Object[]{} ) );
 */

             }
          } catch (InstantiationException ex) {
             // Wird geworfen, wenn die Klasse nicht "instanziert" werden kann
             System.out.println( ex.getMessage() );
          } catch (IllegalAccessException ex) {
             // Wird geworfen, wenn man einen access-modifier nicht beachtet
             // Man kann mittels reflect die modifier aber auch ändern
             System.out.println( ex.getMessage() );
 /*         } catch ( NoSuchMethodException ex ) {
             // Wird geworfen, wenn die Class die spezifizierte Methode nicht implementiert
             System.out.println( ex.getMessage() );
*/          } catch ( ClassNotFoundException ex ) {
             // Wird geworfen, wenn die Klasse nicht gefunden wurde
             System.out.println( ex.getMessage() );
/*          } catch ( InvocationTargetException ex ) {
	     // Wird geworfen, wenn die aufreufene, über Method
	     // reflektierte Methode eine Exception wirft
	     System.out.println( ex.getCause().getMessage() );
*/          }
        }
      }
    }

 return myInstances;


    }
}
