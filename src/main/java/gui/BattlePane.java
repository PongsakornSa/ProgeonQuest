package gui;

import application.Main;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import logic.base.BaseItem;
import logic.base.BaseSkill;
import logic.entities.Monster;
import logic.entities.Player;
import logic.game.BattleSystem;
import logic.game.ResourceManager;

/**
 * หน้าการต่อสู้แบบ Turn-Based
 * Layout ตรงกับภาพที่ 4 และภาพที่ 6 (Pokemon style)
 * - บนซ้าย: stat มอนเตอร์
 * - บนขวา: รูปมอนเตอร์
 * - ล่างซ้าย: รูปผู้เล่น
 * - ล่างขวา: stat ผู้เล่น
 * - ล่างสุด: action panel
 */
public class BattlePane extends BorderPane {

    private Player player;
    private Monster monster;
    private boolean isBoss;
    private Runnable onBattleEnd;
    private BattleSystem battleSystem;
    private ResourceManager rm;

    // UI elements
    private ImageView playerSprite;
    private ImageView monsterSprite;
    private Label monsterNameLabel, monsterLevelLabel;
    private ProgressBar monsterHpBar, monsterManaBar;
    private Label monsterHpLabel, monsterManaLabel;
    private Label playerNameLabel, playerLevelLabel;
    private ProgressBar playerHpBar, playerManaBar;
    private Label playerHpLabel, playerManaLabel;
    private Label logLabel;
    private VBox actionPanel;
    private HBox mainActionButtons;
    private VBox skillList;
    private VBox itemList;

    // State ของ battle
    private enum BattlePhase { CHOOSE_ACTION, CHOOSE_SKILL, CHOOSE_ITEM, SHOW_LOG, BATTLE_OVER }
    private BattlePhase currentPhase = BattlePhase.CHOOSE_ACTION;
    private BattleSystem.BattleAction pendingAction = null;

