package it.unicam.cs.mpgc.rpg126224.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the player's hero in Dungeon Protocol.
 */
public class Hero implements GameEntity {

    private final String id;
    private final String name;
    private final HeroClass heroClass;

    private int currentHp;
    private int maxHp;
    private int attack;
    private int defense;
    private int magic;
    private int currentMana;
    private int maxMana;
    private int level;
    private int experience;
    private int row;
    private int col;

    private final List<Item> inventory;

    private static final int BASE_XP_PER_LEVEL = 30;

    public Hero(String id, String name, HeroClass heroClass) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.heroClass = Objects.requireNonNull(heroClass, "heroClass cannot be null");
        this.maxHp = heroClass.getBaseHp();
        this.currentHp = maxHp;
        this.attack = heroClass.getBaseAttack();
        this.defense = heroClass.getBaseDefense();
        this.magic = heroClass.getBaseMagic();
        this.maxMana = heroClass.getBaseMana();
        this.currentMana = maxMana;
        this.level = 1;
        this.experience = 0;
        this.row = 0;
        this.col = 0;
        this.inventory = new ArrayList<>();
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public int getCurrentHp() { return currentHp; }
    @Override public int getMaxHp() { return maxHp; }
    @Override public int getAttack() { return attack; }
    @Override public int getDefense() { return defense; }

    public int getMagic() { return magic; }
    public int getCurrentMana() { return currentMana; }
    public int getMaxMana() { return maxMana; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public HeroClass getHeroClass() { return heroClass; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public List<Item> getInventory() { return List.copyOf(inventory); }

    public void setPosition(int row, int col) { this.row = row; this.col = col; }
    public void setCurrentHp(int hp) { this.currentHp = Math.max(0, Math.min(maxHp, hp)); }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public void setLevel(int level) { this.level = level; }
    public void setExperience(int xp) { this.experience = xp; }
    public void boostAttack(int amount)  { this.attack += amount; }
    public void boostDefense(int amount) { this.defense += amount; }
    public void boostMagic(int amount)   { this.magic += amount; }

    public void setCurrentMana(int mana) {
        this.currentMana = Math.max(0, Math.min(maxMana, mana));
    }

    public void setMaxMana(int maxMana) { this.maxMana = maxMana; }

    /**
     * Consumes {@code cost} mana. Returns true if successful, false if not enough mana.
     *
     * @param cost mana points to consume (must be positive)
     * @return true if mana was available and consumed
     */
    public boolean useMana(int cost) {
        if (currentMana < cost) return false;
        currentMana -= cost;
        return true;
    }

    /**
     * Restores mana without exceeding maxMana.
     *
     * @param amount mana points to restore
     */
    public void restoreMana(int amount) {
        currentMana = Math.min(maxMana, currentMana + amount);
    }

    /**
     * Permanently increases the maximum mana pool and restores the same amount.
     *
     * @param amount points to add to maxMana
     */
    public void boostMaxMana(int amount) {
        maxMana += amount;
        restoreMana(amount);
    }

    @Override
    public void takeDamage(int damage) {
        int effective = Math.max(1, damage - defense / 2);
        currentHp = Math.max(0, currentHp - effective);
    }

    @Override
    public void heal(int amount) { currentHp = Math.min(maxHp, currentHp + amount); }

    public void addItem(Item item) { inventory.add(Objects.requireNonNull(item)); }

    public boolean removeItem(String itemId) {
        return inventory.removeIf(i -> i.getId().equals(itemId));
    }

    public void gainExperience(int xp) {
        experience += xp;
        while (experience >= xpForNextLevel()) {
            experience -= xpForNextLevel();
            levelUp();
        }
    }

    public int xpForNextLevel() { return BASE_XP_PER_LEVEL * level; }

    private void levelUp() {
        level++;
        maxHp += 15;
        currentHp = maxHp;
        attack += 3;
        defense += 2;
        magic += 2;
        maxMana += 10;
        currentMana = maxMana;   
    }

    @Override
    public String toString() {
        return name + " [" + heroClass + " Lv." + level + "] HP:" + currentHp + "/" + maxHp;
    }
}