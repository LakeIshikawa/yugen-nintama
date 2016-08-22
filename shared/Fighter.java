package shared;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.lksoft.yugen.stateful.Fsm;
import com.lksoft.yugen.stateless.CommandDef;

/**
 * Fighter base class
 */
public class Fighter extends Fsm<Fighter, State<Fighter>, FighterHit> {
    // Constants
    public enum FightPosition {
        STANDING("S"),
        CROUCHING("C"),
        AIR("A");

        public String idchar;
        FightPosition(String idchar){
            this.idchar = idchar;
        }
    }

    // Physics
    public enum Physics{
        NONE,
        STANDING,
        CROUCHING,
        AIR
    }

    // Default states
    public State<Fighter> idle = FighterState.IDLE;
    public State<Fighter> walk = FighterState.WALK;
    public State<Fighter> turn = FighterState.TURN;
    public State<Fighter> jumpstart = FighterState.JUMPSTART;
    public State<Fighter> jumping = FighterState.JUMPING;
    public State<Fighter> landing = FighterState.LANDING;
    public State<Fighter> stand2crouch = FighterState.STAND2CROUCH;
    public State<Fighter> crouching = FighterState.CROUCHING;
    public State<Fighter> crouch2stand = FighterState.CROUCH2STAND;
    public State<Fighter> running = FighterState.RUNNING;
    public State<Fighter> backhop = FighterState.BACKHOP;
    public State<Fighter> backhopland= FighterState.BACKHOPLAND;
    public State<Fighter> groundDamage = FighterState.GROUNDDAMAGE;
    public State<Fighter> airDamage = FighterState.AIRDAMAGE;
    public State<Fighter> punchSHM = FighterState.PUNCHSHM;


    // Default hits
    public FighterHit punchSHMhit = new FighterHit();
    public FighterHit slmpunchHit = new FighterHit();

    // Basilar commands
    public CommandDef runCmd = CommandDef.parse("{10} < ~F, !F >");
    public CommandDef backhopCmd = CommandDef.parse("{10} < ~B, !B >");

    // Default parameters
    public float speed_walk_fwd = 6.0f;
    public float speed_walk_bwd = -4.0f;
    public float speed_air_fwd = 3.0f;
    public float speed_air_bwd = -2.0f;
    public float speed_jump_up = 15.0f;
    public float speed_run_fwd = 9.0f;
    public Vector2 speed_backhop = new Vector2(-9.0f, 5.0f);
    public float standing_friction = 0.4f;
    public float crouching_friction = 0.65f;
    public float air_gravity_y = -0.6f;

    // FightPosition
    public FightPosition fightPosition;
    public Physics physics;

    // References
    public Fsm opponent;

    // Temp
    public int slidetime;

    // Initialization
    public Fighter() {
        setActive(false);

        // -- Default hit values
        // Punches
        punchSHMhit.damageAnimHeight = FighterHit.DamageAnimHeight.HIGH;
        slmpunchHit.damageAnimHeight = FighterHit.DamageAnimHeight.LOW;
    }

    @Override
    public State<Fighter> getInitialState(){
        return idle;
    }

