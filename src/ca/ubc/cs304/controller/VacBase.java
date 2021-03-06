package ca.ubc.cs304.controller;

import ca.ubc.cs304.database.DatabaseConnectionHandler;
import ca.ubc.cs304.delegates.LoginWindowDelegate;

import ca.ubc.cs304.delegates.TerminalTransactionsDelegate;

import ca.ubc.cs304.model.distributor.Facility;
import ca.ubc.cs304.model.patient.PatientAccount;

import ca.ubc.cs304.model.patient.PreExistingCondition;
import ca.ubc.cs304.model.vaccine.Vaccine;
import ca.ubc.cs304.ui.LoginWindow;
import ca.ubc.cs304.ui.TerminalTransactions;

/**
 * This is the main controller class that will orchestrate everything.
 */
public class VacBase implements LoginWindowDelegate, TerminalTransactionsDelegate {
	private DatabaseConnectionHandler dbHandler = null;
	private LoginWindow loginWindow = null;

	public VacBase() {
		dbHandler = new DatabaseConnectionHandler();
	}

	public void start() {
		loginWindow = new LoginWindow();
		loginWindow.showFrame(this);
	}

	/**
	 * LoginWindowDelegate Implementation
	 *
	 * connects to Oracle database with supplied username and password
	 */
	public void login(String username, String password) {
		boolean didConnect = dbHandler.login(username, password);

		if (didConnect) {
			// Once connected, remove login window and start text transaction flow
			loginWindow.dispose();

			TerminalTransactions transaction = new TerminalTransactions();
			transaction.setupDatabase(this);
			transaction.showMainMenu(this);
		} else {
			loginWindow.handleLoginFailed();

			if (loginWindow.hasReachedMaxLoginAttempts()) {
				loginWindow.dispose();
				System.out.println("You have exceeded your number of allowed attempts");
				System.exit(-1);
			}
		}
	}

	// QUERIES /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void selectionQuery() {
		dbHandler.selectionQuery();
	}

	public void searchForPatientAccountQuery(int CareCardNumber) { dbHandler.searchForPatientAccountQuery(CareCardNumber); }

	public void projectionQuery() { dbHandler.projectionQuery("test"); }

	public void joinAggregateWithVaccineRecordQuery() { dbHandler.joinAggregateWithVaccineRecordQuery(1000000000); }

	public void aggregationQueryTotalVaccines() { dbHandler.aggregationQueryTotalVaccines(); }

	public void divisionQuery() { dbHandler.divisionQuery(); }

	public void nestedAggregationQuery() { dbHandler.nestedAggregationQuery(); }


	// PATIENTACCOUNT //////////////////////////////////////////////////////////////////////////////////////////////////

	public void insertPatientAccount(PatientAccount model) { dbHandler.insertPatientAccount(model); }
	public void deletePatientAccount(int careCardNumber) { dbHandler.deletePatientAccount(careCardNumber); }
	public void updatePatientAccount(int CareCardNumber, String newUserName)  {dbHandler.updatePatientAccount(CareCardNumber, newUserName); }

	public void showPatientAccount() {
		PatientAccount[] models = dbHandler.getPatientAccountInfo();

		for (int i = 0; i < models.length; i++) {
			PatientAccount model = models[i];

			// simplified output formatting; truncation may occur
			System.out.printf("%-10.10s", model.getCareCardNumber());
			System.out.printf("%-20.20s", model.getFullName());
//			if (model.getAddress() == null) {
//				System.out.printf("%-20.20s", " ");
//			} else {
//				System.out.printf("%-20.20s", model.getAddress());
//			}
			System.out.printf("%-15.15s", model.getDate());
//			if (model.getPhoneNumber() == 0) {
//				System.out.printf("%-15.15s", " ");
//			} else {
//				System.out.printf("%-15.15s", model.getPhoneNumber());
//			}
			System.out.printf("%-20.20s", model.getUsername());

			System.out.println();
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////





	// VACCINE /////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void deleteVaccine(int vacID) {
		dbHandler.deleteVaccine(vacID);
	}

	public void insertVaccine(Vaccine model) {
		dbHandler.insertVaccine(model);
	}

	public void showVaccine() {
		Vaccine[] models = dbHandler.getVaccineInfo();

		for (int i = 0; i < models.length; i++) {
			Vaccine model = models[i];

			// simplified output formatting; truncation may occur
			System.out.printf("%-10.10s", model.getVacID());
			System.out.printf("%-10.10s", model.getVacName());
			System.out.printf("%-20.20s", model.getType());
			System.out.printf("%-15.15s", model.getDosage());

			System.out.println();
		}
	}

	public void updateVaccine(int vacID, double dosage) {
		dbHandler.updateVaccine(vacID, dosage);
	}

	// FACILITY ////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void deleteFacility(int FID) {
		dbHandler.deleteFacility(FID);
	}

	public void insertFacility(Facility model) {
		dbHandler.insertFacility(model);
	}

	public void showFacility() {
		Facility[] models = dbHandler.getFacilityInfo();

		for (int i = 0; i < models.length; i++) {
			Facility model = models[i];

			// simplified output formatting; truncation may occur
			System.out.printf("%-10.10s", model.getFacilityID());
			System.out.printf("%-10.10s", model.getFacilityName());
			System.out.printf("%-20.20s", model.getAddress());

			System.out.println();
		}
	}

	public void updateFacility(int FID, String address) {
		dbHandler.updateFacility(FID, address);
	}

	// CONDITION ///////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insertCondition(PreExistingCondition model) {
		dbHandler.insertCondition(model);
	}

	public void deleteCondition(int careCardNumber) {
		dbHandler.deleteCondition(careCardNumber, "Headache");
	}

	public void showCondition() {
		PreExistingCondition[] models = dbHandler.getConditionInfo(1000000001);

		for (int i = 0; i < models.length; i++) {
			PreExistingCondition model = models[i];

			// simplified output formatting; truncation may occur
			System.out.printf("%-10.10s", model.getCareCardNumber());
			System.out.printf("%-20.20s", model.getCondition());

			System.out.println();
		}
	}

	public void updateCondition(int careCardNumber, String condition) {
		dbHandler.updateCondition(careCardNumber, condition);
	}

	/**
	 * TerminalTransactionsDelegate Implementation
	 *
	 * The TerminalTransaction instance tells us that it is done with what it's
	 * doing so we are cleaning up the connection since it's no longer needed.
	 */
	public void terminalTransactionsFinished() {
		dbHandler.close();
		dbHandler = null;

		System.exit(0);
	}

	/**
	 * TerminalTransactionsDelegate Implementation
	 *
	 * The TerminalTransaction instance tells us that the user is fine with dropping any existing table
	 * called branch and creating a new one for this project to use
	 */
	public void databaseSetup() {
		dbHandler.databaseSetup();;

	}

	/**
	 * Main method called at launch time
	 */
	public static void main(String args[]) {
		VacBase vacBase = new VacBase();
		vacBase.start();
	}

}
