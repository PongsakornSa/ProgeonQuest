package logic.entities;

import logic.base.BaseEntity;
import logic.base.BaseItem;
import logic.base.BaseSkill;
import logic.items.WeaponItem;
import logic.items.ArmorItem;
import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;

/**
 * คลาสตัวละครหลักของผู้เล่น จัดการ inventory, skill slots, stat point
 */
public class Player extends BaseEntity {

    // Stat points สำหรับอัปเกรด stat
    private IntegerProperty statPoint = new SimpleIntegerProperty(5);
    private IntegerProperty expPoint = new SimpleIntegerProperty(0);
    private IntegerProperty expToNextLevel = new SimpleIntegerProperty(100);
    private IntegerProperty gold = new SimpleIntegerProperty(200);

    // Equipment slots
    private WeaponItem equippedWeapon = null;
    private ArmorItem equippedArmor = null;

    // Inventory: max 6 slots (stackable items)
    private List<BaseItem> inventory = new ArrayList<>();
    private static final int MAX_INVENTORY = 6;

    // Skill slots: slot 0 = โจมตีปกติ, slot 1-3 = skills
    private BaseSkill[] skillSlots = new BaseSkill[4];

    // Stat bonuses จาก equipment (บวกแยกไว้)
    private int bonusPhysAtk = 0;
    private int bonusMagicAtk = 0;
    private int bonusPhysDef = 0;
    private int bonusMagicDef = 0;
    private int bonusMaxHp = 0;
    private int bonusMaxMana = 0;
    private int bonusSpeed = 0;

    /**
     * สร้าง Player ใหม่ด้วยชื่อและ level เริ่มต้น
     */
    public Player(String name) {
        super(name, 1);
        imagePath = "/entities/player.png";
        // ตั้งค่า normal attack เป็น slot 0
        skillSlots[0] = createNormalAttack();
        recalculateStats();
        // ฟื้นฟู HP/Mana เต็ม
        currentHp.set(maxHp.get());
        currentMana.set(maxMana.get());
    }

    /**
     * สร้าง skill โจมตีปกติ (ไม่ใช้ mana)
     */
    private BaseSkill createNormalAttack() {
        return new logic.skills.NormalAttack();
    }

    /**
     * คำนวณ stats รวม base + equipment bonus
     */
    @Override
    public void recalculateStats() {
        super.recalculateStats();
        // บวก bonus จาก equipment
        physAtk.set(physAtk.get() + bonusPhysAtk);
        magicAtk.set(magicAtk.get() + bonusMagicAtk);
        physDef.set(physDef.get() + bonusPhysDef);
        magicDef.set(magicDef.get() + bonusMagicDef);
        maxHp.set(maxHp.get() + bonusMaxHp);
        maxMana.set(maxMana.get() + bonusMaxMana);
        speed.set(speed.get() + bonusSpeed);
    }

    /**
     * เพิ่ม EXP และ level up ถ้าถึงเกณฑ์
     * @return true ถ้า level up
     */
    public boolean addExp(int amount) {
        expPoint.set(expPoint.get() + amount);
        if (expPoint.get() >= expToNextLevel.get()) {
            levelUp();
            return true;
        }
        return false;
    }

    /**
     * Level up: เพิ่ม level และ stat point
     */
    private void levelUp() {
        level.set(level.get() + 1);
        expPoint.set(expPoint.get() - expToNextLevel.get());
        expToNextLevel.set((int)(expToNextLevel.get() * 1.5));
        statPoint.set(statPoint.get() + 3); // ได้ 3 stat point ต่อ level
        // ฟื้นฟู HP/Mana เต็มเมื่อ level up
        currentHp.set(maxHp.get());
        currentMana.set(maxMana.get());
    }

    /**
     * ตรวจสอบว่า inventory มีที่ว่างหรือไม่
     */
    public boolean hasInventorySpace() {
        if (inventory.size() < MAX_INVENTORY) return true;
        return false;
    }

    /**
     * เพิ่มไอเทมใน inventory (stack ถ้าเป็นไอเทมเดิม)
     * @return true ถ้าเพิ่มสำเร็จ
     */
    public boolean addItem(BaseItem item) {
        // ตรวจสอบ stackable
        if (item.isStackable()) {
            for (BaseItem existing : inventory) {
                if (existing.getId().equals(item.getId())) {
                    existing.addQuantity(item.getQuantity());
                    return true;
                }
            }
        }
        // เพิ่มใหม่ถ้ายังมีที่
        if (inventory.size() < MAX_INVENTORY) {
            inventory.add(item);
            return true;
        }
        return false;
    }

