package com.company;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    boolean secondValueStartTyping = false; // flag for knowing when to start typing second value
    boolean calculateFlag = false; // flag for "=" key
    boolean unaryOperationFlag = false;
    boolean resetFlag = false;

    // some more variables
    String tempString = "0";
    double tempValue = 0;
    double firstValue = 0;
    double secondValue = 0;
    int operator = 0;
    int unaryOperator = 0;

    // used for formatting text output
    String pattern = "###,###.###";
    DecimalFormat decimalFormat = new DecimalFormat(pattern);

    // helper methods
    private double getDoubleFromString(String string) {
        double result = 0;
        int exponent = 0;
        int counter = 0;

        String[] array = string.split(",");
        String[] integers = array[0].split("");

        exponent = array[0].length() - 1;
        counter = exponent;
        for (int i = exponent; i >= 0; i--){
            result += Double.parseDouble(integers[counter - i]) * Math.pow(10, exponent--);
        }

        if (array.length > 1){
            String[] decimals = array[1].split("");
            exponent = -1;
            for (int i = 0; i <= decimals.length; i++){
                result += Double.parseDouble(integers[i]) * Math.pow(10, exponent--);
            }

            return (double)Math.round(result * Math.pow(10, decimals.length)) / Math.pow(10, decimals.length);
        }

        return result;
    }

    private void ceKeyPressed() {
        // clears current typed valued
        tempString = "";
        decimalFlag = false;
        screenPane.setText("0");
    }

    private void completeReset() {
        // restores all variables and flags to start up default values
        decimalFlag = false;
        operation = false;
        valueTypedFlag = false;
        secondValueStartTyping = false;
        unaryOperationFlag = false;
        calculateFlag = false;
        tempValue = 0;
        firstValue = 0;
        secondValue = 0;
        operator = 0;
        unaryOperator = 0;
        tempString = "0";
        screenPane.setText(tempString);
    }

    private void resultValueScreenDisplay() {
        String output = decimalFormat.format(tempValue);

        if (output.replace(".", "").replace(",","").length() > 16){
            screenPane.setText(String.valueOf(tempValue));
        } else {
            screenPane.setText(output);
        }
    }

    private void numericalTypedValueScreenDisplay() {
        // used whenever numerical or "," key used
        if (tempString.contains(",")){
            String[] str = tempString.split(",");

            if (str.length == 1){
                screenPane.setText(decimalFormat.format(Double.parseDouble(str[0])) + ",");
            }
            else {
                screenPane.setText(decimalFormat.format(Double.parseDouble(str[0])) + "," + str[1]);
            }
        }
        else {
            if (tempString.equals("9999999999999999")){
                screenPane.setText("9.999.999.999.999.999");
            }
            else {
                screenPane.setText(decimalFormat.format(Double.parseDouble(tempString)));
            }
        }
    }

    private void numberKeyPressed(MouseEvent mouseEvent) {
        // use as naming suggests
        if (tempString.replace(",", "").length() >= 16){
            // maximum length of digits reached => do nothing
            return;
        }
        // base case, every key added to the back of the string which is the number displayed
        if (tempString.equals("0")) {
            tempString = mouseEvent.getComponent().getName();
        }
        else {
            tempString += mouseEvent.getComponent().getName();
        }

        valueTypedFlag = true;

        // show output
        this.numericalTypedValueScreenDisplay();
    }

    private void commaKeyPressed(){
        if (!decimalFlag && tempString.length() <= 15){
            // if decimal already typed do nothing
            tempString += ",";
            decimalFlag = true;
            this.numericalTypedValueScreenDisplay();
        }
    }

    // operator int coded as below
    // add = 0, sub = 1, mult = 2, div = 3
    private void addSubMultDivOperation() {
        if (!operation) {   // pressing +-/* first time
            if(!unaryOperationFlag) {
                firstValue = getDoubleFromString(tempString);
                // value typed flag reset
                valueTypedFlag = false;
            }
            else {
                firstValue = tempValue;
//                valueTypedFlag = true;
                unaryOperationFlag = false;
            }

            operation = true;
            // reset decimal flag
            decimalFlag = false;
            tempString = "0";
        }
    }

    private void calculateResult() throws InterruptedException {
        if (operation) {
            if (valueTypedFlag && !unaryOperationFlag){
                tempValue = getDoubleFromString(tempString);
            }
            else if (!unaryOperationFlag){
                return;
            }

            switch (operator) {
                case 1: // add
                    tempValue += firstValue;
                    break;
                case 2: // sub
                    tempValue = (firstValue - tempValue);
                    break;
                case 3: // mult
                    tempValue *= firstValue;
                    break;
                case 4: // div
                    if (tempValue == 0) {
                        screenPane.setText("Cannot divide by zero");
                        // reset
                        resetFlag = true;
                        return;
                    }
                    tempValue = (firstValue / tempValue);
                    break;
                default:
                    screenPane.setText("No operaton value.\nSomething went really wrong");
                    Thread.sleep(5000);
                    System.exit(-1);
                    break;
            }
            // show result output with proper format
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
        else if (!valueTypedFlag) {
            // when one value is typed and an operation, and uses the same value to calculate the %
            // for add and sub this is x*(x*0.01) e.g. 200 + % = 200 + 400, cause 200*200*0.01 = 400
            // for mult and div is x*0.01, e.g. 50 * % = 50 * 0.5, cause 50 * 0.01 = 0.5
            // here operation is true so firstValue = tempValue, and tempValue = 0
            if (operator == 3 || operator == 4) {
                // mult/div
                tempValue = firstValue * 0.01;
            } else {
                // add/sub
                tempValue = firstValue * firstValue * 0.01;
            }
        }
        else {
            // second number has been typed, so its used to calculate the percentage
            // although the formula is the same the variables slightly differ
            // #here can be implemented better to get less code
            tempValue = getDoubleFromString(tempString);
            switch (operator) {
                case 1:
                case 2:
                    // add/sub
                    tempValue = firstValue * (tempValue * 0.01);
                    break;
                case 3:
                case 4:
                    // mult/div
                    tempValue = tempValue * 0.01;
                    break;
            }
        }

        unaryOperationFlag = true;
        // #here needs to calculate or print result?
        resultValueScreenDisplay();
    }

    // 1/x = 4, x^2 = 5, sqrt(x) = 6
    private void unaryOperatorPressed() throws InterruptedException {
        // operation was pressed after typing a number and not a second value given
        // the operant uses as value the first typed value
        if (!operation || valueTypedFlag){
            tempValue = getDoubleFromString(tempString);
        }
        else {
            tempValue = firstValue;
        }

        switch (unaryOperator) {
            case 4:
                if (tempValue == 0) {
                    screenPane.setText("Cannot divide by zero");
                    // reset
                    resetFlag = true;
                    return;
                } else {
                    tempValue = 1 / tempValue;
                }
                break;
            case 5:
                tempValue = Math.pow(tempValue, 2);
                break;
            case 6:
                tempValue = Math.sqrt(tempValue);
        }

        unaryOperationFlag = true;
        // output result
        resultValueScreenDisplay();
    }

    public MainFrame() {
        // setting format for appropriate firstValue print to screen
        // simple print the double firstValue gives a scientific notation in a too small number
        decimalFormat.setMaximumFractionDigits(360);

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

        // operation +,-,*,/ key listener
        MouseAdapter listener1 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                // assing +-*/ operator value
                operator = Integer.parseInt(mouseEvent.getComponent().getName());
                addSubMultDivOperation();

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

        // comma key listener, for inputing decimal values
        MouseAdapter listener4 = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);

                commaKeyPressed();

            }
        };
        commaButton.addMouseListener(listener4);
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
