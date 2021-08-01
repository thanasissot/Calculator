package com.company;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

public class MainFrame {
    private JPanel screenPanel;
    private JPanel buttonsPanel;
    private JPanel mainPanel;
    private JButton button1;

    // helper variables
    // flags
    boolean decimal = false;  // for adding decimal digits
    int decimalDigitCounter = 1; // for adding the correct value after decimal
    boolean operation = false; // flag for first +-*/ action
    boolean secondValueTypedFlag = false; // flag for every value = true except firstValue
    boolean secondValueStartTyping = false; // flag for knowing when to start typing second value
    boolean flagsReset = false;
    boolean unaryOperation = true;

    // some more variables
    double tempValue = 0;
    double firstValue = 0;
    double secondValue = 0;
    int operator = 0;
    int unaryOperator = 0;

    // used for formatting text output
    String pattern = "###,###.###";
    DecimalFormat decimalFormat = new DecimalFormat(pattern);

    // helper methods
    private void screenDisplayTextOutput() {
        if (tempValue <= 999999999999999d){
            screenPane.setText(decimalFormat.format(tempValue));
        }
        screenPane.setText(String.valueOf(tempValue));
    }

    private void completeReset() {
        // resets all values and flags, like restarting the app
         decimal = false;
         decimalDigitCounter = 1;
         operation = false;
         secondValueTypedFlag = false;
         secondValueStartTyping = false;
         flagsReset = false;
         unaryOperation = true;
         tempValue = 0;
         firstValue = 0;
         secondValue = 0;
         operator = 0;
         unaryOperator = 0;
         screenPane.setText(decimalFormat.format(tempValue));

    }
    private void numberKeyPressed(JEditorPane screenPane, MouseEvent mouseEvent){
        if(secondValueStartTyping && !secondValueTypedFlag){
            // will visit once when second value starts typing
            secondValueTypedFlag = true;
            tempValue = Double.parseDouble(mouseEvent.getComponent().getName());
            screenPane.setText(decimalFormat.format(tempValue));
        }
        else {
            if (decimal) {
                // adding decimal digits to number
                tempValue += (Double.parseDouble(mouseEvent.getComponent().getName()) / (10.0 * decimalDigitCounter));
                decimalDigitCounter++;
                screenPane.setText(decimalFormat.format(tempValue));
            }
            // after pressing +-/%
            else
            if (tempValue <= 999999999999999d){
                if (tempValue == 999999999999999d && mouseEvent.getComponent().getName().equals("9")){
                    // unique case of 999.999.999.999.999 + pressing 9 = 10.000.000.000.000.000
                    tempValue = 9999999999999999d; // correct value assigned
                    screenPane.setText("9.999.999.999.999.999"); // correct value presented to user
                    return;
                }
                //else
                tempValue *= 10;
                tempValue += Double.parseDouble(mouseEvent.getComponent().getName());
                screenPane.setText(decimalFormat.format(tempValue));
            }
        }
    }

    // operator integer code
    // add = 0, sub = 1, mult = 2, div = 3
    private void addSubMultDivOperation(){
        if(!operation){
            // pressing +-/* first time
            secondValueStartTyping = true;
            operation = true;
            firstValue = tempValue;
            tempValue = 0;
            // reset decimal flag for correctly typing next value
            decimal = false;
        }

    }

    private void calculateResult() throws InterruptedException {
        if (operation && secondValueTypedFlag) {
            switch (operator){
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
                        firstValue = 0;
                        // reset app
                        // #here
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
            screenDisplayTextOutput();

            // #here and reset proper flags
        }
    }

    private void percentage() throws InterruptedException {
        // if we hit percentage before second value has been typed and after +-*/ pressed
        // percentage value = percentage of current value
        if (!secondValueTypedFlag){
            if (operator == 3 || operator == 4){
                tempValue = firstValue * 0.01;
            }
            else
                tempValue = firstValue * firstValue * 0.01 ;
        }
        else{
            // second number has been typed
            switch (operator){
                case 1:
                case 2:
                    tempValue = firstValue * (tempValue * 0.01);
                    break;
                case 3:
                case 4:
                    tempValue = tempValue * 0.01;
                    break;
            }
        }
        // needs to be true for equal() to calculate results
        // its true in case user types 2nd value
        // not true when user uses firstValue and +-/* before unaryOperators
        secondValueTypedFlag = true;

        // call equals
        // #here check if equals work
        this.calculateResult();
    }

    // 1/x = 4, x^2 = 5, sqrt(x) = 6
    private void unaryOperatorPressed() throws InterruptedException {
        if(!operation){
            switch (unaryOperator) {
                case 4:
                    if (tempValue == 0){
                        screenPane.setText(decimalFormat.format("Cannot divide by zero"));
                        // reset flags and values
                        // #here
                        return;
                    }
                    else {
                        tempValue = 1 / tempValue;
                    }
                    break;
                case 5:
                    tempValue = Math.pow(tempValue, 2);
                    break;
                case 6:
                    tempValue = Math.sqrt(tempValue);
            }

            // output result
            screenDisplayTextOutput();
            // #here
            // maybe store the value for continuous operations
            // or else reset flags and values
            return;

        }
        else if (operation && !secondValueTypedFlag){
            tempValue = firstValue;
        }

        switch (unaryOperator) {
            case 4:
                if (tempValue == 0){
                    screenPane.setText(decimalFormat.format("Cannot divide by zero"));
                    // reset flags and values
                    // #here
                    return;
                }
                else {
                    tempValue = 1 / tempValue;
                }
                break;
            case 5:
                tempValue = Math.pow(tempValue, 2);
                break;
            case 6:
                tempValue = Math.sqrt(tempValue);
        }
        // needs to be true for equal() to calculate results
        // its true in case user types 2nd value
        // not true when user uses firstValue and +-/* before unaryOperators
        secondValueTypedFlag = true;

        // call equal to give result
        this.calculateResult();
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

                numberKeyPressed(screenPane, mouseEvent);

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
        button8.addMouseListener(listener1);
        xButton.addMouseListener(listener1);
        button16.addMouseListener(listener1);
        button20.addMouseListener(listener1);

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
        a1XButton.addMouseListener(listener2);
        xButton1.addMouseListener(listener2);
        xButton2.addMouseListener(listener2);


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
        button24.addMouseListener(listener3);


        // C button pressed listener. clears all values and reset flags
        cButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                completeReset();
            }
        });

        // % key pressed listener
        button1.addMouseListener(new MouseAdapter() {
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
    private JButton cButton;
    private JButton delButton;
    private JButton a1XButton;
    private JButton xButton1;
    private JButton xButton2;
    private JButton button8;
    private JButton a7Button;
    private JButton a8Button;
    private JButton a9Button;
    private JButton xButton;
    private JButton a4Button;
    private JButton a5Button;
    private JButton a6Button;
    private JButton button16;
    private JButton a1Button;
    private JButton a2Button;
    private JButton a3Button;
    private JButton button20;
    private JButton nullButton;
    private JButton a0Button;
    private JButton button23;
    private JButton button24;

}
