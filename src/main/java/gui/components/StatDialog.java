package gui.components;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import logic.entities.Player;

/**
 * Dialog สำหรับอัปเกรด Stat ของผู้เล่น
 * แสดง: level, stat point, vit/str/agi/int พร้อมปุ่ม +/-
 * และ stat ที่เปลี่ยนแปลงตาม allocation
 */
public class StatDialog {

    private Stage dialogStage;
    private Player player;
    private Runnable onClose;

    // pending stat allocations (ก่อน confirm)
    private int pendingVit, pendingStr, pendingAgi, pendingIntel;
    private int remainingPoints;

    // Labels สำหรับแสดงผล
    private Label pointsLabel;
    private Label vitLabel, strLabel, agiLabel, intLabel;
    private Label hpLabel, physAtkLabel, speedLabel, critLabel;
    private Label maxManaLabel, magicAtkLabel, magicDefLabel, physDefLabel;

    /**
     * สร้าง StatDialog
     * @param player ผู้เล่น
     * @param onClose callback เมื่อปิด
     */
    public StatDialog(Player player, Runnable onClose) {
        this.player = player;
        this.onClose = onClose;

        // เริ่มต้น pending เท่ากับ stat ปัจจุบัน
        pendingVit = player.getVit();
        pendingStr = player.getStr();
        pendingAgi = player.getAgi();
        pendingIntel = player.getIntel();
        remainingPoints = player.getStatPoint();

        setupDialog();
    }

    /**
     * ตั้งค่าหน้าต่าง dialog
     */
    private void setupDialog() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.setTitle("Stat");

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        VBox mainBox = new VBox(12);
        mainBox.setStyle("-fx-background-color: #f5e6c8; -fx-border-color: #8b6914; " +
                "-fx-border-width: 4; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 20;");
        mainBox.setMinWidth(400);
        mainBox.setEffect(new DropShadow(15, Color.BLACK));

        // Title
        Label title = new Label("📊 อัปเกรด Stat");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #4a2800;");

        // Level + Stat points
        HBox levelRow = new HBox(20);
        levelRow.setAlignment(Pos.CENTER_LEFT);
        Label levelLabel = new Label("Level: " + player.getLevel());
        levelLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2a1800;");
        pointsLabel = new Label("Stat Points: " + remainingPoints);
        pointsLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #c04000;");
        levelRow.getChildren().addAll(levelLabel, pointsLabel);

        new Separator();

        // ===== Stat rows =====
        VBox statRows = new VBox(8);

        // VIT
        vitLabel = new Label();
        HBox vitRow = createStatRow("VIT (HP, PhysDef)", vitLabel,
                () -> adjustStat("vit", 1), () -> adjustStat("vit", -1));

        // STR
        strLabel = new Label();
        HBox strRow = createStatRow("STR (PhysAtk, HP)", strLabel,
                () -> adjustStat("str", 1), () -> adjustStat("str", -1));

        // AGI
        agiLabel = new Label();
        HBox agiRow = createStatRow("AGI (Speed, CritRate)", agiLabel,
                () -> adjustStat("agi", 1), () -> adjustStat("agi", -1));

        // INT
        intLabel = new Label();
        HBox intRow = createStatRow("INT (Mana, MagicAtk)", intLabel,
                () -> adjustStat("int", 1), () -> adjustStat("int", -1));

        statRows.getChildren().addAll(vitRow, strRow, agiRow, intRow);

        // ===== Final stats preview =====
        Label previewTitle = new Label("Stats ปัจจุบัน:");
        previewTitle.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #4a2800;");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(4);

        hpLabel = new Label();
        physAtkLabel = new Label();
        speedLabel = new Label();
        critLabel = new Label();
        maxManaLabel = new Label();
        magicAtkLabel = new Label();
        magicDefLabel = new Label();
        physDefLabel = new Label();

        String labelStyle = "-fx-font-size: 12; -fx-text-fill: #2a1800;";
        Label[] labels = {hpLabel, physAtkLabel, speedLabel, critLabel,
                maxManaLabel, magicAtkLabel, magicDefLabel, physDefLabel};
        for (Label l : labels) l.setStyle(labelStyle);

        statsGrid.add(new Label("Max HP:") {{ setStyle(labelStyle); }}, 0, 0);
        statsGrid.add(hpLabel, 1, 0);
        statsGrid.add(new Label("Phys ATK:") {{ setStyle(labelStyle); }}, 2, 0);
        statsGrid.add(physAtkLabel, 3, 0);
        statsGrid.add(new Label("Speed:") {{ setStyle(labelStyle); }}, 0, 1);
        statsGrid.add(speedLabel, 1, 1);
        statsGrid.add(new Label("Crit %:") {{ setStyle(labelStyle); }}, 2, 1);
        statsGrid.add(critLabel, 3, 1);
        statsGrid.add(new Label("Max Mana:") {{ setStyle(labelStyle); }}, 0, 2);
        statsGrid.add(maxManaLabel, 1, 2);
        statsGrid.add(new Label("Magic ATK:") {{ setStyle(labelStyle); }}, 2, 2);
        statsGrid.add(magicAtkLabel, 3, 2);
        statsGrid.add(new Label("Phys DEF:") {{ setStyle(labelStyle); }}, 0, 3);
        statsGrid.add(physDefLabel, 1, 3);
        statsGrid.add(new Label("Magic DEF:") {{ setStyle(labelStyle); }}, 2, 3);
        statsGrid.add(magicDefLabel, 3, 3);

        // ===== Buttons =====
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button confirmBtn = new Button("✅ Confirm");
        confirmBtn.setStyle("-fx-background-color: #2a6e2a; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-padding: 8 20; -fx-cursor: hand;");
        confirmBtn.setOnAction(e -> confirmStats());

