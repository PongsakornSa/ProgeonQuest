package gui;

import application.Main;
import gui.components.GamePopup;
import interfaces.Equippable;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import logic.base.BaseItem;
import logic.entities.Player;
import logic.game.ResourceManager;
import java.util.List;

/**
 * หน้าร้านค้า (Merchant)
 * Layout ตรงกับภาพที่ 3:
 * - ซ้าย: รูปพ่อค้า + เงินผู้เล่น
 * - ขวา: รายการสินค้า
 * - ล่างขวา: ปุ่ม buy/sell
 */
public class MerchantPane extends BorderPane {

    private Player player;
    private List<BaseItem> shopItems;
    private Runnable onExit;
    private ResourceManager rm;
    private boolean isBuyMode = true; // true=ซื้อ, false=ขาย

    // UI
    private VBox itemListPanel;
    private Label goldLabel;
    private Button buyModeBtn, sellModeBtn;

    /**
     * สร้าง MerchantPane
     */
    public MerchantPane(Player player, List<BaseItem> shopItems, Runnable onExit) {
        this.player = player;
        this.shopItems = shopItems;
        this.onExit = onExit;
        this.rm = ResourceManager.getInstance();

        setPrefSize(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        //var bg = rm.loadImage("/ui/Bg_merchant.png",Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        //setBackground();
        setStyle("-fx-background-image: url('/ui/Bg_merchant3.png'); " +
                "-fx-background-size: stretch; " +
                "-fx-background-position: center;");

        setupLayout();
    }

    /**
     * ตั้งค่า layout หลัก
     */
    private void setupLayout() {
        HBox mainContent = new HBox(0);
        mainContent.setPrefSize(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT - 50);

        // ===== ด้านซ้าย: พ่อค้า + เงิน (ขยายขนาดและปรับตำแหน่ง) =====
        VBox leftPanel = new VBox(0);
        leftPanel.setPrefWidth(400); // ขยายความกว้าง Panel เพื่อรองรับพ่อค้าตัวใหญ่
        leftPanel.setPadding(new Insets(10, 0, 0, 0)); // ลด Padding บนเพื่อให้กล่องเงินอยู่สูงขึ้น
        leftPanel.setAlignment(Pos.TOP_CENTER);

        // 1. เงินผู้เล่น (ขยายใหญ่ขึ้นและอยู่บนสุด)
        goldLabel = new Label("💰 " + player.getGold() + " Gold");
        goldLabel.setStyle("-fx-font-size: 24; " + // เพิ่มขนาดตัวอักษรเป็น 24
                "-fx-text-fill: gold; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: rgba(30, 20, 10, 0.9); " + // พื้นหลังเข้มขึ้น
                "-fx-border-color: #daa520; " +
                "-fx-border-width: 3; " + // ขอบหนาขึ้น
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 12 40;"); // เพิ่มความกว้างของกล่อง

        // 2. ช่องว่าง (Spacer) ดันพ่อค้าลงไปข้างล่าง
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // 3. รูปพ่อค้า (ปรับให้ใหญ่ขึ้นมาก)
        ImageView merchantImg = new ImageView();
        var img = rm.loadImage("/entities/merchant_full2.png", 275, 750); // โหลดรูปความละเอียดสูงขึ้น
        merchantImg.setImage(img);
        merchantImg.setFitWidth(275);
        merchantImg.setPreserveRatio(true);

        // ตัดรูปเอาช่วงบน (ปรับเป็น 0.8 เพื่อให้เห็นลำตัวมากขึ้นเมื่อตัวใหญ่ขึ้น)
        merchantImg.setViewport(new Rectangle2D(0, 0, img.getWidth(), img.getHeight() * 0.8));

        // เรียงลำดับ: เงิน -> ช่องว่าง -> พ่อค้า
        leftPanel.getChildren().addAll(goldLabel, spacer, merchantImg);

        // ===== ด้านขวา: รายการสินค้า (ปรับพื้นหลังให้เข้มขึ้น) =====
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setPrefWidth(Main.WINDOW_WIDTH - 400.0);

        Label shopTitle = new Label(isBuyMode ? "สินค้าในร้าน" : "ขายของ");
        shopTitle.setStyle("-fx-font-size: 28; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(three-pass-box, black, 10, 0, 0, 0);");

        itemListPanel = new VBox(8);
        itemListPanel.setPadding(new Insets(15));

        ScrollPane scrollPane = new ScrollPane(itemListPanel);
        scrollPane.setFitToWidth(true);
        // ปรับความเข้มพื้นหลัง (Opacity) เป็น 0.8 เพื่อลดความลายตา
        scrollPane.setStyle("-fx-background: transparent; " +
                "-fx-background-color: rgba(0,0,0,0.8); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: rgba(218, 165, 32, 0.3); " + // เพิ่มขอบสีทองจางๆ
                "-fx-border-radius: 15; " +
                "-fx-padding: 10;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // ===== ส่วนล่างขวา: ปุ่มควบคุม =====
        HBox modeBtns = new HBox(20);
        modeBtns.setAlignment(Pos.CENTER_RIGHT);
        modeBtns.setPadding(new Insets(15, 0, 0, 0));

        Button exitBtn = new Button("ออกจากร้าน");
        exitBtn.setStyle("-fx-background-color: #8b3014; -fx-text-fill: white; -fx-font-size: 16; " +
                "-fx-padding: 12 30; -fx-cursor: hand; -fx-font-weight: bold; " +
                "-fx-border-color: #5a1a0a; -fx-border-radius: 8; -fx-background-radius: 8;");
        exitBtn.setOnAction(e -> onExit.run());

        sellModeBtn = createModeButton("Sell");
        buyModeBtn = createModeButton("Buy");
        buyModeBtn.setStyle(buyModeBtn.getStyle() + "-fx-border-color: gold;");

        // Action Logic สำหรับ Buy/Sell (คงเดิม)
        buyModeBtn.setOnAction(e -> {
            if (!isBuyMode) {
                isBuyMode = true;
                shopTitle.setText("สินค้าในร้าน");
                refreshItemList();
                buyModeBtn.setStyle(buyModeBtn.getStyle() + "-fx-border-color: gold;");
                sellModeBtn.setStyle(sellModeBtn.getStyle().replace("-fx-border-color: gold;", ""));
            }
        });

        sellModeBtn.setOnAction(e -> {
            if (isBuyMode) {
                isBuyMode = false;
                shopTitle.setText("ขายของ");
                refreshItemList();
                sellModeBtn.setStyle(sellModeBtn.getStyle() + "-fx-border-color: gold;");
                buyModeBtn.setStyle(buyModeBtn.getStyle().replace("-fx-border-color: gold;", ""));
            }
        });

        modeBtns.getChildren().addAll(sellModeBtn, buyModeBtn, exitBtn);

        rightPanel.getChildren().addAll(shopTitle, scrollPane, modeBtns);
        mainContent.getChildren().addAll(leftPanel, rightPanel);

        setCenter(mainContent);
        refreshItemList();
    }

    /**
     * อัปเดตรายการสินค้าตาม mode (buy/sell)
     */
    private void refreshItemList() {
        itemListPanel.getChildren().clear();
        goldLabel.setText("💰 " + player.getGold() + " Gold");

        List<BaseItem> items = isBuyMode ? shopItems : player.getInventory();

        if (items.isEmpty()) {
            Label empty = new Label(isBuyMode ? "ไม่มีสินค้า" : "กระเป๋าว่างเปล่า");
            empty.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14;");
            itemListPanel.getChildren().add(empty);
            return;
        }

        for (BaseItem item : items) {
            itemListPanel.getChildren().add(createItemRow(item));
        }
    }

    /**
     * สร้าง row สำหรับแสดงไอเทมแต่ละชิ้น
     */
    private HBox createItemRow(BaseItem item) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 5; -fx-cursor: hand;");
        row.setAlignment(Pos.CENTER_LEFT);

        // รูปไอเทม
        ImageView itemImg = new ImageView();
        itemImg.setFitWidth(40);
        itemImg.setFitHeight(40);
        itemImg.setPreserveRatio(true);
        var img = rm.loadImage(item.getImagePath(), 40, 40);
        itemImg.setImage(img);

        // ชื่อและราคา
        VBox info = new VBox(3);
        Label nameLabel = new Label(item.getName() + (item.getQuantity() > 1 ? " x" + item.getQuantity() : ""));
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        int price = isBuyMode ? item.getBuyPrice() : item.getSellPrice();
        Label priceLabel = new Label((isBuyMode ? "ราคา: " : "ขายได้: ") + price + " Gold");
        priceLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 12;");
        info.getChildren().addAll(nameLabel, priceLabel);

        HBox.setHgrow(info, Priority.ALWAYS);

        // ปุ่ม
        Button actionBtn = new Button(isBuyMode ? "ซื้อ" : "ขาย");
        actionBtn.setStyle("-fx-background-color: " + (isBuyMode ? "#2a6e2a" : "#6e2a2a") + "; " +
                "-fx-text-fill: white; -fx-font-size: 12; -fx-cursor: hand; -fx-padding: 5 12;");
        final BaseItem finalItem = item;
        actionBtn.setOnAction(e -> {
            if (isBuyMode) buyItem(finalItem);
            else showSellDialog(finalItem);
        });

        row.getChildren().addAll(itemImg, info, actionBtn);

        // คลิก row เพื่อดูรายละเอียด
        row.setOnMouseClicked(e -> {
            if (e.getTarget() != actionBtn) showItemDetail(finalItem);
        });

        return row;
    }

    /**
     * ซื้อไอเทม
     */
    private void buyItem(BaseItem item) {
        int price = item.getBuyPrice();
        if (player.getGold() < price) {
            showPopup("Gold ไม่พอ!", "คุณมี Gold ไม่เพียงพอสำหรับการซื้อ " + item.getName());
            return;
        }
        // สร้าง copy ของไอเทมสำหรับ add เข้า inventory
        boolean added = player.addItem(item);
        if (!added) {
            showPopup("กระเป๋าเต็ม!", "ไม่สามารถซื้อได้ กระเป๋าของคุณเต็มแล้ว");
            return;
        }
        player.spendGold(price);
        goldLabel.setText("💰 " + player.getGold() + " Gold");
        showPopup("ซื้อสำเร็จ!", "คุณซื้อ " + item.getName() + " สำเร็จ!");
    }

    /**
     * แสดง dialog ยืนยันการขาย (เลือกจำนวน)
     */
    private void showSellDialog(BaseItem item) {
        if (item.getQuantity() == 1) {
            // ขายทันทีถ้ามีชิ้นเดียว
            int earned = item.getSellPrice();
            player.addGold(earned);
            player.removeItem(item);
            goldLabel.setText("💰 " + player.getGold() + " Gold");
            refreshItemList();
            showPopup("ขายสำเร็จ!", "ขาย " + item.getName() + " ได้ " + earned + " Gold");
        } else {
            // มีหลายชิ้น - ให้เลือกจำนวน
            GamePopup popup = new GamePopup(Main.primaryStage, "ขาย " + item.getName(),
                    "มี " + item.getQuantity() + " ชิ้น ราคาขาย " + item.getSellPrice() + " Gold/ชิ้น\nต้องการขายกี่ชิ้น?");

            Spinner<Integer> spinner = new Spinner<>(1, item.getQuantity(), 1);
            spinner.setEditable(true);
            popup.addContent(spinner);
            popup.addButton("ขาย", () -> {
                int qty = spinner.getValue();
                int earned = item.getSellPrice() * qty;
                player.addGold(earned);
                item.decreaseQuantity(qty);
                if (item.getQuantity() <= 0) player.removeItem(item);
                popup.close();
                goldLabel.setText("💰 " + player.getGold() + " Gold");
                refreshItemList();
            });
            popup.addButton("ยกเลิก", popup::close);
            popup.show();
        }
    }

    /**
     * แสดง popup รายละเอียดไอเทม
     */
    private void showItemDetail(BaseItem item) {
        int price = isBuyMode ? item.getBuyPrice() : item.getSellPrice();
        String details = item.getDescription() + "\n" +
                "ประเภท: " + item.getItemType() + "\n" +
                ((item instanceof Equippable) ? ((Equippable) item).getStatBonusDescription() + "\n" : "") +
                (isBuyMode ? "ราคาซื้อ: " : "ราคาขาย: ") + price + " Gold";
        showPopup(item.getName(), details);
    }

    /**
     * แสดง popup ทั่วไป
     */
    private void showPopup(String title, String msg) {
        GamePopup popup = new GamePopup(Main.primaryStage, title, msg);
        popup.addButton("ปิด", popup::close);
        popup.show();
    }

    /**
     * สร้างปุ่ม mode (buy/sell)
     */
    private Button createModeButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #4a3010; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-padding: 8 20; -fx-border-width: 2; " +
                "-fx-border-color: transparent; -fx-cursor: hand;");
        return btn;
    }
}
