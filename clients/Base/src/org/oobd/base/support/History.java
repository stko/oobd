
package org.oobd.base.support;
import java.util.Vector;


/**
 * \brief A support class to go easily back to previous pages
 * \group visualisation
 * As OOBD is stateless like a webbrowser, the history class help to identify page changes and to provide a history of the last visited pages
 * 
 * The History function works as follows:
 * \li it listen to the ongoing function calls and saves the actual call
 * \li when then a openPage() call occurs, it's assumed that this new page is caused by the last (saved) function call, so that this function equals a page
 * \li this relation of function call and the new page is stored in a list
 * \li when then the back() function is called, the history returns the function call which caused the previous page
 * \li when back() returns null, the root level is reached
 * 
 */

public class History {

    class PageInfo{
        public String functionName=null;
        public String pageTitle="";
        public PageInfo(String f, String p){
            functionName=f;
            pageTitle=p;
        }
    }

    String lastID = "";
    Vector<PageInfo> pageList = new Vector();
    
    /**
     * \brief listen to ongoing function calls
     * 
     * @param actualCall
     */
    public void listen(String actualCall){
        lastID=actualCall;        
    }

    /**
     * \brief reacts on new page calls and records this in the backlist
     * @param pageTitle
     */

    public void tellNewPage(String pageTitle){
        int i=0;
        for (; i<pageList.size() && !pageList.elementAt(i).functionName.equals(lastID);i++); //looking, if that functionID is already used previously
        if (i<pageList.size()){ //found
            //delete all entries in the list which came after
            i++;
            while (i<pageList.size()){
                pageList.removeElementAt(i);
            }
        }else{
            pageList.add(new PageInfo(lastID,pageTitle));
        }
    }

    /**
     * \brief tells the function of the previous page
     * @return functionID which caused the previous page
     */

    public String Back(){
        if (pageList.size()>1){
            pageList.remove(pageList.size()-1); //delete the actual page
            return pageList.remove(pageList.size()-1).functionName; // and also delete the previous page, but returning also their functioncall
        }else{
            return null;
        }
    }
}
