package logic.skills;

import logic.base.BaseEntity;
import logic.base.BaseSkill;

/**
 * สกิล "รักษา" - ฟื้นฟู HP ตัวเอง ใช้มานา
 */
public class HealSkill extends BaseSkill {

    public HealSkill() {
        super("heal", "รักษา", "ฟื้นฟู HP ของตัวเอง",
              "/skills/heal.png", "heal", 15, 100);
    }

    /**
     * ฟื้นฟู HP = magicAtk + power
     */
    @Override
    public int execute(BaseEntity user, BaseEntity target) {
        if (!user.useMana(manaCost)) return 0;
        int healAmount = user.getMagicAtk() + power;
        return user.heal(healAmount);
    }

    @Override
    public String getAnimationId() { return "anim_heal"; }
}