    /**
     * ลบไอเทมออกจาก inventory
     */
    public void removeItem(BaseItem item) {
        inventory.remove(item);
    }

    /**
     * สวมใส่อาวุธ ถ้ามีอยู่แล้วให้ถอดก่อนแล้วเอาเข้า inventory
     */
    public boolean equipWeapon(WeaponItem weapon) {
        if (equippedWeapon != null) {
            // ถอดอาวุธเดิมกลับ inventory
            if (!hasInventorySpace()) return false;
            inventory.add(equippedWeapon);
            bonusPhysAtk -= equippedWeapon.getAtkBonus();
            bonusMagicAtk -= equippedWeapon.getMagicAtkBonus();
        }
        equippedWeapon = weapon;
        bonusPhysAtk += weapon.getAtkBonus();
        bonusMagicAtk += weapon.getMagicAtkBonus();
        inventory.remove(weapon);
        recalculateStats();
        return true;
    }

    /**
     * สวมใส่ชุดเกราะ ถ้ามีอยู่แล้วให้ถอดก่อน
     */
    public boolean equipArmor(ArmorItem armor) {
        if (equippedArmor != null) {
            if (!hasInventorySpace()) return false;
            inventory.add(equippedArmor);
            bonusPhysDef -= equippedArmor.getPhysDefBonus();
            bonusMagicDef -= equippedArmor.getMagicDefBonus();
            bonusMaxHp -= equippedArmor.getHpBonus();
        }
        equippedArmor = armor;
        bonusPhysDef += armor.getPhysDefBonus();
        bonusMagicDef += armor.getMagicDefBonus();
        bonusMaxHp += armor.getHpBonus();
        inventory.remove(armor);
        recalculateStats();
        return true;
    }

    /**
     * ถอดอาวุธออกคืน inventory
     */
    public boolean unequipWeapon() {
        if (equippedWeapon == null) return false;
        if (!hasInventorySpace()) return false;
        bonusPhysAtk -= equippedWeapon.getAtkBonus();
        bonusMagicAtk -= equippedWeapon.getMagicAtkBonus();
        inventory.add(equippedWeapon);
        equippedWeapon = null;
        recalculateStats();
        return true;
    }

    /**
     * ถอดชุดเกราะออกคืน inventory
     */
    public boolean unequipArmor() {
        if (equippedArmor == null) return false;
        if (!hasInventorySpace()) return false;
        bonusPhysDef -= equippedArmor.getPhysDefBonus();
        bonusMagicDef -= equippedArmor.getMagicDefBonus();
        bonusMaxHp -= equippedArmor.getHpBonus();
        inventory.add(equippedArmor);
        equippedArmor = null;
        recalculateStats();
        return true;
    }

    /**
     * ใส่ skill ในช่อง slot (1-3 เท่านั้น slot 0 คือ normal attack)
     */
    public BaseSkill setSkillSlot(int slot, BaseSkill skill) {
        if (slot < 1 || slot > 3) return null;
        BaseSkill old = skillSlots[slot];
        skillSlots[slot] = skill;
        return old; // คืน skill เดิมถ้ามี
    }

    @Override
    public String getEntityType() { return "player"; }

    // ===== Getters =====
    public int getStatPoint() { return statPoint.get(); }
    public IntegerProperty statPointProperty() { return statPoint; }
    public int getGold() { return gold.get(); }
    public IntegerProperty goldProperty() { return gold; }
    public List<BaseItem> getInventory() { return inventory; }
    public WeaponItem getEquippedWeapon() { return equippedWeapon; }
    public ArmorItem getEquippedArmor() { return equippedArmor; }
    public BaseSkill[] getSkillSlots() { return skillSlots; }
    public int getExpPoint() { return expPoint.get(); }
    public int getExpToNextLevel() { return expToNextLevel.get(); }

    // ===== Setters =====
    public void setStatPoint(int sp) { statPoint.set(sp); }
    public void addGold(int amount) { gold.set(gold.get() + amount); }
    public boolean spendGold(int amount) {
        if (gold.get() >= amount) { gold.set(gold.get() - amount); return true; }
        return false;
    }
}
