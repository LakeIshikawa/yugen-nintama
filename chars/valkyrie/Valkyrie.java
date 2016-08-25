package chars.valkyrie;

import yugen.Fighter;
import com.lksoft.yugen.stateful.FsmResources;

/**
 * Valkyrie fighter
 */
@FsmResources(anm = "chars/valkyrie/valkyrie.anm")
public class Valkyrie extends Fighter {

    // Initialization
    public Valkyrie(){
        scale = 0.75f;

        // Middle punch
        punchSHMhit.damage = 30;
    }
}