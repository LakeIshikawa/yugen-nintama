package chars.ryu;

import com.lksoft.sweat.stateful.FsmResources;
import yugen.Fighter;

/**
 * Valkyrie fighter
 */
@FsmResources(anm = "chars/ryu/ryu.anm")
public class Ryu extends Fighter {

    // Initialization
    public Ryu(){
        scale = 0.75f;
        speed_backhop.x = -9;
        speed_backhop.y = 0;
        time_backhop = 12;
        speed_run_fwd = 12;
        time_run_fwd = 16;

        // Middle punch
        punchSMhit.damage = 30;
    }
}