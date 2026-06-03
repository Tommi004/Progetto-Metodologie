package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Enumeration of all enemy types in Dungeon Protocol.
 */
public enum EnemyType {

    GOBLIN(    30,  8,  2,  0,  15, "Goblin"),
    SKELETON(  40, 10,  4,  0,  20, "Skeleton"),
    DARK_MAGE( 55,  6,  2, 14,  25, "Dark Mage"),
    TROLL(    100, 14,  6,  0,  60, "Troll"),
    ASSASSIN(  85, 16,  4, 10,  80, "Assassin"),
    DRAGON(   160, 24, 10, 12, 150, "Dragon"),
    KNIGHT(    70, 14,  8,  0, 40, "Dark Knight"),
    WITCH(     60,  8,  2, 17,  40, "Witch"),
    DEMON(     65, 18,  4,  8, 50, "Demon"),
    LEVIATHAN(200, 28, 12, 14, 200, "Leviathan"),
    DEMON_LORD(240, 26, 14, 18, 300, "Demon Lord"),
    DEMON_SOUL(100, 38,  6, 30, 500, "Demon Soul");

    private final int baseHp, baseAttack, baseDefense, baseMagic, xpReward;
    private final String displayName;

    EnemyType(int baseHp, int baseAttack, int baseDefense,
              int baseMagic, int xpReward, String displayName) {
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseMagic = baseMagic;
        this.xpReward = xpReward;
        this.displayName = displayName;
    }

    public int getBaseHp() { return baseHp; }
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getBaseMagic() { return baseMagic; }
    public int getXpReward() { return xpReward; }
    public String getDisplayName() { return displayName; }
}