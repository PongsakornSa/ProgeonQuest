package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import gui.TitlePane;

/**
 * จุดเริ่มต้นของโปรแกรม - ตั้งค่า Stage หลักและโหลดหน้า Title
 */
public class Main extends Application {

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static Stage primaryStage;

    /**
     * เริ่มต้น JavaFX Application ตั้งค่าหน้าต่างหลัก
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("Dungeon Quest");
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.setResizable(false);

        // โหลด icon ถ้ามี
        try {
            Image icon = new Image(Main.class.getResourceAsStream("/ui/icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found, skipping.");
        }

        // แสดงหน้า Title เป็นหน้าแรก
        showTitleScreen();
        stage.show();
    }

    /**
     * เปลี่ยนไปแสดงหน้า TitlePane (หน้าเริ่มเกม)
     */
    public static void showTitleScreen() {
        TitlePane titlePane = new TitlePane();
        Scene scene = new Scene(titlePane, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
    }

    /**
     * Main method - launch JavaFX
     */
    public static void main(String[] args) {
        launch(args);
    }
}
