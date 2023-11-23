package com.orderbook.actors;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

import static com.orderbook.OrderBookService.diffDepthQueue;

// This class fetches order book's diffs through websocket connection
public class DiffFetchingActor {
    private static final String BASE_WS_URL = "wss://stream.binance.com:9443/ws/%s@depth";

    // This static method construct and run thread listening binance websocket
    public static void initialiseAndRun(String symbol) {

        URI uri = URI.create(String.format(BASE_WS_URL, symbol.toLowerCase()));

        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                System.out.println("Connected to Binance WebSocket");
            }

            @Override
            public void onMessage(String message) {
                diffDepthQueue.add(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Closed connection to Binance WebSocket.");
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        };

        client.connect();
    }



}

