package com.example.johnscalculatorapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay;
    private double firstNumber = 0;
    private String operator = "";
    private boolean isNewInput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tvDisplay);

        // ===== DIGIT BUTTONS =====
        int[] digitIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };
        for (int id : digitIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> onDigit(((Button) v).getText().toString()));
        }

        // ===== DOT =====
        findViewById(R.id.btnDot).setOnClickListener(v -> {
            String current = tvDisplay.getText().toString();
            if (isNewInput) {
                tvDisplay.setText("0.");
                isNewInput = false;
            } else if (!current.contains(".")) {
                tvDisplay.setText(current + ".");
            }
        });

        // ===== CLEAR =====
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            tvDisplay.setText("0");
            firstNumber = 0;
            operator = "";
            isNewInput = true;
        });

        // ===== +/- =====
        findViewById(R.id.btnPlusMinus).setOnClickListener(v -> {
            try {
                double val = Double.parseDouble(tvDisplay.getText().toString());
                tvDisplay.setText(formatNumber(val * -1));
            } catch (NumberFormatException e) {
                tvDisplay.setText("0");
            }
        });

        // ===== % =====
        findViewById(R.id.btnPercent).setOnClickListener(v -> {
            try {
                double val = Double.parseDouble(tvDisplay.getText().toString());
                tvDisplay.setText(formatNumber(val / 100.0));
            } catch (NumberFormatException e) {
                tvDisplay.setText("0");
            }
        });

        // ===== BASIC OPERATORS =====
        setOperatorListener(R.id.btnDivide, "÷");
        setOperatorListener(R.id.btnMultiply, "×");
        setOperatorListener(R.id.btnSubtract, "-");
        setOperatorListener(R.id.btnAdd, "+");

        // ===== BONUS OPERATORS (binary) =====
        setOperatorListener(R.id.btnNPR, "nPr");
        setOperatorListener(R.id.btnNCR, "nCr");
        setOperatorListener(R.id.btnPower, "pow");
        setOperatorListener(R.id.btnEE, "EE");

        // ===== EQUALS =====
        findViewById(R.id.btnEquals).setOnClickListener(v -> {
            if (operator.isEmpty()) return;
            try {
                double secondNumber = Double.parseDouble(tvDisplay.getText().toString());
                double result = 0;

                switch (operator) {
                    case "+": result = firstNumber + secondNumber; break;
                    case "-": result = firstNumber - secondNumber; break;
                    case "×": result = firstNumber * secondNumber; break;
                    case "÷":
                        if (secondNumber == 0) {
                            tvDisplay.setText(R.string.error);
                            operator = "";
                            isNewInput = true;
                            return;
                        }
                        result = firstNumber / secondNumber;
                        break;
                    case "pow": result = Math.pow(firstNumber, secondNumber); break;
                    case "nPr": result = permutation((int) firstNumber, (int) secondNumber); break;
                    case "nCr": result = combination((int) firstNumber, (int) secondNumber); break;
                    case "EE": result = firstNumber * Math.pow(10, secondNumber); break;
                }

                tvDisplay.setText(formatNumber(result));
                operator = "";
                isNewInput = true;
            } catch (NumberFormatException e) {
                tvDisplay.setText(R.string.error);
                isNewInput = true;
            }
        });

        // ===== SCIENTIFIC (UNARY) BUTTONS =====
        findViewById(R.id.btnSin).setOnClickListener(v -> applyUnary(x -> Math.sin(Math.toRadians(x))));
        findViewById(R.id.btnCos).setOnClickListener(v -> applyUnary(x -> Math.cos(Math.toRadians(x))));
        findViewById(R.id.btnTan).setOnClickListener(v -> applyUnary(x -> Math.tan(Math.toRadians(x))));

        findViewById(R.id.btnSinh).setOnClickListener(v -> applyUnary(Math::sinh));
        findViewById(R.id.btnCosh).setOnClickListener(v -> applyUnary(Math::cosh));
        findViewById(R.id.btnTanh).setOnClickListener(v -> applyUnary(Math::tanh));

        findViewById(R.id.btnLog).setOnClickListener(v -> applyUnary(x -> (x <= 0) ? Double.NaN : Math.log10(x)));
        findViewById(R.id.btnLn).setOnClickListener(v -> applyUnary(x -> (x <= 0) ? Double.NaN : Math.log(x)));
        findViewById(R.id.btnSqrt).setOnClickListener(v -> applyUnary(x -> (x < 0) ? Double.NaN : Math.sqrt(x)));
        findViewById(R.id.btnSquare).setOnClickListener(v -> applyUnary(x -> x * x));
        findViewById(R.id.btnFactorial).setOnClickListener(v -> applyUnary(x -> (double) factorial((int) x)));

        // ===== π =====
        findViewById(R.id.btnPi).setOnClickListener(v -> {
            tvDisplay.setText(formatNumber(Math.PI));
            isNewInput = true;
        });
    }

    // ---------- helpers ----------

    private void onDigit(String digit) {
        if (isNewInput) {
            tvDisplay.setText(digit);
            isNewInput = false;
        } else {
            String current = tvDisplay.getText().toString();
            if (current.equals("0")) {
                tvDisplay.setText(digit);
            } else {
                tvDisplay.setText(current + digit);
            }
        }
    }

    private void setOperatorListener(int id, String op) {
        findViewById(id).setOnClickListener(v -> {
            try {
                firstNumber = Double.parseDouble(tvDisplay.getText().toString());
                operator = op;
                isNewInput = true;
            } catch (NumberFormatException e) {
                // Ignore
            }
        });
    }

    private interface UnaryOp { double apply(double x); }

    private void applyUnary(UnaryOp op) {
        try {
            double val = Double.parseDouble(tvDisplay.getText().toString());
            double result = op.apply(val);
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                tvDisplay.setText(R.string.error);
            } else {
                tvDisplay.setText(formatNumber(result));
            }
            isNewInput = true;
        } catch (NumberFormatException e) {
            tvDisplay.setText(R.string.error);
            isNewInput = true;
        }
    }

    private long factorial(int n) {
        if (n < 0 || n > 20) return 0;
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }

    private double permutation(int n, int r) {
        if (r > n || r < 0 || n > 20) return 0;
        return (double) factorial(n) / factorial(n - r);
    }

    private double combination(int n, int r) {
        if (r > n || r < 0 || n > 20) return 0;
        return (double) factorial(n) / (factorial(r) * factorial(n - r));
    }

    private String formatNumber(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            return "Error";
        }

        // Round to 10 decimal places to eliminate floating-point noise
        // (e.g. sin(30°) = 0.49999999999999994 instead of 0.5)
        double rounded = Math.round(val * 1e10) / 1e10;

        if (rounded == Math.floor(rounded) && Math.abs(rounded) < 1e15) {
            return String.valueOf((long) rounded);
        }

        // Strip trailing zeros from the decimal part
        String result = String.valueOf(rounded);
        if (result.contains(".")) {
            result = result.replaceAll("0+$", "");
            result = result.replaceAll("\\.$", "");
        }
        return result;
    }
}