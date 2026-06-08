package it.unicam.cs.mpgc.rpg126224.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the player's hero in Level Up!.
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
    private int gold;

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
        this.gold = 0;
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
    public int getGold() { return gold; }

    public void setPosition(int row, int col) { this.row = row; this.col = col; }
    public void setGold(int gold) { this.gold = Math.max(0, gold); }

    /**
     * Adds the given amount to the hero's gold purse.
     *
     * @param amount gold to add; must be positive
     */
    public void addGold(int amount) { if (amount > 0) gold += amount; }

    /**
     * Spends the given amount of gold if the hero has enough.
     *
     * @param amount gold to spend
     * @return {@code true} if the transaction succeeded; {@code false} if
     *         the hero had insufficient gold
     */
    public boolean spendGold(int amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    public List<Item> getInventory() { return List.copyOf(inventory); }
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

    /**
     * Adds an item to the inventory.
     *
     * <p>If the item is stackable (potion) and a stack of the same type
     * already exists, the quantity of the existing stack is incremented
     * instead of adding a new entry.</p>
     *
     * @param item the item to add; must not be null
     */
    public void addItem(Item item) {
        Objects.requireNonNull(item);
        if (item.isStackable()) {
            inventory.stream()
                    .filter(i -> i.getType() == item.getType())
                    .findFirst()
                    .ifPresentOrElse(
                            Item::incrementQuantity,
                            () -> inventory.add(item)
                    );
        } else {
            inventory.add(item);
        }
    }

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