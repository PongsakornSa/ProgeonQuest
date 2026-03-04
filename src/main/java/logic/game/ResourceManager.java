package logic.game;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

/**
 * จัดการการโหลดและ cache รูปภาพและเสียงจาก resources
 * ป้องกันการโหลดซ้ำโดยใช้ HashMap เป็น cache
 */
public class ResourceManager {

    private static ResourceManager instance;
    private Map<String, Image> imageCache = new HashMap<>();
    private Map<String, AudioClip> soundCache = new HashMap<>();

    private ResourceManager() {}

    /**
     * คืนค่า singleton instance
     */
    public static ResourceManager getInstance() {
        if (instance == null) instance = new ResourceManager();
        return instance;
    }

    /**
     * โหลดรูปภาพจาก path ใน resources (cache ไว้หลังโหลด)
     * @param path เช่น "/entities/player.png"
     * @return Image หรือ placeholder ถ้าไม่พบ
     */
    public Image loadImage(String path) {
        if (path == null) return createPlaceholder();
        if (imageCache.containsKey(path)) return imageCache.get(path);
        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            if (!img.isError()) {
                imageCache.put(path, img);
                return img;
            }
        } catch (Exception e) {
            System.out.println("Image not found: " + path);
        }
        return createPlaceholder();
    }

    /**
     * โหลดรูปภาพพร้อมกำหนดขนาด
     */
    public Image loadImage(String path, double width, double height) {
        String key = path + "_" + (int)width + "x" + (int)height;
        if (imageCache.containsKey(key)) return imageCache.get(key);
        try {
            Image img = new Image(getClass().getResourceAsStream(path), width, height, true, true);
            if (!img.isError()) {
                imageCache.put(key, img);
                return img;
            }
        } catch (Exception e) {
            System.out.println("Image not found: " + path);
        }
        return createPlaceholder(width, height);
    }

    /**
     * โหลดเสียงจาก resources
     * @param path เช่น "/sounds/battle_bgm.mp3"
     */
    public AudioClip loadSound(String path) {
        if (soundCache.containsKey(path)) return soundCache.get(path);
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                AudioClip clip = new AudioClip(url.toString());
                soundCache.put(path, clip);
                return clip;
            }
        } catch (Exception e) {
            System.out.println("Sound not found: " + path);
        }
        return null;
    }

    /**
     * เล่นเสียง
     */
    public void playSound(String path) {
        AudioClip clip = loadSound(path);
        if (clip != null) clip.play();
    }

    /**
     * สร้าง placeholder image สีเทาเมื่อไม่พบรูป
     */
    private Image createPlaceholder() {
        return createPlaceholder(64, 64);
    }

    /**
     * สร้าง placeholder image ด้วยขนาดที่กำหนด
     */
    private Image createPlaceholder(double w, double h) {
        // สร้าง placeholder ด้วย Canvas
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(w, h);
        var gc = canvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        gc.fillRect(0, 0, w, h);
        gc.setFill(javafx.scene.paint.Color.GRAY);
        gc.fillText("?", w/2 - 4, h/2 + 4);
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    /**
     * โหลดรูปธีมแผนที่ตามธีมและประเภท tile
     * @param theme MapTheme
     * @param tileName ชื่อ tile เช่น "floor", "wall"
     */
    public Image loadMapTile(GameMap.MapTheme theme, String tileName) {
        return loadImage(theme.resourcePath + tileName + ".png", 32, 32);
    }

    /**
     * ล้าง cache ทั้งหมด (เรียกตอน restart game)
     */
    public void clearCache() {
        imageCache.clear();
        soundCache.clear();
    }
}
