package ca.ubc.cs304.project.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import static ca.ubc.cs304.project.ui.HelpfulFunctions.*;

public class PatientPage {
    Scene page;
    Button viewRecord;
    Button viewChildrenRecord;
    Button viewConditions;
    Button vacLocations;

    public PatientPage(String name) {
        VBox pane = new VBox(30);
        pane.setAlignment(Pos.CENTER);
        VBox menuButtons = new VBox(10);
        menuButtons.setAlignment(Pos.CENTER);

        Label fullName = new Label(name);
        fullName.setFont(new Font("Montserrat", 64));

        viewRecord = makeButton(new Button("View Vaccine Record"));
        setButtonSpecs(viewRecord);

        viewChildrenRecord = makeButton(new Button("View Minor's Record"));
        setButtonSpecs(viewChildrenRecord);

        viewConditions = makeButton(new Button("View Preexisting Conditions"));
        setButtonSpecs(viewConditions);

        vacLocations = makeButton(new Button("Where can I get vaccinated"));
        setButtonSpecs(vacLocations);

        menuButtons.getChildren().addAll(viewRecord, viewChildrenRecord, viewConditions, vacLocations);

        pane.getChildren().addAll(fullName, menuButtons);
        setBackgroundColor(pane);
        addButtonFunctionality();
        page = new Scene(pane, pageWidth, pageHeight);

    }

    private void addButtonFunctionality() {
        viewRecord.setOnAction((event -> {
            // Todo: Add functionality
            System.out.println("View Vaccine Record");
        }));
        viewChildrenRecord.setOnAction((event -> {
            // Todo: Add functionality
            System.out.println("View Minor's Record");
        }));
        viewConditions.setOnAction((event -> {
            // Todo: Add functionality
            System.out.println("View Preexisting Conditions");
        }));
        vacLocations.setOnAction((event -> {
            // Todo: Add functionality
            System.out.println("Where can I get vaccinated?");
        }));
    }

    private void setButtonSpecs(Button button) {
        button.setFont(new Font("Montserrat", 24));
        button.setMinSize(420, 56);
        button.setMaxSize(420, 56);
    }

    public Scene getPage() {
        return page;
    }

}
