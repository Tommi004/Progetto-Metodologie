package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Enumeration of all enemy types in Level Up!.
 */
public enum EnemyType {

    GOBLIN(    30,  8,  2,  0,  15,   8, "Goblin"),
    SKELETON(  40, 10,  4,  0,  20,  12, "Skeleton"),
    DARK_MAGE( 55,  6,  2, 14,  25,  15, "Dark Mage"),
    TROLL(    100, 14,  6,  0,  60,  40, "Troll"),
    ASSASSIN(  85, 16,  4, 10,  80,  55, "Assassin"),
    DRAGON(   160, 24, 10, 12, 150, 100, "Dragon"),
    KNIGHT(    70, 14,  8,  0,  40,  28, "Dark Knight"),
    WITCH(     60,  8,  2, 17,  40,  28, "Witch"),
    DEMON(     65, 18,  4,  8,  50,  35, "Demon"),
    LEVIATHAN(200, 28, 12, 14, 200, 140, "Leviathan"),
    DEMON_LORD(240, 26, 14, 18, 300, 200, "Demon Lord"),
    DEMON_SOUL(100, 38,  6, 30, 500, 350, "Demon Soul");

    private final int baseHp, baseAttack, baseDefense, baseMagic, xpReward, goldReward;
    private final String displayName;

    EnemyType(int baseHp, int baseAttack, int baseDefense,
              int baseMagic, int xpReward, int goldReward, String displayName) {
        this.baseHp      = baseHp;
        this.baseAttack  = baseAttack;
        this.baseDefense = baseDefense;
        this.baseMagic   = baseMagic;
        this.xpReward    = xpReward;
        this.goldReward  = goldReward;
        this.displayName = displayName;
    }

    public int    getBaseHp()      { return baseHp; }
    public int    getBaseAttack()  { return baseAttack; }
    public int    getBaseDefense() { return baseDefense; }
    public int    getBaseMagic()   { return baseMagic; }
    public int    getXpReward()    { return xpReward; }
    public int    getGoldReward()  { return goldReward; }
    public String getDisplayName() { return displayName; }
}