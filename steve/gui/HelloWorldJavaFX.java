import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HelloWorldJavaFX extends Application {
    @Override
    public void start(Stage stage) {
        Label label = new Label("Hello, World!");
        Scene scene = new Scene(label, 300, 100);
        stage.setTitle("Hello, World (JavaFX)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

