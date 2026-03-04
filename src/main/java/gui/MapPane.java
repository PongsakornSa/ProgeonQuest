package gui;

import application.Main;
import gui.components.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import logic.base.BaseItem;
import logic.entities.Monster;
import logic.game.*;

/**
 * หน้าแผนที่เขาวงกต
 * แสดงแผนที่, ปุ่มเมนูตามขอบ, รับ input WASD
 * Layout ตรงกับภาพที่ 2
 */
public class MapPane extends BorderPane {

    private static final int TILE_SIZE = 32;
    private Canvas mapCanvas;
    private GraphicsContext gc;
    private GameState gameState;
    private ResourceManager rm;

    // ป้องกันการกดปุ่มซ้อนกัน
    private boolean dialogOpen = false;
    // สำหรับ animation เดิน
    private boolean isMoving = false;

    // Label แสดง info ผู้เล่น
    private Label playerInfoLabel;
    private Label mapInfoLabel;

    /**
     * สร้าง MapPane: ตั้งค่า layout, canvas, buttons, keyboard
     */
    public MapPane() {
        gameState = GameState.getInstance();
        rm = ResourceManager.getInstance();
        setupTopBar();
        setupMapCanvas();
        setupBottomBar();
        setupKeyboardInput();
        drawMap();
        setFocusTraversable(true);
    }