    /**
     * สร้าง BattlePane
     * @param onBattleEnd callback เรียกเมื่อการต่อสู้จบ
     */
    public BattlePane(Player player, Monster monster, boolean isBoss, Runnable onBattleEnd) {
        this.player = player;
        this.monster = monster;
        this.isBoss = isBoss;
        this.onBattleEnd = onBattleEnd;
        this.battleSystem = new BattleSystem(player, monster);
        this.rm = ResourceManager.getInstance();

        setPrefSize(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        setStyle("-fx-background-color: #2d5016;"); // พื้นหลัง battle

        setupBattleField();
        setupActionPanel();
        updateAllStats();

        setFocusTraversable(true);

        // เล่นเพลง battle
        rm.playSound("/sounds/battle_bgm.mp3");
    }

    /**
     * สร้าง battlefield: รูปตัวละคร + stat boxes
     * Layout ตรงกับภาพที่ 4 และ 6
     */
    private void setupBattleField() {
        // พื้นที่ battle (70% ของ height)
        BorderPane battleArea = new BorderPane();
        battleArea.setPrefHeight(Main.WINDOW_HEIGHT * 0.65);
        battleArea.setStyle("-fx-background-color: #8fbc8f;"); // สีพื้น battle

        // ===== ส่วนบน: Monster stat (ซ้าย) + Monster sprite (ขวา) =====
        HBox topSection = new HBox();
        topSection.setPrefHeight(battleArea.getPrefHeight() * 0.5);

        // Monster stat box (ด้านซ้ายบน)
        VBox monsterStatBox = createMonsterStatBox();
        monsterStatBox.setPrefWidth(280);
        monsterStatBox.setPadding(new Insets(15));

        // Monster sprite (ด้านขวาบน)
        StackPane monsterArea = new StackPane();
        monsterArea.setPrefWidth(Main.WINDOW_WIDTH - 280.0);
        monsterArea.setAlignment(Pos.BOTTOM_CENTER);

        monsterSprite = new ImageView();
        monsterSprite.setFitWidth(150);
        monsterSprite.setFitHeight(150);
        monsterSprite.setPreserveRatio(true);
        loadEntitySprite(monsterSprite, monster.getImagePath(), 150);
        monsterArea.getChildren().add(monsterSprite);
        // วาง monster sprite บนพื้น
        StackPane.setAlignment(monsterSprite, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(monsterSprite, new Insets(0, 60, 20, 0));

        topSection.getChildren().addAll(monsterStatBox, monsterArea);

        // ===== ส่วนล่าง: Player sprite (ซ้าย) + Player stat (ขวา) =====
        HBox bottomSection = new HBox();
        bottomSection.setPrefHeight(battleArea.getPrefHeight() * 0.5);

        // Player sprite (ด้านซ้ายล่าง) - เล็กกว่า monster
        StackPane playerArea = new StackPane();
        playerArea.setPrefWidth(Main.WINDOW_WIDTH - 300.0);

        playerSprite = new ImageView();
        playerSprite.setFitWidth(110);
        playerSprite.setFitHeight(110);
        playerSprite.setPreserveRatio(true);
        loadEntitySprite(playerSprite, player.getImagePath(), 110);
        playerArea.getChildren().add(playerSprite);
        StackPane.setAlignment(playerSprite, Pos.BOTTOM_LEFT);
        StackPane.setMargin(playerSprite, new Insets(0, 0, 10, 60));

        // Player stat box (ด้านขวาล่าง)
        VBox playerStatBox = createPlayerStatBox();
        playerStatBox.setPrefWidth(300);
        playerStatBox.setPadding(new Insets(15));

        bottomSection.getChildren().addAll(playerArea, playerStatBox);

        battleArea.setTop(topSection);
        battleArea.setBottom(bottomSection);

        setTop(battleArea);
    }

    /**
     * สร้าง stat box ของมอนเตอร์ (บนซ้าย)
     */
    private VBox createMonsterStatBox() {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: rgba(240,230,200,0.9); -fx-border-color: #8b7355; " +
                "-fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 10;");

        // ชื่อและ level
        HBox nameRow = new HBox(10);
        monsterNameLabel = new Label(monster.getName());
        monsterNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        monsterLevelLabel = new Label("Lv." + monster.getLevel());
        monsterLevelLabel.setStyle("-fx-font-size: 14;");
        nameRow.getChildren().addAll(monsterNameLabel, monsterLevelLabel);

        // HP bar
        Label hpText = new Label("HP");
        hpText.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        monsterHpBar = new ProgressBar(1.0);
        monsterHpBar.setPrefWidth(200);
        monsterHpBar.setStyle("-fx-accent: #e04040;");
        monsterHpLabel = new Label();
        monsterHpLabel.setStyle("-fx-font-size: 11;");

        HBox hpRow = new HBox(5, hpText, monsterHpBar, monsterHpLabel);
        hpRow.setAlignment(Pos.CENTER_LEFT);

        // MP bar
        Label mpText = new Label("MP");
        mpText.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        monsterManaBar = new ProgressBar(1.0);
        monsterManaBar.setPrefWidth(200);
        monsterManaBar.setStyle("-fx-accent: #4080e0;");
        monsterManaLabel = new Label();
        monsterManaLabel.setStyle("-fx-font-size: 11;");

        HBox mpRow = new HBox(5, mpText, monsterManaBar, monsterManaLabel);
        mpRow.setAlignment(Pos.CENTER_LEFT);

        // Stats row
        Label statsLabel = new Label(String.format("ATK:%d  DEF:%d  INT:%d  AGI:%d",
                monster.getPhysAtk(), monster.getPhysDef(), monster.getIntel(), monster.getAgi()));
        statsLabel.setStyle("-fx-font-size: 11;");

        box.getChildren().addAll(nameRow, hpRow, mpRow, statsLabel);
        return box;
    }

    /**
     * สร้าง stat box ของผู้เล่น (ล่างขวา)
     */
    private VBox createPlayerStatBox() {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: rgba(240,230,200,0.9); -fx-border-color: #8b7355; " +
                "-fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 10;");

        HBox nameRow = new HBox(10);
        playerNameLabel = new Label(player.getName());
        playerNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        playerLevelLabel = new Label("Lv." + player.getLevel());
        playerLevelLabel.setStyle("-fx-font-size: 14;");
        nameRow.getChildren().addAll(playerNameLabel, playerLevelLabel);

        // HP bar
        Label hpText = new Label("HP");
        hpText.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        playerHpBar = new ProgressBar(1.0);
        playerHpBar.setPrefWidth(200);
        playerHpBar.setStyle("-fx-accent: #40c040;");
        playerHpLabel = new Label();
        playerHpLabel.setStyle("-fx-font-size: 11;");

        HBox hpRow = new HBox(5, hpText, playerHpBar, playerHpLabel);
        hpRow.setAlignment(Pos.CENTER_LEFT);

        // MP bar
        Label mpText = new Label("MP");
        mpText.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        playerManaBar = new ProgressBar(1.0);
        playerManaBar.setPrefWidth(200);
        playerManaBar.setStyle("-fx-accent: #4080e0;");
        playerManaLabel = new Label();
        playerManaLabel.setStyle("-fx-font-size: 11;");

        HBox mpRow = new HBox(5, mpText, playerManaBar, playerManaLabel);
        mpRow.setAlignment(Pos.CENTER_LEFT);

        Label statsLabel = new Label(String.format("ATK:%d  DEF:%d  INT:%d  AGI:%d",
                player.getPhysAtk(), player.getPhysDef(), player.getIntel(), player.getAgi()));
        statsLabel.setStyle("-fx-font-size: 11;");

        box.getChildren().addAll(nameRow, hpRow, mpRow, statsLabel);
        return box;
    }

