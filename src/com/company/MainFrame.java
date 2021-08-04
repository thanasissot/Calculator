package com.company;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class MainFrame {
    private JPanel screenPanel;
    private JPanel buttonsPanel;
    private JPanel mainPanel;
    private JButton percentButton;

    // helper variables
    // flags
    boolean decimalFlag = false;  // for adding decimal digits
    boolean operation = false; // flag for first +-*/ action
    boolean valueTypedFlag = false; // flag for every value = true except firstValue
    boolean nextValueTypeFlag = false; // flag for knowing when to start typing second value
    boolean calculateFlag = false; // flag for "=" key
    boolean unaryOperationFlag = false;
    boolean resetFlag = false;

    // some more variables
    String displayValueString = "0";
    String leftHandValueString = "";
    String rightHandValueString = "";
    BigDecimal bigDec;
    int operator = 0;
    int unaryOperator = 0;

    // used for formatting text output
    String pattern = "###,###.###";
    DecimalFormat decimalFormat = new DecimalFormat(pattern);

    // helper methods
    private void ceKeyPressed() {
        // clears current typed valued
        displayValueString = "";
        decimalFlag = false;
        screenPane.setText("0");
    }

    private void completeReset() {
        // restores all variables and flags to start up default values
        decimalFlag = false;
        operation = false;
        valueTypedFlag = false;
        nextValueTypeFlag = false;
        unaryOperationFlag = false;
        calculateFlag = false;
        operator = 0;
        unaryOperator = 0;
        displayValueString = "0";
        leftHandValueString = "";
        screenPane.setText(displayValueString);
    }

    private void resultValueScreenDisplay() {
        nextValueTypeFlag = true;
        bigDec = bigDec.stripTrailingZeros();
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
        // use as naming suggests
        if (nextValueTypeFlag){
            nextValueTypeFlag = false;
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

            //
            nextValueTypeFlag = true;

            // reset decimal flag
            decimalFlag = false;
        }
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

            System.out.println(displayValueString);
            System.out.println(leftHandValueString);

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
                            screenPane.setText("Result undefined!");
                        }
                        else {
                            screenPane.setText("Cannot divide by zero!");
                        }
                        // reset #here
                        resetFlag = true;
                    }
                    else {
                        bigDec = new BigDecimal(leftHandValueString).divide(new BigDecimal(displayValueString), 16, BigDecimal.ROUND_CEILING);
                    }
                    break;
                default:
                    screenPane.setText("No operaton value.\nSomething went really wrong");
                    Thread.sleep(5000);
                    System.exit(-1);
                    break;
            }

            // used for multiple "="
            // uses previous result + latest right hand value for operation
            calculateFlag = true;

            resultValueScreenDisplay();
            // #here and reset proper flags
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
            displayValueString = bigDec.toString();
        } else {
            // add/sub
            bigDec = new BigDecimal(displayValueString).multiply(new BigDecimal(displayValueString)).multiply(new BigDecimal("0.01"));
            displayValueString = bigDec.toString();
        }

        // #here needs to calculate or print result?
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
                    // reset #here
                    return;
                } else {
                    bigDec = new BigDecimal("1").divide(new BigDecimal(displayValueString), 16, BigDecimal.ROUND_CEILING);
                }
                break;
            case 5:
                bigDec = new BigDecimal(displayValueString).pow(2);
                break;
            case 6:
                bigDec = Controller.bigDecimalSQRT(new BigDecimal(displayValueString), 16);
        }

        // output result
        resultValueScreenDisplay();
    }

    public MainFrame() {
        // setting format for appropriate firstValue print to screen
        // simple print the double firstValue gives a scientific notation in a too small number
        decimalFormat.setMaximumFractionDigits(20);


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
