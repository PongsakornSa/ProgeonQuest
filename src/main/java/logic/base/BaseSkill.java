package logic.base;

import logic.base.BaseEntity;

/**
 * คลาสแม่ของ Skill ทุกประเภท ประกอบด้วยข้อมูลพื้นฐานของสกิล
 */
public abstract class BaseSkill {

    protected String id;             // รหัสสกิล
    protected String name;           // ชื่อสกิล
    protected String description;    // คำอธิบาย
    protected String imagePath;      // path รูปจาก resources
    protected String damageType;     // "physical", "magic", "heal", "buff"
    protected int manaCost;          // mana ที่ใช้
    protected int power;             // พลังฐาน

    /**
     * Constructor พื้นฐาน
     */
    public BaseSkill(String id, String name, String description, String imagePath,
                     String damageType, int manaCost, int power) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.damageType = damageType;
        this.manaCost = manaCost;
        this.power = power;
    }

    /**
     * ใช้สกิล: คำนวณและ apply ผลกระทบ
     * @param user ตัวละครที่ใช้สกิล
     * @param target เป้าหมาย
     * @return จำนวน damage หรือ heal ที่เกิดขึ้น
     */
    public abstract int execute(BaseEntity user, BaseEntity target);

    /**
     * ชื่ออนิเมชันของสกิลนี้ (ใช้โหลด effect จาก resources)
     */
    public abstract String getAnimationId();

    /**
     * ตรวจสอบว่า mana พอใช้สกิลนี้หรือไม่
     */
    public boolean canUse(BaseEntity user) {
        return user.getCurrentMana() >= manaCost;
    }

    // ===== Getters =====
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public String getDamageType() { return damageType; }
    public int getManaCost() { return manaCost; }
    public int getPower() { return power; }
}
