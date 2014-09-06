package application;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class YesNoDialog extends Stage {
	
	
	public YesNoDialog(String title, String message, EventHandler<ActionEvent> op1, EventHandler<ActionEvent> op2, EventHandler<ActionEvent> op3) {
		super();
		
		this.setTitle(title);
		
		
		Text text = new Text(message);
		text.setWrappingWidth(300);
		text.setFont(new Font("Sans Serif", 18));
		text.setLayoutX(50);
		text.setLayoutY(40);
		
		Button buttonOp1 = new Button("Graphic Only");
		buttonOp1.setOnAction(op1);
		
		Button buttonOp2 = new Button("Data Only");
		buttonOp2.setOnAction(op2);
		
		Button buttonOp3 = new Button("Both");
		buttonOp3.setOnAction(op3);
		
		EventHandler<MouseEvent> closer = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				if (e.getButton() == MouseButton.PRIMARY) {
					YesNoDialog.this.close();
				}
			}
		};
		
		//buttonOp1.addEventHandler(MouseEvent.MOUSE_RELEASED, closer);
		//buttonOp2.addEventHandler(MouseEvent.MOUSE_RELEASED, closer);
		//buttonOp3.addEventHandler(MouseEvent.MOUSE_RELEASED, closer);
		
		Scene scene = new Scene(new Group(text, buttonOp1, buttonOp2, buttonOp3), 400, 120);
		
		buttonOp1.setMinWidth(120);
		
		buttonOp1.setLayoutX(10);
		buttonOp1.setLayoutY(90);
		
		buttonOp2.setMinWidth(120);
		
		buttonOp2.setLayoutX(145);
		buttonOp2.setLayoutY(90);
		
		buttonOp3.setMinWidth(120);
		
		buttonOp3.setLayoutX(280);
		buttonOp3.setLayoutY(90);
		
		this.setScene(scene);
		
		
		this.setResizable(false);
		this.show();
	}
	
}
