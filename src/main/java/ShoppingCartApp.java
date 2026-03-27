import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ShoppingCartApp extends Application {

    private static Stage primaryStage;
    private static final String APP_TITLE = "Kumudu Nallaperuma / Shopping Cart App";

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        loadScene(new Locale("en", "US"));
        primaryStage.show();
    }

    public static void loadScene(Locale locale) throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("MessagesBundle", locale);

        FXMLLoader loader = new FXMLLoader(
                ShoppingCartApp.class.getResource("/main-view.fxml"),
                bundle
        );

        Parent root = loader.load();

        Scene scene = new Scene(root, 850, 650);
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Controller {

        @FXML
        private Label selectLanguageLabel;

        @FXML
        private ComboBox<String> languageComboBox;

        @FXML
        private Label numberOfItemsLabel;

        @FXML
        private TextField itemCountField;

        @FXML
        private Button enterItemsButton;

        @FXML
        private VBox itemsContainer;

        @FXML
        private Button calculateTotalButton;

        @FXML
        private Label totalCostLabel;

        @FXML
        private Label totalValueLabel;

        @FXML
        private Label instructionLabel;

        private final List<TextField> priceFields = new ArrayList<>();
        private final List<TextField> quantityFields = new ArrayList<>();
        private final List<Label> itemTotalLabels = new ArrayList<>();

        private final CartCalculator calculator = new CartCalculator();
        private final DecimalFormat df = new DecimalFormat("0.00");

        @FXML
        public void initialize() {
            languageComboBox.setItems(FXCollections.observableArrayList(
                    "English", "Finnish", "Swedish", "Japanese", "Arabic"
            ));
            languageComboBox.setValue("English");
        }

        @FXML
        private void handleLanguageChange() {
            String selected = languageComboBox.getValue();
            Locale locale = mapLanguageToLocale(selected);

            try {
                ShoppingCartApp.loadScene(locale);
            } catch (IOException e) {
                showError("Could not change language.");
            }
        }

        @FXML
        private void handleEnterItems() {
            itemsContainer.getChildren().clear();
            priceFields.clear();
            quantityFields.clear();
            itemTotalLabels.clear();
            totalValueLabel.setText("0.00");

            int itemCount;

            try {
                itemCount = Integer.parseInt(itemCountField.getText().trim());

                if (itemCount <= 0) {
                    showError(getBundleText("error.positive.items"));
                    return;
                }
            } catch (NumberFormatException e) {
                showError(getBundleText("error.invalid.items"));
                return;
            }

            ResourceBundle bundle = currentBundle();

            for (int i = 0; i < itemCount; i++) {
                Label itemLabel = new Label(bundle.getString("item") + " " + (i + 1) + ":");

                TextField priceField = new TextField();
                priceField.setPromptText(bundle.getString("enter.price.for.item"));

                TextField quantityField = new TextField();
                quantityField.setPromptText(bundle.getString("enter.quantity.for.item"));

                Label itemTotalTextLabel = new Label(bundle.getString("item.total"));
                Label itemTotalValueLabel = new Label("0.00");

                HBox row = new HBox(10);
                row.getChildren().addAll(
                        itemLabel,
                        priceField,
                        quantityField,
                        itemTotalTextLabel,
                        itemTotalValueLabel
                );

                if (isArabicSelected()) {
                    row.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                } else {
                    row.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                }

                itemsContainer.getChildren().add(row);
                priceFields.add(priceField);
                quantityFields.add(quantityField);
                itemTotalLabels.add(itemTotalValueLabel);
            }

            if (isArabicSelected()) {
                itemsContainer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            } else {
                itemsContainer.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
        }

        @FXML
        private void handleCalculateTotal() {
            if (priceFields.isEmpty()) {
                showError(getBundleText("error.enter.items.first"));
                return;
            }

            double[] prices = new double[priceFields.size()];
            int[] quantities = new int[quantityFields.size()];

            for (int i = 0; i < priceFields.size(); i++) {
                try {
                    double price = Double.parseDouble(priceFields.get(i).getText().trim());
                    int quantity = Integer.parseInt(quantityFields.get(i).getText().trim());

                    if (price < 0 || quantity < 0) {
                        showError(getBundleText("error.nonnegative.values"));
                        return;
                    }

                    prices[i] = price;
                    quantities[i] = quantity;

                    double itemTotal = calculator.calculateItemTotal(price, quantity);
                    itemTotalLabels.get(i).setText(df.format(itemTotal));

                } catch (NumberFormatException e) {
                    showError(getBundleText("error.invalid.price.quantity") + " " + (i + 1));
                    return;
                }
            }

            double total = calculator.calculateCartTotal(prices, quantities);
            totalValueLabel.setText(df.format(total));
        }

        private Locale mapLanguageToLocale(String language) {
            return switch (language) {
                case "Finnish" -> new Locale("fi", "FI");
                case "Swedish" -> new Locale("sv", "SE");
                case "Japanese" -> new Locale("ja", "JP");
                case "Arabic" -> new Locale("ar", "AR");
                default -> new Locale("en", "US");
            };
        }

        private ResourceBundle currentBundle() {
            Locale locale = mapLanguageToLocale(languageComboBox.getValue());
            return ResourceBundle.getBundle("MessagesBundle", locale);
        }

        private String getBundleText(String key) {
            return currentBundle().getString(key);
        }

        private boolean isArabicSelected() {
            return "Arabic".equals(languageComboBox.getValue());
        }

        private void showError(String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(getBundleText("error.title"));
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}