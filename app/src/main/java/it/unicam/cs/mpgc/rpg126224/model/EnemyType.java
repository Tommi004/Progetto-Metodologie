package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Enumeration of all enemy types in Dungeon Protocol.
 */
public enum EnemyType {

    GOBLIN(30, 8, 2, 0, 15, "Goblin"),
    SKELETON(50, 12, 6, 0, 20, "Skeleton"),
    DARK_MAGE(65, 6, 3, 18, 25, "Dark Mage"),
    TROLL(120, 16, 8, 0, 60, "Troll"),
    ASSASSIN(100, 20, 5, 10, 80, "Assassin"),
    DRAGON(200, 28, 12, 15, 150, "Dragon");

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