    /**
     * สร้าง action panel ล่างสุด (คล้าย Pokemon)
     * แบ่งเป็น log text (ซ้าย) + action buttons (ขวา)
     */
    private void setupActionPanel() {
        actionPanel = new VBox();
        actionPanel.setPrefHeight(Main.WINDOW_HEIGHT * 0.35);
        actionPanel.setStyle("-fx-background-color: #e8d5a3; -fx-border-color: #8b7355; -fx-border-width: 3 0 0 0;");

        HBox panelRow = new HBox();
        panelRow.setPrefHeight(actionPanel.getPrefHeight());

        // Log panel (ซ้าย)
        StackPane logPanel = new StackPane();
        logPanel.setPrefWidth(Main.WINDOW_WIDTH * 0.55);
        logPanel.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-border-color: #8b7355; " +
                "-fx-border-width: 0 2 0 0; -fx-padding: 15;");
        logLabel = new Label("เริ่มการต่อสู้!");
        logLabel.setWrapText(true);
        logLabel.setStyle("-fx-font-size: 15; -fx-font-family: 'Arial';");
        logLabel.setMaxWidth(Double.MAX_VALUE);
        logPanel.getChildren().add(logLabel);
        StackPane.setAlignment(logLabel, Pos.TOP_LEFT);

        // Action buttons panel (ขวา)
        StackPane rightPanel = new StackPane();
        rightPanel.setPrefWidth(Main.WINDOW_WIDTH * 0.45);
        rightPanel.setPadding(new Insets(10));

        // Main action buttons: โจมตี, ไอเทม, ไม่ทำอะไร
        mainActionButtons = new HBox(10);
        mainActionButtons.setAlignment(Pos.CENTER);

        Button attackBtn = createActionButton("⚔ โจมตี", "#c04040");
        Button itemBtn = createActionButton("🎒 ไอเทม", "#4080a0");
        Button skipBtn = createActionButton("⏭ ข้ามเทิร์น", "#806040");

