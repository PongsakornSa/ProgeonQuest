package gui;

import application.Main;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import logic.game.GameState;
import logic.game.ResourceManager;

/**
 * หน้าเริ่มเกม (Title Screen)
 * แสดงชื่อเกม, พื้นหลัง และปุ่ม Start
 * Layout ตรงกับภาพที่ 1
 */
public class TitlePane extends StackPane {

    /**
     * สร้าง TitlePane: ตั้งค่า layout, โหลดพื้นหลัง, ผูกปุ่ม
     */
    public TitlePane() {
        setPrefSize(Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
        setupBackground();
        setupContent();
    }

    /**
     * ตั้งค่าพื้นหลัง: โหลดรูปจาก resources หรือใช้ gradient ถ้าไม่มี
     */
    private void setupBackground() {
        try {
            var img = ResourceManager.getInstance().loadImage("/ui/title_bg.png",
                    Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
            ImageView bg = new ImageView(img);
            bg.setFitWidth(Main.WINDOW_WIDTH);
            bg.setFitHeight(Main.WINDOW_HEIGHT);
            getChildren().add(bg);
        } catch (Exception e) {
            // ใช้สีพื้นหลังถ้าไม่มีรูป
            setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e, #0f3460);");
        }
    }

    /**
     * สร้าง content หลัก: กล่องชื่อเกม + ปุ่ม start
     */
    private void setupContent() {
        VBox content = new VBox(40);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(500);

        // กล่องชื่อเกม (ตรงกับภาพที่ 1)
        VBox titleBox = new VBox(10);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-border-color: white; " +
                "-fx-border-width: 3; -fx-padding: 30;");
        titleBox.setPrefWidth(400);
        titleBox.setPrefHeight(120);

        Text gameTitle = new Text("Progeon Quest");
        gameTitle.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gameTitle.setFill(Color.GOLD);

        Text subtitle = new Text("~ Turn-Based RPG ~");
        subtitle.setFont(Font.font("Arial", FontPosture.ITALIC, 16));
        subtitle.setFill(Color.LIGHTGRAY);

        titleBox.getChildren().addAll(gameTitle, subtitle);

        // ปุ่ม START (ตรงกับภาพที่ 1)
        Button startBtn = createStyledButton("START");
        startBtn.setPrefWidth(150);
        startBtn.setPrefHeight(45);
        startBtn.setOnAction(e -> startGame());

        content.getChildren().addAll(titleBox, startBtn);
        getChildren().add(content);
    }

    /**
     * สร้าง Button ที่มี style สม่ำเสมอ
     */
    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: white; " +
                "-fx-border-width: 2; -fx-text-fill: white; -fx-font-size: 16; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
        // Hover effect
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                "-fx-border-color: gold; -fx-border-width: 2; -fx-text-fill: gold; " +
                "-fx-font-size: 16; -fx-font-weight: bold; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; " +
                "-fx-border-color: white; -fx-border-width: 2; -fx-text-fill: white; " +
                "-fx-font-size: 16; -fx-font-weight: bold; -fx-cursor: hand;"));
        return btn;
    }

    /**
     * เริ่มเกมใหม่: สร้าง GameState และเปลี่ยนไปหน้าแผนที่
     * ใช้ Thread + Platform.runLater เพื่อไม่ block UI thread
     */
    private void startGame() {
        // ใช้ Thread สำหรับ initialize game (อาจหนักถ้ามีการโหลด resources เยอะ)
        Thread initThread = new Thread(() -> {
            GameState.getInstance().startNewGame();
            // Platform.runLater เพื่ออัปเดต UI จาก non-FX thread
            Platform.runLater(() -> {
                MapPane mapPane = new MapPane();
                Main.primaryStage.getScene().setRoot(mapPane);
                mapPane.requestFocus(); // ให้ focus สำหรับ keyboard control
            });
        });
        initThread.setDaemon(true);
        initThread.start();
    }
}
