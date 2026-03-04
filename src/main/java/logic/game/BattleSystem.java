package logic.game;

import logic.base.BaseSkill;
import logic.entities.Monster;
import logic.entities.Player;
import java.util.Random;

/**
 * ระบบการต่อสู้แบบ Turn-Based
 * จัดการลำดับการออกท่า, การคำนวณดาเมจ, และผลลัพธ์ของการต่อสู้
 */
public class BattleSystem {

    public enum BattleResult { PLAYER_WIN, PLAYER_LOSE, ONGOING }
    public enum ActionType { ATTACK, ITEM, SKIP }

    private Player player;
    private Monster monster;
    private int turnCount = 0;
    private static final Random random = new Random();

    /**
     * สร้าง BattleSystem ด้วย player และ monster
     */
    public BattleSystem(Player player, Monster monster) {
        this.player = player;
        this.monster = monster;
    }

    /**
     * BattleAction: เก็บ action ที่ผู้เล่นเลือก
     */
    public static class BattleAction {
        public ActionType type;
        public BaseSkill skill;       // ถ้า type = ATTACK
        public logic.base.BaseItem item; // ถ้า type = ITEM

        public BattleAction(ActionType type, BaseSkill skill) {
            this.type = type; this.skill = skill;
        }
        public BattleAction(ActionType type, logic.base.BaseItem item) {
            this.type = type; this.item = item;
        }
        public BattleAction(ActionType type) { this.type = type; }
    }

    /**
     * ผลลัพธ์ของแต่ละ action ใน turn
     */
    public static class TurnResult {
        public String playerLog;       // log การกระทำของผู้เล่น
        public String monsterLog;      // log การกระทำของมอน
        public int playerDamage;       // ดาเมจที่ผู้เล่นได้รับ
        public int monsterDamage;      // ดาเมจที่มอนได้รับ
        public int playerHeal;         // heal ที่ผู้เล่นได้รับ
        public boolean playerGoesFirst;
        public BattleResult result;
        public String animationId;     // id อนิเมชันที่จะเล่น
    }

    /**
     * ดำเนินการ 1 เทิร์น
     * @param playerAction action ที่ผู้เล่นเลือก
     * @return TurnResult ที่ GUI จะนำไปแสดง
     */
    public TurnResult processTurn(BattleAction playerAction) {
        TurnResult result = new TurnResult();
        turnCount++;

        // ตรวจสอบว่าใครเร็วกว่า
        boolean playerFast = player.getSpeed() >= monster.getSpeed();
        result.playerGoesFirst = playerFast;

        if (playerFast) {
            // ผู้เล่นออกท่าก่อน
            result.playerLog = executePlayerAction(playerAction, result);
            result.monsterDamage = result.playerDamage; // temp
            if (!monster.isDead()) {
                result.monsterLog = executeMonsterAction(result);
            } else {
                result.monsterLog = monster.getName() + " สิ้นใจแล้ว!";
            }
        } else {
            // มอนออกท่าก่อน
            result.monsterLog = executeMonsterAction(result);
            if (!player.isDead()) {
                result.playerLog = executePlayerAction(playerAction, result);
            } else {
                result.playerLog = player.getName() + " ล้มลงแล้ว!";
            }
        }

        // ฟื้นฟู mana ท้ายเทิร์น
        player.regenMana();
        monster.regenMana();

        // ตรวจสอบผลลัพธ์
        if (player.isDead()) result.result = BattleResult.PLAYER_LOSE;
        else if (monster.isDead()) result.result = BattleResult.PLAYER_WIN;
        else result.result = BattleResult.ONGOING;

        return result;
    }

    /**
     * ดำเนินการ action ของผู้เล่น
     */
    private String executePlayerAction(BattleAction action, TurnResult result) {
        switch (action.type) {
            case ATTACK:
                if (action.skill == null) return "ไม่มีสกิล!";
                if (!action.skill.canUse(player)) return "Mana ไม่พอสำหรับ " + action.skill.getName() + "!";
                result.animationId = action.skill.getAnimationId();
                int dmg = action.skill.execute(player, monster);
                result.monsterDamage = dmg;
                return player.getName() + " ใช้ " + action.skill.getName() + "! ทำดาเมจ " + dmg + " จุด";

            case ITEM:
                if (action.item == null) return "ไม่มีไอเทม!";
                if (action.item instanceof interfaces.Trowable) {
                    interfaces.Trowable t = (interfaces.Trowable) action.item;
                    String throwResult = t.throwAt(monster);
                    decreaseItemQuantity(action.item);
                    return throwResult;
                } else if (action.item instanceof interfaces.Consumable) {
                    interfaces.Consumable c = (interfaces.Consumable) action.item;
                    String useResult = c.use(player);
                    result.playerHeal = player.getCurrentHp(); // track heal
                    result.animationId = "anim_heal";
                    decreaseItemQuantity(action.item);
                    return useResult;
                }
                return "ใช้ไอเทมไม่ได้!";

            case SKIP:
                return player.getName() + " ข้ามเทิร์น";

            default: return "";
        }
    }

    /**
     * ดำเนินการ AI ของมอนเตอร์
     */
    private String executeMonsterAction(TurnResult result) {
        BaseSkill skill = monster.chooseAction();
        int dmg = skill.execute(monster, player);
        result.playerDamage = dmg;
        result.animationId = skill.getAnimationId();
        return monster.getName() + " ใช้ " + skill.getName() + "! ทำดาเมจ " + dmg + " จุด";
    }

    /**
     * ลดจำนวนไอเทมและลบออกถ้าหมด
     */
    private void decreaseItemQuantity(logic.base.BaseItem item) {
        boolean remaining = item.decreaseQuantity(1);
        if (!remaining) {
            player.getInventory().remove(item);
        }
    }

    // ===== Getters =====
    public Player getPlayer() { return player; }
    public Monster getMonster() { return monster; }
    public int getTurnCount() { return turnCount; }
}
