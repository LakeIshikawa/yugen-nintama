package chars.valkyrie;

import com.lksoft.sweat.stateful.FsmResources;
import yugen.Fighter;

/**
 * Valkyrie fighter
 */
@FsmResources(anm = "chars/valkyrie/valkyrie.anm")
public class Valkyrie extends Fighter {

    // Initialization
    public Valkyrie(){
        scale = 0.75f;

        // Middle punch
        punchSMhit.damage = 30;
    }
}