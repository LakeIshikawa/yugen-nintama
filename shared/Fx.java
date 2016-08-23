package shared;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.lksoft.yugen.stateful.NonFsm;

/**
 * 1-time effect
 */
public class Fx extends NonFsm {
    @Override
    protected State<NonFsm> getInitialState() {
        return new State<NonFsm>() {
            @Override
            public void enter(NonFsm entity) {}

            @Override
            public void update(NonFsm fx) {
                if( fx.getAnimCycles() == 1 ){
                    fx.destroyFSM(fx.getName());
                }
            }

            @Override
            public void exit(NonFsm entity) {}

            @Override
            public boolean onMessage(NonFsm entity, Telegram telegram) {
                return false;
            }
        };
    }
}
