package com.company;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;

public class MainFrame {
    private JPanel screenPanel;
    private JPanel buttonsPanel;
    private JPanel mainPanel;
    private JButton percentButton;

    // helper variables
    // flags
    boolean decimalFlag = false;  // for adding decimal digits
    boolean operation = false; // flag for first +-*/ action
    boolean nextValueCanBeTypedFlag = false; // flag for knowing when to start typing second value
    boolean calculateFlag = false; // flag for "=" key
    boolean deleteFlag = false;

    // some more variables
    String displayValueString = "0";
    String leftHandValueString = "";
    String rightHandValueString = "";
    BigDecimal bigDec;
    int operator = 0;
    int unaryOperator = 0;

    // helper methods
    private void negate() {
        if (!displayValueString.equals("0")) {
            if (displayValueString.startsWith("-")){
                displayValueString = displayValueString.replace("-", "");
            }
            else {
                displayValueString = "-" + displayValueString;
            }

            numKeyPressedDisplay();
        }
    }

    private void ceKeyPressed() {
        // clears completely if used exactly after "="
        if(calculateFlag){
            completeReset();
            return;
        }

        // clears current typed valued
        displayValueString = "0";
        decimalFlag = false;
        screenPane.setText(displayValueString);
    }

    private void completeReset() {
        // restores all variables and flags to start up default values
        decimalFlag = false;
        operation = false;
        nextValueCanBeTypedFlag = false;
        calculateFlag = false;
        deleteFlag = false;

        operator = 0;
        unaryOperator = 0;
        displayValueString = "0";
        leftHandValueString = "";
        rightHandValueString = "";
        screenPane.setText(displayValueString);
    }

    private void resultValueScreenDisplay() {
        // after "=" this flag makes it possible to type a new value
        nextValueCanBeTypedFlag = true;

        // strip Trailing Zeros for proper display purposes
        bigDec = bigDec.stripTrailingZeros();

        // keep current display value as right hand value in case of multiple "="
        // example 5 + 3 = 8 press("=") => = 11 press("=") => 14
        // the 3 is stored for that purpose
        rightHandValueString = displayValueString;

        if (bigDec.toString().replace(".","").length() > 16){
            displayValueString = Controller.formatBigNumber(bigDec, 16);
            screenPane.setText(displayValueString);
        }

        else {
            displayValueString = Controller.formatInRangeNumber(bigDec);
            screenPane.setText(displayValueString);
        }
    }

    private void numKeyPressedDisplay() {
        // used whenever numerical or "," key used
        if (displayValueString.contains(",")){
            String[] str = displayValueString.split(",");

            if (str.length == 1){
                screenPane.setText(Controller.formatInRangeNumber(new BigDecimal(str[0])) + ",");
            }
            else {
                screenPane.setText(Controller.formatInRangeNumber(new BigDecimal(str[0])) + "," + str[1]);
            }
        }
        else {
            screenPane.setText(Controller.formatInRangeNumber(new BigDecimal(displayValueString)));
        }
    }

    private void numberKeyPressed(MouseEvent mouseEvent) {
        // use is as name indicates
        if (nextValueCanBeTypedFlag){
            nextValueCanBeTypedFlag = false;
            displayValueString = "0";
        }

        if (displayValueString.replace(",", "").length() >= 16){
            // maximum length of digits reached => do nothing
            return;
        }

        if (!decimalFlag && mouseEvent.getComponent().getName().equals("comma")){
            decimalFlag = true;
            displayValueString += ",";
        }
        // base case, every key added to the back of the string which is the number displayed
        else if (displayValueString.equals("0")) {
            displayValueString = mouseEvent.getComponent().getName();
        }
        else {
            displayValueString += mouseEvent.getComponent().getName();
        }

        // reset deleteFlag
        deleteFlag = false;

        // show output
        this.numKeyPressedDisplay();
    }

