package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Enumeration of available hero classes.
 * Each class defines base statistics used when creating a new hero.
 */
public enum HeroClass {

    //                   hp   atk  def  mag  mana
    WARRIOR(120, 18, 8,  4,  40),
    MAGE(    70,  6, 4, 20,  80),
    ARCHER(  90, 14, 6,  8,  55);

    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;
    private final int baseMagic;
    private final int baseMana;

    HeroClass(int baseHp, int baseAttack, int baseDefense,
              int baseMagic, int baseMana) {
        this.baseHp      = baseHp;
        this.baseAttack  = baseAttack;
        this.baseDefense = baseDefense;
        this.baseMagic   = baseMagic;
        this.baseMana    = baseMana;
    }

    public int getBaseHp()      { return baseHp; }
    public int getBaseAttack()  { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getBaseMagic()   { return baseMagic; }
    public int getBaseMana()    { return baseMana; }

    /** Mana cost to use the Special action for this class. */
    public int getSpecialManaCost() {
        return switch (this) {
            case WARRIOR -> 10;
            case MAGE    -> 20;
            case ARCHER  -> 15;
        };
    }
}