        Button closeBtn = new Button("✖ ปิด");
        closeBtn.setStyle("-fx-background-color: #8b3014; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-padding: 8 20; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> close());

        btnRow.getChildren().addAll(confirmBtn, closeBtn);

        mainBox.getChildren().addAll(title, levelRow, new Separator(), statRows,
                previewTitle, statsGrid, new Separator(), btnRow);
        root.getChildren().add(mainBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);

        updateLabels();
    }

    /**
     * สร้าง row สำหรับ stat หนึ่งค่า (ชื่อ, ค่า, ปุ่ม +/-)
     */
    private HBox createStatRow(String name, Label valueLabel, Runnable onPlus, Runnable onMinus) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #2a1800;");
        nameLabel.setMinWidth(180);

        valueLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #4a2800;");
        valueLabel.setMinWidth(40);

        Button minusBtn = new Button("-");
        minusBtn.setStyle("-fx-background-color: #c04040; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-padding: 2 10; -fx-cursor: hand;");
        minusBtn.setOnAction(e -> { onMinus.run(); updateLabels(); });

        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-background-color: #4080c0; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-padding: 2 10; -fx-cursor: hand;");
        plusBtn.setOnAction(e -> { onPlus.run(); updateLabels(); });

        row.getChildren().addAll(nameLabel, minusBtn, valueLabel, plusBtn);
        return row;
    }

    /**
     * ปรับ stat (pending) +/-
     * @param stat ชื่อ stat
     * @param delta +1 หรือ -1
     */
    private void adjustStat(String stat, int delta) {
        if (delta > 0 && remainingPoints <= 0) return;

        int originalVal = getOriginalStat(stat);
        int currentVal = getPendingStat(stat);
        int newVal = currentVal + delta;

        // ไม่ให้ลดต่ำกว่าค่าเดิมก่อนอัป
        if (newVal < originalVal) return;

        setPendingStat(stat, newVal);
        remainingPoints -= delta;
    }

    /**
     * ดึงค่า stat เดิมของ player (ก่อนอัป)
     */
    private int getOriginalStat(String stat) {
        return switch (stat) {
            case "vit" -> player.getVit();
            case "str" -> player.getStr();
            case "agi" -> player.getAgi();
            case "int" -> player.getIntel();
            default -> 0;
        };
    }

    /**
     * ดึงค่า pending stat
     */
    private int getPendingStat(String stat) {
        return switch (stat) {
            case "vit" -> pendingVit;
            case "str" -> pendingStr;
            case "agi" -> pendingAgi;
            case "int" -> pendingIntel;
            default -> 0;
        };
    }

    /**
     * กำหนดค่า pending stat
     */
    private void setPendingStat(String stat, int val) {
        switch (stat) {
            case "vit" -> pendingVit = val;
            case "str" -> pendingStr = val;
            case "agi" -> pendingAgi = val;
            case "int" -> pendingIntel = val;
        }
    }

    /**
     * อัปเดต labels แสดงค่า stat ทั้งหมด
     * คำนวณ preview stats จาก pending values
     */
    private void updateLabels() {
        pointsLabel.setText("Stat Points: " + remainingPoints);
        vitLabel.setText(String.valueOf(pendingVit));
        strLabel.setText(String.valueOf(pendingStr));
        agiLabel.setText(String.valueOf(pendingAgi));
        intLabel.setText(String.valueOf(pendingIntel));

        // คำนวณ preview
        int previewMaxHp = 100 + (pendingVit * 15) + (pendingStr * 5);
        int previewPhysAtk = 10 + (pendingStr * 3) + (pendingAgi * 1);
        int previewSpeed = 10 + (pendingAgi * 2);
        int previewCrit = 5 + (pendingAgi * 1);
        int previewMaxMana = 50 + (pendingIntel * 10);
        int previewMagicAtk = 10 + (pendingIntel * 3);
        int previewMagicDef = 5 + (pendingIntel * 2);
        int previewPhysDef = 5 + (pendingVit * 2);

        hpLabel.setText(String.valueOf(previewMaxHp));
        physAtkLabel.setText(String.valueOf(previewPhysAtk));
        speedLabel.setText(String.valueOf(previewSpeed));
        critLabel.setText(previewCrit + "%");
        maxManaLabel.setText(String.valueOf(previewMaxMana));
        magicAtkLabel.setText(String.valueOf(previewMagicAtk));
        magicDefLabel.setText(String.valueOf(previewMagicDef));
        physDefLabel.setText(String.valueOf(previewPhysDef));
    }

    /**
     * ยืนยันการอัป stat (apply ค่า pending ลงใน player จริง)
     * หลัง confirm แล้วแก้ไขไม่ได้ แต่ยังอยู่หน้า dialog
     */
    private void confirmStats() {
        player.setVit(pendingVit);
        player.setStr(pendingStr);
        player.setAgi(pendingAgi);
        player.setIntel(pendingIntel);
        player.setStatPoint(remainingPoints);
        player.recalculateStats();

        // อัปเดต label ให้ reflect stat จริง
        updateLabels();

        // แสดงว่า confirm แล้ว
        pointsLabel.setText("Stat Points: " + remainingPoints + " ✅ บันทึกแล้ว");
    }

    /**
     * ปิด dialog
     */
    private void close() {
        dialogStage.close();
        onClose.run();
    }

    /**
     * แสดง dialog
     */
    public void show() {
        dialogStage.sizeToScene();
        dialogStage.centerOnScreen();
        dialogStage.show();
    }
}
