package com.precificador.precificador;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

public class MainController {
    @FXML
    private Label dollarValue;
    @FXML
    private TextField productCostValue;
    @FXML
    private TextField checkoutTaxValue;
    @FXML
    private TextField saleTaxValue;
    @FXML
    private TextField desiredProfitValue;
    @FXML
    private Label finalPriceValue;

    @FXML
    protected void onLoadDolarExchange() {
        // Url de pesquisa da cotação do dólar
        String strURL = "https://economia.awesomeapi.com.br/json/last/USD-BRL";

        // Realizando a request de pesquisa
        try {
            URL url = new URL(strURL);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) request.getContent()));

            StringBuilder json = new StringBuilder();

            int c;

            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }

            //    {"USDBRL":{"code":"USD"                   [0]
            //    "codein":"BRL"                            [1]
            //    "name":"Dólar Americano/Real Brasileiro"  [2]
            //    "high":"5.1572"                           [3]
            //    "low":"5.1572"                            [4]
            //    "varBid":"0.0005"                         [5]
            //    "pctChange":"0.01"                        [6]
            //    "bid":"5.1567"                            [7] <=
            //    "ask":"5.1577"                            [8]
            //    "timestamp":"1667884526"                  [9]
            //    "create_date":"2022-11-08 02:15:26"}}     [10]
            String aux = json.toString().split(",")[7].split(":")[1].replace("\"", "");

            Double dollarExchange = Double.parseDouble(aux);
            System.out.println("U$ " + dollarExchange);

            dollarValue.setText(String.format("%,.2f", dollarExchange));
        } catch (SocketException e) {
            this.showMessages(Alert.AlertType.ERROR, "Não foi possível carregar a cotação do dia! Verifique sua conexão com a internet!");
        } catch (IOException e) {
            this.showMessages(Alert.AlertType.ERROR, e.getLocalizedMessage());
        }
    }
    @FXML
    protected void onCalcButtonClick() {
        if (dollarValue.getText().equals("0.0")) {
            System.out.println("Cotação Dólar");
            this.showMessages(Alert.AlertType.INFORMATION,"A cotação do dólar deve ser carregado!");
        } else {
            if (productCostValue.getText().equals("0.0")) {
                System.out.println("Custo Produto");
                this.showMessages(Alert.AlertType.INFORMATION,"O custo bruto do produto deve ser informado!");
            } else {
                if (checkoutTaxValue.getText().equals("0.0")) {
                    System.out.println("Taxa Checkout");
                    this.showMessages(Alert.AlertType.INFORMATION,"A taxa de checkout deve ser informada!");
                } else {
                    if (saleTaxValue.getText().equals("0.0")) {
                        System.out.println("Taxa Venda");
                        this.showMessages(Alert.AlertType.INFORMATION,"A taxa de venda deve ser informada!");
                    } else {
                        if (desiredProfitValue.getText().equals("0.0")) {
                            System.out.println("Lucro Desejado");
                            this.showMessages(Alert.AlertType.INFORMATION,"O lucro desejado deve ser informado!");
                        } else {
                            double productCost = 0;
                            double checkoutTax = 0;
                            double saleTax = 0;
                            double profitValue = 0;

                            try {
                                productCost = (Double.parseDouble(dollarValue.getText().replace(",", ".")) *
                                        Double.parseDouble(productCostValue.getText().replace(",", ".")));
                            } catch (NumberFormatException e) {
                                this.showMessages(Alert.AlertType.ERROR, "Custo Produto inválido!");
                                productCostValue.setText("0.0");
                                return;
                            }

                            try {
                                checkoutTax = (productCost * (Double.parseDouble(checkoutTaxValue.getText().replace(",", ".")) / 100));
                            } catch (NumberFormatException e) {
                                this.showMessages(Alert.AlertType.ERROR, "Taxa Checkout inválida!");
                                checkoutTaxValue.setText("0.0");
                                return;
                            }

                            try {
                                saleTax = (productCost * (Double.parseDouble(saleTaxValue.getText().replace(",", ".")) / 100));
                            } catch (NumberFormatException e) {
                                this.showMessages(Alert.AlertType.ERROR, "Taxa Venda inválida!");
                                saleTaxValue.setText("0.0");
                                return;
                            }

                            double costValue = productCost + checkoutTax + saleTax;

                            try {
                                profitValue = costValue * (Double.parseDouble(desiredProfitValue.getText().replace(",", ".")) / 100);
                            } catch (NumberFormatException e) {
                                this.showMessages(Alert.AlertType.ERROR, "Lucro Desejado inválido!");
                                desiredProfitValue.setText("0.0");
                                return;
                            }

                            double priceValue = costValue + profitValue;

                            finalPriceValue.setText(String.format("%,.2f", priceValue));

                            this.writePrecificationToFile(dollarValue.getText().replace(",", "."),
                                    productCostValue.getText().replace(",", "."),
                                    checkoutTaxValue.getText().replace(",", "."),
                                    saleTaxValue.getText().replace(",", "."),
                                    desiredProfitValue.getText().replace(",", "."),
                                    finalPriceValue.getText().replace(",", "."));
                        }
                    }
                }
            }
        }
    }
    @FXML
    protected void onCleanButtonClick() {
        productCostValue.setText("0.0");
        checkoutTaxValue.setText("0.0");
        saleTaxValue.setText("0.0");
        desiredProfitValue.setText("0.0");
        finalPriceValue.setText("0,00");
    }
    private void writePrecificationToFile(String dollarValue, String productCostValue, String checkoutTaxValue,
                                          String saleTaxValue, String desiredProfitValue, String finalPriceValue) {
        File precificationFile = new File("precificationList.csv");

        if (!precificationFile.exists()) {
            this.createPrecificationFile();
        }

        try {
            FileWriter writer = new FileWriter(precificationFile.getName(), true);
            writer.write(String.format("%s,%s,%s,%s,%s,%s;\n", dollarValue, productCostValue, checkoutTaxValue,
                                                            saleTaxValue, desiredProfitValue, finalPriceValue));
            writer.close();

            System.out.println("Gravação finalizada com sucesso!");
        } catch (IOException e) {
            this.showMessages(Alert.AlertType.ERROR, e.getLocalizedMessage());
        }
    }
    private void createPrecificationFile() {
        File precificationFile = new File("precificationList.csv");

        try {
            if (precificationFile.createNewFile()) {
                System.out.println("Arquivo criado: " + precificationFile.getName());
            }
        } catch (IOException e) {
            this.showMessages(Alert.AlertType.ERROR, e.getLocalizedMessage());
        }
    }
    private void showMessages(Alert.AlertType alertType, String msg) {
        Alert alert = new Alert(alertType, msg, ButtonType.OK);
        alert.showAndWait();
    }
}