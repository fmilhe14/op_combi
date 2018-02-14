package gui;

import components.Grue;
import components.Navire;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.VariableFactory;
import planning.GlobalPlanning;
import planning.PlanningGrue;
import planning.PlanningNavire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Main extends Application {

    private ArrayList<Grue> grues;
    private ArrayList<Navire> navires;
    private Solver solver;

    private int longueurQuai;
    private int dateFinDeJournee;

    private int currentIdGrue;
    private int currentIdNavire;

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.longueurQuai = 0;
        this.dateFinDeJournee = 0;
        this.grues = new ArrayList<>();
        this.navires = new ArrayList<>();

        this.currentIdGrue = 0;
        this.currentIdNavire = 1;
        this.solver = new Solver("Résolution planning");


        primaryStage.setTitle("Planning");


        // Create the registration form pane
        GridPane gridPane = createRegistrationFormPane();
        // Create a scene with the registration form gridPane as the root node.

        addUIControls(gridPane, primaryStage);

        Scene scene = new Scene(gridPane, 800, 500);
        // Set the scene in primary stage
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private GridPane createRegistrationFormPane() {
        // Instantiate a new Grid Pane
        GridPane gridPane = new GridPane();

        // Position the pane at the center of the screen, both vertically and horizontally
        gridPane.setAlignment(Pos.CENTER);

        // Set a padding of 20px on each side
        gridPane.setPadding(new Insets(40, 40, 40, 40));

        // Set the horizontal gap between columns
        gridPane.setHgap(50);

        // Set the vertical gap between rows
        gridPane.setVgap(10);

        // Add Column Constraints

        // columnOneConstraints will be applied to all the nodes placed in column one.
        ColumnConstraints columnOneConstraints = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        columnOneConstraints.setHalignment(HPos.RIGHT);

        // columnTwoConstraints will be applied to all the nodes placed in column two.
        ColumnConstraints columnTwoConstrains = new ColumnConstraints(200, 200, Double.MAX_VALUE);
        columnTwoConstrains.setHgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(columnOneConstraints, columnTwoConstrains);

        return gridPane;
    }

    private void addUIControls(GridPane gridPane, Stage primaryStage) {
        // Add Header
        Label headerLabel = new Label("Planning :");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gridPane.add(headerLabel, 0, 0, 2, 1);
        GridPane.setHalignment(headerLabel, HPos.CENTER);
        GridPane.setMargin(headerLabel, new Insets(20, 0, 20, 0));

        // Add Name Label
        Label nameLabel = new Label("Taille du Quai :");
        gridPane.add(nameLabel, 0, 1);

        // Add Name Text Field
        TextField tailleDuQuai = new TextField();
        tailleDuQuai.setPrefHeight(40);
        gridPane.add(tailleDuQuai, 1, 1);

        tailleDuQuai.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue) {
                    try {
                        longueurQuai = Integer.parseInt(tailleDuQuai.getText());
                    }
                    catch (NumberFormatException o){

                    }
                }
            }
        });


        // Add heures Label
        Label heureDeJournee = new Label("Nb H/Jour :");
        gridPane.add(heureDeJournee, 0, 2);

        // Add heures Text Field
        TextField heureJournee = new TextField();
        heureJournee.setPrefHeight(40);
        gridPane.add(heureJournee, 1, 2);

        heureJournee.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue) {
                    try {
                        dateFinDeJournee = Integer.parseInt(heureJournee.getText())*4;
                    }
                    catch (NumberFormatException o){

                    }
                }
            }
        });

        // Add heures Label
        Label ajouterGrue = new Label("Ajouter Grue :");
        gridPane.add(ajouterGrue, 0, 3);

        Button buttonGrues = new Button("Ajouter une grue"); // the button
        gridPane.add(buttonGrues, 1, 3); // add the button to the root

        // add action listener, I will use the lambda style (which is data and code at the same time, read more about it in Oracle documentation)
        buttonGrues.setOnAction(e -> {
            createGrueForm(primaryStage);
            primaryStage.close();
        });

        // Add heures Label
        Label ajouterNavire = new Label("Ajouter Navire :");
        gridPane.add(ajouterNavire, 0, 4);

        Button buttonNavire = new Button("Ajouter un navire"); // the button
        gridPane.add(buttonNavire, 1, 4); // add the button to the root

        // add action listener, I will use the lambda style (which is data and code at the same time, read more about it in Oracle documentation)
        buttonNavire.setOnAction(e -> {

            if(grues.size() == 0){

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Attention");
                alert.setHeaderText("Grues manquantes");
                alert.setContentText("Il faut avoir ajouté au préalable au moins une grue !");

                alert.showAndWait();
            }

            else {


                createNavireForm(primaryStage);
                primaryStage.close();
            }
        });


        // Add Submit Button
        Button submitButton = new Button("Submit");
        submitButton.setPrefHeight(40);
        submitButton.setDefaultButton(true);
        submitButton.setPrefWidth(100);
        gridPane.add(submitButton, 0, 5, 2, 1);
        GridPane.setHalignment(submitButton, HPos.CENTER);
        GridPane.setMargin(submitButton, new Insets(20, 0, 20, 0));

        submitButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {


                if(grues.size() == 0 || navires.size() == 0 || longueurQuai == 0 || dateFinDeJournee == 0){

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Attention");
                    alert.setHeaderText("Valeur manquante");
                    alert.setContentText("Attention, vous avez oublié de remplir un champ, ou d'ajouter des grues/navires pour la journée");

                    alert.showAndWait();
                }

                else {

                    navires.sort(Comparator.comparing(Navire::getId));

                     PlanningNavire planningNavire = new PlanningNavire(longueurQuai, dateFinDeJournee, navires.toArray(new Navire[navires.size()]), solver);
                     PlanningGrue planningGrue = new PlanningGrue(dateFinDeJournee, longueurQuai,solver, navires.toArray(new Navire[navires.size()]));


                    GlobalPlanning globalPlanning = new GlobalPlanning(planningNavire, planningGrue, solver);

                    solver.findSolution();

                    String[][] planningGrueMatrix = new String[planningGrue.getPlanningGrue().length+1][planningGrue.getPlanningGrue()[0].length];

                    for(int k = 0; k < planningGrue.getPlanningGrue()[0].length; k++) planningGrueMatrix[0][k] = ""+k ;

                    for(int i = 0; i < planningGrue.getPlanningGrue().length; i++){
                        for(int j = 0; j < planningGrue.getPlanningGrue()[0].length; j++){
                            planningGrueMatrix[i][j] = ""+planningGrue.getPlanningGrue()[i][j].getValue();
                        }
                    }

                    String[][] planningNavireMatrix = new String[planningNavire.getPlanningNavire().length+1][planningNavire.getPlanningNavire()[0].length];


                    for(int k = 0; k < planningNavire.getPlanningNavire()[0].length; k++) planningNavireMatrix[0][k] = ""+k ;

                    for(int i = 0; i < planningNavire.getPlanningNavire().length; i++){
                        for(int j = 0; j < planningNavire.getPlanningNavire()[0].length; j++){
                            planningNavireMatrix[i][j] = ""+planningNavire.getPlanningNavire()[i][j].getValue();
                        }
                    }

                    drawMatrix(planningGrueMatrix, primaryStage, "Planning Grue");
                    drawMatrix(planningNavireMatrix, primaryStage, "Planning Navire");

                    Chatterbox.printStatistics(solver);
                }
            }
        });
    }

    private void createGrueForm(Stage primaryStage) {

        GridPane gridPane = new GridPane();

        // Position the pane at the center of the screen, both vertically and horizontally
        gridPane.setAlignment(Pos.CENTER);

        // Set a padding of 20px on each side
        gridPane.setPadding(new Insets(40, 40, 40, 40));

        // Set the horizontal gap between columns
        gridPane.setHgap(50);

        // Set the vertical gap between rows
        gridPane.setVgap(10);

        // Add Column Constraints

        // columnOneConstraints will be applied to all the nodes placed in column one.
        ColumnConstraints columnOneConstraints = new ColumnConstraints(200, 200, Double.MAX_VALUE);
        columnOneConstraints.setHalignment(HPos.LEFT);

        // columnTwoConstraints will be applied to all the nodes placed in column two.
        ColumnConstraints columnTwoConstrains = new ColumnConstraints(200, 200, Double.MAX_VALUE);
        columnTwoConstrains.setHgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(columnOneConstraints, columnTwoConstrains);

        Label h = new Label("Grues");
        h.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gridPane.add(h, 0, 0, 2, 1);
        GridPane.setHalignment(h, HPos.CENTER);
        GridPane.setMargin(h, new Insets(20, 0, 20, 0));

        // Add Name Label
        Label nameLabel = new Label("Capacité de la grue :");
        gridPane.add(nameLabel, 0, 1);

        // Add Name Text Field
        TextField capaciteGrue = new TextField();
        capaciteGrue.setPrefHeight(40);
        capaciteGrue.setPrefWidth(20);
        gridPane.add(capaciteGrue, 1, 1);


        Button submitButton = new Button("Submit");
        submitButton.setPrefHeight(40);
        submitButton.setDefaultButton(true);
        submitButton.setPrefWidth(100);
        gridPane.add(submitButton, 0, 4, 2, 1);
        GridPane.setHalignment(submitButton, HPos.CENTER);
        GridPane.setMargin(submitButton, new Insets(20, 0, 20, 0));
        Scene secondScene = new Scene(gridPane, 500, 500);
        Stage secondStage = new Stage();

        submitButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {

                Grue grue = new Grue(currentIdGrue, Integer.parseInt(capaciteGrue.getText()), longueurQuai, dateFinDeJournee, solver);
                grues.add(grue);
                currentIdGrue++;
                secondStage.close();
                primaryStage.show();


            }
        });

        secondStage.setScene(secondScene); // set the scene
        secondStage.setTitle("Second Form");
        secondStage.show();


    }

    private void drawMatrix(String[][] array, Stage primaryStage, String title){

        Stage secondStage = new Stage();

        secondStage.setTitle(title);

        GridPane root = new GridPane();
        ObservableList<String[]> data = FXCollections.observableArrayList();
        data.addAll(Arrays.asList(array));
        data.remove(0);//remove titles from data
        TableView<String[]> table = new TableView<>();
        for (int i = 0; i < array[0].length; i++) {
            TableColumn tc = new TableColumn(array[0][i]);
            final int colNo = i;
            tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<String[], String> p) {
                    return new SimpleStringProperty((p.getValue()[colNo]));
                }
            });
            tc.setPrefWidth(90);
            table.getColumns().add(tc);
        }
        table.setItems(data);
        root.getChildren().add(table);

        Scene secondScene = new Scene(root, 500, 500);

        secondStage.setScene(secondScene);
        primaryStage.close();
        secondStage.show();

    }

    private void createNavireForm(Stage primaryStage) {

        GridPane gridPane = new GridPane();

        // Position the pane at the center of the screen, both vertically and horizontally
        gridPane.setAlignment(Pos.CENTER);

        // Set a padding of 20px on each side
        gridPane.setPadding(new Insets(40, 40, 40, 40));

        // Set the horizontal gap between columns
        gridPane.setHgap(50);

        // Set the vertical gap between rows
        gridPane.setVgap(10);

        // Add Column Constraints

        // columnOneConstraints will be applied to all the nodes placed in column one.
        ColumnConstraints columnOneConstraints = new ColumnConstraints(200, 200, Double.MAX_VALUE);
        columnOneConstraints.setHalignment(HPos.LEFT);

        // columnTwoConstraints will be applied to all the nodes placed in column two.
        ColumnConstraints columnTwoConstrains = new ColumnConstraints(200, 200, Double.MAX_VALUE);
        columnTwoConstrains.setHgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(columnOneConstraints, columnTwoConstrains);

        Label h = new Label("Navires");
        h.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gridPane.add(h, 0, 0, 2, 1);
        GridPane.setHalignment(h, HPos.CENTER);
        GridPane.setMargin(h, new Insets(20, 0, 20, 0));

        // Add Name Label
        Label conteneurs = new Label("Nombre de conteneurs :");
        gridPane.add(conteneurs, 0, 1);

        // Add Name Text Field
        TextField nbConteneurs = new TextField();
        nbConteneurs.setPrefHeight(40);
        nbConteneurs.setPrefWidth(20);
        gridPane.add(nbConteneurs, 1, 1);

        // Add Name Label
        Label depart = new Label("Date de départ prévue :");
        gridPane.add(depart
                , 0, 2);

        // Add Name Text Field
        TextField departPrevu = new TextField();
        departPrevu.setPrefHeight(40);
        departPrevu.setPrefWidth(20);
        gridPane.add(departPrevu, 1, 2);


        // Add Name Label
        Label taille = new Label("Taille du Navire :");
        gridPane.add(taille, 0, 3);

        // Add Name Text Field
        TextField tailleDuNavire = new TextField();
        tailleDuNavire.setPrefHeight(40);
        tailleDuNavire.setPrefWidth(20);
        gridPane.add(tailleDuNavire, 1, 3);

        // Add Name Label
        Label cout = new Label("Coût du retard :");
        gridPane.add(cout, 0, 4);

        // Add Name Text Field
        TextField coutDeRetard = new TextField();
        coutDeRetard.setPrefHeight(40);
        coutDeRetard.setPrefWidth(20);
        gridPane.add(coutDeRetard, 1, 4);


        Button submitButton = new Button("Submit");
        submitButton.setPrefHeight(40);
        submitButton.setDefaultButton(true);
        submitButton.setPrefWidth(100);
        gridPane.add(submitButton, 0, 5, 2, 1);
        GridPane.setHalignment(submitButton, HPos.CENTER);
        GridPane.setMargin(submitButton, new Insets(20, 0, 20, 0));
        Scene secondScene = new Scene(gridPane, 500, 500);
        Stage secondStage = new Stage();

        submitButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {

                Navire navire = new Navire(currentIdNavire, Integer.parseInt(nbConteneurs.getText()), Integer.parseInt(tailleDuNavire.getText()), longueurQuai,
                        Integer.parseInt(departPrevu.getText()),
                        Integer.parseInt(coutDeRetard.getText()),  dateFinDeJournee,
                        grues.toArray(new Grue[grues.size()]), solver);
                navires.add(navire);
                currentIdNavire++;
                secondStage.close();
                primaryStage.show();


            }
        });

        secondStage.setScene(secondScene); // set the scene
        secondStage.setTitle("Second Form");
        secondStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}

