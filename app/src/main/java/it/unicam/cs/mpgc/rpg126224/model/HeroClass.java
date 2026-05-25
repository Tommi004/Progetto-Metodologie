package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Enumeration of available hero classes.
 * Each class defines base statistics used when creating a new hero.
 */
public enum HeroClass {

    WARRIOR(120, 18, 8, 4),
    MAGE(70, 6, 4, 20),
    ARCHER(90, 14, 6, 8);

    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;
    private final int baseMagic;

    HeroClass(int baseHp, int baseAttack, int baseDefense, int baseMagic) {
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseMagic = baseMagic;
    }

    public int getBaseHp() { return baseHp; }
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getBaseMagic() { return baseMagic; }
}