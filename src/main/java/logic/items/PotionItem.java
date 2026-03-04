package logic.items;

import interfaces.Consumable;
import logic.base.BaseItem;
import logic.entities.Player;

/**
 * ไอเทม Potion สำหรับฟื้นฟู HP หรือ Mana
 */
public class PotionItem extends BaseItem implements Consumable {

    public enum PotionType { HP, MANA }

    private PotionType type;
    private int restoreAmount;

    /**
     * สร้าง Potion
     * @param type HP หรือ MANA
     * @param restoreAmount จำนวนที่ฟื้นฟู
     */
    public PotionItem(String id, String name, PotionType type, int restoreAmount, int buyPrice) {
        super(id, name,
              type == PotionType.HP ? "ฟื้นฟู HP " + restoreAmount + " จุด" : "ฟื้นฟู Mana " + restoreAmount + " จุด",
              type == PotionType.HP ? "/items/hp_potion.png" : "/items/mana_potion.png",
              buyPrice, buyPrice / 2);
        this.type = type;
        this.restoreAmount = restoreAmount;
    }

    /**
     * ใช้ potion กับผู้เล่น
     */
    @Override
    public String use(Player target) {
        if (type == PotionType.HP) {
            int healed = target.heal(restoreAmount);
            return name + ": ฟื้นฟู HP " + healed + " จุด";
        } else {
            int before = target.getCurrentMana();
            target.setCurrentMana(Math.min(target.getMaxMana(), before + restoreAmount));
            int restored = target.getCurrentMana() - before;
            return name + ": ฟื้นฟู Mana " + restored + " จุด";
        }
    }

    @Override
    public String getEffectDescription() {
        return type == PotionType.HP ? "ฟื้นฟู HP " + restoreAmount : "ฟื้นฟู Mana " + restoreAmount;
    }

    @Override
    public boolean isStackable() { return true; }

    @Override
    public String getItemType() { return "consumable"; }

    public PotionType getPotionType() { return type; }
}
