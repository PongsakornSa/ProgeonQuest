package gui.components;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Popup หน้าต่างในเกม (ไม่ใช่ browser alert)
 * แสดงข้อมูล, ปุ่มต่างๆ และ custom content
 * ใช้ Stage แบบ TRANSPARENT + undecorated เพื่อให้เหมือนในเกม
 */
public class GamePopup {

    private Stage popupStage;
    private VBox contentBox;
    private HBox buttonBox;
    private List<Button> buttons = new ArrayList<>();

    /**
     * สร้าง GamePopup
     * @param owner Stage หลัก
     * @param title ชื่อหัวข้อ
     * @param message ข้อความ
     */
    public GamePopup(Stage owner, String title, String message) {
        popupStage = new Stage();
        popupStage.initOwner(owner);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.TRANSPARENT);

        // สร้าง root container
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        // กล่อง popup หลัก (style เหมือนในเกม)
        VBox popupBox = new VBox(15);
        popupBox.setStyle("-fx-background-color: #f5e6c8; -fx-border-color: #8b6914; " +
                "-fx-border-width: 4; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-padding: 20;");
        popupBox.setMinWidth(300);
        popupBox.setMaxWidth(500);
        popupBox.setEffect(new DropShadow(15, Color.BLACK));

        // Title bar
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #4a2800;");
        titleLabel.setWrapText(true);

        // Divider
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #8b6914;");

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2a1800;");
        messageLabel.setWrapText(true);

        // Content area (สำหรับ custom content เช่น Spinner)
        contentBox = new VBox(8);
        contentBox.setAlignment(Pos.CENTER);

        // Button area
        buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        popupBox.getChildren().addAll(titleLabel, sep, messageLabel, contentBox, buttonBox);
        root.getChildren().add(popupBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popupStage.setScene(scene);
    }

    /**
     * เพิ่มปุ่มใน popup
     * @param text ข้อความบนปุ่ม
     * @param action สิ่งที่จะทำเมื่อกด
     */
    public void addButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #a0522d; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        btn.setOnAction(e -> action.run());
        buttonBox.getChildren().add(btn);
        buttons.add(btn);
    }

    /**
     * เพิ่ม custom UI node ใน content area
     */
    public void addContent(Node node) {
        contentBox.getChildren().add(node);
    }

    /**
     * แสดง popup
     */
    public void show() {
        popupStage.sizeToScene();
        popupStage.centerOnScreen();
        popupStage.show();
    }

    /**
     * ปิด popup
     */
    public void close() {
        popupStage.close();
    }
}