    /**
     * สร้าง top bar: ปุ่ม stat, skill, inventory, return to title
     * Layout ตรงกับภาพที่ 2 - ปุ่มอยู่แถวบน
     */
    private void setupTopBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(8));
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        Button statBtn = createMenuButton("Stat");
        Button skillBtn = createMenuButton("Skill");
        Button inventoryBtn = createMenuButton("Inventory");
        Button returnBtn = createMenuButton("Return to Title");

        // กด Stat -> เปิด StatDialog
        statBtn.setOnAction(e -> {
            if (!dialogOpen) {
                dialogOpen = true;
                StatDialog dialog = new StatDialog(gameState.getPlayer(), () -> dialogOpen = false);
                dialog.show();
            }
        });

        // กด Skill -> เปิด SkillDialog
        skillBtn.setOnAction(e -> {
            if (!dialogOpen) {
                dialogOpen = true;
                SkillDialog dialog = new SkillDialog(gameState.getPlayer(),
                        gameState.getAllSkills(), () -> dialogOpen = false);
                dialog.show();
            }
        });

        // กด Inventory -> เปิด InventoryDialog
        inventoryBtn.setOnAction(e -> {
            if (!dialogOpen) {
                dialogOpen = true;
                InventoryDialog dialog = new InventoryDialog(gameState.getPlayer(), () -> dialogOpen = false);
                dialog.show();
            }
        });

        // กด Return to Title -> ขึ้น confirm dialog
        returnBtn.setOnAction(e -> {
            if (!dialogOpen) {
                dialogOpen = true;
                showReturnConfirm();
            }
        });

        topBar.getChildren().addAll(statBtn, skillBtn, inventoryBtn, returnBtn);
        setTop(topBar);
    }

    /**
     * สร้าง Canvas สำหรับวาดแผนที่
     */
    private void setupMapCanvas() {
        GameMap map = gameState.getCurrentMap();
        int canvasW = map.getWidth() * TILE_SIZE;
        int canvasH = map.getHeight() * TILE_SIZE;

        // ScrollPane สำหรับแผนที่ใหญ่กว่าหน้าต่าง
        mapCanvas = new Canvas(canvasW, canvasH);
        gc = mapCanvas.getGraphicsContext2D();

        ScrollPane scrollPane = new ScrollPane(mapCanvas);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: #111;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(false);

        setCenter(scrollPane);
    }

    /**
     * สร้าง bottom bar: แสดงข้อมูล player และ map
     */
    private void setupBottomBar() {
        HBox bottomBar = new HBox(20);
        bottomBar.setPadding(new Insets(5, 10, 5, 10));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setStyle("-fx-background-color: rgba(0,0,0,0.8);");

        playerInfoLabel = new Label();
        playerInfoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12;");
        mapInfoLabel = new Label();
        mapInfoLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 12;");

        Label controlHint = new Label("ควบคุม: WASD เพื่อเดิน");
        controlHint.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11;");

        updatePlayerInfo();

        bottomBar.getChildren().addAll(playerInfoLabel, mapInfoLabel, controlHint);
        setBottom(bottomBar);
    }

    /**
     * อัปเดต label ข้อมูล player
     */
    private void updatePlayerInfo() {
        var p = gameState.getPlayer();
        playerInfoLabel.setText(String.format("Lv.%d %s  HP: %d/%d  MP: %d/%d  Gold: %d",
                p.getLevel(), p.getName(), p.getCurrentHp(), p.getMaxHp(),
                p.getCurrentMana(), p.getMaxMana(), p.getGold()));
        mapInfoLabel.setText("Map " + gameState.getCurrentMapLevel() + "/3");
    }

    /**
     * ตั้งค่า keyboard input สำหรับ WASD
     * ไม่รับ input ถ้า dialog เปิดอยู่
     */
    private void setupKeyboardInput() {
        setOnKeyPressed(event -> {
            if (dialogOpen || isMoving) return;

            int dx = 0, dy = 0;
            KeyCode key = event.getCode();

            if (key == KeyCode.W || key == KeyCode.UP) dy = -1;
            else if (key == KeyCode.S || key == KeyCode.DOWN) dy = 1;
            else if (key == KeyCode.A || key == KeyCode.LEFT) dx = -1;
            else if (key == KeyCode.D || key == KeyCode.RIGHT) dx = 1;
            else return;

            movePlayer(dx, dy);
        });
    }

    /**
     * เคลื่อนที่ผู้เล่นและจัดการ collision
     * ใช้ Thread เพื่อป้องกัน UI freeze
     */
    private void movePlayer(int dx, int dy) {
        isMoving = true;
        GameMap map = gameState.getCurrentMap();
        int oldX = map.getPlayerX();
        int oldY = map.getPlayerY();

        GameMap.TileType tileHit = map.movePlayer(dx, dy);

        // อัปเดต UI หลังเคลื่อนที่
        Platform.runLater(() -> {
            drawMap();
            updatePlayerInfo();
            scrollToPlayer();
        });

        // จัดการ tile ที่ชน
        switch (tileHit) {
            case MONSTER -> handleMonsterEncounter(false, map.getPlayerX(), map.getPlayerY());
            case BOSS -> handleMonsterEncounter(true, map.getBossX(), map.getBossY());
            case CHEST -> handleChestPickup(map.getPlayerX(), map.getPlayerY());
            case MERCHANT -> handleMerchant();
            case WARP -> handleWarp();
            default -> isMoving = false;
        }
    }

    /**
     * จัดการการเจอมอนเตอร์ -> เข้าสู่ BattlePane
     */
    private void handleMonsterEncounter(boolean isBoss, int mx, int my) {
        dialogOpen = true;
        // สร้างมอนเตอร์
        Monster monster = isBoss ? gameState.spawnBoss() : gameState.spawnRandomMonster();

        // ลบตำแหน่งมอนออกจากแผนที่ก่อนเข้าต่อสู้
        GameMap map = gameState.getCurrentMap();

        Platform.runLater(() -> {
            BattlePane battlePane = new BattlePane(gameState.getPlayer(), monster, isBoss, () -> {
                // callback หลังจบการต่อสู้
                if (monster.isDead()) {
                    if (isBoss) map.defeatBoss();
                    else map.removeMonsterAt(mx, my);
                }
                // กลับมาที่ MapPane
                Main.primaryStage.getScene().setRoot(MapPane.this);
                MapPane.this.requestFocus();
                drawMap();
                updatePlayerInfo();
                dialogOpen = false;
                isMoving = false;
            });
            Main.primaryStage.getScene().setRoot(battlePane);
            battlePane.requestFocus();
        });
    }

    /**
     * จัดการการเก็บกล่องสมบัติ
     */
    private void handleChestPickup(int x, int y) {
        dialogOpen = true;
        GameMap map = gameState.getCurrentMap();
        BaseItem item = gameState.generateChestItem();
        boolean added = gameState.getPlayer().addItem(item);

        Platform.runLater(() -> {
            if (added) {
                map.removeChestAt(x, y);
                showGamePopup("ได้รับไอเทม!", "คุณได้รับ: " + item.getName(), () -> {
                    dialogOpen = false;
                    isMoving = false;
                    drawMap();
                });
            } else {
                // กระเป๋าเต็ม - ให้เลือกทิ้งหรือไม่เอา
                showInventoryFullDialog(item, () -> {
                    map.removeChestAt(x, y);
                    drawMap();
                }, () -> {
                    dialogOpen = false;
                    isMoving = false;
                });
            }
        });
    }

    /**
     * จัดการการเข้าร้านค้า
     */
    private void handleMerchant() {
        dialogOpen = true;
        Platform.runLater(() -> {
            MerchantPane merchantPane = new MerchantPane(gameState.getPlayer(),
                    gameState.generateShopInventory(), () -> {
                        Main.primaryStage.getScene().setRoot(MapPane.this);
                        MapPane.this.requestFocus();
                        updatePlayerInfo();
                        dialogOpen = false;
                        isMoving = false;
                    });
            Main.primaryStage.getScene().setRoot(merchantPane);
        });
    }

    /**
     * จัดการประตู Warp (ไปแผนที่ถัดไป)
     */
    private void handleWarp() {
        dialogOpen = true;
        Platform.runLater(() -> {
            boolean hasNext = gameState.goToNextMap();
            if (hasNext) {
                showGamePopup("ไปต่อ!", "คุณเข้าสู่ด่านที่ " + gameState.getCurrentMapLevel() + "!", () -> {
                    // สร้าง MapPane ใหม่สำหรับแผนที่ถัดไป
                    MapPane newMap = new MapPane();
                    Main.primaryStage.getScene().setRoot(newMap);
                    newMap.requestFocus();
                });
            } else {
                showGamePopup("จบเกม!", "ยินดีด้วย! คุณผ่านเกมแล้ว!", () -> {
                    Main.showTitleScreen();
                });
            }
        });
    }

    /**
     * วาดแผนที่บน Canvas
     * แต่ละ tile วาดด้วยสีหรือรูปจาก resources
     */
    private void drawMap() {
        GameMap map = gameState.getCurrentMap();
        GameMap.TileType[][] tiles = map.getTiles();
        GameMap.MapTheme theme = map.getTheme();

        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                double px = x * TILE_SIZE;
                double py = y * TILE_SIZE;
                GameMap.TileType tile = tiles[y][x];

                // พยายามโหลดรูปจาก resources ก่อน
                drawTile(gc, tile, theme, px, py);
            }
        }
    }

    /**
     * วาด tile เดียว: ลอง load รูปจาก resources ถ้าไม่มีใช้สีแทน
     */
    private void drawTile(GraphicsContext gc, GameMap.TileType tile,
                          GameMap.MapTheme theme, double px, double py) {
        Color color;
        String tileImg = null;

        switch (tile) {
            case WALL -> { color = Color.web("#333"); tileImg = theme.resourcePath + "wall.png"; }
            case FLOOR, ROOM -> { color = Color.web("#666"); tileImg = theme.resourcePath + "floor.png"; }
            case PLAYER -> { color = Color.DODGERBLUE; tileImg = "/entities/player.png"; }
            case MONSTER -> { color = Color.RED; tileImg = "/entities/monster_marker.png"; }
            case BOSS -> { color = Color.DARKRED; tileImg = "/entities/boss_marker.png"; }
            case CHEST -> { color = Color.GOLD; tileImg = "/items/chest.png"; }
            case MERCHANT -> { color = Color.GREEN; tileImg = "/entities/merchant.png"; }
            case WARP -> { color = Color.PURPLE; tileImg = "/ui/warp.png"; }
            default -> { color = Color.BLACK; }
        }

        // ลองโหลดรูป
        if (tileImg != null) {
            try {
                var img = rm.loadImage(tileImg, TILE_SIZE, TILE_SIZE);
                gc.drawImage(img, px, py, TILE_SIZE, TILE_SIZE);
                return;
            } catch (Exception e) { /* ใช้สีแทน */ }
        }

        // fallback: วาดสี
        gc.setFill(color);
        gc.fillRect(px, py, TILE_SIZE, TILE_SIZE);

        // วาดตัวอักษรแทนไอคอน
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(10));
        String symbol = switch (tile) {
            case PLAYER -> "P";
            case MONSTER -> "M";
            case BOSS -> "B!";
            case CHEST -> "C";
            case MERCHANT -> "S";
            case WARP -> "W";
            default -> "";
        };
        if (!symbol.isEmpty()) gc.fillText(symbol, px + 8, py + 20);
    }

    /**
     * Scroll canvas ให้ผู้เล่นอยู่กลางหน้าจอ
     */
    private void scrollToPlayer() {
        GameMap map = gameState.getCurrentMap();
        // คำนวณ scroll position
        double scrollX = (map.getPlayerX() * TILE_SIZE - Main.WINDOW_WIDTH / 2.0) / mapCanvas.getWidth();
        double scrollY = (map.getPlayerY() * TILE_SIZE - Main.WINDOW_HEIGHT / 2.0) / mapCanvas.getHeight();
        // จำกัดค่า 0-1
        scrollX = Math.max(0, Math.min(1, scrollX));
        scrollY = Math.max(0, Math.min(1, scrollY));
    }

    /**
     * สร้างปุ่ม menu style
     */
    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: white; " +
                "-fx-border-width: 1.5; -fx-text-fill: white; -fx-font-size: 13; -fx-cursor: hand;" +
                "-fx-padding: 5 12;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.15); " +
                "-fx-border-color: gold; -fx-border-width: 1.5; -fx-text-fill: gold; " +
                "-fx-font-size: 13; -fx-cursor: hand; -fx-padding: 5 12;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; " +
                "-fx-border-color: white; -fx-border-width: 1.5; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-cursor: hand; -fx-padding: 5 12;"));
        return btn;
    }

    /**
     * แสดง popup ยืนยันการกลับ title
     */
    private void showReturnConfirm() {
        GamePopup popup = new GamePopup(Main.primaryStage,
                "Return to Title",
                "ต้องการกลับไปหน้าหลัก?\nความคืบหน้าจะไม่ถูกบันทึก");
        popup.addButton("ยืนยัน", () -> {
            popup.close();
            Main.showTitleScreen();
        });
        popup.addButton("ยกเลิก", () -> {
            popup.close();
            dialogOpen = false;
            isMoving = false;
            MapPane.this.requestFocus();
        });
        popup.show();
    }

    /**
     * แสดง popup ทั่วไปพร้อม callback
     */
    private void showGamePopup(String title, String message, Runnable onClose) {
        GamePopup popup = new GamePopup(Main.primaryStage, title, message);
        popup.addButton("ตกลง", () -> {
            popup.close();
            onClose.run();
        });
        popup.show();
    }

    /**
     * แสดง popup เมื่อกระเป๋าเต็มตอนเก็บของ
     */
    private void showInventoryFullDialog(BaseItem item, Runnable onReplace, Runnable onCancel) {
        GamePopup popup = new GamePopup(Main.primaryStage,
                "กระเป๋าเต็ม!",
                "ไม่สามารถเก็บ " + item.getName() + " ได้\nกระเป๋าของคุณเต็มแล้ว");
        popup.addButton("ไม่เอา", () -> {
            popup.close();
            dialogOpen = false;
            isMoving = false;
            MapPane.this.requestFocus();
        });
        popup.show();
    }
}
