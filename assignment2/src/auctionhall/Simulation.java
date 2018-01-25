package auctionhall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class provided for ease of test. This will not be used in the project 
 * evaluation, so feel free to modify it as you like.
 */ 
public class Simulation
{
    public static void main(String[] args)
    {                
        int nrSellers = 50;
        int nrBidders = 20;
        
        Thread[] sellerThreads = new Thread[nrSellers];
        Thread[] bidderThreads = new Thread[nrBidders];
        Seller[] sellers = new Seller[nrSellers];
        Bidder[] bidders = new Bidder[nrBidders];
        
        // Start the sellers
        for (int i=0; i<nrSellers; ++i)
        {
            sellers[i] = new Seller(
            		AuctionServer.getInstance(), 
            		"Seller"+i, 
            		100, 50, i
            );
            sellerThreads[i] = new Thread(sellers[i]);
            sellerThreads[i].start();
        }
        
        // Start the buyers
        for (int i=0; i<nrBidders; ++i)
        {
            bidders[i] = new Bidder(
            		AuctionServer.getInstance(), 
            		"Buyer"+i, 
            		1000, 20, 150, i
            );
            bidderThreads[i] = new Thread(bidders[i]);
            bidderThreads[i].start();
        }
        
        // Join on the sellers
        for (int i=0; i<nrSellers; ++i)
        {
            try
            {
                sellerThreads[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        // Join on the bidders
        for (int i=0; i<nrBidders; ++i)
        {
            try
            {
            	bidderThreads[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        // TODO: Add code as needed to debug
        System.out.println (" ");
        System.out.println ("Auction Over");
        System.out.println ("Result:");
        System.out.print("Totally Sold Items: "+ AuctionServer.getInstance().soldItemsCount()+"   ");
        System.out.println("Revenue: $"+ AuctionServer.getInstance().revenue());
        
        HashMap<String, List<Integer>> sellerHistory = new HashMap<String, List<Integer>>();
        HashMap<String, List<Integer>> bidderHistory = new HashMap<String, List<Integer>>();
        
        HashMap<Integer, Integer> highestBids = AuctionServer.getInstance().getHighestBids();
        HashMap<Integer, String> highestBidders  = AuctionServer.getInstance().getHighestBidders();
        HashMap<Integer, Item> itemsAndIDs = AuctionServer.getInstance().getItemsAndIDs();
        List<Item> unbidItem = AuctionServer.getInstance().getUnbidItem();
        
        for(int itemId: highestBidders.keySet()) {
        	if (!bidderHistory.containsKey(highestBidders.get(itemId))) {
        		List<Integer> itemList = new ArrayList<Integer>();
        		itemList.add(itemId);
        		bidderHistory.put(highestBidders.get(itemId),itemList );
        	}
        	else {
        		List<Integer> itemList = bidderHistory.get(highestBidders.get(itemId));
        		itemList.add(itemId);
        		bidderHistory.put(highestBidders.get(itemId),itemList );
        	}
        }
        
        for(int itemId: highestBids.keySet()) {
        	if(!sellerHistory.containsKey(itemsAndIDs.get(itemId).seller())) {
        		List<Integer> itemList = new ArrayList<Integer>();
        		itemList.add(itemId);
        		sellerHistory.put(itemsAndIDs.get(itemId).seller(), itemList);
        	}
        	else {
        		List<Integer> itemList = sellerHistory.get(itemsAndIDs.get(itemId).seller());
        		itemList.add(itemId);
        		sellerHistory.put(itemsAndIDs.get(itemId).seller(), itemList);
        	}
        }
        
        //print seller result
        System.out.println("The items each seller sold: ");
        for(String seller: sellerHistory.keySet()) {
        	int count = 0;
        	int income=0;
        	System.out.print(seller+" : ");
        	for(int item:sellerHistory.get(seller)) {
//        		System.out.print("[Item ID: "+item+ " Price: "+highestBids.get(item)+"]");
        		count++;
        		income+=highestBids.get(item);
        	}
        	System.out.println("Totaly sold: " +count +"  Income: $" + income);
        }
        
        System.out.println("************************************************");
        
        //print bidder result
        System.out.println("The items each bidder bought: ");
        for(String bidder: bidderHistory.keySet()) {
        	int count = 0;
        	int spend =0;
        	System.out.print(bidder+" : ");
        	for(int item:bidderHistory.get(bidder)) {
 //       		System.out.print("[Item ID: "+item+ " Price: "+highestBids.get(item)+"]");
        		count++;
        		spend+=highestBids.get(item);
        	}
        	System.out.println("Totaly bought: " +count +"  Spend: $" + spend);
        }
        
        System.out.println("************************************************");
        System.out.println("Unbid item number: "+unbidItem.size() + "    Sold rate: " 
        				  +(double)AuctionServer.getInstance().soldItemsCount()/(double)((unbidItem.size()+AuctionServer.getInstance().soldItemsCount()))+"%");
        
    }
}