        attackBtn.setOnAction(e -> showSkillList());
        itemBtn.setOnAction(e -> showItemList());
        skipBtn.setOnAction(e -> executeAction(new BattleSystem.BattleAction(BattleSystem.ActionType.SKIP)));

        mainActionButtons.getChildren().addAll(attackBtn, itemBtn, skipBtn);

        // Skill list (ซ่อนไว้ก่อน)
        skillList = new VBox(5);
        skillList.setVisible(false);
        skillList.setAlignment(Pos.CENTER_LEFT);

        // Item list (ซ่อนไว้ก่อน)
        itemList = new VBox(5);
        itemList.setVisible(false);
        itemList.setAlignment(Pos.CENTER_LEFT);

        rightPanel.getChildren().addAll(mainActionButtons, skillList, itemList);
        panelRow.getChildren().addAll(logPanel, rightPanel);
        actionPanel.getChildren().add(panelRow);

        // ให้คลิกที่ log เพื่อไปต่อ (เหมือน Pokemon)
        logPanel.setOnMouseClicked(e -> {
            if (currentPhase == BattlePhase.SHOW_LOG) {
                currentPhase = BattlePhase.CHOOSE_ACTION;
                showMainActions();
            }
        });

        setBottom(actionPanel);
    }

    /**
     * แสดง skill list เพื่อเลือก
     */
    private void showSkillList() {
        mainActionButtons.setVisible(false);
        itemList.setVisible(false);
        skillList.setVisible(true);
        skillList.getChildren().clear();

        // ปุ่มย้อนกลับ
        Button backBtn = createSmallButton("← กลับ", "#555");
        backBtn.setOnAction(e -> showMainActions());
        skillList.getChildren().add(backBtn);

        BaseSkill[] slots = player.getSkillSlots();
        for (int i = 0; i < slots.length; i++) {
            BaseSkill sk = slots[i];
            if (sk == null) continue;
            final BaseSkill skill = sk;
            boolean canUse = sk.canUse(player);
            Button skillBtn = createSmallButton(
                    sk.getName() + (sk.getManaCost() > 0 ? " (MP:" + sk.getManaCost() + ")" : ""),
                    canUse ? "#4080a0" : "#888");
            if (!canUse) skillBtn.setDisable(true);
            skillBtn.setOnAction(e -> {
                executeAction(new BattleSystem.BattleAction(BattleSystem.ActionType.ATTACK, skill));
            });
            skillList.getChildren().add(skillBtn);
        }
    }

    /**
     * แสดง item list เพื่อเลือก
     */
    private void showItemList() {
        mainActionButtons.setVisible(false);
        skillList.setVisible(false);
        itemList.setVisible(true);
        itemList.getChildren().clear();

        Button backBtn = createSmallButton("← กลับ", "#555");
        backBtn.setOnAction(e -> showMainActions());
        itemList.getChildren().add(backBtn);

        var inventory = player.getInventory();
        if (inventory.isEmpty()) {
            itemList.getChildren().add(new Label("ไม่มีไอเทม"));
            return;
        }

        for (BaseItem item : inventory) {
            final BaseItem finalItem = item;
            Button itemBtn = createSmallButton(item.getName() + " x" + item.getQuantity(), "#40804f");
            itemBtn.setOnAction(e -> {
                executeAction(new BattleSystem.BattleAction(BattleSystem.ActionType.ITEM, finalItem));
            });
            itemList.getChildren().add(itemBtn);
        }
    }

    /**
     * แสดง main action buttons
     */
    private void showMainActions() {
        skillList.setVisible(false);
        itemList.setVisible(false);
        mainActionButtons.setVisible(true);
    }

    /**
     * ดำเนินการ action ที่ผู้เล่นเลือก
     * ใช้ Thread + Platform.runLater สำหรับ animation
     */
    private void executeAction(BattleSystem.BattleAction action) {
        currentPhase = BattlePhase.SHOW_LOG;
        mainActionButtons.setVisible(false);
        skillList.setVisible(false);
        itemList.setVisible(false);

        // ประมวลผลใน background thread
        Thread battleThread = new Thread(() -> {
            BattleSystem.TurnResult result = battleSystem.processTurn(action);

            Platform.runLater(() -> {
                // แสดง log ผู้เล่น
                if (result.playerGoesFirst) {
                    showLogThenContinue(result.playerLog, () ->
                        playDamageAnimation(monsterSprite, result.monsterDamage > 0, false, () ->
                            showLogThenContinue(result.monsterLog, () ->
                                playDamageAnimation(playerSprite, result.playerDamage > 0, true, () ->
                                    finishTurn(result)))));
                } else {
                    showLogThenContinue(result.monsterLog, () ->
                        playDamageAnimation(playerSprite, result.playerDamage > 0, true, () ->
                            showLogThenContinue(result.playerLog, () ->
                                playDamageAnimation(monsterSprite, result.monsterDamage > 0, false, () ->
                                    finishTurn(result)))));
                }
            });
        });
        battleThread.setDaemon(true);
        battleThread.start();
    }

    /**
     * แสดง log และรอให้คลิกเพื่อไปต่อ (เหมือน Pokemon)
     */
    private void showLogThenContinue(String log, Runnable onContinue) {
        logLabel.setText(log + "\n(คลิกเพื่อไปต่อ)");
        // ใช้ EventHandler ชั่วคราว
        actionPanel.setOnMouseClicked(e -> {
            actionPanel.setOnMouseClicked(null);
            onContinue.run();
        });
    }

    /**
     * เล่นอนิเมชัน damage/heal บน sprite
     * @param sprite sprite ที่จะให้กระพริบ
     * @param isDamage true=กระพริบแดง, false=กระพริบเขียว (heal)
     * @param isPlayer true ถ้าเป็น player sprite
     */
    private void playDamageAnimation(ImageView sprite, boolean isDamage, boolean isPlayer, Runnable onDone) {
        if (!isDamage) { onDone.run(); return; }

        Color flashColor = isPlayer ? Color.RED : Color.RED;
        // สร้าง flash animation
        FadeTransition flash = new FadeTransition(Duration.millis(100), sprite);
        flash.setFromValue(1.0);
        flash.setToValue(0.3);
        flash.setCycleCount(6);
        flash.setAutoReverse(true);
        flash.setOnFinished(e -> {
            sprite.setOpacity(1.0);
            updateAllStats();
            onDone.run();
        });
        flash.play();
    }

    /**
     * จบเทิร์น: อัปเดต stat และตรวจสอบผลลัพธ์การต่อสู้
     */
    private void finishTurn(BattleSystem.TurnResult result) {
        updateAllStats();

        switch (result.result) {
            case PLAYER_WIN -> handlePlayerWin();
            case PLAYER_LOSE -> handlePlayerLose();
            case ONGOING -> {
                currentPhase = BattlePhase.CHOOSE_ACTION;
                logLabel.setText("เทิร์น " + battleSystem.getTurnCount() + " - เลือกการกระทำ");
                showMainActions();
            }
        }
    }

    /**
     * ผู้เล่นชนะ: แจกของดรอปและ EXP
     */
    private void handlePlayerWin() {
        currentPhase = BattlePhase.BATTLE_OVER;
        StringBuilder winLog = new StringBuilder();
        winLog.append(monster.getName()).append(" ถูกสังหารแล้ว!\n");

        // EXP
        int exp = monster.getExpReward();
        boolean levelUp = player.addExp(exp);
        winLog.append("ได้รับ EXP: ").append(exp);
        if (levelUp) winLog.append(" - LEVEL UP! Lv.").append(player.getLevel());

        // Gold
        int gold = monster.getDropGold();
        player.addGold(gold);
        winLog.append("\nได้รับ Gold: ").append(gold);

        // Drop items
        for (BaseItem drop : monster.getDropItems()) {
            boolean added = player.addItem(drop);
            if (added) winLog.append("\nได้รับไอเทม: ").append(drop.getName());
            else winLog.append("\n(กระเป๋าเต็ม ไม่ได้รับ ").append(drop.getName()).append(")");
        }

        logLabel.setText(winLog.toString() + "\n(คลิกเพื่อออก)");
        actionPanel.setOnMouseClicked(e -> {
            actionPanel.setOnMouseClicked(null);
            onBattleEnd.run();
        });
    }

    /**
     * ผู้เล่นตาย: แสดงหน้า Game Over
     */
    private void handlePlayerLose() {
        currentPhase = BattlePhase.BATTLE_OVER;
        logLabel.setText("คุณพ่ายแพ้...\n" + player.getName() + " สิ้นใจแล้ว\n(คลิกเพื่อไปหน้า Game Over)");
        actionPanel.setOnMouseClicked(e -> {
            actionPanel.setOnMouseClicked(null);
            showGameOver();
        });
    }

    /**
     * แสดงหน้า Game Over
     */
    private void showGameOver() {
        StackPane gameOver = new StackPane();
        gameOver.setStyle("-fx-background-color: black;");
        gameOver.setPrefSize(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Text youDied = new Text("YOU DIED");
        youDied.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        youDied.setFill(Color.DARKRED);

        Button returnBtn = new Button("กลับหน้าหลัก");
        returnBtn.setStyle("-fx-background-color: #600; -fx-text-fill: white; " +
                "-fx-font-size: 16; -fx-padding: 10 25; -fx-cursor: hand;");
        returnBtn.setOnAction(e -> Main.showTitleScreen());

        content.getChildren().addAll(youDied, returnBtn);
        gameOver.getChildren().add(content);
        Main.primaryStage.getScene().setRoot(gameOver);
    }

    /**
     * อัปเดต stat bars ทั้งหมด
     */
    private void updateAllStats() {
        // Monster stats
        double monHpRatio = (double) monster.getCurrentHp() / monster.getMaxHp();
        monsterHpBar.setProgress(Math.max(0, monHpRatio));
        monsterHpLabel.setText(monster.getCurrentHp() + "/" + monster.getMaxHp());
        double monMpRatio = (double) monster.getCurrentMana() / monster.getMaxMana();
        monsterManaBar.setProgress(Math.max(0, monMpRatio));
        monsterManaLabel.setText(monster.getCurrentMana() + "/" + monster.getMaxMana());

        // Player stats
        double plHpRatio = (double) player.getCurrentHp() / player.getMaxHp();
        playerHpBar.setProgress(Math.max(0, plHpRatio));
        playerHpLabel.setText(player.getCurrentHp() + "/" + player.getMaxHp());
        double plMpRatio = (double) player.getCurrentMana() / player.getMaxMana();
        playerManaBar.setProgress(Math.max(0, plMpRatio));
        playerManaLabel.setText(player.getCurrentMana() + "/" + player.getMaxMana());
    }

    /**
     * โหลด sprite จาก resources
     */
    private void loadEntitySprite(ImageView iv, String path, double size) {
        try {
            var img = rm.loadImage(path, size, size);
            iv.setImage(img);
        } catch (Exception e) {
            System.out.println("Sprite not found: " + path);
        }
    }

    /**
     * สร้างปุ่ม action หลัก
     */
    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 10 15; " +
                "-fx-background-radius: 5; -fx-cursor: hand;");
        btn.setPrefWidth(130);
        return btn;
    }

    /**
     * สร้างปุ่มขนาดเล็กสำหรับ skill/item list
     */
    private Button createSmallButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-size: 12; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }
}