    @Override
    public void statelessUpdate(){
        boolean standing = fightPosition == FightPosition.STANDING;
        boolean crouching = fightPosition == FightPosition.CROUCHING;

        // Physics
        switch (physics){
            case STANDING:
                vel.x = vel.x * standing_friction;
                if(Math.abs(vel.x) < 0.1) { vel.x = 0; }
                break;

            case CROUCHING:
                vel.x = vel.x * crouching_friction;
                if(Math.abs(vel.x) < 0.1) { vel.x = 0; }
                break;

            case AIR:
                vel.y = vel.y + air_gravity_y;
                if(pos.y < 0.0) {
                    vel.y = 0;
                    pos.y = 0;
                }
                break;
        }

        // Special and normal commands
        if( isCtrl() ){
            // Run
            if( standing && getAnimation("running") != null && matchCommand(runCmd) ){
                setCtrl(false);
                changeState(running);
                return;
            }

            // Backhop
            if( standing && getAnimation("backhop") != null && matchCommand(backhopCmd) ){
                setCtrl(false);
                changeState(backhop);
                return;
            }

            // Normal attacks
            if( standing && getAnimation("punchSHM") != null && keyPress("B1") ){
                setCtrl(false);
                changeState(punchSHM);
                return;
            }
        }

        // Hit check
        if( isHit() ){
            // Block
            boolean standBlock = keyHold("B") && standing && getHit().guardflags.contains("S");
            boolean crouchBlock = keyHold("B") && crouching && getHit().guardflags.contains("C");
            if( standBlock ){
                // TODO Stand block
            }
            else if( crouchBlock ){
                // TODO Crouch block
            }
            else {
                // -- DAMAGE
                // Pause players
                pause(getHit().pausetime);
                opponent.pause(getHit().pausetime);

                // Air damage
                if( fightPosition == FightPosition.AIR ){
                    setAnimation("damageFall");
                    vel.x = getHit().air_velocity * (flip ? -1 : 1);
                    vel.y = 0;
                    clearHit();
                    changeState(airDamage);
                    return;
                }

                // Falls
                if( getHit().fall ){
                    setAnimation("damageFall");
                    vel.x = getHit().air_velocity * (flip ? -1 : 1);
                    vel.y = 15f;
                    clearHit();
                    changeState(airDamage);
                    return;
                }

                // Ground damage
                vel.x = getHit().ground_velocity * (flip ? -1 : 1);
                slidetime = getHit().ground_slidetime;

                String animName = "damage" + fightPosition.idchar + getHit().damageAnimHeight.idchar + getHit().damageAnimType.idchar;
                setAnimation(animName);
                clearHit();
                changeState(groundDamage);
                return;
            }
        }
    }
}

/**
 * Fighter state machine
 */
enum FighterState implements State<Fighter> {
    IDLE() {
        public void enter(Fighter f){
            // Save opponent
            if( f.opponent == null ) {
                f.opponent = (f == f.getFSM("p1")) ? f.getFSM("p2") : f.getFSM("p1");
            }

            f.setAnimation("idle");
            f.fightPosition = Fighter.FightPosition.STANDING;
            f.physics = Fighter.Physics.STANDING;
            f.setCtrl(true);
            f.setLayer(5);
        }

        public void update(Fighter f) {
            // Turn
            if( !f.facing(f.opponent) ){
                f.changeState(f.turn);
                return;
            }

            // Walk
            if( f.keyHold("F") ^ f.keyHold("B") ){
                f.changeState(f.walk);
                return;
            }

            // Jump
            if( f.keyHold("U") ){
                f.changeState(f.jumpstart);
                return;
            }

            // Crouch
            if( f.keyHold("D") ){
                f.changeState(f.stand2crouch);
                return;
            }
        }
    },

    WALK {
        public void enter(Fighter f) {
            f.physics = Fighter.Physics.NONE;
        }

        public void update(Fighter f) {
            // Fwd
            if( f.keyHold("F") && !f.keyHold("B") ){
                f.vel.x = f.speed_walk_fwd * (f.flip ? -1 : 1);
                f.setAnimation("walkfwd");
            }

            // Bwd
            if( f.keyHold("B") && !f.keyHold("F") ){
                f.vel.x = f.speed_walk_bwd * (f.flip ? -1 : 1);
                f.setAnimation("walkbwd");
            }

            // Turn
            if( !f.facing(f.opponent) ){
                f.changeState(f.turn);
                return;
            }

            // Stop walking
            if( !(f.keyHold("F") ^ f.keyHold("B")) ){
                f.changeState(f.idle);
            }

            // Jump
            if( f.keyHold("U") ){
                f.changeState(f.jumpstart);
                return;
            }

            // Crouch
            if( f.keyHold("D") ){
                f.changeState(f.stand2crouch);
                return;
            }
        }
    },

    TURN {
        public void enter(Fighter f){
            f.setAnimation("turn");
        }

        public void update(Fighter f) {
            if( f.getAnimCycles() == 1 ){
                f.flip = !f.flip;
                f.changeState(f.idle);
                return;
            }
        }
    },

