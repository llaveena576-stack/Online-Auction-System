# Online Auction System using Priority Queue

A simple console-based Java Online Auction System that demonstrates real-time highest-bid tracking and time-based auction closing using the Priority Queue (Heap) data structure. The project is designed as a Low-Level Design (LLD) implementation showcasing Object-Oriented Programming (OOP) concepts such as classes, objects, encapsulation, and object composition.

## Overview
The application allows the system to:
- Create auctions for items with a base price and a closing time
- Accept bids from multiple users
- Track the current highest bid at any point in time
- Automatically close auctions once their time expires and declare the winner

The system uses two Priority Queues to efficiently manage auctions and bids:
- **Bid Heap (max-heap)** – Stores all bids for one auction, highest bid always on top.
- **Closing Scheduler (min-heap)** – Stores all active auctions ordered by closing time, soonest-to-close always on top.

## Problem Statement
Develop a console-based online auction system that supports real-time highest-bid lookup and efficient auction closing using Priority Queues while following Object-Oriented Programming principles.

## Objectives
- Implement an online auction system in Java.
- Demonstrate the Priority Queue (Heap) data structure.
- Implement real-time highest-bid tracking.
- Implement automatic, time-based auction closing.
- Apply OOP concepts using classes and objects.

## Features
- Create item auctions with base price and duration
- Place bids (validated against current highest bid)
- Instantly view the current highest bid
- Auto-close auctions once their time expires
- Declare the winning bid and mark the item as sold
- Custom Comparable-based heap ordering
- Single Java source file using multiple classes

## Technologies Used
- Language: Java
- IDE: Visual Studio Code
- Version Control: Git
- Repository: GitHub

## Data Structure Used
### Priority Queue (Heap)
The application maintains two priority queues:
- **Bid Heap (max-heap, per auction)** — Highest bid always on top; `peek()` is O(1), `offer()` is O(log n).
- **Closing Scheduler (min-heap, in AuctionManager)** — Ordered by `endTime`; always knows which auction expires next without scanning every auction; `poll()` is O(log n).

## Project Structure
```
OnlineAuctionSystem
│
├── README.md
├── .gitignore
│
├── docs
│   ├── FlowDiagram.png
│   └── README_Project.md
│
├── output
│   └── SampleOutput.txt
│
└── src
    └── Main.java
```

## Classes Used
Although the project uses a single source file (`Main.java`), it contains five classes:

**Main**
- Creates the `AuctionManager`, an `Item`, and an `Auction`
- Simulates multiple bids being placed
- Waits for the auction timer to expire
- Triggers auction closing and displays the result

**User**
Represents any person using the system (bidder/seller/admin).
- Fields: `userId`, `name`, `email`, `role`

**Item**
Represents the product being auctioned.
- Fields: `itemId`, `title`, `description`, `sellerId`, `basePrice`, `status`

**Bid**
Represents a single bid placed by a user, `Comparable<Bid>` so it can sit directly in the bid heap.
- Orders by amount descending (max-heap); ties broken by earlier timestamp

**Auction**
Represents one live auction for one item, `Comparable<Auction>` so it can sit in the closing scheduler.
- Methods: `placeBid()`, `getHighestBid()`
- Orders by `endTime` ascending (min-heap)

**AuctionManager**
Central engine controlling every auction.
- Fields: `activeAuctions` (HashMap for O(1) lookup by id), `closingScheduler` (min-heap by endTime)
- Methods: `createAuction()`, `placeBid()`, `closeExpiredAuctions()`, `getTopBid()`

## Algorithm
1. Start the application.
2. Create the `AuctionManager` object.
3. Create an `Item` and wrap it in an `Auction` with a fixed duration.
4. Register the auction: add to `activeAuctions` map and push into `closingScheduler`.
5. For each incoming bid:
   - If auction is not ACTIVE, reject.
   - If bid amount ≤ current highest (`bidHeap.peek()`), reject.
   - Otherwise push the bid into `bidHeap`.
6. To check the current leader at any time: `bidHeap.peek()` — O(1).
7. Periodically (or after the timer expires): check `closingScheduler.peek()`.
8. While the top auction's `endTime` has passed:
   - Pop it from `closingScheduler`.
   - Mark it CLOSED, pop the highest bid as the winner.
   - Mark the item SOLD, remove the auction from `activeAuctions`.
9. Exit the application.

## Sample Output
```
Created: Auction[A1] Vintage Cricket Bat
Bid accepted: U1 bid 600.0
Bid rejected: must be higher than current highest bid (600.0)
Bid accepted: U3 bid 800.0
Bid rejected: must be higher than current highest bid (800.0)
Bid accepted: U2 bid 900.0
Current top bid: U2 bid 900.0
SOLD! Vintage Cricket Bat -> U2 at 900.0
```

## OOP Concepts Used
- Classes and Objects
- Encapsulation
- Abstraction
- Object Composition
- Interfaces (`Comparable`)
- Modularity

## Future Enhancements
- GUI or web-based front end
- Multiple concurrent auctions with a live dashboard
- Multi-threading for real bidding concurrency
- Notify bidders when they've been outbid (Observer pattern)
- Persist auctions and bids to a database
- Auto-extend auction time on last-second bids

## Learning Outcomes
- Understanding of Priority Queue (Heap) data structures.
- Practical use of two heaps for two different orderings in one system.
- Experience with Java Object-Oriented Programming.
- Git and GitHub version control.
- Low-Level Design (LLD) implementation using classes and objects.

## Author
Laveena

Electrical and Electronics Engineering

## License
This project is developed for educational and academic purposes.
