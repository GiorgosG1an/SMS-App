package gr.uop;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private Label toLabel;
    private TextField recipientsTextField;
    private Button addButton;
    private TextArea messageTextArea;
    private Button sendButton;
    private Text statusText;


    @Override
    public void start(Stage stage) {

        toLabel = new Label("To:");

        recipientsTextField = new TextField();
        recipientsTextField.setPromptText("Type numbers seperated with ';'");

        addButton = new Button("Add...");
        

        // create a HBox for the top of our app
        HBox topBarHBox = new HBox();
        topBarHBox.getChildren().addAll(toLabel, recipientsTextField, addButton);
        topBarHBox.setSpacing(5);
        topBarHBox.setPadding(new Insets(5));
        topBarHBox.setAlignment(Pos.TOP_CENTER);

        // set the TextField to grow horizontally
        HBox.setHgrow(recipientsTextField, Priority.ALWAYS);

        // Main Text Area of the app
        messageTextArea = new TextArea();
        messageTextArea.setPromptText("Type message text.");
        messageTextArea.setWrapText(true);

        HBox messageHBox = new HBox(messageTextArea);
        HBox.setHgrow(messageTextArea, Priority.ALWAYS);

        // send Button settings
        sendButton = new Button("Send");
        sendButton.setMaxWidth(Double.MAX_VALUE);
        
        HBox sendHBox = new HBox();
        sendHBox.getChildren().addAll(sendButton);
        HBox.setHgrow(sendButton, Priority.ALWAYS);

        // status settings
        statusText = new Text("0 / 160");

        HBox statusHBox = new HBox();
        statusHBox.getChildren().addAll(statusText);
        statusHBox.setAlignment(Pos.BOTTOM_RIGHT);
        
        // the main VBox containg all the other HBoxes 
        VBox mainVBox = new VBox();
        mainVBox.getChildren().addAll(topBarHBox, messageHBox, sendHBox, statusHBox);

        mainVBox.setSpacing(5);
        mainVBox.setPadding(new Insets(5));
        VBox.setVgrow(messageHBox, Priority.ALWAYS);

        // Add Button functionality
        addButton.setOnAction((e) -> {
            AddRecipients d = new AddRecipients(stage);
            Optional<String> result = d.showAndWait();
            if (result.isPresent()) {
                System.out.println(result.get());
            }
            else {
                System.out.println("No result");
            }
        });

        // Send Button functionality
        sendButton.setOnAction((e) ->{
            if(recipientsTextField.getText().isEmpty()){
                // create a Warning if recipients are empty
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Missing Recipients");
                alert.setContentText("Missing Recipients!!!\nMessage not sent!!!");
                alert.setHeaderText(null);
                alert.initModality(Modality.WINDOW_MODAL);
                alert.initOwner(stage);
                alert.showAndWait();
            }
            else if( messageTextArea.getText().isEmpty()){
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Empty Message");
                alert.setContentText("The body of your message is empty.\nDo you want to proceed?");
                alert.setHeaderText(null);
                alert.initModality(Modality.WINDOW_MODAL);
                alert.initOwner(stage);

                alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.YES){
                    System.out.println("Message sent");
                    recipientsTextField.clear();
                    messageTextArea.clear();
                }
                else{
                    System.out.println("Return");
                }
            }
            else if (!messageTextArea.getText().isEmpty()){
                // message has body and recipient/s
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Sending Message");
                alert.setHeaderText(null);
                alert.setContentText("Your message has been sent");

                alert.initModality(Modality.WINDOW_MODAL);
                alert.initOwner(stage);
                alert.showAndWait();

                messageTextArea.clear();
                recipientsTextField.clear();
            }   
        });

        // add listener to the messageTextArea in order to update the statusText
        messageTextArea.textProperty().addListener((observable, oldValue, newValue)->{
            int length = newValue.length();
            int smsCount = length / 160 + 1;
            
            if(length > 160){
                statusText.setText(length + " / " + (smsCount*160) + " " + smsCount + " SMS");
            }
            else{
                statusText.setText(length + " / " + 160);
            }
        });

        // handle the exit request when there are characters in the messageTextArea
        stage.setOnCloseRequest((e) ->{

            if (!messageTextArea.getText().isEmpty()){
                Alert alert = new Alert(AlertType.CONFIRMATION, "Do you want to close the application?\nYou have text in your message Area.");
                alert.setTitle("Closing the Application");
                alert.setHeaderText(null);
                alert.initOwner(stage);

                Optional<ButtonType> result = alert.showAndWait();
                if(result.isPresent()) {
                    if(result.get() == ButtonType.OK) {
                        System.out.println("Closing...");
                    }
                    else if (result.get() == ButtonType.CANCEL){
                        System.out.println("Returning to the application");
                        e.consume();
                    }
                }
            }
            else{
                System.out.println("Closing");
            }
        });


        var scene = new Scene(mainVBox,480, 380);
        stage.setTitle("SMS App");
        stage.setMinWidth(400);
        stage.setMinHeight(300);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


    // Add Recipients dialog window class
    private class AddRecipients extends Dialog<String> {
        Random rand = new Random();
        public AddRecipients(Stage stage) {
            this.initOwner(stage);
            this.initModality(Modality.WINDOW_MODAL);
            setTitle("Add Recipients");

            // Content of the dialog
            ListView<String> leftListView = new ListView<>();
            leftListView.setMaxHeight(Double.MAX_VALUE);
            leftListView.setPrefWidth(150);
            leftListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            
            for (int i=0; i< 30; i++){
                // a random 8 digit number to be followed after 69
                int randomNum = rand.nextInt((99999999 - 10000000) + 1) + 10000000;
                leftListView.getItems().add("69"+randomNum);
            }

            Button addButton = new Button("Add");
            addButton.setMaxWidth(Double.MAX_VALUE);
            addButton.setDisable(true);

            Button removeButton = new Button("Remove");
            removeButton.setMaxWidth(Double.MAX_VALUE);
            // remove button start as disabled
            removeButton.setDisable(true);

            // VBox for the buttons
            VBox buttonsVBox = new VBox();
            buttonsVBox.getChildren().addAll(addButton, removeButton);
            buttonsVBox.setSpacing(5);
            buttonsVBox.setPadding(new Insets(5));
            buttonsVBox.setAlignment(Pos.CENTER);

            ListView<String> rightListView = new ListView<>();
            rightListView.setMaxHeight(Double.MAX_VALUE);
            rightListView.setPrefWidth(leftListView.getPrefWidth());
            rightListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // get the user input from the main dialog and add it to the right List
            String recipients = recipientsTextField.getText();
            if (!recipients.isEmpty()){
                String[] numbers = recipients.split(";");
                for (String number : numbers){
                    rightListView.getItems().add(number);
                }
            }

            // main HBox for the content 

            HBox mainHBox = new HBox();
            mainHBox.getChildren().addAll(leftListView, buttonsVBox, rightListView);
            mainHBox.setPadding(new Insets(5, 5, 10, 5));
            mainHBox.setAlignment(Pos.CENTER);


            // Button Actions
            addButton.setOnAction((e) ->{

                var leftItem = leftListView.getSelectionModel().getSelectedItems();

                rightListView.getItems().addAll(leftItem);
                leftListView.getItems().removeAll(leftItem);
            });

            removeButton.setOnAction((e) ->{
                var rightItem = rightListView.getSelectionModel().getSelectedItems();

                leftListView.getItems().addAll(rightItem);
                rightListView.getItems().removeAll(rightItem);
            });

            // set buttons disabled when they dont have items in their list
            leftListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    addButton.setDisable(newValue.intValue() == -1);
                }
            });

            // disable remove button
            rightListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    removeButton.setDisable(newValue.intValue() == -1);
                }
            });


            // add the main content to the dialog
            this.getDialogPane().setContent(mainHBox);

            // add Buttons
            this.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

            this.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    // Get all items from the rightListView
                    List<String> numbers = rightListView.getItems();
                    
                    // Join the items with ';' delimiter
                    String joinNumbers = String.join(";", numbers);

                    recipientsTextField.clear();
                    recipientsTextField.setText(joinNumbers);

                    return joinNumbers;
                }

                else{
                    return null;
                }
            });
        }
    }
}