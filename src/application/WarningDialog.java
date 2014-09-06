package application;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class WarningDialog extends Stage {
	
	private static final Image warningImage = new Image(WarningDialog.class.getResourceAsStream("/error.png"));
	
	public WarningDialog(String title, String message) {
		super();
		
		this.setTitle(title);
		this.getIcons().add(warningImage);
		
		ImageView img = new ImageView(warningImage);
		img.setLayoutX(10);
		img.setLayoutY(14);
		
		
		Text text = new Text(message);
		text.setWrappingWidth(290);
		text.setFont(new Font("Sans Serif", 18));
		text.setLayoutX(100);
		text.setLayoutY(40);
		
		Button button = new Button("Ok");
		button.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				WarningDialog.this.close();
			}
		});
		
		Scene scene = new Scene(new Group(img, text, button), 400, 120);
		
		button.setMinWidth(100);
		
		button.setLayoutX(150);
		button.setLayoutY(90);
		
		this.setScene(scene);
		
		
		this.setResizable(false);
		this.show();
	}
}
