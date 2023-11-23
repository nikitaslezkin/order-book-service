package com.orderbook.actors;

import com.orderbook.model.OrderBookStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.orderbook.OrderBookService.diffDepthQueue;

// This class analyses diff queue and apply them into current order book state
public class DiffProcessingActor implements Runnable {

    private static final long ROLL_OUT_TIMEOUT = 5; // ms

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // previous last update index. Need to analyse data consistency (U_n = u_{n - 1} + 1)
    public static int prevLastUpdate;

    // this actor continuously processes caching queue and applies changes to stored data
    public void run() {
        while (true) {
            try {
                if (!diffDepthQueue.isEmpty()) {
                    String item = diffDepthQueue.poll();
                    JsonNode jsonNode = objectMapper.readTree(item);
                    int firstUpdate = jsonNode.get("U").asInt();
                    int lastUpdate = jsonNode.get("u").asInt();
                    if (firstUpdate != prevLastUpdate + 1 && prevLastUpdate != 0) {
                        throw new RuntimeException("Serial data does not match");
                    }
                    prevLastUpdate = lastUpdate;
                    if (firstUpdate <= OrderBookStorage.lastUpdateId.get() && lastUpdate >= OrderBookStorage.lastUpdateId.get() + 1 ||
                            firstUpdate > OrderBookStorage.lastUpdateId.get()) {
                        OrderBookStorage.updateOrderBook(item);
                    }
                } else {
                    Thread.sleep(ROLL_OUT_TIMEOUT);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}

