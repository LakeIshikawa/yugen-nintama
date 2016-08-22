package shared;

/**
 * Fighter hit information
 */
public class FighterHit {
    public enum DamageAnimType {
        LIGHT("L"),
        MEDIUM("M"),
        HEAVY("H");

        public String idchar;
        DamageAnimType(String idchar){
            this.idchar = idchar;
        }
    };

    public enum DamageAnimHeight {
        HIGH("H"),
        LOW("L");

        public String idchar;
        DamageAnimHeight(String idchar){
            this.idchar = idchar;
        }
    }

    // Fields
    public int damage = 20;
    public String guardflags = "SC";
    public int pausetime  = 12;
    public DamageAnimType damageAnimType = DamageAnimType.MEDIUM;
    public DamageAnimHeight damageAnimHeight = DamageAnimHeight.HIGH;
    public boolean fall = false;
    public float ground_velocity = -7f;
    public int ground_slidetime = 10;
    public float air_velocity = -5f;
}