    // operator int coded as below
    // add = 0, sub = 1, mult = 2, div = 3
    private void operator() {
        if (!operation) {
            // pressing +-/* first time
            operation = true;

            // store display value for leftHandValueString of operation
            leftHandValueString = displayValueString;

            // reset decimal flag
            decimalFlag = false;

            // delete should not be able to work unless a new numerical key is typed
            deleteFlag = true;
        }
        else if (calculateFlag){
            // started new calculation using display value as left hand
            calculateFlag = false;
            // store display value for leftHandValueString of operation
            leftHandValueString = displayValueString;
            // CE specific flag for case A + B = X + CE = X + 0 = X
        }

        //
        nextValueCanBeTypedFlag = true;
    }

    private void calculateResult() throws InterruptedException {
        if (calculateFlag){
            leftHandValueString = displayValueString;
            displayValueString = rightHandValueString;
        }

        if (operation) {
            // correct string format to use constructor of BigDecimal
            displayValueString = displayValueString.replace(".","").replace(",", ".");
            leftHandValueString = leftHandValueString.replace(".","").replace(",", ".");
            rightHandValueString = displayValueString;

            switch (operator) {

                case 1: // add
                    bigDec = new BigDecimal(leftHandValueString).add(new BigDecimal(displayValueString));
                    break;
                case 2: // sub
                    bigDec = new BigDecimal(leftHandValueString).subtract(new BigDecimal(displayValueString));
                    break;
                case 3: // mult
                    bigDec = new BigDecimal(leftHandValueString).multiply(new BigDecimal(displayValueString));
                    break;
                case 4: // div
                    if (displayValueString.equals("0")) {
                        if (unaryOperator == 7) {
                            screenPane.setText("Result \nundefined!");
                        }
                        else {
                            screenPane.setText("Cannot divide \nby zero!");
                        }
                        addListenerToMainPanel();
                        return;
                    }
                    else {
                        bigDec = new BigDecimal(leftHandValueString).divide(new BigDecimal(displayValueString), 16, BigDecimal.ROUND_CEILING);
                    }
                    break;
                default:
                    screenPane.setText("No operaton value.\nSomething went really wrong");
                    addListenerToMainPanel();
                    break;
            }

            // used for multiple "="
            // uses previous result + latest right hand value for operation
            calculateFlag = true;

            // reset unaryOperator int needed for percentage "Result undefined!" message
            // a / 0 % = "Result undefined!" compared to a / 0 = cannot divide by zero
            unaryOperator = 0;

            deleteFlag = true;

            resultValueScreenDisplay();
        }
    }

    private void percentage() throws InterruptedException {
        // used when percentage % key pressed
        if (!operation) {
            // no operation pressed just a typed first value, always results to 0
            completeReset();
            return;
        }

        // correct string format to use constructor of BigDecimal
        displayValueString = displayValueString.replace(",", ".");
        leftHandValueString = leftHandValueString.replace(",", ".");

        // for add and sub this is x*(x*0.01) e.g. 200 + % = 200 + 400, cause 200*200*0.01 = 400
        // for mult and div is x*0.01, e.g. 50 * % = 50 * 0.5, cause 50 * 0.01 = 0.5
        if (operator == 3 || operator == 4) {
            // mult/div
            bigDec = new BigDecimal(displayValueString).multiply(new BigDecimal("0.01"));
//            displayValueString = bigDec.toString();
        } else {
            // add/sub
            bigDec = new BigDecimal(leftHandValueString).multiply(new BigDecimal(displayValueString)).multiply(new BigDecimal("0.01"));
//            displayValueString = bigDec.toString();
        }

        resultValueScreenDisplay();
    }

    // 1/x = 4, x^2 = 5, sqrt(x) = 6
    private void unaryOperatorPressed() throws InterruptedException {
        // operation was pressed after typing a number and not a second value given
        // the operant uses as value the first typed value

        // correct string format to use constructor of BigDecimal
        displayValueString = displayValueString.replace(",", ".");
        leftHandValueString = leftHandValueString.replace(",", ".");

        switch (unaryOperator) {
            case 4:
                if (displayValueString.equals("0") || leftHandValueString.equals("0")) {
                    screenPane.setText("Cannot divide by zero");
                    addListenerToMainPanel();
                    return;
                } else {
                    bigDec = new BigDecimal("1").divide(new BigDecimal(displayValueString), 16, BigDecimal.ROUND_CEILING);
                }
                break;
            case 5:
                bigDec = new BigDecimal(displayValueString).pow(2);
                break;
            case 6:
                if (displayValueString.startsWith("-")){
                    screenPane.setText("Invalid input");
                    addListenerToMainPanel();
                    return;
                }else {
                    bigDec = Controller.bigDecimalSQRT(new BigDecimal(displayValueString), 16);
                }
                break;
        }

        // output result
        resultValueScreenDisplay();
    }

