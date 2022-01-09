/*
 * The MIT License
 *
 * Copyright 2017 Leif Lindbäck <leifl@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
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

package se.kth.iv1351.bankjdbc.view;

import java.util.List;
import java.util.Scanner;

import se.kth.iv1351.bankjdbc.controller.Controller;
import se.kth.iv1351.bankjdbc.model.InstrumentDTO;

/**
 * Reads and interprets user commands. This command interpreter is blocking, the
 * user
 * interface does not react to user input while a command is being executed.
 */
public class BlockingInterpreter {
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private Controller ctrl;
    private boolean keepReceivingCmds = false;

    /**
     * Creates a new instance that will use the specified controller for all
     * operations.
     * 
     * @param ctrl The controller used by this instance.
     */
    public BlockingInterpreter(Controller ctrl) {
        this.ctrl = ctrl;
    }

    /**
     * Stops the commend interpreter.
     */
    public void stop() {
        keepReceivingCmds = false;
    }

    /**
     * Interprets and performs user commands. This method will not return until the
     * UI has been stopped. The UI is stopped either when the user gives the
     * "quit" command, or when the method <code>stop()</code> is called.
     */
    public void handleCmds() {
        keepReceivingCmds = true;
        while (keepReceivingCmds) {
            try {
                CmdLine cmdLine = new CmdLine(readNextLine());
                switch (cmdLine.getCmd()) {
                    case HELP:
                        System.out.println(
                                "\n*Guide for SoundGood commands*");
                        for (Command command : Command.values()) {
                            if (command == Command.ILLEGAL_COMMAND) {
                                continue;
                            }
                            System.out.println(" - " + command.toString()); // .toLowerCase()
                        }
                        System.out.println(
                                "\nLIST - All instruments that are available to rent. Type the instrument you want to rent.\n"
                                        + "END - Terminate rental of a instrument. Type the id of the instrument you want to end rental of. \n"
                                        + "HELP - See all available commands.\n"
                                        + "QUIT - Leave the chat application.\n"
                                        + "RENT - Rent a new instrument. Type the id of the student and then of the instrument's.\n");
                        break;
                    case QUIT:
                        keepReceivingCmds = false;
                        break;
                    case RENT:
                        ctrl.rentInstrument((cmdLine.getParameter(0)), cmdLine.getParameter(1));
                        break;
                    case END:
                        ctrl.terminateRental(cmdLine.getParameter(0));
                        break;
                    /**
                     * Lists all available instruments or a by a specific type.
                     */
                    case LIST:
                        List<? extends InstrumentDTO> instruments = null;
                        instruments = ctrl.printListAllInstruments();
                        if (cmdLine.getParameter(0).equals("")) {
                            System.out.println(
                                    "\n--All available Instruments--");
                            for (InstrumentDTO instrument : instruments) {
                                System.out.println(
                                        "-- Instrument ID: " + instrument.getInstrument_id()
                                                + " | Type: " + instrument.getInstrument_type()
                                                + " | Brand: " + instrument.getInstrument_brand()
                                                + " | Price: " + instrument.getInstrument_price() + " SEK --\n");
                            }
                        } else {
                            if (instruments.size() > 0) {
                                System.out.println(
                                        "Available Instruments of the type -" + cmdLine.getParameter(0).toUpperCase()
                                                + "-\n");
                                boolean foundInstruments = false;
                                for (InstrumentDTO instrument : instruments) {
                                    if (cmdLine.getParameter(0).equals(instrument.getInstrument_type())) {
                                        foundInstruments = true;
                                        System.out.println("Instrument ID: " + instrument.getInstrument_id()
                                                + "\nType: " + instrument.getInstrument_type()
                                                + "\nBrand: " + instrument.getInstrument_brand()
                                                + "\nPrice: " + instrument.getInstrument_price() + " SEK\n");
                                    }
                                }
                                if (!foundInstruments)
                                    System.out.println(
                                            "Found no available instruments of type \"" + cmdLine.getParameter(0)
                                                    + "\"!\nPlease check your spelling or reenter a different instrument.\n");
                            }
                        }
                        break;
                    default:
                        System.out.println("Illegal command");
                }
            } catch (Exception e) {
                System.out.println("Operation failed");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private String readNextLine() {
        System.out.print(PROMPT);
        return console.nextLine();
    }
}
