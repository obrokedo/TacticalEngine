package tactical.game.item;

import java.io.Serializable;

import org.newdawn.slick.Image;

public class Item implements Serializable
{
	private static final long serialVersionUID = 1L;

	protected transient String name = null;
	protected transient int cost;
	protected transient String description;
	protected transient boolean isUsuable;
	protected transient boolean isEquippable;
	protected transient Image image;
	protected transient ItemUse itemUse;
	protected SpellItemUse spellUse;
	protected int itemId;
	protected ItemDurability durability = ItemDurability.PERFECT;
	protected boolean isDeal = false;
	protected boolean useDamagesItem = false;
	protected boolean isDroppable = true;

	public Item(String name, int cost, String description, ItemUse itemUse, SpellItemUse spellUse,
			boolean isEquippable, boolean useDamagesItem, boolean isDeal, boolean isDroppable, int itemId) {
		super();
		this.name = name;
		this.cost = cost;
		this.description = description;
		this.isUsuable = ((itemUse != null) || (spellUse != null));
		this.spellUse = (spellUse != null ? spellUse.copy() : null);
		this.isEquippable = isEquippable;
		this.useDamagesItem = useDamagesItem;
		this.itemUse = itemUse;
		this.itemId = itemId;
		this.durability = ItemDurability.PERFECT;
		this.isDeal = isDeal;
		this.isDroppable = isDroppable;
	}

	public String getName() {
		return name;
	}

	public int getCost() {
		return cost;
	}

	public String getDescription() {
		return description;
	}

	public boolean isUsuable() {
		return isUsuable;
	}

	public boolean isEquippable() {
		return isEquippable;
	}

	public int getItemId() {
		return itemId;
	}

	public ItemUse getItemUse() {
		return itemUse;
	}

	public SpellItemUse getSpellUse() {
		return spellUse;
	}

	public boolean useDamagesItem() {
		return useDamagesItem;
	}

	public static EquippableDifference getEquippableDifference(EquippableItem oldItem, EquippableItem newItem)
	{
		return new EquippableDifference(oldItem, newItem);
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public Image getImage() {
		return image;
	}

	public ItemDurability getDurability() {
		return durability;
	}

	public void setDurability(ItemDurability durability) {
		this.durability = durability;
	}

	public boolean isDeal() {
		return isDeal;
	}

	public void damageItem() {
		switch (durability) {
			case PERFECT:
				durability = ItemDurability.DAMAGED;
				break;
			case DAMAGED:
				durability = ItemDurability.BROKEN;
				break;
			case BROKEN:
				break;
		}
	}

	public void repairItem() {
		durability = ItemDurability.PERFECT;
	}

	public static class EquippableDifference
	{
		public int atk, def, spd, mov;

		public EquippableDifference(EquippableItem oldItem, EquippableItem newItem)
		{
			if (oldItem != null)
			{
				atk = newItem.getAttack() - oldItem.getAttack();
				def = newItem.getDefense() - oldItem.getDefense();
				spd = newItem.getSpeed() - oldItem.getSpeed();
			}
			else
			{
				atk = newItem.getAttack();
				def = newItem.getDefense();
				spd = newItem.getSpeed();
			}
		}
	}

	public enum ItemDurability {
		PERFECT,
		DAMAGED,
		BROKEN
	}

	public void initializeTransientFieldsFromItem(Item item) {
		this.cost = item.cost;
		this.description = item.description;
		this.isEquippable = item.isEquippable;
		this.isUsuable = item.isUsuable;
		this.itemUse = item.itemUse;
		this.name = item.name;
	}
	
	public Item copyItem() {
		return new Item(name, cost, description, isUsuable, 
				isEquippable, image, itemUse,  (spellUse != null ? spellUse.copy() : null), itemId, durability, isDeal, useDamagesItem);
	}

	private Item(String name, int cost, String description, boolean isUsuable, boolean isEquippable, Image image,
			ItemUse itemUse, SpellItemUse spellUse, int itemId, ItemDurability durability, boolean isDeal,
			boolean useDamagesItem) {
		super();
		this.name = name;
		this.cost = cost;
		this.description = description;
		this.isUsuable = isUsuable;
		this.isEquippable = isEquippable;
		this.image = image;
		this.itemUse = itemUse;
		this.spellUse = (spellUse != null ? spellUse.copy() : null);
		this.itemId = itemId;
		this.durability = durability;
		this.isDeal = isDeal;
		this.useDamagesItem = useDamagesItem;
	}
}
