package logic.entities;

import logic.base.BaseEntity;
import logic.base.BaseItem;
import logic.base.BaseSkill;
import logic.skills.NormalAttack;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * คลาส Monster ประกอบด้วย AI, drops และ skills ของมอนเตอร์
 */
public class Monster extends BaseEntity {

    private List<BaseItem> dropItems = new ArrayList<>();   // ของที่จะดรอปเมื่อตาย
    private int dropGold = 0;                               // gold ที่จะดรอป
    private int expReward = 0;                              // EXP ที่ผู้เล่นได้รับ
    private List<BaseSkill> skills = new ArrayList<>();     // สกิลของมอน
    private boolean isBoss = false;                         // เป็น boss หรือไม่
    private static final Random random = new Random();

    /**
     * สร้าง Monster ทั่วไป
     */
    public Monster(String name, int level, String imagePath) {
        super(name, level);
        this.imagePath = imagePath;
        // กำหนด stats ตาม level
        setupStatsByLevel(level);
        // ใส่ normal attack เป็น skill พื้นฐาน
        skills.add(new NormalAttack());
        recalculateStats();
        currentHp.set(maxHp.get());
        currentMana.set(maxMana.get());
    }

    /**
     * กำหนด stats พื้นฐานตาม level ของมอนเตอร์
     */
    private void setupStatsByLevel(int lvl) {
        vit.set(2 + lvl);
        str.set(2 + lvl);
        agi.set(1 + lvl);
        intel.set(1 + (lvl / 2));
        dropGold = 10 + (lvl * 5);
        expReward = 20 + (lvl * 10);
    }

    /**
     * สร้าง Boss version ของมอน (stats x2, drop x2)
     */
    public static Monster createBoss(String name, int level, String imagePath) {
        Monster boss = new Monster(name, level, imagePath);
        boss.isBoss = true;
        // Boss stats x2
        boss.vit.set(boss.vit.get() * 2);
        boss.str.set(boss.str.get() * 2);
        boss.agi.set(boss.agi.get() * 2);
        boss.intel.set(boss.intel.get() * 2);
        boss.dropGold *= 2;
        boss.expReward *= 2;
        boss.recalculateStats();
        boss.currentHp.set(boss.maxHp.get());
        return boss;
    }

    /**
     * AI ตัดสินใจเลือกสกิลที่จะใช้ในเทิร์นนี้
     * @return BaseSkill ที่มอนจะใช้
     */
    public BaseSkill chooseAction() {
        // กรองเฉพาะ skill ที่ mana พอ
        List<BaseSkill> usable = new ArrayList<>();
        for (BaseSkill sk : skills) {
            if (sk.canUse(this)) usable.add(sk);
        }
        if (usable.isEmpty()) return skills.get(0); // normal attack เสมอ
        return usable.get(random.nextInt(usable.size()));
    }

    /**
     * เพิ่ม skill ให้มอน
     */
    public void addSkill(BaseSkill skill) { skills.add(skill); }

    /**
     * เพิ่มของดรอป
     */
    public void addDrop(BaseItem item) { dropItems.add(item); }

    @Override
    public String getEntityType() { return isBoss ? "boss" : "monster"; }

    // ===== Getters =====
    public List<BaseItem> getDropItems() { return dropItems; }
    public int getDropGold() { return dropGold; }
    public int getExpReward() { return expReward; }
    public boolean isBoss() { return isBoss; }
}
