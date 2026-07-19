// ==========================================================================
// ONLINE AUCTION SYSTEM - Java LLD Project
// Core DSA concept used: PRIORITY QUEUE (Heap)
//   1. Each Auction keeps a MAX-HEAP of Bids -> highest bid always at top (O(1) peek, O(log n) insert)
//   2. AuctionManager keeps a MIN-HEAP of Auctions ordered by endTime -> knows which auction
//      expires next, so closing auctions is efficient instead of checking all of them every time
// ==========================================================================

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

// --------------------------------------------------------------------------
// MAIN CLASS - entry point, demo run showing the whole flow end-to-end
// --------------------------------------------------------------------------
public class OnlineAuctionSystem {
    public static void main(String[] args) throws InterruptedException {

        // Step 1: Create the central manager that controls all auctions
        AuctionManager manager = new AuctionManager();

        // Step 2: Create an item that a seller wants to auction
        Item bat = new Item("I1", "Vintage Cricket Bat", "Signed bat", "S1", 500.0);

        // Step 3: Create an auction for that item, set to auto-close in 3000ms (3 sec)
        Auction auction1 = new Auction("A1", bat, 3000);
        manager.createAuction(auction1);

        // Step 4: Simulate multiple bidders placing bids (bidding war)
        manager.placeBid("A1", "U1", 600);   // accepted - first bid
        manager.placeBid("A1", "U2", 550);   // rejected - lower than current highest (600)
        manager.placeBid("A1", "U3", 800);   // accepted - new highest bid
        manager.placeBid("A1", "U1", 750);   // rejected - lower than current highest (800)
        manager.placeBid("A1", "U2", 900);   // accepted - new highest bid

        // Step 5: Check current highest bid at any point in time (before auction closes)
        System.out.println("Current top bid: " + manager.getTopBid("A1"));

        // Step 6: Wait until the auction's 3-second timer expires
        Thread.sleep(3500);

        // Step 7: Close all auctions whose time has expired, declare the winner
        manager.closeExpiredAuctions();
    }
}

// --------------------------------------------------------------------------
// USER CLASS - represents any person using the system (bidder/seller/admin)
// --------------------------------------------------------------------------
class User {
    private String userId;   // unique id for the user
    private String name;     // display name
    private String email;    // contact email
    private String role;     // BIDDER, SELLER, or ADMIN

    // Constructor - creates a new user with given details
    public User(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Getters - allow other classes to read private fields safely
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    // Custom string representation, e.g. "Ravi (BIDDER)"
    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}

// --------------------------------------------------------------------------
// ITEM CLASS - represents the product/item being auctioned
// --------------------------------------------------------------------------
class Item {
    private String itemId;        // unique id for the item
    private String title;         // item name, e.g. "Vintage Cricket Bat"
    private String description;   // item details
    private String sellerId;      // which user is selling this item
    private double basePrice;     // minimum starting price
    private String status;        // ACTIVE, CLOSED, or SOLD

    // Constructor - new item always starts as ACTIVE
    public Item(String itemId, String title, String description, String sellerId, double basePrice) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.sellerId = sellerId;
        this.basePrice = basePrice;
        this.status = "ACTIVE";
    }

    // Getters and one setter (status changes over the item's lifecycle)
    public String getItemId() { return itemId; }
    public String getTitle() { return title; }
    public double getBasePrice() { return basePrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return title + " (Base: " + basePrice + ", Status: " + status + ")";
    }
}

// --------------------------------------------------------------------------
// BID CLASS - represents a single bid placed by a user on an auction
// implements Comparable<Bid> so it can be stored directly in a PriorityQueue
// --------------------------------------------------------------------------
class Bid implements Comparable<Bid> {
    private String bidId;       // unique id for this bid
    private String auctionId;   // which auction this bid belongs to
    private String userId;      // who placed the bid
    private double amount;      // bid amount
    private long timestamp;     // when the bid was placed (used to break ties)

    // Constructor - timestamp is captured automatically at creation time
    public Bid(String bidId, String auctionId, String userId, double amount) {
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.userId = userId;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUserId() { return userId; }
    public double getAmount() { return amount; }
    public long getTimestamp() { return timestamp; }

    // compareTo defines the HEAP ORDER for the bidHeap in the Auction class.
    // We want a MAX-HEAP (highest amount comes out first), so we reverse the
    // natural ascending order by comparing "other" to "this" instead of the usual way.
    // If two bids have the exact same amount, the EARLIER bid wins (fair auction rule).
    @Override
    public int compareTo(Bid other) {
        if (this.amount != other.amount) {
            return Double.compare(other.amount, this.amount); // descending by amount -> max-heap
        }
        return Long.compare(this.timestamp, other.timestamp); // ascending by time -> earlier bid first
    }

    @Override
    public String toString() {
        return userId + " bid " + amount;
    }
}

// --------------------------------------------------------------------------
// AUCTION CLASS - represents one live auction for one item
// implements Comparable<Auction> so auctions can be stored in AuctionManager's
// closing-scheduler min-heap (ordered by which one ends soonest)
// --------------------------------------------------------------------------
class Auction implements Comparable<Auction> {
    private String auctionId;             // unique id for this auction
    private Item item;                    // the item being auctioned
    private long endTime;                 // exact epoch-millis timestamp when this auction closes
    private PriorityQueue<Bid> bidHeap;   // MAX-HEAP of all bids placed - top of heap = current highest bid
    private String status;                // ACTIVE or CLOSED

