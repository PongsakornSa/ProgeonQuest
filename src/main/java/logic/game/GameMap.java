package logic.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * สร้างและจัดการแผนที่เขาวงกตแบบสุ่ม
 * ใช้ Recursive Backtracking algorithm สำหรับ maze generation
 */
public class GameMap {

    // ประเภทของแต่ละช่อง
    public enum TileType {
        WALL,       // กำแพง
        FLOOR,      // ทางเดิน
        PLAYER,     // ตำแหน่งผู้เล่น
        MONSTER,    // มอนเตอร์
        BOSS,       // บอส
        CHEST,      // กล่องสมบัติ
        MERCHANT,   // ร้านค้า
        WARP,       // ประตูวาร์ป (หลังจากฆ่าบอส)
        ROOM        // ห้องโถง
    }

    // ธีมของแผนที่
    public enum MapTheme {
        GRASSLAND("/map/themes/grassland/"),
        DUNGEON("/map/themes/dungeon/"),
        CAVE("/map/themes/cave/");

        public final String resourcePath;
        MapTheme(String path) { this.resourcePath = path; }
    }

    private int width;
    private int height;
    private TileType[][] tiles;
    private int playerX, playerY;       // ตำแหน่งผู้เล่น
    private int bossX, bossY;           // ตำแหน่งบอส
    private boolean bossDefeated = false;
    private MapTheme theme;
    private int mapLevel;               // ด่านที่ (1=ทุ่งหญ้า, 2-3=ดันเจี้ยน)
    private static final Random random = new Random();

    // รายการตำแหน่งพิเศษ
    private List<int[]> monsterPositions = new ArrayList<>();
    private List<int[]> chestPositions = new ArrayList<>();
    private List<int[]> merchantPositions = new ArrayList<>();

    /**
     * สร้างแผนที่ใหม่ด้วยขนาดและ level ที่กำหนด
     * @param width ความกว้าง (ควรเป็นเลขคี่)
     * @param height ความสูง (ควรเป็นเลขคี่)
     * @param mapLevel ด่านที่กำหนดธีม
     */
    public GameMap(int width, int height, int mapLevel) {
        // ทำให้เป็นเลขคี่เพื่อ maze generation
        this.width = (width % 2 == 0) ? width + 1 : width;
        this.height = (height % 2 == 0) ? height + 1 : height;
        this.mapLevel = mapLevel;
        this.theme = getThemeByLevel(mapLevel);
        tiles = new TileType[this.height][this.width];
        generateMaze();
        placeSpecialTiles();
    }

    /**
     * กำหนดธีมตามด่าน
     */
    private MapTheme getThemeByLevel(int level) {
        if (level == 1) return MapTheme.GRASSLAND;
        if (level == 2 || level == 3) return MapTheme.DUNGEON;
        return MapTheme.CAVE;
    }

    /**
     * สร้างเขาวงกตด้วย Recursive Backtracking (DFS)
     * เริ่มจากทุกช่องเป็น WALL แล้วค่อยเจาะทาง
     */
    private void generateMaze() {
        // เริ่มต้นทุกช่องเป็น WALL
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                tiles[y][x] = TileType.WALL;

        // เริ่มที่ตำแหน่ง (1,1) และ recurse
        carvePath(1, 1);

        // กำหนดตำแหน่งเริ่มต้นผู้เล่น
        playerX = 1;
        playerY = 1;
        tiles[playerY][playerX] = TileType.PLAYER;
    }

