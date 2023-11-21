package com.orderbook.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// This class keep order book data and additional methods to maintain order book.
public class OrderBookStorage {

    private static final double EPS = 0.00000001;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ConcurrentNavigableMap<String, String> bids = new ConcurrentSkipListMap<>((o1, o2) -> Double.compare(Double.parseDouble(o2), Double.parseDouble(o1)));
    private static final ConcurrentNavigableMap<String, String> asks = new ConcurrentSkipListMap<>(Comparator.comparingDouble(Double::parseDouble));
    public static AtomicInteger lastUpdateId = new AtomicInteger();

    // Initialise order book with binance api data
    public static void initiateOrderBook(JsonNode jsonNode) {
        if (jsonNode.has("bids")) {
            updateOrders(bids, jsonNode.get("bids"));
        }

        if (jsonNode.has("asks")) {
            updateOrders(asks, jsonNode.get("asks"));
        }

        if (jsonNode.has("lastUpdateId")) {
            lastUpdateId.set(jsonNode.get("lastUpdateId").asInt());
        }
    }

    // Initialise order book with binance websocket diff data
    public static void updateOrderBook(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            if (jsonNode.has("b")) {
                updateOrders(bids, jsonNode.get("b"));
            }

            if (jsonNode.has("a")) {
                updateOrders(asks, jsonNode.get("a"));
            }

            if (jsonNode.has("u")) {
                lastUpdateId.set(jsonNode.get("u").asInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update particular side of order book (bid or ask)
    private static void updateOrders(Map<String, String> orders, JsonNode updates) {
        Iterator<JsonNode> elements = updates.elements();

        while (elements.hasNext()) {
            JsonNode update = elements.next();
            String price = update.get(0).asText();
            String quantity = update.get(1).asText();
            double quantityFloat = update.get(1).asDouble();

            if (quantityFloat < EPS) {
                orders.remove(price);
            } else {
                orders.put(price, quantity);
            }
        }
    }

    // Helper method for converting a string with a given precision
    public static String convertPrecision(String input, int newPrecision) {
        try {
            double value = Double.parseDouble(input);
            String pattern = "0." + new String(new char[newPrecision]).replace('\0', '0');
            DecimalFormat decimalFormat = new DecimalFormat(pattern);
            return decimalFormat.format(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    // This method formats and outputs order book to the console
    public static void printOrderBook(int levels, int pricePrecision, int amountPrecision) {

        System.out.printf("%-10s  %10s|%-10s  %10s%n", "BID_SIZE", "BID_PRICE", "ASK_PRICE", "ASK_SIZE");

        List<String> a = bids.entrySet().stream().limit(levels).map((position) ->
                String.format("%-10s  %10s", convertPrecision(position.getValue(), amountPrecision), convertPrecision(position.getKey(), pricePrecision)))
                .collect(Collectors.toList());

        List<String> b = asks.entrySet().stream().limit(levels).map((position) ->
                String.format("%-10s  %10s", convertPrecision(position.getKey(), pricePrecision), convertPrecision(position.getValue(), amountPrecision)))
                .collect(Collectors.toList());

        for(int i = 0; i < Math.max(a.size(), b.size()); i++) {
            String output = (i < a.size()) ? a.get(i) : String.format("%-22s","");
            output += "|" + ((i < b.size()) ? b.get(i) : String.format("%-22s",""));
            System.out.println(output);
        }
        System.out.println("---------------------------------------------");
    }
}
