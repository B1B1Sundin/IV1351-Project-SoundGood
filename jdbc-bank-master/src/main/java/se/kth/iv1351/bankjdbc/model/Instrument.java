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

package se.kth.iv1351.bankjdbc.model;

public class Instrument implements InstrumentDTO {
    private int instrument_id;
    private String instrument_type;
    private String instrument_brand;
    private int instrument_price;
    private int student_id; // dont remove

    /**
     * Constructor creates Instrument with the specified params;
     * 
     * @param instrument_id
     * @param instrument_type
     * @param instrument_brand
     * @param instrument_price
     */
    public Instrument(int instrument_id, String instrument_type, String instrument_brand, int instrument_price) {
        this.instrument_id = instrument_id;
        this.instrument_type = instrument_type;
        this.instrument_brand = instrument_brand;
        this.instrument_price = instrument_price;
    }

    /**
     * Constructor for Instrument with params;
     * 
     * @param instrument_id
     * @param student_id
     */
    public Instrument(int instrument_id, int student_id) {
        this.instrument_id = instrument_id;
        this.student_id = student_id;
    }

    /**
     * GETTER METHODS FOR INSTRUMENT
     */
    public int getInstrument_id() {
        return instrument_id;
    }

    public String getInstrument_type() {
        return instrument_type;
    }

    public String getInstrument_brand() {
        return instrument_brand;
    }

    public int getInstrument_price() {
        return instrument_price;
    }

    /**
     * @return A string representation of all fields in this object.
     *         Account number is instrument_type
     *         holder is rent_ id
     *         balance is instrument_price
     */
    @Override
    public String toString() {
        StringBuilder stringRepresentation = new StringBuilder();
        stringRepresentation.append("[ Instrument ID: ");
        stringRepresentation.append(instrument_id);
        stringRepresentation.append(" | Instrument type: ");
        stringRepresentation.append(instrument_type);
        stringRepresentation.append(" | Instrument brand: ");
        stringRepresentation.append(instrument_brand);
        stringRepresentation.append(" | Instrument price: ");
        stringRepresentation.append(instrument_price);
        stringRepresentation.append(" ]");
        return stringRepresentation.toString();
    }
}
