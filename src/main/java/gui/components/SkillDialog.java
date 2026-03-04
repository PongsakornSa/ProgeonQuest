package gui.components;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import logic.base.BaseSkill;
import logic.entities.Player;
import logic.game.ResourceManager;
import java.util.List;

/**
 * Dialog แสดงและจัดการ Skill ของผู้เล่น
 * แบ่งเป็น 2 ฝั่ง:
 * - ซ้าย: skill slots ของตัวละคร (4 ช่อง)
 * - ขวา: list skill ทั้งหมด
 */
public class SkillDialog {

    private Stage dialogStage;
    private Player player;
    private List<BaseSkill> allSkills;
    private Runnable onClose;
    private ResourceManager rm;

    private VBox leftSlots;       // skill slots ซ้าย
    private VBox rightSkillList;  // list skill ขวา

    // state สำหรับการ equip
    private BaseSkill selectedSkill = null;  // skill ที่กดเลือกจะ equip
    private boolean waitingForSlot = false;  // รอให้กดช่อง slot

    /**
     * สร้าง SkillDialog
     */
    public SkillDialog(Player player, List<BaseSkill> allSkills, Runnable onClose) {
        this.player = player;
        this.allSkills = allSkills;
        this.onClose = onClose;
        this.rm = ResourceManager.getInstance();
        setupDialog();
    }

    /**
     * ตั้งค่า dialog
     */
    private void setupDialog() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        VBox mainBox = new VBox(10);
        mainBox.setStyle("-fx-background-color: #f5e6c8; -fx-border-color: #8b6914; " +
                "-fx-border-width: 4; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 20;");
        mainBox.setMinWidth(600);
        mainBox.setMinHeight(400);
        mainBox.setEffect(new DropShadow(15, Color.BLACK));

        Label title = new Label("⚔ จัดการ Skill");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #4a2800;");

        HBox content = new HBox(15);
        content.setPrefHeight(350);

        // ===== ซ้าย: Skill Slots =====
        VBox leftPanel = new VBox(10);
        leftPanel.setPrefWidth(250);
        leftPanel.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-padding: 10; -fx-background-radius: 5;");

        Label slotTitle = new Label("Skill Slots");
        slotTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #4a2800;");

        leftSlots = new VBox(8);
        refreshSlots();

        leftPanel.getChildren().addAll(slotTitle, leftSlots);

        // ===== ขวา: Skill List =====
        VBox rightPanel = new VBox(10);
        rightPanel.setPrefWidth(280);
        rightPanel.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-padding: 10; -fx-background-radius: 5;");

        Label listTitle = new Label("Skill ที่มี");
        listTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #4a2800;");

        rightSkillList = new VBox(8);
        refreshSkillList();

        ScrollPane skillScroll = new ScrollPane(rightSkillList);
        skillScroll.setFitToWidth(true);
        skillScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        rightPanel.getChildren().addAll(listTitle, skillScroll);
        content.getChildren().addAll(leftPanel, rightPanel);

