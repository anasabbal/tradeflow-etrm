#!/usr/bin/env python3
"""PLACEHOLDER - synthetic trade generator.

Intent: emit 50 randomised trades across the 5 portfolios, with volumes between 5 and 50 MW and prices
near the latest ENTSO-E day-ahead price, by POSTing to trade-capture-service (POST /api/v1/trades).
Also intended to generate daily TTF/PEG/NBP-style synthetic gas prices with a random walk.

TODO: implement once trade-capture-service exposes its API.
"""

if __name__ == "__main__":
    raise SystemExit("generate_trades.py is a placeholder - not implemented yet")
