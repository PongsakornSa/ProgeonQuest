package logic.game;

import logic.entities.Monster;
import logic.entities.Player;
import logic.items.*;
import logic.skills.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * จัดการ state ของเกมทั้งหมด: player, map, current battle
 * เป็น Singleton ที่ GUI ทุกหน้าเข้าถึงร่วมกัน
 */
public class GameState {

    private static GameState instance;

    private Player player;
    private GameMap currentMap;
    private int currentMapLevel = 1;
    private static final int MAX_MAPS = 3;
    private static final Random random = new Random();

    // สกิลทั้งหมดในเกม (pool สำหรับแสดงใน skill dialog)
    private List<logic.base.BaseSkill> allSkills = new ArrayList<>();

    /**
     * Private constructor - singleton
     */
    private GameState() {}

    /**
     * คืนค่า instance เดียวของ GameState
     */
    public static GameState getInstance() {
        if (instance == null) instance = new GameState();
        return instance;
    }

    /**
     * เริ่มเกมใหม่: สร้าง player และ map ด่านแรก
     */
    public void startNewGame() {
        player = new Player("Hero");
        currentMapLevel = 1;
        generateMap(currentMapLevel);
        setupAllSkills();
        // ให้ผู้เล่นเริ่มด้วย HP/Mana potion
        player.addItem(new PotionItem("hp_pot_sm", "HP Potion เล็ก", PotionItem.PotionType.HP, 50, 50));
        player.addItem(new PotionItem("mp_pot_sm", "Mana Potion เล็ก", PotionItem.PotionType.MANA, 30, 40));
    }

    /**
     * สร้างแผนที่ใหม่ตาม level
     */
    public void generateMap(int level) {
        currentMap = new GameMap(21, 15, level);
    }

    /**
     * เลื่อนไปแผนที่ถัดไป (หลังชนะบอส + ผ่านประตู warp)
     * @return true ถ้ายังมีแผนที่ถัดไป
     */
    public boolean goToNextMap() {
        if (currentMapLevel >= MAX_MAPS) return false;
        currentMapLevel++;
        generateMap(currentMapLevel);
        return true;
    }

    /**
     * สร้าง pool สกิลทั้งหมดที่ผู้เล่นสามารถเรียนได้
     */
    private void setupAllSkills() {
        allSkills.clear();
        allSkills.add(new ThunderStrike());
        allSkills.add(new HealSkill());
        allSkills.add(new PowerSlash());
        // สามารถเพิ่ม skill อื่นๆ ได้ที่นี่
    }

    /**
     * สุ่ม Monster ตามด่านปัจจุบัน
     */
    public Monster spawnRandomMonster() {
        String[] names;
        String[] imgPaths;
        int level = currentMapLevel * 2 + random.nextInt(3);

        if (currentMapLevel == 1) {
            names = new String[]{"Slime", "Goblin", "Wolf"};
            imgPaths = new String[]{"/entities/slime.png", "/entities/goblin.png", "/entities/wolf.png"};
        } else {
            names = new String[]{"Skeleton", "Orc", "Dark Knight"};
            imgPaths = new String[]{"/entities/skeleton.png", "/entities/orc.png", "/entities/dark_knight.png"};
        }
        int idx = random.nextInt(names.length);
        Monster m = new Monster(names[idx], level, imgPaths[idx]);
        // เพิ่มดรอป
        if (random.nextInt(100) < 50) {
            m.addDrop(new PotionItem("hp_pot_sm", "HP Potion เล็ก", PotionItem.PotionType.HP, 50, 50));
        }
        return m;
    }

    /**
     * สร้าง Boss ตามด่านปัจจุบัน
     */
    public Monster spawnBoss() {
        String name;
        String img;
        int level = currentMapLevel * 5;

        if (currentMapLevel == 1) { name = "Forest Guardian"; img = "/entities/boss1.png"; }
        else if (currentMapLevel == 2) { name = "Dungeon Lord"; img = "/entities/boss2.png"; }
        else { name = "Shadow Dragon"; img = "/entities/boss3.png"; }

        Monster boss = Monster.createBoss(name, level, img);
        boss.addDrop(new WeaponItem("iron_sword", "Iron Sword", "ดาบเหล็กธรรมดา", "/items/iron_sword.png", 20, 0, 200));
        boss.addDrop(new PotionItem("hp_pot_lg", "HP Potion ใหญ่", PotionItem.PotionType.HP, 150, 150));
        return boss;
    }

    /**
     * สร้างรายการสินค้าร้านค้า (2 lock + 4 random)
     */
    public List<logic.base.BaseItem> generateShopInventory() {
        List<logic.base.BaseItem> shop = new ArrayList<>();
        // 2 ช่องล็อค: HP Potion และ Mana Potion
        shop.add(new PotionItem("hp_pot_sm", "HP Potion เล็ก", PotionItem.PotionType.HP, 50, 50));
        shop.add(new PotionItem("mp_pot_sm", "Mana Potion เล็ก", PotionItem.PotionType.MANA, 30, 40));
        // 4 ช่องสุ่ม
        List<logic.base.BaseItem> pool = new ArrayList<>();
        pool.add(new PotionItem("hp_pot_lg", "HP Potion ใหญ่", PotionItem.PotionType.HP, 150, 120));
        pool.add(new BombItem("bomb_sm", "ระเบิดเล็ก", 80, 60));
        pool.add(new WeaponItem("iron_sword", "Iron Sword", "ดาบเหล็ก", "/items/iron_sword.png", 20, 0, 200));
        pool.add(new ArmorItem("leather_armor", "Leather Armor", "เกราะหนัง", "/items/leather_armor.png", 10, 5, 30, 180));
        pool.add(new BombItem("bomb_lg", "ระเบิดใหญ่", 150, 100));
        java.util.Collections.shuffle(pool, random);
        for (int i = 0; i < 4 && i < pool.size(); i++) shop.add(pool.get(i));
        return shop;
    }

    /**
     * สร้างของในกล่องสมบัติ (สุ่ม)
     */
    public logic.base.BaseItem generateChestItem() {
        int roll = random.nextInt(100);
        if (roll < 40) return new PotionItem("hp_pot_sm", "HP Potion เล็ก", PotionItem.PotionType.HP, 50, 50);
        if (roll < 60) return new PotionItem("mp_pot_sm", "Mana Potion เล็ก", PotionItem.PotionType.MANA, 30, 40);
        if (roll < 75) return new BombItem("bomb_sm", "ระเบิดเล็ก", 80, 60);
        if (roll < 88) return new WeaponItem("iron_sword", "Iron Sword", "ดาบเหล็ก", "/items/iron_sword.png", 20, 0, 200);
        return new ArmorItem("leather_armor", "Leather Armor", "เกราะหนัง", "/items/leather_armor.png", 10, 5, 30, 180);
    }

    // ===== Getters =====
    public Player getPlayer() { return player; }
    public GameMap getCurrentMap() { return currentMap; }
    public int getCurrentMapLevel() { return currentMapLevel; }
    public List<logic.base.BaseSkill> getAllSkills() { return allSkills; }
    public boolean isLastMap() { return currentMapLevel >= MAX_MAPS; }
}
