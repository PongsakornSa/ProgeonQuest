package logic.skills;

import logic.base.BaseEntity;
import logic.base.BaseSkill;
import java.util.Random;

/**
 * สกิล "โจมตีปกติ" ที่ตัวละครทุกคนมีเป็น default ใน slot 0
 * ไม่ใช้ mana, ดาเมจจาก physAtk ของผู้ใช้
 */
public class NormalAttack extends BaseSkill {

    private static final Random random = new Random();

    public NormalAttack() {
        super("normal_attack", "โจมตีปกติ", "โจมตีกายภาพพื้นฐาน ไม่ใช้มานา",
              "/skills/normal_attack.png", "physical", 0, 1);
    }

    /**
     * ใช้สกิล: ดาเมจ = physAtk * power บวกโบนัส crit
     */
    @Override
    public int execute(BaseEntity user, BaseEntity target) {
        int baseDamage = (int)(user.getPhysAtk() * 1.0);
        // ตรวจสอบ critical hit
        boolean isCrit = random.nextInt(100) < user.getCritRate();
        if (isCrit) baseDamage = (int)(baseDamage * 1.5);
        return target.takeDamage(baseDamage, true);
    }

    @Override
    public String getAnimationId() { return "anim_slash"; }
}
