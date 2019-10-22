package client.simple;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.event.MouseAdapter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller{
    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    Button btn1;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADRESS = "localhost";
    final int PORT = 8189;

    private boolean isAuthorized;
    private boolean isRegistered;


    @FXML
    HBox bottomPanel;

    @FXML
    VBox upperPanel;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;

    @FXML
    TextField nickFieldReg;

    @FXML
    TextField loginFieldReg;

    @FXML
    PasswordField passwordFieldReg;

    @FXML
    VBox firstPanel;

    @FXML
    Label lb, lb1, lb2, lb3, lb4, lb5, lb6;

    @FXML
    ListView<String> clientsList;


    private void setAuthrizedAndReg(boolean isAuthorized, boolean isRegistered) {
        this.isAuthorized = isAuthorized;
        this.isRegistered = isRegistered;
        if ((!isRegistered)&&(!isAuthorized)) {
            firstPanel.setVisible(true);
            firstPanel.setManaged(true);
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
        } else if ((isRegistered)&&(!isAuthorized)) {
            firstPanel.setVisible(false);
            firstPanel.setManaged(false);
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
        } else if ((isRegistered)&&(isAuthorized)){
            firstPanel.setVisible(false);
            firstPanel.setManaged(false);
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
        }
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/authok")) {
                                setAuthrizedAndReg(true, true);
                                break;
                            } else if (str.startsWith("/regok")){
                                    setAuthrizedAndReg(false, true);
                            }
                                    else {
                                        textArea.appendText(str + "++\n");
                                    }
                        }

                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/")){
                                if (str.startsWith("/clientslist")) {
                                    String[] tokens = str.split(" ");
                                    Platform.runLater(() ->{
                                        clientsList.getItems().clear();
                                        for (int i = 1; i < tokens.length; i++) {
                                            clientsList.getItems().add(tokens[i]);
                                        }

                                    });
                                }
                                if (str.equalsIgnoreCase("/clientClose")) {
                                    break;
                                }
                            } else
                                textArea.appendText(str + "\n");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthrizedAndReg(false,false);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Dispose() {
        System.out.println("Отправляем сообщение на сервер о завершении работы");
        try {
            if (out != null) {
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//отсылаем форму регистрации
    public void tryToReg(ActionEvent actionEvent) {
        if ((socket == null) || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF("/reg " + nickFieldReg.getText()+ " " + loginFieldReg.getText() + " " + passwordFieldReg.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToAuth(ActionEvent actionEvent){
        isRegistered=true;
        isAuthorized=false;
        firstPanel.setVisible(false);
        firstPanel.setManaged(false);
        upperPanel.setVisible(true);
        upperPanel.setManaged(true);
        bottomPanel.setVisible(false);
        bottomPanel.setManaged(false);
    }

    public void selectClient (MouseEvent mouseEvent){
        if(mouseEvent.getClickCount()==2){
 //           MiniStage ms = new MiniStage(clientsList.getSelectionModel().getSelectedItems(), out, textArea);
        }
    }
}
