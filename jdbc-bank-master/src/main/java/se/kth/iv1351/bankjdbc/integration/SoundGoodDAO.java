
package se.kth.iv1351.bankjdbc.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.kth.iv1351.bankjdbc.model.Instrument;
import se.kth.iv1351.bankjdbc.model.InstrumentDTO;

/**
 * This data access object (DAO) encapsulates all database calls in the
 * SoundGood
 * application. No code outside this class shall have any knowledge about the
 * database.
 */
public class SoundGoodDAO {

    private Connection connection;
    private PreparedStatement findAccountByNameStmt;

    private PreparedStatement addRental;
    private PreparedStatement terminateRental;
    private PreparedStatement checkStudentRentalsStmt;
    private PreparedStatement checkInstrumentRentalStmt;
    private PreparedStatement listInstrumentStmt;
    private PreparedStatement listAllInstrumentsStmt;

    /**
     * Constructs a new DAO object connected to the SoundGood database.
     */
    public SoundGoodDAO() throws SoundGoodException {
        try {
            System.out.println("Connecting to SoundGood.....");
            connectToSoundGoodDB();
            prepareStatements();
        } catch (ClassNotFoundException | SQLException exception) {
            throw new SoundGoodException("Could not connect to datasource.", exception);
        }
    }

    private void connectToSoundGoodDB() throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/soundgood",
                "postgres", "example");
        System.out.println("Connected!");
        // connection =
        // DriverManager.getConnection("jdbc:mysql://localhost:3306/bankdb",
        // "mysql", "mysql");
        connection.setAutoCommit(false);
    }

    /**
     * SELECT * FROM instrument WHERE type_of_instrument = 'Piano' AND instrument_id
     * NOT IN (SELECT rentingInstrument.instrument_id
     * FROM rentingInstrument
     * INNER JOIN instrument ON rentingInstrument.instrument_id =
     * instrument.instrument_id);
     * 
     * SELECT instrument_id FROM rentingInstrument WHERE current_date >= from_date
     * AND current_date < to_date;
     * 
     * @throws SQLException
     */

    /**
     * The sql queries
     * 
     * @throws SQLException
     */
    private void prepareStatements() throws SQLException {
        /**
         * Used in printListInstrument()
         * Finds all instruments where the type of instrument is x and the instrument is
         * not being registered as rented in rentingInstruments.
         * 2022-01-09 "The previous SQl querie only searched if instrument was NOT in
         * rentingInstrument, even if the rental had expired. Now changed to search NOT
         * (instrument exist in rentingInstruments and current_date is inbetween
         * from_to_date"
         */
        listInstrumentStmt = connection.prepareStatement("" +
                "SELECT *" +
                " FROM instrument" +
                " WHERE type_of_instrument = ? AND instrument_id" +
                " NOT IN (SELECT rentingInstrument.instrument_id" +
                " FROM rentingInstrument" +
                " INNER JOIN instrument ON rentingInstrument.instrument_id = instrument.instrument_id" +
                " AND current_date > from_date" +
                " AND current_date < to_date)");
        /**
         * Prints all instruments, that are not being rented out
         * used in printListAllInstruments()
         */
        listAllInstrumentsStmt = connection.prepareStatement("" +
                " SELECT * FROM instrument WHERE instrument_id" +
                " NOT IN (SELECT rentingInstrument.instrument_id" +
                " FROM rentingInstrument" +
                " INNER JOIN instrument ON rentingInstrument.instrument_id = instrument.instrument_id" +
                " AND current_date > from_date" +
                " AND current_date < to_date);");

        /**
         * Counts amount of rentals by a student where to_date is greater than
         * current_date.
         * Used in checkStudentRental()
         */
        checkStudentRentalsStmt = connection.prepareStatement("" +
                " SELECT COUNT(*) FROM rentingInstrument" +
                " WHERE student_ID = ? AND to_date > current_date");

        /**
         * Counts/checks if requested instrument is rented out or not.
         * Counts 1 for true and 0 for false.
         * Used in checkInstrumentRental()
         */
        checkInstrumentRentalStmt = connection.prepareStatement("" +
                " SELECT COUNT(*) FROM instrument" +
                " WHERE instrument.instrument_id = ? AND instrument.instrument_id NOT IN" +
                " (SELECT instrument_id FROM rentingInstrument" +
                " WHERE CURRENT_DATE < to_date AND  CURRENT_DATE > from_date)");
        /**
         * Registers a new rental to rentingInstruments with the request of student_id,
         * with requested instrument_id. from_date is set to current_date and default
         * to_date is set one year ahead.
         * Used in rentInstrument()
         */
        addRental = connection.prepareStatement("" +
                "INSERT INTO rentingInstrument Values" +
                "(?, ?, CURRENT_DATE, CURRENT_DATE + 365);");
        /**
         * Terminates rental by changing the to_date to current_date. Ends rental ahead
         * of default to_date.
         * used in terminateRental()
         */
        terminateRental = connection.prepareStatement("" +
                "UPDATE rentingInstrument" +
                " SET to_date = CURRENT_DATE" +
                " WHERE instrument_ID = ?;");
    }

    /**
     * Prints table with all instruments are available to rent.
     * Checks if the entered type of instrument exists in table "instrument"
     * && if the instrument ID is NOT in rentingInstrument.
     * 
     * @param instrument_type
     * @return Returns List<Instrument> with the available instruments with the
     *         columns;
     *         [instruments_id, instrument_type, instrument_brand, instrument_price]
     * @throws SoundGoodException
     */
    public List<Instrument> printListInstrument(String instrument_type) throws SoundGoodException {
        String failureMsg = "Could not search for specified instrument :(";
        ResultSet result = null;
        List<Instrument> instruments = new ArrayList<>();
        try {
            listInstrumentStmt.setString(1, instrument_type);
            result = listInstrumentStmt.executeQuery();
            while (result.next()) {
                instruments.add(new Instrument(
                        result.getInt("instrument_id"),
                        result.getString("type_of_instrument"),
                        result.getString("instrument_brand"),
                        result.getInt("instrument_price")));
            }
            commit(); // replace with method commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return instruments;
    }

    /**
     * Default list, lists all available instruments.
     * 
     * @return list of all available instruments of all types.
     * @throws SoundGoodException
     */
    public List<Instrument> printListAllInstruments() throws SoundGoodException {
        String failureMsg = "Could not list all rentable instruments.";
        ResultSet result = null;
        List<Instrument> instruments = new ArrayList<>();
        try {
            result = listAllInstrumentsStmt.executeQuery();
            while (result.next()) {
                instruments.add(new Instrument(
                        result.getInt("instrument_id"),
                        result.getString("type_of_instrument"),
                        result.getString("instrument_brand"),
                        result.getInt("instrument_price")));
            }
            commit(); // replace with method commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            closeResultSet(failureMsg, result);
        }
        return instruments;
    }

    /**
     * Checks the amount of rentals of a student using the student's ID.
     * Returns 0 if the student's id isn't present in the table "rentingInstrument"
     * OR if the to_date is less than current_date. I.E the rental's return date has
     * passed. Returns 1 OR 2 if student's ID is present and rental date is greater
     * than current_date. Returns -1 if SQLException has been caught.
     * 
     * @param student_id
     * @return quantity of instruments rented by @param student_id
     */
    public int checkStudentRental(int student_id) throws SoundGoodException {
        String failureMsg = "Could not retrive rental status for student";
        try {
            checkStudentRentalsStmt.setInt(1, student_id);
            ResultSet rentStudent = checkStudentRentalsStmt.executeQuery();
            rentStudent.next();
            commit();
            return rentStudent.getInt(1);
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
            return -1;
        }
    }

    /**
     * Checks if searched instrument exists in "instrument" && instrument is NOT
     * currently being rented out.
     * 
     * @param instrument_id
     * @return 1 if True, 0 if False and -1 if SQLException was caught.
     * @throws SoundGoodException
     */
    public int checkInstrumentRental(int instrument_id) throws SoundGoodException {
        String failureMsg = "Could not retrive rental status for instrument";
        try {
            checkInstrumentRentalStmt.setInt(1, instrument_id);
            ResultSet rentInstrument = checkInstrumentRentalStmt.executeQuery();
            rentInstrument.next();
            commit();
            return rentInstrument.getInt(1);
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
            return -1;
        }
    }

    /**
     * Rents the requested instrument.
     * 
     * @param student_id
     * @param instrument_id
     * @throws SoundGoodException
     */
    public void rentInstrument(int student_id, int instrument_id) throws SoundGoodException {
        String failureMsg = "Could not find the requested instrument available for rental.";
        try {
            addRental.setInt(1, student_id);
            addRental.setInt(2, instrument_id);
            int updateRows = addRental.executeUpdate();
            if (updateRows != 1) {
                handleException(failureMsg, null);
            } else {
                System.out.println("The requested rental for:\n" + "Instrument with the ID: " + instrument_id
                        + "\nBy student with the ID: "
                        + student_id + "\n...has been granted.");
                commit(); // connection.commit();
            }
        } catch (SQLException e) {
            handleException(failureMsg, null);
        }
    }

    /**
     * Terminates the rental of requested instrument.
     * If a student "books" a instrument,
     * 
     * @param instrumet_id
     * @throws SoundGoodException
     */
    public void terminateRental(int instrumet_id) throws SoundGoodException {
        String failureMsg = "Could not terminate rental.";
        try {
            terminateRental.setInt(1, instrumet_id);
            int updatedRows = terminateRental.executeUpdate();
            if (updatedRows != 1) {
                handleException(failureMsg, null);
            } else {
                System.out.println("Termination of rental for instrument ID: " + instrumet_id);
                commit(); // replace with method commit();
            }
        } catch (SQLException e) {
            handleException(failureMsg, null);

        }
    }

    /**
     * Used in commit(), terminateRental(), rentInstrument(), checkStudentRental(),
     * checkInstrumentRental() and printListInstrument().
     * 
     * @param failureMsg
     * @param cause
     * @throws SoundGoodException
     */
    private void handleException(String failureMsg, Exception cause) throws SoundGoodException {
        String completeFailureMsg = failureMsg;
        try {
            connection.rollback();
        } catch (SQLException rollbackExc) {
            completeFailureMsg = completeFailureMsg +
                    ". Also failed to rollback transaction because of: " + rollbackExc.getMessage();
        }
        if (cause != null) {
            throw new SoundGoodException(failureMsg, cause);
        } else {
            throw new SoundGoodException(failureMsg);
        }
    }

    /**
     * Used in printListInstrument()
     * 
     * @param failureMsg
     * @param result
     * @throws SoundGoodException
     */
    private void closeResultSet(String failureMsg, ResultSet result) throws SoundGoodException {
        try {
            result.close();
        } catch (Exception e) {
            throw new SoundGoodException(failureMsg + " Could not close result set.", e);
        }
    }

    /**
     * Commits, not used
     * 
     * @throws SoundGoodException
     */
    public void commit() throws SoundGoodException {
        try {
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to commit", e);
        }
    }
}
