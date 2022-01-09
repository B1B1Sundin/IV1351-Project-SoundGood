/*
 * The MIT License (MIT)
 * Copyright (c) 2020 Leif Lindbäck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction,including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so,subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.kth.iv1351.bankjdbc.controller;

import java.util.ArrayList;
import java.util.List;

import se.kth.iv1351.bankjdbc.integration.SoundGoodDAO;
import se.kth.iv1351.bankjdbc.integration.SoundGoodException;
import se.kth.iv1351.bankjdbc.model.Instrument;
import se.kth.iv1351.bankjdbc.model.InstrumentDTO;
import se.kth.iv1351.bankjdbc.model.InstrumentException;
import se.kth.iv1351.bankjdbc.model.RejectedException;

/**
 * This is the application's only controller, all calls to the model pass here.
 * The controller is also responsible for calling the DAO. Typically, the
 * controller first calls the DAO to retrieve data (if needed), then operates on
 * the data, and finally tells the DAO to store the updated data (if any).
 */
public class Controller {
    private final SoundGoodDAO soundGood;

    /**
     * Creates a new instance, and retrieves a connection to the database.
     * 
     * @throws SoundGoodException If unable to connect to the database.
     */
    public Controller() throws SoundGoodException {
        soundGood = new SoundGoodDAO();
    }

    /**
     * Returns instruments available for rental
     * 
     * @param instrument_type
     * @return
     * @throws InstrumentException
     */
    public List<? extends InstrumentDTO> printListInstrument(String instrument_type) throws InstrumentException {
        try {
            return soundGood.printListInstrument(instrument_type);
        } catch (SoundGoodException e) {

            throw new InstrumentException("Could not list any available instrument!", e);
        }
    }

    public List<? extends InstrumentDTO> printListAllInstruments() throws InstrumentException {
        try {
            return soundGood.printListAllInstruments();
        } catch (SoundGoodException e) {

            throw new InstrumentException("Could not list any available instruments!", e);
        }
    }

    /**
     * Requests rental of instrument by a student. It checks if the student has not
     * exceeded the limit for the amount of rentable instruments && the requested
     * instrument is NOT currently rented. If rental is granted then the "from_date"
     * is set to "current_date" and "to_date" is set to one year ahead.
     * 
     * @param instrument_id
     * @param student_id
     * @throws InstrumentException
     */
    public void rentInstrument(String student_id, String instrument_id) throws InstrumentException {
        int instrument = Integer.parseInt(instrument_id);
        int student = Integer.parseInt(student_id);
        try {
            if ((soundGood.checkStudentRental(student) < 2)
                    && (soundGood.checkInstrumentRental(instrument) == 1)) {
                soundGood.rentInstrument(student, instrument);
                System.out.println("The requested rental has been logged.");
            } else {
                System.out.println("Requested rental has been denied. \n The student with id \"" + student_id
                        + "\" has reached rental limit OR the instrument with id \"" + instrument_id
                        + "\" is already rented.");
            }
        } catch (SoundGoodException e) {
            System.out.println(
                    "Couldn't create rental of instrument with id \"" + instrument + "\" by student with id \""
                            + student
                            + "\" because student has either reached rental limit or the instrument is rented out.");
            // throw new InstrumentException(failureMsg, e);
        }
    }

    /**
     * Terminates the rental before "to_date" has expired, sets the "to_date" to
     * "current_date".
     * 
     * @param instrument_id
     * @throws InstrumentException
     */
    public void terminateRental(String instrument_id) throws InstrumentException {
        int instrument = Integer.parseInt(instrument_id);
        try {
            soundGood.terminateRental(instrument);
            System.out.println("Rental of instrument with the id " + instrument_id + " has been terminated");
        } catch (SoundGoodException e) {
            System.out.println("Couldn't remove rental of instrument with id " + instrument_id
                    + " because it hasn't been rented out.");
            // throw new InstrumentException(failureMsg, e);
        }
    }

    /**
     *
     * DELETE
     * 
     * @param holderName The holder who's accounts shall be listed.
     * @return A list with all accounts owned by the specified holder. The list is
     *         empty if the holder does not have any accounts, or if there is no
     *         such holder.
     * @throws InstrumentException If unable to retrieve the holder's accounts.
     */
    public List<? extends InstrumentDTO> listInstrument(String instrument) throws InstrumentException {
        if (instrument == null) {
            return new ArrayList<>();
        }
        try {
            return soundGood.printListInstrument(instrument);
        } catch (Exception e) {
            throw new InstrumentException("Could not search for account.", e);
        }
    }

}
