package main.java;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by ania on 21.05.17.
 */
public class ClientController implements Initializable {
    @FXML
    private String userName;
    private Image oImage;
    private Image xImage;
    private Image currentImage;

    private XmlRpcClientConfigImpl config;
    private XmlRpcClient client;
    private HashMap<String, ImageView> imageViews = new HashMap<>();
    private Timeline timeline;

    @FXML
    private Button register;

    @FXML
    private Button leave;

    @FXML
    private ImageView v_0_0;
    @FXML
    private ImageView v_0_1;
    @FXML
    private ImageView v_0_2;
    @FXML
    private ImageView v_1_0;
    @FXML
    private ImageView v_1_1;
    @FXML
    private ImageView v_1_2;
    @FXML
    private ImageView v_2_0;
    @FXML
    private ImageView v_2_1;
    @FXML
    private ImageView v_2_2;

    @FXML
    private Label you_points;

    @FXML
    private Label opponent_points;

    private void initializeClient() {
        try {
            this.config = new XmlRpcClientConfigImpl();
            config.setEnabledForExtensions(true);
            config.setServerURL(new URL("http://127.0.0.1:8082/xmlrpc"));
            this.client = new XmlRpcClient();
            this.client.setConfig(config);

        } catch (Exception exception) {
            System.err.println("JavaClient: " + exception);
        }
    }

    private void initializeImage(int whichImage) {
        oImage = new Image("o.png");
        xImage = new Image("x.png");

        currentImage = xImage;

        if(whichImage == 1) {
            currentImage = oImage;
        }
    }

    private void showErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText("Connection error");
        alert.setContentText("Server not found!");

        alert.showAndWait();
    }

    private void showInfoDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void showNameDialog() {
        TextInputDialog dialog = new TextInputDialog("nick");
        dialog.setTitle("Text Input Dialog");
        dialog.setHeaderText("Look, a Text Input Dialog");
        dialog.setContentText("Please enter your name:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && result.get() != null){
            this.userName = result.get();
        } else {
            this.userName = "nick" +  Math.random();
        }
    }

    @FXML
    private void registerToGame(ActionEvent event) throws IOException {
        if(event.getSource() == register) {
            try {

                Vector<String> params = new Vector<String>();
                params.addElement(this.userName);

                client.execute("Communication.registerToGame", params);
                showInfoDialog("Successfully registered");
            }
            catch (XmlRpcException e) {
                e.printStackTrace();
                showErrorDialog();
            }
        }
    }

    @FXML
    private void leaveGame(ActionEvent event) throws IOException {
        if(event.getSource() == leave) {
            try {

                Vector params = new Vector();
                params.addElement(userName);

                client.execute("Communication.unregisterFromGame", params);
                showInfoDialog("Successfully unregistered");
            }
            catch (XmlRpcException e) {
                e.printStackTrace();
                showErrorDialog();
            }
        }
    }

    @FXML
    private void setImage(javafx.scene.input.MouseEvent event) throws IOException {
        try {
            Object response = client.execute("Communication.canStart", new Vector());
            if((boolean) response == false) {
                showInfoDialog("Wait for second player!");
                return;
            }
        }
        catch (XmlRpcException e) {
            e.printStackTrace();
            showErrorDialog();
        }

        try {
            Object response = client.execute("Communication.getCurrentPlayer", new Vector());
            System.out.println(response.toString() + " Cuttent name: " + userName);
            if(response.equals(userName)) {
                Object symbol = client.execute("Communication.getCurrentSymbol", new Vector());
                initializeImage((int) symbol);
                drawASymbol(event);
            }
        }
        catch (XmlRpcException e) {
            e.printStackTrace();
            showErrorDialog();
        }
    }

    private void drawASymbol(javafx.scene.input.MouseEvent event)
    {
        //jesli ja jestem obecnym userem na serwerze
        if(event.getSource() instanceof ImageView && !((ImageView) event.getSource()).isDisabled()) {
            ImageView view = (ImageView) event.getSource();
            view.setImage(currentImage);
            view.setDisable(true);

            Vector params = new Vector();
            params.addElement(userName);

            String viewId = view.getId();
            String[] parts = viewId.split("_");

            params.addElement(Integer.parseInt(parts[1]));
            params.addElement(Integer.parseInt(parts[2]));

            try {
                client.execute("Communication.makeAMove", params);
            } catch (XmlRpcException e) {
                e.printStackTrace();
                showErrorDialog();
            }
        }
    }

    private void updateBoard()
    {
        try {
            Object response = client.execute("Communication.getFields", new Vector());
            Object pointsResponse = client.execute("Communication.getPoints", new Vector());
            Object[] returnedArray = (Object[]) client.execute("Communication.getUsers", new Vector());

            List<String> usersList = new ArrayList<>();
            for (Object element : returnedArray) {
                usersList.add(element.toString());
            }

            int[] fields = (int[]) response;
            int[] points = (int[]) pointsResponse;

            for(int i = 0 ; i < 3 ; i ++) {
                for (int j = 0; j < 3; j ++) {
                    String key = "" + i + j;
                    if ((int) fields[3 * i + j] == 1) {
                        imageViews.get(key).setImage(oImage);
                        imageViews.get(key).setDisable(true);
                    } else if ((int) fields[3 * i + j] == 2) {
                        imageViews.get(key).setImage(xImage);
                        imageViews.get(key).setDisable(true);
                    } else if ((int) fields[3 * i + j] == 0) {
                        imageViews.get(key).setImage(null);
                        imageViews.get(key).setDisable(false);
                    }
                }
            }

            pointsBoardsSetup(usersList, points);

        } catch (XmlRpcException e) {
            e.printStackTrace();
            showErrorDialog();
        }
    }

    private void pointsBoardsSetup(List<String> users, int[] points)
    {
        if(users.size() == 2) {

            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).equals(userName)) {
                    you_points.setText(Integer.toString(points[i]));
                } else {
                    opponent_points.setText(Integer.toString(points[i]));
                }
            }
        }
    }

    private void createImageViewCollection()
    {
        imageViews.put("00", v_0_0);
        imageViews.put("01",v_0_1);
        imageViews.put("02",v_0_2);
        imageViews.put("10",v_1_0);
        imageViews.put("11",v_1_1);
        imageViews.put("12",v_1_2);
        imageViews.put("20",v_2_0);
        imageViews.put("21",v_2_1);
        imageViews.put("22",v_2_2);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showNameDialog();
        initializeClient();
        createImageViewCollection();

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(4),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                updateBoard();
                            }
                        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
}