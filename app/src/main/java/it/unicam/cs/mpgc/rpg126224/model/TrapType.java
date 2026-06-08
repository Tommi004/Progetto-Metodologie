package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Enumeration of all trap types in Level Up!
 * Each trap has a display name, HP damage, MP damage,
 * and an optional ATK debuff applied to the next combat.
 */
public enum TrapType {

    //                        name              hpDmg  mpDmg  atkDebuff  emoji
    SPIKE_TRAP(   "Spike Trap",    15,    0,   false,  "🗡"),
    VENOM_POOL(   "Venom Pool",     8,   20,   false,  "☠"),
    HEX_MARK(     "Hex Mark",       0,    0,   true,   "🔮"),
    LIFE_SIPHON(  "Life Siphon",   25,   30,   false,  "💀"),
    BRIMSTONE_PIT("Brimstone Pit", 40,    0,   false,  "🔥");

    private final String  displayName;
    private final int     hpDamage;
    private final int     mpDamage;
    private final boolean atkDebuff;
    private final String  emoji;

    TrapType(String displayName, int hpDamage, int mpDamage,
             boolean atkDebuff, String emoji) {
        this.displayName = displayName;
        this.hpDamage    = hpDamage;
        this.mpDamage    = mpDamage;
        this.atkDebuff   = atkDebuff;
        this.emoji       = emoji;
    }

    public String  getDisplayName() { return displayName; }
    public int     getHpDamage()    { return hpDamage; }
    public int     getMpDamage()    { return mpDamage; }
    public boolean hasAtkDebuff()   { return atkDebuff; }
    public String  getEmoji()       { return emoji; }

    /** Returns a human-readable description of the trap's effect. */
    public String getEffectDescription() {
        if (atkDebuff) return "ATK halved for the next combat";
        StringBuilder sb = new StringBuilder();
        if (hpDamage > 0) sb.append("-").append(hpDamage).append(" HP");
        if (mpDamage > 0) {
            if (sb.length() > 0) sb.append("  ");
            sb.append("-").append(mpDamage).append(" MP");
        }
        return sb.toString();
    }
}