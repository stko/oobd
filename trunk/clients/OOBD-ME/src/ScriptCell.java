/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author steffen
 */
public class ScriptCell {

    private String title;
    private String value;
    private String function;
    private String id;
    private boolean update;
    private boolean timer;

    ScriptCell(String title, String function, String initalValue, boolean update, boolean timer, String id) {
        this.title = title;
        this.value = initalValue;
        this.function = function;
        this.update = update;
        this.timer = timer;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getFunction() {
        return function;
    }

    public boolean getUpdate() {
        return update;
    }

    public boolean getTimer() {
        return timer;
    }

    public String toString() {
        return title + ":" + value;
    }

    public boolean execute(int updateType, Script scriptEngine) {
        value = scriptEngine.callFunction(function,new Object[]{value,id});
            return true;
    }
}
