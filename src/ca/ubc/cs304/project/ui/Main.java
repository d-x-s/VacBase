package ca.ubc.cs304.project.ui;

import ca.ubc.cs304.database.DatabaseConnectionHandler;
import ca.ubc.cs304.model.distributor.Facility;
import ca.ubc.cs304.model.patient.AgeBracketLookup;
import ca.ubc.cs304.model.patient.LoginInfo;
import ca.ubc.cs304.model.patient.PatientAccount;

import ca.ubc.cs304.model.patient.VaccineRecordAggregation;
import ca.ubc.cs304.model.statistics.NestedAggregation;
import ca.ubc.cs304.model.vaccine.Nurse;
import ca.ubc.cs304.model.vaccine.Vaccine;

import ca.ubc.cs304.model.patient.PreExistingCondition;

import javafx.application.Application;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Array;
import java.sql.Date;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static javafx.application.Platform.exit;

public class Main extends Application {

    Stage window;
    Scene scene;

    PatientPage patientPage;
    LoginPage loginPage;
    CreateAccountPage createPage;
    TabPage tabPage;
    ConditionPage conditionPage;
    PatientVaccineCarePage vaccineCarePage;

    PatientAccount currentUser;
    DatabaseConnectionHandler dbh;

    private BufferedReader bufferedReader = null;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setOnCloseRequest(e -> dbh.close());

        dbh = new DatabaseConnectionHandler();
        boolean isConnected = false;
        int count = 0;

        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter Oracle login in the console");
        String Username = null;
        String Password = null;
        try {
            while (Username == null)
            {
                System.out.println("Enter username");
                Username = bufferedReader.readLine();
            }

            while (Password == null)
            {
                System.out.println("Enter password");
                Password = bufferedReader.readLine();
            }

            dbh.login(Username, Password);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        isConnected = dbh.login("ora_dsong04", "a29241874");
//        while (!isConnected) {
//            if (count == 0) {
//                isConnected = dbh.login("ora_akang28", "a74159187");
//                System.out.println("Successfully Logged in as Alice");
//            } else if (count == 1) {
//                isConnected = dbh.login("ora_dsong04", "a29241874");
//                System.out.println("Successfully Logged in as Davis");
//            } else if (count == 2){
//                isConnected = dbh.login("ora_jyu19", "a67758979");
//                System.out.println("Successfully Logged in as Jonathan");
//            } else {
//                count = 0; /* loop*/
//            }
//            count++;
//        }
        dbh.databaseSetup();


        patientPage = new PatientPage();
        loginPage = new LoginPage();
        createPage = new CreateAccountPage();
        tabPage = new TabPage(dbh);
        conditionPage = new ConditionPage(dbh);
        vaccineCarePage = new PatientVaccineCarePage();
        addFunctionality();
        scene = loginPage.getPage();

        window.setScene(scene);
        window.setTitle("VacBase");
        window.show();
    }


    private void addFunctionality() {
        addFunctionalityTabPage();
        addFunctionalityPatientPage();
        addFunctionalityConditionPage();
        addFunctionalityCreatePage();
        addFunctionalityLoginPage();
    }

    private void addFunctionalityTabPage() {
        addFunctionalityPatientTab();
        addFunctionalityFacilityTab();
        addFunctionalityVaccineTab();
        addFunctionalityPatientVaccineCarePage();
        addFunctionalityStatisticsTab();
    }

