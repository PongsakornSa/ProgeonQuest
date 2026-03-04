package gui.components;

import interfaces.Consumable;
import interfaces.Equippable;
import interfaces.Trowable;
import javafx.animation.FadeTransition;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import javafx.util.Duration;
import logic.base.BaseItem;
import logic.entities.Player;
import logic.game.ResourceManager;
import logic.items.ArmorItem;
import logic.items.WeaponItem;

/**
 * Dialog แสดงและจัดการ Inventory ของผู้เล่น
 * แบ่ง 2 ฝั่ง:
 * - ซ้าย: รูปตัวละคร + ช่องสวมใส่ (อาวุธ, เกราะ)
 * - ขวา: ช่องเก็บของ (6 ช่อง)
 */
public class InventoryDialog {

    private Stage dialogStage;
    private Player player;
    private Runnable onClose;
    private ResourceManager rm;

    private VBox leftPanel;
    private VBox weaponSlot, armorSlot;
    private GridPane inventoryGrid;

    /**
     * สร้าง InventoryDialog
     */
    public InventoryDialog(Player player, Runnable onClose) {
        this.player = player;
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
        mainBox.setMinWidth(620);
        mainBox.setEffect(new DropShadow(15, Color.BLACK));

        Label title = new Label("🎒 Inventory");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #4a2800;");

        HBox content = new HBox(15);

        // ===== ซ้าย: Equipment =====
        leftPanel = new VBox(10);
        leftPanel.setPrefWidth(250);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-padding: 10; -fx-background-radius: 5;");

        // รูปตัวละคร
        ImageView playerImg = new ImageView(rm.loadImage(player.getImagePath(), 100, 100));
        playerImg.setFitWidth(100);
        playerImg.setFitHeight(100);
        playerImg.setPreserveRatio(true);

        // เรียกเมธอดแยกเพื่อวาดเนื้อหาใน leftPanel
        rebuildLeftPanel();

        // ===== ขวา: Inventory Grid =====
        VBox rightPanel = new VBox(10);
        rightPanel.setPrefWidth(320);
        rightPanel.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-padding: 10; -fx-background-radius: 5;");

        Label invTitle = new Label("ของในกระเป๋า (6 ช่อง)");
        invTitle.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #4a2800;");

        inventoryGrid = new GridPane();
        inventoryGrid.setHgap(8);
        inventoryGrid.setVgap(8);
        inventoryGrid.setAlignment(Pos.CENTER);
        refreshInventoryGrid();

        rightPanel.getChildren().addAll(invTitle, inventoryGrid);
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
     * สร้าง equip slot (อาวุธหรือเกราะ)
     */
    private VBox createEquipSlot(String slotName, BaseItem equipped) {
        VBox slot = new VBox(5);
        slot.setAlignment(Pos.CENTER);
        slot.setPadding(new Insets(8));
        slot.setStyle("-fx-background-color: rgba(255,255,255,0.5); -fx-background-radius: 5; " +
                "-fx-border-color: #8b6914; -fx-border-width: 1; -fx-cursor: hand;");
        slot.setPrefSize(220, 80);

        Label name = new Label(slotName);
        name.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #4a2800;");

        if (equipped != null) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER);
            ImageView img = new ImageView(rm.loadImage(equipped.getImagePath(), 40, 40));
            img.setFitWidth(40);
            img.setFitHeight(40);
            Label itemName = new Label(equipped.getName());
            itemName.setStyle("-fx-font-size: 12; -fx-text-fill: #2a1800;");
            row.getChildren().addAll(img, itemName);
            slot.getChildren().addAll(name, row);
        } else {
            Label empty = new Label("- ว่าง -");
            empty.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");
            slot.getChildren().addAll(name, empty);
        }