    // Constructor - endTime is calculated as "now + how long the auction should run for"
    public Auction(String auctionId, Item item, long durationMillis) {
        this.auctionId = auctionId;
        this.item = item;
        this.endTime = System.currentTimeMillis() + durationMillis;
        this.bidHeap = new PriorityQueue<>(); // uses Bid's compareTo automatically
        this.status = "ACTIVE";
    }

    public String getAuctionId() { return auctionId; }
    public Item getItem() { return item; }
    public long getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Called every time someone tries to bid on this auction
    public void placeBid(Bid bid) {
        // Rule 1: cannot bid on a closed auction
        if (!status.equals("ACTIVE")) {
            System.out.println("Auction closed. Cannot place bid.");
            return;
        }
        // Rule 2: new bid must be strictly higher than the current highest bid
        // bidHeap.peek() is O(1) - instantly gives the current highest bid without searching
        if (!bidHeap.isEmpty() && bid.getAmount() <= bidHeap.peek().getAmount()) {
            System.out.println("Bid rejected: must be higher than current highest bid ("
                    + bidHeap.peek().getAmount() + ")");
            return;
        }
        // Valid bid -> push into the heap. O(log n) insert.
        bidHeap.offer(bid);
        System.out.println("Bid accepted: " + bid);
    }

    // Returns the current highest bid without removing it (O(1) peek)
    public Bid getHighestBid() {
        return bidHeap.peek();
    }

    // This ordering is used by AuctionManager's closingScheduler (min-heap by endTime),
    // so the auction that will expire SOONEST always comes out of that heap first.
    @Override
    public int compareTo(Auction other) {
        return Long.compare(this.endTime, other.endTime);
    }

    @Override
    public String toString() {
        return "Auction[" + auctionId + "] " + item.getTitle();
    }
}

// --------------------------------------------------------------------------
// AUCTION MANAGER CLASS - the central engine that controls every auction.
// Combines a HashMap (fast lookup by auctionId) with a PriorityQueue
// (fast lookup of "which auction ends next").
// --------------------------------------------------------------------------
class AuctionManager {
    // Fast O(1) lookup: auctionId -> Auction object
    private Map<String, Auction> activeAuctions;

    // MIN-HEAP of auctions ordered by endTime (soonest-to-close auction is always at the top).
    // This means closeExpiredAuctions() never has to scan every auction - it just checks the top.
    private PriorityQueue<Auction> closingScheduler;

    // Simple counter to generate unique bid IDs
    private int bidCounter = 0;

    public AuctionManager() {
        activeAuctions = new HashMap<>();
        closingScheduler = new PriorityQueue<>(); // uses Auction's compareTo automatically
    }

    // Registers a new auction into both the lookup map and the closing scheduler
    public void createAuction(Auction auction) {
        activeAuctions.put(auction.getAuctionId(), auction);
        closingScheduler.offer(auction);
        System.out.println("Created: " + auction);
    }

    // Finds the auction by id and forwards the bid to it
    public void placeBid(String auctionId, String userId, double amount) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            System.out.println("Auction not found.");
            return;
        }
        bidCounter++;
        Bid bid = new Bid("B" + bidCounter, auctionId, userId, amount);
        auction.placeBid(bid);
    }

    // Checks the closingScheduler min-heap and closes every auction whose endTime
    // has already passed. In a real system this would be called periodically
    // by a background scheduler thread (e.g. every few seconds).
    public void closeExpiredAuctions() {
        long now = System.currentTimeMillis();

        // Keep closing auctions as long as the one at the top of the heap has expired.
        // Because it's a min-heap by endTime, the moment we see one that hasn't
        // expired yet, we know none of the remaining ones have either - so we can stop.
        while (!closingScheduler.isEmpty() && closingScheduler.peek().getEndTime() <= now) {
            Auction auction = closingScheduler.poll(); // removes and returns the soonest-expiring auction
            auction.setStatus("CLOSED");

            Bid winningBid = auction.getHighestBid();
            if (winningBid != null) {
                auction.getItem().setStatus("SOLD");
                System.out.println("SOLD! " + auction.getItem().getTitle()
                        + " -> " + winningBid.getUserId() + " at " + winningBid.getAmount());
            } else {
                System.out.println("Auction " + auction.getAuctionId() + " closed with no bids.");
            }

            // Remove from the active lookup map since it's no longer live
            activeAuctions.remove(auction.getAuctionId());
        }
    }

    // Utility method - lets outside code check the current highest bid on a given auction
    public Bid getTopBid(String auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        return auction != null ? auction.getHighestBid() : null;
    }
}
// ==========================================================================
// END OF FILE
// ==========================================================================