    //region tabPage subroutines
    private void addFunctionalityPatientTab() {
        tabPage.getSearchBar().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                int careCardNum = Integer.parseInt(tabPage.getSearchBar().getText());
                PatientAccount user = dbh.getSpecificPatientAccount(careCardNum);
                currentUser = user;
                patientPage.setCurrentUser(user);
                vaccineCarePage.setPatientView(false);
                conditionPage.setCareCardNumber(careCardNum);
                window.setScene(patientPage.getPage());
            }
        });
    }

    private void addFunctionalityStatisticsTab() {
        tabPage.getDivisionButton().setOnAction(event -> {
            ArrayList<PatientAccount> list = dbh.divisionQuery();
            for (PatientAccount account : list) {
                System.out.println(account);
                tabPage.getDivisionList().add(account);
            }
        });
        tabPage.getNestedAggregationButton().setOnAction(event -> {
            ArrayList<NestedAggregation> list = dbh.nestedAggregationQuery();
            for (NestedAggregation aggregation : list) {
                tabPage.getNestedAggregationList().add(aggregation);
            }
        });
        tabPage.getAggregationButton().setOnAction(event -> {
            tabPage.getAggregationLabel().setText(dbh.aggregationQueryTotalVaccines());
        });
    }

    private void addFunctionalityFacilityTab() {
        // insertion
        tabPage.getInsertFacilityButton().setOnAction(e -> {
            Facility temp;
            try {
                temp = new Facility(Integer.parseInt(tabPage.getFacilityIDField().getText()),
                        tabPage.getFacilityNameField().getText(),
                        tabPage.getAddressField().getText());
                tabPage.getFacilityList().add(temp);
                dbh.insertFacility(temp);
                tabPage.getFacilityIDField().clear();
                tabPage.getFacilityNameField().clear();
                tabPage.getAddressField().clear();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        });

        // deletion
        tabPage.getFacilityView().setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                Facility selectedItem = tabPage.getFacilityView().getSelectionModel().getSelectedItem();
                tabPage.getFacilityList().remove(selectedItem);
                dbh.deleteFacility(selectedItem.getFacilityID());
            }
        });

        // update
        tabPage.getUpdateFacilityButton().setOnAction(event -> {
            int selectedIndex = tabPage.getFacilityView().getSelectionModel().getSelectedIndex();
            int IDToUpdate = Integer.parseInt(tabPage.getFacilityIDField().getText());
            String name = tabPage.getFacilityNameField().getText();
            String addressToUpdate = tabPage.getAddressField().getText();
            Facility updated = new Facility(IDToUpdate, name, addressToUpdate);
            dbh.updateFacility(IDToUpdate, addressToUpdate);

            tabPage.getFacilityList().set(selectedIndex, updated);
            tabPage.getFacilityIDField().clear();
            tabPage.getAddressField().clear();
            tabPage.getFacilityNameField().clear();
        });
    }


    private void addFunctionalityVaccineTab() {
        tabPage.getViewButton().setOnAction(event -> {
            tabPage.getVaccineList().clear();
            tabPage.getVaccineListView().getColumns().clear();
            tabPage.getVaccineListView().getColumns().addAll(tabPage.getVacIDColumn(), tabPage.getVacNameColumn());
            String sqlColumn = tabPage.generateTableView();
            ArrayList<Vaccine> projection = dbh.projectionQuery(sqlColumn);
            for (Vaccine vaccine: projection) {
                tabPage.getVaccineList().add(vaccine);
            }
            tabPage.getVaccineListView().setItems(tabPage.getVaccineList());
        });
    }
    //endregion

    private void addFunctionalityPatientPage() {
        patientPage.getViewRecord().setOnAction(event -> {
            ArrayList<VaccineRecordAggregation> list = dbh.joinAggregateWithVaccineRecordQuery(currentUser.getCareCardNumber());
            for (VaccineRecordAggregation v : list) {
                vaccineCarePage.getVaccineRecordList().add(v);
            }
            window.setScene(vaccineCarePage.getPage());
        });
        patientPage.getDeleteAccount().setOnAction(event -> {
            window.setScene(loginPage.getPage());
            if (currentUser == null) {
                exit();
            }
            int careCardNumber = currentUser.getCareCardNumber();
            currentUser = null;
            dbh.deletePatientAccount(careCardNumber);
        });
        patientPage.getViewConditions().setOnAction(event -> {
            window.setScene(conditionPage.getPage());
            conditionPage.setUpTable();
        });
        patientPage.getLogOut().setOnAction(event -> {
            window.setScene(loginPage.getPage());
            currentUser = null;
            conditionPage.setCareCardNumber(0);
            vaccineCarePage.setPatientView(true);
            vaccineCarePage.getVaccineRecordList().clear();
        });
    }

    private void addFunctionalityConditionPage() {
        conditionPage.getBackButton().setOnAction( event -> {
            window.setScene(patientPage.getPage());
        });

        conditionPage.getInsertButton().setOnAction( event -> {
            PreExistingCondition temp;
            try {
                temp = new PreExistingCondition(conditionPage.getCareCardNumber(),
                                                conditionPage.getConditionInput().getText());
                conditionPage.getConditions().add(temp);
                dbh.insertCondition(temp);
                conditionPage.getConditionInput().clear();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        });

        conditionPage.getViewConditions().setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                PreExistingCondition selected = conditionPage.getViewConditions().getSelectionModel().getSelectedItem();
                conditionPage.getConditions().remove(selected);
                dbh.deleteCondition(conditionPage.getCareCardNumber(), selected.getCondition().trim());
            }
        });
    }

    private void addFunctionalityCreatePage() {
        createPage.getBackButton().setOnAction(event -> {
            window.setScene(loginPage.getPage());
        });
        createPage.getConfirmButton().setOnAction(event -> {
            /* TODO: Creates account.
            Utilize createPage.getFullNameField(); createPage.getCareCardNumberField();
                createPage.getPasswordField(); createPage.getUsernameField();
              - .getText()
              - .getClear()
              There's a function called highlightField(TextField field) in helpful functions that turns a textfield red
              might be useful for indicating errors
             */
            String name = createPage.getFullNameField().getText();
            int ccn = Integer.parseInt(createPage.getCareCardNumberField().getText());
            Date dob = Date.valueOf(createPage.getDobField().getValue());
            String password = createPage.getPasswordField().getText();
            String username = createPage.getUsernameField().getText();

            PatientAccount newUser = new PatientAccount(ccn, name, dob, username);
            LoginInfo newUserLogin = new LoginInfo(username, password);
            AgeBracketLookup newUserAgeBracket = new AgeBracketLookup(dob, getAgeBracket(dob));
            dbh.insertPatientAccount(newUser);
            dbh.insertLoginInfo(newUserLogin);
            dbh.insertAgeBracket(newUserAgeBracket);
            window.setScene(loginPage.getPage());
        });
    }

    private void addFunctionalityLoginPage() {
        loginPage.getLoginPatient().setOnAction(event -> {
            currentUser = dbh.loginToAccount(loginPage.getUsernameField().getText(), loginPage.getPasswordField().getText());
            patientPage.setCurrentUser(currentUser);
            conditionPage.setCareCardNumber(currentUser.getCareCardNumber());

            loginPage.getUsernameField().clear();
            loginPage.getPasswordField().clear();
            window.setScene(patientPage.getPage());
        });
        loginPage.getLoginAdmin().setOnAction(event -> {
            window.setScene(tabPage.getPage());
        });

        loginPage.getCreateAccount().setOnAction(event -> {
            window.setScene(createPage.getPage());
        });
    }

    private void addFunctionalityPatientVaccineCarePage() {
        vaccineCarePage.getBackButton().setOnAction(event -> {
            window.setScene(patientPage.getPage());
        });
        vaccineCarePage.getInsertButton().setOnAction(event -> {
            Nurse nurse = dbh.getSpecificNurse(Integer.parseInt(vaccineCarePage.getNurseIDField().getText()));
            Vaccine vaccine = dbh.getSpecificVaccine(Integer.parseInt(vaccineCarePage.getVacIDField().getText()));
            Facility facility = dbh.getSpecificFacility(Integer.parseInt(vaccineCarePage.getFacilityIDField().getText()));
            int newID = dbh.getMaxVaccineCareCardID() + 1;
            System.out.println("The next id is: " + newID);
            int newEventID = dbh.getMaxEventID() + 1;
            System.out.println("The next event id is: " + newEventID);
            Date currentDate = new java.sql.Date(System.currentTimeMillis());

            VaccineRecordAggregation newRecord = new VaccineRecordAggregation(currentUser.getCareCardNumber(), newID, newEventID, nurse.getNurseID(),
                    vaccine.getVacID(),facility.getFacilityID(), currentDate, vaccine.getVacName(), facility.getFacilityName(), nurse.getNurseName());
            System.out.println(newRecord);
            dbh.insertAdministeredVaccGivenToPatient(newRecord.makeAdministeredVaccGivenToPatient());
            System.out.println(newRecord.makeAdministeredVaccGivenToPatient());
            dbh.insertInclude(newRecord.makeInclude());
            System.out.println(newRecord.makeInclude());
            dbh.insertHappensIn(newRecord.makeHappensIn());
            System.out.println(newRecord.makeHappensIn());
            dbh.insertVaccineRecord(newRecord.makeVaccineRecord());
            System.out.println(newRecord.makeVaccineRecord());
            vaccineCarePage.getVaccineRecordList().add(newRecord);

            vaccineCarePage.getFacilityIDField().clear();
            vaccineCarePage.getNurseIDField().clear();
            vaccineCarePage.getVacIDField().clear();
        });
    }

    public String getAgeBracket(Date date) {
        long diff = TimeUnit.DAYS.convert(Date.valueOf(java.time.LocalDate.now()).getTime() - date.getTime(), TimeUnit.MILLISECONDS);
        if (diff > 21900) {
            return "60+";
        } else if (diff > 16425) {
            return "45-59";
        } else if (diff > 10950) {
            return "30-44";
        } else if (diff > 6570) {
            return "18-29";
        } else {
            return "18-";
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

}
