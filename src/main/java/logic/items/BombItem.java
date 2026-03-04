package logic.items;

import interfaces.Consumable;
import interfaces.Trowable;
import logic.base.BaseEntity;
import logic.base.BaseItem;
import logic.entities.Player;

/**
 * ระเบิด - ขว้างใส่ศัตรูได้ในการต่อสู้ ทำดาเมจกายภาพ
 */
public class BombItem extends BaseItem implements Consumable, Trowable {

    private int damage;

    public BombItem(String id, String name, int damage, int buyPrice) {
        super(id, name, "ระเบิดทำดาเมจกายภาพ " + damage + " จุด",
              "/items/bomb.png", buyPrice, buyPrice / 2);
        this.damage = damage;
    }

    /**
     * ใช้ระเบิดกับเป้าหมาย (wrapper สำหรับ Consumable interface)
     */
    @Override
    public String use(Player target) {
        return "ระเบิดต้องขว้างใส่ศัตรูในการต่อสู้!";
    }

    /**
     * ขว้างระเบิดใส่ศัตรู
     */
    @Override
    public String throwAt(BaseEntity target) {
        int actualDmg = target.takeDamage(damage, true);
        return name + " ระเบิดทำดาเมจ " + actualDmg + " จุด!";
    }

    @Override
    public int getThrowPower() { return damage; }

    @Override
    public String getDamageType() { return "physical"; }

    @Override
    public String getEffectDescription() { return "ดาเมจกายภาพ " + damage + " จุด"; }

    @Override
    public boolean isStackable() { return true; }

    @Override
    public String getItemType() { return "throwable"; }
}
