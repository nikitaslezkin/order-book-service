package com.orderbook;

import com.orderbook.actors.DiffProcessingActor;
import com.orderbook.actors.DiffFetchingActor;
import com.orderbook.clients.DepthSnapshotApi;
import com.orderbook.model.OrderBookStorage;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class OrderBookService {

    private static final String SYMBOL = "ETHUSDT";
    private static final int LEVELS_TO_VIEW = 10;
    private static final int PRICE_PRECISION = 2;
    private static final int AMOUNT_PRECISION = 4;
    private static final int ORDER_BOOK_DEPTH_LIMIT = 5000;
    private static final int VIEW_TIMEOUT = 10000; // ms
    private static final int INTERMEDIATE_TIMEOUT = 5000; // ms

    // queue of diffs
    public static ConcurrentLinkedQueue<String> diffDepthQueue;

    public static void main(String[] args) throws Exception {

        diffDepthQueue = new ConcurrentLinkedQueue<>();
        // start accumulating diff buffer
        DiffFetchingActor.initialiseAndRun(SYMBOL);

        TimeUnit.MILLISECONDS.sleep(INTERMEDIATE_TIMEOUT);

        // read and save full order book snapshot
        OrderBookStorage.initiateOrderBook(DepthSnapshotApi.getDepthSnapshot(SYMBOL, ORDER_BOOK_DEPTH_LIMIT));

        // run thread for apply order book's diffs
        Thread diffProcessingActor = new Thread(new DiffProcessingActor());
        diffProcessingActor.start();

        TimeUnit.MILLISECONDS.sleep(INTERMEDIATE_TIMEOUT);

        // begin printing order book
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    OrderBookStorage.printOrderBook(LEVELS_TO_VIEW, PRICE_PRECISION, AMOUNT_PRECISION);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, VIEW_TIMEOUT);
    }

}
