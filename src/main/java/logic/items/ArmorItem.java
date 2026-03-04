package logic.items;

import interfaces.Equippable;
import logic.base.BaseItem;
import logic.entities.Player;

/**
 * ไอเทมชุดเกราะ เพิ่ม physDef, magicDef และ HP
 */
public class ArmorItem extends BaseItem implements Equippable {

    private int physDefBonus;
    private int magicDefBonus;
    private int hpBonus;

    public ArmorItem(String id, String name, String description, String imagePath,
                     int physDefBonus, int magicDefBonus, int hpBonus, int buyPrice) {
        super(id, name, description, imagePath, buyPrice, buyPrice / 2);
        this.physDefBonus = physDefBonus;
        this.magicDefBonus = magicDefBonus;
        this.hpBonus = hpBonus;
    }

    @Override
    public void equip(Player player) { player.equipArmor(this); }

    @Override
    public void unequip(Player player) { player.unequipArmor(); }

    @Override
    public SlotType getSlotType() { return SlotType.ARMOR; }

    @Override
    public String getStatBonusDescription() {
        return "Phys DEF +" + physDefBonus + "\nMagic DEF +" + magicDefBonus + "\nMax HP +" + hpBonus;
    }

    @Override
    public boolean isStackable() { return false; }

    @Override
    public String getItemType() { return "armor"; }

    public int getPhysDefBonus() { return physDefBonus; }
    public int getMagicDefBonus() { return magicDefBonus; }
    public int getHpBonus() { return hpBonus; }
}
