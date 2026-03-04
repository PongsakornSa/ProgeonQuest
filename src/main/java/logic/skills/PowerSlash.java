package logic.skills;

import logic.base.BaseEntity;
import logic.base.BaseSkill;

/**
 * สกิล "ฟันทรงพลัง" - โจมตีกายภาพแรง ใช้มานาน้อย
 */
public class PowerSlash extends BaseSkill {

    public PowerSlash() {
        super("power_slash", "ฟันทรงพลัง", "ฟันด้วยพลังกายภาพสูงกว่าปกติ",
              "/skills/power_slash.png", "physical", 10, 120);
    }

    /**
     * ดาเมจ = physAtk * 1.8
     */
    @Override
    public int execute(BaseEntity user, BaseEntity target) {
        if (!user.useMana(manaCost)) return 0;
        int dmg = (int)(user.getPhysAtk() * 1.8);
        return target.takeDamage(dmg, true);
    }

    @Override
    public String getAnimationId() { return "anim_power_slash"; }
}