        return slot;
    }

    /**
     * อัปเดต inventory grid (6 ช่อง)
     */
    private void refreshInventoryGrid() {
        inventoryGrid.getChildren().clear();
        var inventory = player.getInventory();
        int cols = 3;

        for (int i = 0; i < 6; i++) {
            VBox slot = new VBox(4);
            slot.setAlignment(Pos.CENTER);
            slot.setPrefSize(90, 90);
            slot.setPadding(new Insets(5));
            slot.setStyle("-fx-background-color: rgba(255,255,255,0.5); -fx-background-radius: 5; " +
                    "-fx-border-color: #8b6914; -fx-border-width: 1;");

            if (i < inventory.size()) {
                BaseItem item = inventory.get(i);
                ImageView img = new ImageView(rm.loadImage(item.getImagePath(), 48, 48));
                img.setFitWidth(48);
                img.setFitHeight(48);
                img.setPreserveRatio(true);

                Label nameLabel = new Label(item.getName().length() > 10
                        ? item.getName().substring(0, 9) + "." : item.getName());
                nameLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #2a1800;");

                Label qtyLabel = null;
                if (item.isStackable() && item.getQuantity() > 1) {
                    qtyLabel = new Label("x" + item.getQuantity());
                    qtyLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #4a6000;");
                }

                slot.getChildren().addAll(img, nameLabel);
                if (qtyLabel != null) slot.getChildren().add(qtyLabel);
                slot.setStyle(slot.getStyle() + " -fx-cursor: hand;");

                final BaseItem finalItem = item;
                slot.setOnMouseClicked(e -> showItemDetail(finalItem));
            } else {
                Label empty = new Label("ว่าง");
                empty.setStyle("-fx-font-size: 11; -fx-text-fill: #aaa;");
                slot.getChildren().add(empty);
            }

            inventoryGrid.add(slot, i % cols, i / cols);
        }
    }

    /**
     * แสดง popup รายละเอียดไอเทม
     */
    private void showItemDetail(BaseItem item) {
        String details = item.getDescription();
        if (item instanceof Consumable && !(item instanceof Trowable)) {
            details += "\nราคาขาย: " + item.getSellPrice() + " Gold";
        }else if (item instanceof Equippable) {
            details += "\n" + ((Equippable) item).getStatBonusDescription() + "\nราคาขาย: " + item.getSellPrice() + " Gold";
        }
        
        GamePopup popup = new GamePopup(dialogStage, item.getName(), details);

        // ปุ่มตามประเภทไอเทม
        if (item instanceof Consumable && !(item instanceof Trowable)) {
            popup.addButton("กดใช้", () -> {
                Consumable c = (Consumable) item;
                String result = c.use(player);
                boolean remaining = item.decreaseQuantity(1);
                if (!remaining) player.removeItem(item);
                popup.close();
                // แสดง popup ผล
                GamePopup resultPopup = new GamePopup(dialogStage, "ใช้ไอเทม", result);
                resultPopup.addButton("ตกลง", () -> { resultPopup.close(); refreshInventoryGrid(); });
                resultPopup.show();
            });
        } else if (item instanceof Equippable) {
            popup.addButton("สวมใส่", () -> {
                Equippable eq = (Equippable) item;
                eq.equip(player);
                popup.close();
                refreshInventoryGrid();
                refreshEquipSlots();
                //show();
            });
        }

        popup.addButton("ปิด", popup::close);
        popup.show();
    }

    /**
     * แสดง popup รายละเอียด equipment ที่สวมใส่ (พร้อมปุ่มถอด)
     */
    private void showEquipInfo(BaseItem equipped) {
        String details = equipped.getDescription() + "\n";
        if (equipped instanceof Equippable) {
            details += ((Equippable) equipped).getStatBonusDescription();
        }
        details += "\nราคาขาย: " + equipped.getSellPrice() + " Gold";

        GamePopup popup = new GamePopup(dialogStage, equipped.getName(), details);
        popup.addButton("ถอด", () -> {
            if (equipped instanceof WeaponItem) {
                boolean ok = player.unequipWeapon();
                if (!ok) {
                    popup.close();
                    GamePopup fullPopup = new GamePopup(dialogStage, "กระเป๋าเต็ม!", "ไม่สามารถถอดได้ กระเป๋าเต็ม");
                    fullPopup.addButton("ตกลง", fullPopup::close);
                    fullPopup.show();
                    return;
                }
            } else if (equipped instanceof ArmorItem) {
                boolean ok = player.unequipArmor();
                if (!ok) {
                    popup.close();
                    GamePopup fullPopup = new GamePopup(dialogStage, "กระเป๋าเต็ม!", "ไม่สามารถถอดได้ กระเป๋าเต็ม");
                    fullPopup.addButton("ตกลง", fullPopup::close);
                    fullPopup.show();
                    return;
                }
            }
            popup.close();
            refreshInventoryGrid();
            refreshEquipSlots();
            //show();
        });
        popup.addButton("ปิด", popup::close);
        popup.show();
    }

    private void rebuildLeftPanel() {
        leftPanel.getChildren().clear(); // ล้างของเก่าออก

        // รูปตัวละคร
        ImageView playerImg = new ImageView(rm.loadImage(player.getImagePath(), 100, 100));
        playerImg.setFitWidth(100);
        playerImg.setFitHeight(100);
        playerImg.setPreserveRatio(true);

        Label equipTitle = new Label("Equipment");
        equipTitle.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #4a2800;");

        // Weapon slot
        weaponSlot = createEquipSlot("⚔ อาวุธ", player.getEquippedWeapon());
        weaponSlot.setOnMouseClicked(e -> {
            if (player.getEquippedWeapon() != null) showEquipInfo(player.getEquippedWeapon());
        });

        // Armor slot
        armorSlot = createEquipSlot("🛡 เกราะ", player.getEquippedArmor());
        armorSlot.setOnMouseClicked(e -> {
            if (player.getEquippedArmor() != null) showEquipInfo(player.getEquippedArmor());
        });

        leftPanel.getChildren().addAll(playerImg, equipTitle, weaponSlot, armorSlot);
    }

    /**
     * อัปเดต equip slots ด้านซ้าย พร้อมเอฟเฟกต์ Fade
     */
    private void refreshEquipSlots() {
        // 1. สร้างเอฟเฟกต์ Fade Out (ทำให้จางลง) ให้กับ leftPanel
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), leftPanel);
        fadeOut.setFromValue(1.0);  // ความชัด 100%
        fadeOut.setToValue(0.2);  // จางลงเหลือ 20%

        // 2. กำหนดว่าถ้า Fade Out เสร็จแล้ว ให้ทำอะไรต่อ
        fadeOut.setOnFinished(e -> {
            // อัปเดตข้อมูลไอเทมใหม่ใส่ leftPanel (เมธอดที่เราสร้างไว้รอบที่แล้ว)
            rebuildLeftPanel();

            // 3. สร้างเอฟเฟกต์ Fade In (ทำให้สว่างกลับมา)
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), leftPanel);
            fadeIn.setFromValue(0.2); // เริ่มจาก 20%
            fadeIn.setToValue(1.0); // กลับมาสว่างเต็มที่ 100%
            fadeIn.play(); // สั่งรัน Fade In
        });

        // สั่งรัน Fade Out เพื่อเริ่มกระบวนการ
        fadeOut.play();
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
