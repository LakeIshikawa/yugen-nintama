package chars.yui;

import com.lksoft.yugen.stateful.FsmResources;
import yugen.Fighter;

/**
 * Valkyrie fighter
 */
@FsmResources(anm = "chars/yui/yui.anm")
public class Yui extends Fighter {

    // Initialization
    public Yui(){
        scale = 1.0f;
    }
}