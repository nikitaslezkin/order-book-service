# Order Book Service
This project periodically writes to the console the current state of the Binance order book of the usdt-eth pair with pre-configured formatting. 

Before starting the writing, the program updates the state of the order book by receiving a snapshot of the order book from the Binance API, and then sequentially rolling up the difference in the order book.

While the program is running, the consistency of the order book is constantly maintained.