package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Enumeration of all enemy types in Level Up!.
 */
public enum EnemyType {

    GOBLIN(    35,  7,  2,  0,  15,   8, "Goblin"),
    SKELETON(  55,  9,  4,  0,  20,  12, "Skeleton"),
    DARK_MAGE( 65,  5,  2,  9,  25,  15, "Dark Mage"),
    TROLL(    150, 10,  8,  0,  60,  40, "Troll"),
    ASSASSIN( 110, 16,  4,  7,  80,  55, "Assassin"),
    DRAGON(   190, 20,  9,  9, 150, 100, "Dragon"),
    KNIGHT(    85, 11,  8,  0,  40,  28, "Dark Knight"),
    WITCH(     75,  7,  2, 11,  40,  28, "Witch"),
    DEMON(     80, 14,  3,  6,  50,  35, "Demon"),
    LEVIATHAN(290, 26, 10, 11, 200, 140, "Leviathan"),
    DEMON_LORD(370, 25, 18, 14, 300, 200, "Demon Lord"),
    DEMON_SOUL(230, 40,  8, 28, 500, 350, "Demon Soul");

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