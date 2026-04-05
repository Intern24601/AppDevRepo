package com.example.converterapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.converterapp.R;

/**
 * MainActivity handles the logic for a multi-category Unit Converter.
 * It supports Currency, Fuel Efficiency, Distance, Volume, and Temperature conversions.
 */
public class MainActivity extends AppCompatActivity {

    // UI COMPONENTS
    private Spinner spinnerCategory; // Selects the conversion category
    private Spinner spinnerSource;   // Selects the "From" unit
    private Spinner spinnerDest;     // Selects the "To" unit
    private EditText editInput;      // User input for numeric value
    private Button btnConvert;       // Action button to perform conversion
    private TextView tvResult;       // Displays the converted value
    private TextView tvResultLabel;  // Displays the "RESULT" header

    // CATEGORY CONSTANTS
    private static final String CAT_CURRENCY    = "💱  Currency";
    private static final String CAT_EFFICIENCY  = "⛽  Fuel Efficiency";
    private static final String CAT_DISTANCE    = "📏  Distance";
    private static final String CAT_VOLUME      = "🧪  Volume";
    private static final String CAT_TEMPERATURE = "🌡  Temperature";

    // UNIT CONVERSION DATA

    // Currency conversion rates (relative to 1 USD)
    private static final Map<String, Double> CURRENCY_TO_USD = new LinkedHashMap<>();
    static {
        CURRENCY_TO_USD.put("USD", 1.0);
        CURRENCY_TO_USD.put("AUD", 1.0 / 1.55);
        CURRENCY_TO_USD.put("EUR", 1.0 / 0.92);
        CURRENCY_TO_USD.put("JPY", 1.0 / 148.50);
        CURRENCY_TO_USD.put("GBP", 1.0 / 0.78);
    }
    
    // Rates to convert from USD back to specific currencies
    private static final Map<String, Double> USD_TO_CURRENCY = new LinkedHashMap<>();
    static {
        USD_TO_CURRENCY.put("USD", 1.0);
        USD_TO_CURRENCY.put("AUD", 1.55);
        USD_TO_CURRENCY.put("EUR", 0.92);
        USD_TO_CURRENCY.put("JPY", 148.50);
        USD_TO_CURRENCY.put("GBP", 0.78);
    }

    // LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupCategorySpinner();
        setupConvertButton();
    }

    // SETUP METHODS

    /**
     * Finds and assigns all UI components from the layout.
     */
    private void bindViews() {
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerSource   = findViewById(R.id.spinnerSource);
        spinnerDest     = findViewById(R.id.spinnerDest);
        editInput       = findViewById(R.id.editInput);
        btnConvert      = findViewById(R.id.btnConvert);
        tvResult        = findViewById(R.id.tvResult);
        tvResultLabel   = findViewById(R.id.tvResultLabel);
    }

    /**
     * Initializes the category spinner and handles selection changes.
     */
    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>();
        categories.add(CAT_CURRENCY);
        categories.add(CAT_EFFICIENCY);
        categories.add(CAT_DISTANCE);
        categories.add(CAT_VOLUME);
        categories.add(CAT_TEMPERATURE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String cat = categories.get(pos);
                updateUnitSpinners(cat);
                clearResult();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        updateUnitSpinners(CAT_CURRENCY);
    }

    /**
     * Updates the From/To unit spinners based on the selected category.
     */
    private void updateUnitSpinners(String category) {
        List<String> units = new ArrayList<>();
        switch (category) {
            case CAT_CURRENCY:
                units.addAll(CURRENCY_TO_USD.keySet());
                break;
            case CAT_EFFICIENCY:
                units.add("mpg");
                units.add("L/100km");
                break;
            case CAT_DISTANCE:
                units.add("Nautical Mile");
                units.add("Kilometer");
                units.add("Mile");
                units.add("Meter");
                break;
            case CAT_VOLUME:
                units.add("Gallon (US)");
                units.add("Liter");
                break;
            case CAT_TEMPERATURE:
                units.add("Celsius");
                units.add("Fahrenheit");
                units.add("Kelvin");
                break;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSource.setAdapter(adapter);
        spinnerDest.setAdapter(adapter);

        if (units.size() > 1) spinnerDest.setSelection(1);
    }

    /**
     * Sets up the logic for the conversion button.
     */
    private void setupConvertButton() {
        btnConvert.setOnClickListener(v -> {
            String inputStr = editInput.getText().toString().trim();
            if (inputStr.isEmpty()) {
                editInput.setError("Please enter a value");
                return;
            }

            double inputValue;
            try {
                inputValue = Double.parseDouble(inputStr);
            } catch (NumberFormatException e) {
                editInput.setError("Invalid number");
                return;
            }

            String category = spinnerCategory.getSelectedItem().toString();
            String source   = spinnerSource.getSelectedItem().toString();
            String dest     = spinnerDest.getSelectedItem().toString();

            if (source.equals(dest)) {
                showResult(inputValue, dest, category);
                return;
            }

            double result = convert(category, source, dest, inputValue);
            showResult(result, dest, category);
        });
    }

    // ===CONVERSION LOGIC===

    /**
     * Orchestrates the conversion based on category.
     */
    public double convert(String category, String source, String dest, double value) {
        switch (category) {
            case CAT_CURRENCY:
                return convertCurrency(source, dest, value);
            case CAT_EFFICIENCY:
                return convertEfficiency(source, dest, value);
            case CAT_DISTANCE:
                return convertDistance(source, dest, value);
            case CAT_VOLUME:
                return convertVolume(source, dest, value);
            case CAT_TEMPERATURE:
                return convertTemperature(source, dest, value);
            default:
                return value;
        }
    }

    private double convertCurrency(String source, String dest, double value) {
        Double sourceRate = CURRENCY_TO_USD.get(source);
        double inUSD = value * (sourceRate != null ? sourceRate : 1.0);
        Double destRate = USD_TO_CURRENCY.get(dest);
        return inUSD * (destRate != null ? destRate : 1.0);
    }

    /**
     * Handles Fuel Efficiency conversion. 
     * Note: L/100km is an inverse measure.
     * Formula: L/100km = 235.215 / mpg
     */
    private double convertEfficiency(String source, String dest, double value) {
        if (value <= 0) return 0; // Avoid division by zero
        // Both units (mpg and L/100km) use the same inverse formula for conversion
        // If source equals dest, it's handled upstream.
        return 235.215 / value;
    }

    private double convertDistance(String source, String dest, double value) {
        double km = toKm(source, value);
        return fromKm(dest, km);
    }

    private double convertVolume(String source, String dest, double value) {
        double liters = toLiters(source, value);
        return fromLiters(dest, liters);
    }

    // ===HELPER METHODS===

    private double toLiters(String unit, double v) {
        if (unit.equals("Gallon (US)")) return v * 3.78541;
        return v;
    }

    private double fromLiters(String unit, double v) {
        if (unit.equals("Gallon (US)")) return v / 3.78541;
        return v;
    }

    private double toKm(String unit, double v) {
        switch (unit) {
            case "Nautical Mile": return v * 1.852;
            case "Mile":          return v * 1.60934;
            case "Meter":         return v / 1000.0;
            default:              return v;
        }
    }

    private double fromKm(String unit, double v) {
        switch (unit) {
            case "Nautical Mile": return v / 1.852;
            case "Mile":          return v / 1.60934;
            case "Meter":         return v * 1000.0;
            default:              return v;
        }
    }

    private double convertTemperature(String source, String dest, double value) {
        double celsius;
        switch (source) {
            case "Fahrenheit":
                celsius = (value - 32) / 1.8;
                break;
            case "Kelvin":
                celsius = value - 273.15;
                break;
            default:
                celsius = value;
        }

        switch (dest) {
            case "Fahrenheit":
                return (celsius * 1.8) + 32;
            case "Kelvin":
                return celsius + 273.15;
            default:
                return celsius;
        }
    }

    /**
     * Formats and displays the conversion result with appropriate pluralization.
     */
    private void showResult(double result, String destUnit, String category) {
        // Use different precision based on category/unit
        String pattern = "%.2f"; // Default to 2 decimal places

        switch (category) {
            case CAT_CURRENCY:
                if (destUnit.equals("JPY")) {
                    pattern = "%.0f"; // Japanese Yen usually doesn't use decimals
                } else {
                    pattern = "%.2f"; // Most currencies use 2 decimal places
                }
                break;
            case CAT_DISTANCE:
            case CAT_VOLUME:
                pattern = "%.4f"; // Distance and Volume benefit from higher precision
                break;
            case CAT_EFFICIENCY:
            case CAT_TEMPERATURE:
                pattern = "%.2f"; // Efficiency and Temperature are usually fine with 2
                break;
        }

        // Format to the chosen precision
        String formatted = String.format(java.util.Locale.US, pattern, result);

        // Strip unnecessary trailing zeros for non-currency units (e.g., 1.50 -> 1.5)
        // For Currency (except JPY), we keep exactly 2 decimal places (e.g., 10.50)
        // We only strip if a decimal point exists to avoid turning 150 into 15
        if (!category.equals(CAT_CURRENCY) || destUnit.equals("JPY")) {
            if (formatted.contains(".")) {
                formatted = formatted.replaceAll("0*$", "").replaceAll("\\.$", "");
            }
        }

        // Safeguard: If the result is non-zero but formatted to "0" due to precision,
        // use more decimal places to show it's not actually zero.
        if (result != 0 && (formatted.equals("0") || formatted.equals("-0") || formatted.isEmpty())) {
            formatted = String.format(java.util.Locale.US, "%.6f", result);
            if (formatted.contains(".")) {
                formatted = formatted.replaceAll("0*$", "").replaceAll("\\.$", "");
            }
        }
        
        // Determine the correct singular/plural form of the unit
        String finalUnit = formatted.equals("1") ? destUnit : getPluralUnit(destUnit);
        
        findViewById(R.id.resultCard).setVisibility(View.VISIBLE);
        tvResultLabel.setVisibility(View.VISIBLE);
        tvResult.setText(formatted + " " + finalUnit);
    }

    /**
     * Returns the plural form of a unit string if applicable.
     */
    private String getPluralUnit(String unit) {
        switch (unit) {
            case "Nautical Mile": return "Nautical Miles";
            case "Kilometer":     return "Kilometers";
            case "Mile":          return "Miles";
            case "Meter":         return "Meters";
            case "Gallon (US)":   return "Gallons (US)";
            case "Liter":         return "Liters";
            case "Kelvin":        return "Kelvins";
            default:              return unit; // Currency codes, Efficiency symbols, Celsius, Fahrenheit
        }
    }

    /**
     * Hides the result view.
     */
    private void clearResult() {
        findViewById(R.id.resultCard).setVisibility(View.INVISIBLE);
        tvResultLabel.setVisibility(View.INVISIBLE);
        tvResult.setText("");
    }
}