    JUMPSTART {
        public void enter(Fighter f){
            f.setAnimation("jumpstart");
        }

        public void update(Fighter f) {
            // Jump up
            if( f.getAnimCycles() == 1 ){
                // Set proper animation
                if( f.keyHold("F") ) f.setAnimation("jumpingFwd");
                else if ( f.keyHold("B") ) f.setAnimation("jumpingBwd");
                else f.setAnimation("jumping");

                // Jump
                f.vel.y = f.speed_jump_up;
                f.changeState(f.jumping);
                return;
            }
        }
    },

    JUMPING {
        public void enter(Fighter f){
            f.fightPosition = Fighter.FightPosition.AIR;
            f.physics = Fighter.Physics.AIR;
        }
        public void update(Fighter f) {
            // Controls
            if( f.keyHold("F") ){
                f.vel.x  = f.speed_air_fwd * (f.flip ? -1 : 1);
            }
            if( f.keyHold("B") ){
                f.vel.x  = f.speed_air_bwd * (f.flip ? -1 : 1);
            }

            // Landing
            if( f.pos.y < 0 ){
                f.changeState(f.landing);
                return;
            }
        }
    },

    LANDING {
        public void enter(Fighter f){
            f.setAnimation("landing");
        }

        public void update(Fighter f) {
            if( f.getAnimCycles() == 1 ){
                f.changeState(f.idle);
            }
        }
    },

    STAND2CROUCH {
        public void enter(Fighter f){
            f.fightPosition = Fighter.FightPosition.CROUCHING;
            f.physics = Fighter.Physics.CROUCHING;
            f.setAnimation("stand2crouch");
        }
        public void update(Fighter f) {
            if( f.getAnimCycles() == 1 ){
                f.changeState(f.crouching);
                return;
            }
        }
    },

    CROUCHING {
        public void enter(Fighter f){
            f.setAnimation("crouching");
        }

        public void update(Fighter f) {
            if( !f.keyHold("D") ){
                f.changeState(f.crouch2stand);
                return;
            }
        }
    },

    CROUCH2STAND {
        public void enter(Fighter f){
            f.setAnimation("crouch2stand");
        }

        public void update(Fighter f) {
            if( f.getAnimCycles() == 1 ){
                f.changeState(f.idle);
                return;
            }
        }
    },

    RUNNING {
        public void enter(Fighter f){
            f.setAnimation("running");
            f.vel.x = f.speed_run_fwd * (f.flip?-1:1);
        }

        public void update(Fighter f) {
            if( !f.keyHold("F") ){
                f.changeState(f.idle);
                return;
            }
        }
    },

    BACKHOP {
        public void enter(Fighter f){
            f.setAnimation("backhop");
            f.vel.x = f.speed_backhop.x * (f.flip?-1:1);
            f.vel.y = f.speed_backhop.y;
            f.physics = Fighter.Physics.AIR;
        }

        public void update(Fighter f) {
            if( f.pos.y < 0 ){
                f.changeState(f.backhopland);
                return;
            }
        }
    },

    BACKHOPLAND {
        public void enter(Fighter f){
            f.setAnimation("backhopland");
        }

        public void update(Fighter f) {
            if( f.getAnimCycles() == 1 ){
                f.changeState(f.idle);
                return;
            }
        }
    },

    GROUNDDAMAGE {
        public void enter(Fighter f){
            f.physics = Fighter.Physics.NONE;
        }
        public void update(Fighter f) {
            if( f.getStatetime() == f.slidetime ){
                f.changeState(f.idle);
            }
        }
    },

    AIRDAMAGE {
        public void enter(Fighter f){
            f.fightPosition = Fighter.FightPosition.AIR;
            f.physics = Fighter.Physics.AIR;
        }
        public void update(Fighter f) {
            if( f.pos.y < 0 ){
                f.changeState(f.landing);
                return;
            }
        }
    },

    PUNCHSHM {
        public void enter(Fighter f){
            f.setLayer(6);
            f.setAnimation("punchSHM");
            f.vel.x = 0;
            f.setCtrl(false);
            f.setAttackHit(f.punchSHMhit);
        }

        public void update(Fighter f) {
            if( f.getAnimCycles() == 1 ){
                f.changeState(f.idle);
                return;
            }
        }
    }
    ;

    public void enter(Fighter entity){}
    public void update(Fighter entity) {}
    public void exit(Fighter entity){}
    public boolean onMessage(Fighter entity, Telegram telegram) {return false;}
}