        // ปุ่มปิด
        Button closeBtn = new Button("✖ ปิด");
        closeBtn.setStyle("-fx-background-color: #8b3014; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-padding: 8 20; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> { dialogStage.close(); onClose.run(); });

        HBox btnRow = new HBox();
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.getChildren().add(closeBtn);

        mainBox.getChildren().addAll(title, new Separator(), content, btnRow);
        root.getChildren().add(mainBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
    }

    /**
     * อัปเดต skill slots (ด้านซ้าย)
     */
    private void refreshSlots() {
        leftSlots.getChildren().clear();
        BaseSkill[] slots = player.getSkillSlots();

        for (int i = 0; i < slots.length; i++) {
            final int slotIdx = i;
            BaseSkill sk = slots[i];

            HBox slotRow = new HBox(8);
            slotRow.setAlignment(Pos.CENTER_LEFT);
            slotRow.setPadding(new Insets(6));
            slotRow.setStyle("-fx-background-color: rgba(255,255,255,0.5); -fx-background-radius: 5; " +
                    (waitingForSlot && i > 0 ? "-fx-border-color: gold; -fx-border-width: 2;" : "") + " -fx-cursor: hand;");

            Label slotNum = new Label((i == 0 ? "★" : String.valueOf(i)) + ".");
            slotNum.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #4a2800;");

            if (sk != null) {
                ImageView skillImg = new ImageView(rm.loadImage(sk.getImagePath(), 32, 32));
                skillImg.setFitWidth(32);
                skillImg.setFitHeight(32);
                Label skillName = new Label(sk.getName() + (i == 0 ? " (ปกติ)" : ""));
                skillName.setStyle("-fx-font-size: 12; -fx-text-fill: #2a1800;");
                slotRow.getChildren().addAll(slotNum, skillImg, skillName);
            } else {
                Label empty = new Label("- ว่าง -");
                empty.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");
                slotRow.getChildren().addAll(slotNum, empty);
            }

            // กด slot เพื่อ equip (เฉพาะ slot 1-3)
            if (i > 0) {
                slotRow.setOnMouseClicked(e -> {
                    if (waitingForSlot && selectedSkill != null) {
                        equipSkillToSlot(slotIdx);
                    }
                });
            }

            leftSlots.getChildren().add(slotRow);
        }

        // แสดง instruction ถ้ากำลังรอเลือก slot
        if (waitingForSlot) {
            Label hint = new Label("👆 กดที่ช่อง slot 1-3 เพื่อใส่ skill");
            hint.setStyle("-fx-font-size: 11; -fx-text-fill: #c04000;");
            leftSlots.getChildren().add(hint);
        }
    }

    /**
     * อัปเดต skill list (ด้านขวา)
     * ไม่แสดง skill ที่ equip แล้ว
     */
    private void refreshSkillList() {
        rightSkillList.getChildren().clear();
        BaseSkill[] equippedSlots = player.getSkillSlots();

        for (BaseSkill sk : allSkills) {
            // ตรวจสอบว่า equip แล้วหรือไม่
            boolean isEquipped = false;
            for (int i = 1; i < equippedSlots.length; i++) {
                if (equippedSlots[i] != null && equippedSlots[i].getId().equals(sk.getId())) {
                    isEquipped = true;
                    break;
                }
            }
            if (isEquipped) continue;

            HBox skillRow = new HBox(8);
            skillRow.setAlignment(Pos.CENTER_LEFT);
            skillRow.setPadding(new Insets(5));
            skillRow.setStyle("-fx-background-color: rgba(255,255,255,0.5); -fx-background-radius: 5; -fx-cursor: hand;");

            ImageView img = new ImageView(rm.loadImage(sk.getImagePath(), 36, 36));
            img.setFitWidth(36);
            img.setFitHeight(36);

            VBox info = new VBox(2);
            Label nameLabel = new Label(sk.getName());
            nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2a1800;");
            Label typeLabel = new Label(sk.getDamageType() + "  MP:" + sk.getManaCost());
            typeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");
            info.getChildren().addAll(nameLabel, typeLabel);

            skillRow.getChildren().addAll(img, info);

            final BaseSkill finalSk = sk;
            skillRow.setOnMouseClicked(e -> showSkillDetail(finalSk));

            rightSkillList.getChildren().add(skillRow);
        }
    }

    /**
     * แสดง popup รายละเอียด skill
     */
    private void showSkillDetail(BaseSkill sk) {
        String details = sk.getDescription() + "\n" +
                "ประเภทดาเมจ: " + sk.getDamageType() + "\n" +
                "Mana ที่ใช้: " + sk.getManaCost();

        GamePopup popup = new GamePopup(dialogStage, sk.getName(), details);
        popup.addButton("สวมใส่", () -> {
            popup.close();
            selectedSkill = sk;
            waitingForSlot = true;
            refreshSlots(); // แสดง highlight
        });
        popup.addButton("ปิด", popup::close);
        popup.show();
    }

    /**
     * ใส่ skill ในช่อง slot ที่เลือก
     */
    private void equipSkillToSlot(int slotIdx) {
        if (selectedSkill == null || slotIdx < 1 || slotIdx > 3) return;
        BaseSkill replaced = player.setSkillSlot(slotIdx, selectedSkill);
        selectedSkill = null;
        waitingForSlot = false;
        refreshSlots();
        refreshSkillList();
        // skill ที่ถูกแทนที่จะกลับมาใน list
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
