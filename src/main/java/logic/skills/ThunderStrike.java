package logic.skills;

import logic.base.BaseEntity;
import logic.base.BaseSkill;

/**
 * สกิล "ฟ้าผ่า" - โจมตีด้วยพลังเวทย์ ใช้มานา
 */
public class ThunderStrike extends BaseSkill {

    public ThunderStrike() {
        super("thunder_strike", "ฟ้าผ่า", "เรียกฟ้าผ่าใส่ศัตรู ดาเมจเวทย์สูง",
              "/skills/thunder.png", "magic", 20, 150);
    }

    /**
     * ดาเมจ = magicAtk * 1.5 เป็นเวทย์
     */
    @Override
    public int execute(BaseEntity user, BaseEntity target) {
        if (!user.useMana(manaCost)) return 0;
        int dmg = (int)(user.getMagicAtk() * 1.5);
        return target.takeDamage(dmg, false);
    }

    @Override
    public String getAnimationId() { return "anim_thunder"; }
}
