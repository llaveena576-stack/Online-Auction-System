import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class OnlineAuctionSystem {
    public static void main(String[] args) throws InterruptedException {
        AuctionManager manager = new AuctionManager();

        Item bat = new Item("I1", "Vintage Cricket Bat", "Signed bat", "S1", 500.0);
        Auction auction1 = new Auction("A1", bat, 3000);
        manager.createAuction(auction1);

        manager.placeBid("A1", "U1", 600);
        manager.placeBid("A1", "U2", 550);
        manager.placeBid("A1", "U3", 800);
        manager.placeBid("A1", "U1", 750);
        manager.placeBid("A1", "U2", 900);

        System.out.println("Current top bid: " + manager.getTopBid("A1"));

        Thread.sleep(3500);

        manager.closeExpiredAuctions();
    }
}

class User {
    private String userId;
    private String name;
    private String email;
    private String role;

    public User(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}

class Item {
    private String itemId;
    private String title;
    private String description;
    private String sellerId;
    private double basePrice;
    private String status;

    public Item(String itemId, String title, String description, String sellerId, double basePrice) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.sellerId = sellerId;
        this.basePrice = basePrice;
        this.status = "ACTIVE";
    }

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

class Bid implements Comparable<Bid> {
    private String bidId;
    private String auctionId;
    private String userId;
    private double amount;
    private long timestamp;

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

    @Override
    public int compareTo(Bid other) {
        if (this.amount != other.amount) {
            return Double.compare(other.amount, this.amount);
        }
        return Long.compare(this.timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return userId + " bid " + amount;
    }
}

class Auction implements Comparable<Auction> {
    private String auctionId;
    private Item item;
    private long endTime;
    private PriorityQueue<Bid> bidHeap;
    private String status;

    public Auction(String auctionId, Item item, long durationMillis) {
        this.auctionId = auctionId;
        this.item = item;
        this.endTime = System.currentTimeMillis() + durationMillis;
        this.bidHeap = new PriorityQueue<>();
        this.status = "ACTIVE";
    }

    public String getAuctionId() { return auctionId; }
    public Item getItem() { return item; }
    public long getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void placeBid(Bid bid) {
        if (!status.equals("ACTIVE")) {
            System.out.println("Auction closed. Cannot place bid.");
            return;
        }
        if (!bidHeap.isEmpty() && bid.getAmount() <= bidHeap.peek().getAmount()) {
            System.out.println("Bid rejected: must be higher than current highest bid ("
                    + bidHeap.peek().getAmount() + ")");
            return;
        }
        bidHeap.offer(bid);
        System.out.println("Bid accepted: " + bid);
    }

    public Bid getHighestBid() {
        return bidHeap.peek();
    }

    @Override
    public int compareTo(Auction other) {
        return Long.compare(this.endTime, other.endTime);
    }

    @Override
    public String toString() {
        return "Auction[" + auctionId + "] " + item.getTitle();
    }
}

class AuctionManager {
    private Map<String, Auction> activeAuctions;
    private PriorityQueue<Auction> closingScheduler;
    private int bidCounter = 0;

    public AuctionManager() {
        activeAuctions = new HashMap<>();
        closingScheduler = new PriorityQueue<>();
    }

    public void createAuction(Auction auction) {
        activeAuctions.put(auction.getAuctionId(), auction);
        closingScheduler.offer(auction);
        System.out.println("Created: " + auction);
    }

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

    public void closeExpiredAuctions() {
        long now = System.currentTimeMillis();

        while (!closingScheduler.isEmpty() && closingScheduler.peek().getEndTime() <= now) {
            Auction auction = closingScheduler.poll();
            auction.setStatus("CLOSED");

            Bid winningBid = auction.getHighestBid();
            if (winningBid != null) {
                auction.getItem().setStatus("SOLD");
                System.out.println("SOLD! " + auction.getItem().getTitle()
                        + " -> " + winningBid.getUserId() + " at " + winningBid.getAmount());
            } else {
                System.out.println("Auction " + auction.getAuctionId() + " closed with no bids.");
            }

            activeAuctions.remove(auction.getAuctionId());
        }
    }

    public Bid getTopBid(String auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        return auction != null ? auction.getHighestBid() : null;
    }
}