    /**
     * Recursive backtracking สำหรับเจาะทางในเขาวงกต
     */
    private void carvePath(int x, int y) {
        tiles[y][x] = TileType.FLOOR;

        // 4 ทิศทาง (ข้ามช่องเพื่อสร้าง maze pattern)
        int[][] directions = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};
        List<int[]> dirs = new ArrayList<>();
        for (int[] d : directions) dirs.add(d);
        Collections.shuffle(dirs, random);

        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1
                    && tiles[ny][nx] == TileType.WALL) {
                // เจาะกำแพงระหว่างกัน
                tiles[y + dir[1] / 2][x + dir[0] / 2] = TileType.FLOOR;
                carvePath(nx, ny);
            }
        }
    }

    /**
     * วางสิ่งพิเศษบนแผนที่ (มอน, บอส, ร้านค้า, กล่อง)
     */
    private void placeSpecialTiles() {
        List<int[]> floorTiles = new ArrayList<>();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (tiles[y][x] == TileType.FLOOR && !(x == playerX && y == playerY))
                    floorTiles.add(new int[]{x, y});

        Collections.shuffle(floorTiles, random);

        int idx = 0;

        // วาง Boss ที่มุมตรงข้ามผู้เล่น (ไกลที่สุด)
        int[] bossPos = floorTiles.get(floorTiles.size() - 1);
        bossX = bossPos[0]; bossY = bossPos[1];
        tiles[bossY][bossX] = TileType.BOSS;
        idx++;

        // วาง Merchant 1-2 ตัว
        int merchantCount = 1 + random.nextInt(2);
        for (int i = 0; i < merchantCount && idx < floorTiles.size() - 2; i++, idx++) {
            int[] pos = floorTiles.get(idx);
            tiles[pos[1]][pos[0]] = TileType.MERCHANT;
            merchantPositions.add(pos);
        }

        // วาง Chest 2-3 กล่อง
        int chestCount = 2 + random.nextInt(2);
        for (int i = 0; i < chestCount && idx < floorTiles.size() - 2; i++, idx++) {
            int[] pos = floorTiles.get(idx);
            tiles[pos[1]][pos[0]] = TileType.CHEST;
            chestPositions.add(pos);
        }

        // วาง Monster หลายตัว
        int monsterCount = 5 + random.nextInt(5);
        for (int i = 0; i < monsterCount && idx < floorTiles.size() - 2; i++, idx++) {
            int[] pos = floorTiles.get(idx);
            tiles[pos[1]][pos[0]] = TileType.MONSTER;
            monsterPositions.add(pos);
        }
    }

    /**
     * เคลื่อนที่ผู้เล่น คืนค่า tile ที่ชน
     * @param dx ทิศ x (-1, 0, 1)
     * @param dy ทิศ y (-1, 0, 1)
     * @return TileType ของช่องที่จะไป, หรือ WALL ถ้าไปไม่ได้
     */
    public TileType movePlayer(int dx, int dy) {
        int newX = playerX + dx;
        int newY = playerY + dy;

        // ตรวจสอบขอบแผนที่
        if (newX < 0 || newX >= width || newY < 0 || newY >= height) return TileType.WALL;
        if (tiles[newY][newX] == TileType.WALL) return TileType.WALL;

        TileType destination = tiles[newY][newX];

        // อัปเดตตำแหน่งผู้เล่น
        tiles[playerY][playerX] = TileType.FLOOR;
        playerX = newX;
        playerY = newY;
        tiles[playerY][playerX] = TileType.PLAYER;

        return destination;
    }

    /**
     * ลบมอนเตอร์ออกจากตำแหน่ง (หลังสังหาร)
     */
    public void removeMonsterAt(int x, int y) {
        if (tiles[y][x] == TileType.MONSTER) tiles[y][x] = TileType.FLOOR;
        monsterPositions.removeIf(pos -> pos[0] == x && pos[1] == y);
    }

    /**
     * ลบกล่องสมบัติ (หลังเก็บ)
     */
    public void removeChestAt(int x, int y) {
        if (tiles[y][x] == TileType.CHEST) tiles[y][x] = TileType.FLOOR;
        chestPositions.removeIf(pos -> pos[0] == x && pos[1] == y);
    }

    /**
     * บอสตายแล้ว วางประตู warp แทน
     */
    public void defeatBoss() {
        bossDefeated = true;
        tiles[bossY][bossX] = TileType.WARP;
    }

    // ===== Getters =====
    public TileType[][] getTiles() { return tiles; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getBossX() { return bossX; }
    public int getBossY() { return bossY; }
    public MapTheme getTheme() { return theme; }
    public int getMapLevel() { return mapLevel; }
    public boolean isBossDefeated() { return bossDefeated; }
    public TileType getTileAt(int x, int y) { return tiles[y][x]; }
}
