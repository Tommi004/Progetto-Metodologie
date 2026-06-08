package it.unicam.cs.mpgc.rpg126224.exception;

/**
 * Thrown when an operation references an item by id that does not exist
 * in the hero's inventory.
 *
 * <p>This is an unchecked exception. The UI always selects items from the
 * current inventory list, so a missing id indicates either a stale reference
 * (item already consumed) or a programming error in the controller layer.</p>
 *
 * <h2>Example usage</h2>
 * <pre>{@code
 * Item item = hero.getInventory().stream()
 *         .filter(i -> i.getId().equals(itemId))
 *         .findFirst()
 *         .orElseThrow(() -> new ItemNotFoundException(itemId));
 * }</pre>
 */
public class ItemNotFoundException extends RuntimeException {

    /** The id that was not found in the inventory. */
    private final String itemId;

    /**
     * Constructs a new exception for the given missing item id.
     *
     * @param itemId the id that could not be found in the inventory
     */
    public ItemNotFoundException(String itemId) {
        super("Item not found in inventory: id='" + itemId + "'.");
        this.itemId = itemId;
    }

    /**
     * Returns the item id that triggered the exception.
     *
     * @return the missing item id
     */
    public String getItemId() { return itemId; }
}