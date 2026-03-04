package logic.items;

import interfaces.Equippable;
import logic.base.BaseItem;
import logic.entities.Player;

/**
 * ไอเทมอาวุธ เพิ่ม physAtk และ magicAtk
 */
public class WeaponItem extends BaseItem implements Equippable {

    private int atkBonus;
    private int magicAtkBonus;

    /**
     * สร้างอาวุธ
     */
    public WeaponItem(String id, String name, String description, String imagePath,
                      int atkBonus, int magicAtkBonus, int buyPrice) {
        super(id, name, description, imagePath, buyPrice, buyPrice / 2);
        this.atkBonus = atkBonus;
        this.magicAtkBonus = magicAtkBonus;
    }

    @Override
    public void equip(Player player) { player.equipWeapon(this); }

    @Override
    public void unequip(Player player) { player.unequipWeapon(); }

    @Override
    public SlotType getSlotType() { return SlotType.WEAPON; }

    @Override
    public String getStatBonusDescription() {
        return "ATK +" + atkBonus + (magicAtkBonus > 0 ? "\nMagic ATK +" + magicAtkBonus : "");
    }

    @Override
    public boolean isStackable() { return false; }

    @Override
    public String getItemType() { return "weapon"; }

    public int getAtkBonus() { return atkBonus; }
    public int getMagicAtkBonus() { return magicAtkBonus; }
}
