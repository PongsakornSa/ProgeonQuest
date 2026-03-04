package logic.base;

import javafx.beans.property.*;

/**
 * คลาสแม่ของ Player และ Monster ประกอบด้วย stat พื้นฐานทั้งหมด
 * ใช้ JavaFX Property เพื่อให้ UI อัปเดตอัตโนมัติ
 */
public abstract class BaseEntity {

    // ข้อมูลพื้นฐาน
    protected StringProperty name = new SimpleStringProperty();
    protected IntegerProperty level = new SimpleIntegerProperty(1);

    // Stats หลัก (Base stats จากการอัปเลเวล)
    protected IntegerProperty vit = new SimpleIntegerProperty(1);
    protected IntegerProperty str = new SimpleIntegerProperty(1);
    protected IntegerProperty agi = new SimpleIntegerProperty(1);
    protected IntegerProperty intel = new SimpleIntegerProperty(1);

    // Stats ที่คำนวณจาก base stats + equipment
    protected IntegerProperty maxHp = new SimpleIntegerProperty(100);
    protected IntegerProperty currentHp = new SimpleIntegerProperty(100);
    protected IntegerProperty maxMana = new SimpleIntegerProperty(50);
    protected IntegerProperty currentMana = new SimpleIntegerProperty(50);
    protected IntegerProperty physAtk = new SimpleIntegerProperty(10);
    protected IntegerProperty magicAtk = new SimpleIntegerProperty(10);
    protected IntegerProperty physDef = new SimpleIntegerProperty(5);
    protected IntegerProperty magicDef = new SimpleIntegerProperty(5);
    protected IntegerProperty speed = new SimpleIntegerProperty(10);
    protected IntegerProperty critRate = new SimpleIntegerProperty(5); // เปอร์เซ็นต์
    protected IntegerProperty manaRegen = new SimpleIntegerProperty(5); // ฟื้นฟูต่อเทิร์น

    // รูปภาพ path สำหรับโหลดจาก resources
    protected String imagePath;

    /**
     * Constructor พื้นฐาน กำหนดชื่อและ recalculate stats
     */
    public BaseEntity(String name, int level) {
        this.name.set(name);
        this.level.set(level);
    }

    /**
     * คำนวณ stats ทั้งหมดจาก base stats (vit, str, agi, int)
     * เรียกทุกครั้งที่มีการเปลี่ยนแปลง stat
     */
    public void recalculateStats() {
        int v = vit.get();
        int s = str.get();
        int a = agi.get();
        int i = intel.get();

        // vit: +15 hp ต่อ 1 point, +2 physDef
        int baseMaxHp = 100 + (v * 15) + (s * 5);
        maxHp.set(baseMaxHp);

        // str: +3 physAtk, +5 hp
        physAtk.set(10 + (s * 3) + (a * 1));

        // agi: +2 speed, +1 physAtk, +1 critRate
        speed.set(10 + (a * 2));
        critRate.set(5 + (a * 1));

        // int: +10 maxMana, +2 magicAtk, +2 magicDef, +1 manaRegen
        maxMana.set(50 + (i * 10));
        magicAtk.set(10 + (i * 3));
        magicDef.set(5 + (i * 2));
        manaRegen.set(5 + (i * 1));
        physDef.set(5 + (v * 2));

        // ถ้า currentHp เกิน maxHp ให้ปรับลง
        if (currentHp.get() > maxHp.get()) currentHp.set(maxHp.get());
        if (currentMana.get() > maxMana.get()) currentMana.set(maxMana.get());
    }

    /**
     * ลด HP ตามดาเมจที่ได้รับ (หลังหัก defense)
     * @param damage ดาเมจก่อนหัก defense
     * @param isPhysical true = กายภาพ, false = เวทย์
     * @return ดาเมจจริงที่ได้รับ
     */
    public int takeDamage(int damage, boolean isPhysical) {
        int def = isPhysical ? physDef.get() : magicDef.get();
        int actualDamage = Math.max(1, damage - def);
        currentHp.set(Math.max(0, currentHp.get() - actualDamage));
        return actualDamage;
    }

    /**
     * ฟื้นฟู HP
     * @param amount จำนวน HP ที่ฟื้นฟู
     * @return จำนวนที่ฟื้นฟูจริง
     */
    public int heal(int amount) {
        int before = currentHp.get();
        currentHp.set(Math.min(maxHp.get(), before + amount));
        return currentHp.get() - before;
    }

    /**
     * ฟื้นฟู Mana ต่อเทิร์น ตามค่า manaRegen
     */
    public void regenMana() {
        currentMana.set(Math.min(maxMana.get(), currentMana.get() + manaRegen.get()));
    }

    /**
     * ใช้ Mana
     * @param amount จำนวน mana ที่ใช้
     * @return true ถ้า mana พอ
     */
    public boolean useMana(int amount) {
        if (currentMana.get() >= amount) {
            currentMana.set(currentMana.get() - amount);
            return true;
        }
        return false;
    }

    /**
     * ตรวจสอบว่าตายแล้วหรือไม่
     */
    public boolean isDead() {
        return currentHp.get() <= 0;
    }

    // ===== Getters =====
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public int getLevel() { return level.get(); }
    public IntegerProperty levelProperty() { return level; }

    public int getVit() { return vit.get(); }
    public int getStr() { return str.get(); }
    public int getAgi() { return agi.get(); }
    public int getIntel() { return intel.get(); }

    public int getMaxHp() { return maxHp.get(); }
    public int getCurrentHp() { return currentHp.get(); }
    public IntegerProperty currentHpProperty() { return currentHp; }
    public IntegerProperty maxHpProperty() { return maxHp; }

    public int getMaxMana() { return maxMana.get(); }
    public int getCurrentMana() { return currentMana.get(); }
    public IntegerProperty currentManaProperty() { return currentMana; }
    public IntegerProperty maxManaProperty() { return maxMana; }

    public int getPhysAtk() { return physAtk.get(); }
    public int getMagicAtk() { return magicAtk.get(); }
    public int getPhysDef() { return physDef.get(); }
    public int getMagicDef() { return magicDef.get(); }
    public int getSpeed() { return speed.get(); }
    public int getCritRate() { return critRate.get(); }
    public int getManaRegen() { return manaRegen.get(); }
    public String getImagePath() { return imagePath; }

    // ===== Setters =====
    public void setCurrentHp(int hp) { currentHp.set(Math.max(0, Math.min(maxHp.get(), hp))); }
    public void setCurrentMana(int mana) { currentMana.set(Math.max(0, Math.min(maxMana.get(), mana))); }
    public void setVit(int v) { vit.set(v); }
    public void setStr(int s) { str.set(s); }
    public void setAgi(int a) { agi.set(a); }
    public void setIntel(int i) { intel.set(i); }
    public void setImagePath(String path) { imagePath = path; }

    /**
     * สำหรับให้ subclass implement AI หรือ action ของตัวเอง
     */
    public abstract String getEntityType();
}