    private void deleteButtonPressed() {
        // if screen displays result after using "=" button should not work
        if (deleteFlag){
            return;
        }

        // else it starts to delete the current input until "0"
        if (displayValueString.length() == 1){
            displayValueString = "0";
        }
        else {
            displayValueString = displayValueString.substring(0, displayValueString.length() - 1);
        }

        numKeyPressedDisplay();

    }


    MouseAdapter listener4 = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            super.mousePressed(mouseEvent);

            // reset all values
            completeReset();

            // remove listener
            mainPanel.removeMouseListener(listener4);

        }
    };

    // adding event listener to panel.
    // used after getting an error like divide with 0, to clear text and reset values
    // so the calculator is ready to be used again
    private void addListenerToMainPanel() {

        mainPanel.addMouseListener(listener4);

    }

    public MainFrame() {

        // numerical key listener
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                numberKeyPressed(mouseEvent);

            }
        };
        a1Button.addMouseListener(listener);
        a7Button.addMouseListener(listener);
        a8Button.addMouseListener(listener);
        a9Button.addMouseListener(listener);
        a4Button.addMouseListener(listener);
        a5Button.addMouseListener(listener);
        a6Button.addMouseListener(listener);
        a2Button.addMouseListener(listener);
        a3Button.addMouseListener(listener);
        a0Button.addMouseListener(listener);
        commaButton.addMouseListener(listener);

        // operation +,-,*,/ key listener
        MouseAdapter listener1 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                // assing +-*/ operator value
                operator = Integer.parseInt(mouseEvent.getComponent().getName());
                operator();

            }
        };
        divButton.addMouseListener(listener1);
        multButton.addMouseListener(listener1);
        subButton.addMouseListener(listener1);
        addButton.addMouseListener(listener1);

        // 1/x, x^2, sqrt(x)
        MouseAdapter listener2 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                // assign 1/x, x^2, sqrt(x) unary operator value
                unaryOperator = Integer.parseInt(mouseEvent.getComponent().getName());
                try {
                    unaryOperatorPressed();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("Something went wrong in listener for unary Operators.");
                }

            }
        };
        oneDivXButton.addMouseListener(listener2);
        xpow2Button.addMouseListener(listener2);
        sqrtXButton2.addMouseListener(listener2);

        // % key pressed listener
        percentButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                unaryOperator = Integer.parseInt(mouseEvent.getComponent().getName());

                try {
                    percentage();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("Something went horrible wrong at % listener.");
                }

            }
        });


        // equals "=" key listener
        MouseAdapter listener3 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                try {
                    calculateResult();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("Something went wrong pressing \"=\" key.");
                }

            }
        };
        equalsButton.addMouseListener(listener3);


        // C button pressed listener. clears all values and reset flags
        CButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                completeReset();

            }
        });

        CEButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                ceKeyPressed();

            }
        });
        nullButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                negate();

            }
        });

        delButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                deleteButtonPressed();
            }
        });

    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Calculator");
        frame.setContentPane(new MainFrame().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }

    private JEditorPane screenPane;
    private JButton CEButton;
    private JButton CButton;
    private JButton delButton;
    private JButton oneDivXButton;
    private JButton xpow2Button;
    private JButton sqrtXButton2;
    private JButton divButton;
    private JButton a7Button;
    private JButton a8Button;
    private JButton a9Button;
    private JButton multButton;
    private JButton a4Button;
    private JButton a5Button;
    private JButton a6Button;
    private JButton subButton;
    private JButton a1Button;
    private JButton a2Button;
    private JButton a3Button;
    private JButton addButton;
    private JButton nullButton;
    private JButton a0Button;
    private JButton commaButton;
    private JButton equalsButton;
}
