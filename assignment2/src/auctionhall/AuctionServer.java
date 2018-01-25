package auctionhall;

/**
 *  @author WenboSun
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class AuctionServer
{
	/**
	 * Singleton: the following code makes the server a Singleton. You should
	 * not edit the code in the following noted section.
	 * 
	 * For test purposes, we made the constructor protected. 
	 */

	/* Singleton: Begin code that you SHOULD NOT CHANGE! */
	protected AuctionServer()
	{
	}

	private static AuctionServer instance = new AuctionServer();

	public static AuctionServer getInstance()
	{
		return instance;
	}

	/* Singleton: End code that you SHOULD NOT CHANGE! */





	/* Statistic variables and server constants: Begin code you should likely leave alone. */


	/**
	 * Server statistic variables and access methods:
	 */
	private int soldItemsCount = 0;
	private int revenue = 0;

	public int soldItemsCount()
	{
		return this.soldItemsCount;
	}

	public int revenue()
	{
		return this.revenue;
	}



	/**
	 * Server restriction constants:
	 */
	public static final int maxBidCount = 10; // The maximum number of bids at any given time for a buyer.
	public static final int maxSellerItems = 20; // The maximum number of items that a seller can submit at any given time.
	public static final int serverCapacity = 80; // The maximum number of active items at a given time.


	/* Statistic variables and server constants: End code you should likely leave alone. */


	/**
	 * Some variables we think will be of potential use as you implement the server...
	 */

	// List of items currently up for bidding (will eventually remove things that have expired).
	private List<Item> itemsUpForBidding = new ArrayList<Item>();

	// The last value used as a listing ID.  We'll assume the first thing added gets a listing ID of 0.
	private int lastListingID = -1; 

	// List of item IDs and actual items.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Item> itemsAndIDs = new HashMap<Integer, Item>();

	// List of itemIDs and the highest bid for each item.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Integer> highestBids = new HashMap<Integer, Integer>();

	// List of itemIDs and the person who made the highest bid for each item.   This is a running list with everything ever bid upon.
	private HashMap<Integer, String> highestBidders = new HashMap<Integer, String>(); 

	// List of sellers and how many items they have currently up for bidding.
	private HashMap<String, Integer> itemsPerSeller = new HashMap<String, Integer>();

	// List of buyers and how many items on which they are currently bidding.
	private HashMap<String, Integer> itemsPerBuyer = new HashMap<String, Integer>();
	
	// List of expired item without bidding.
	private List<Item> unbidItem = new ArrayList<Item>();
	
	// List of to count a seller's <75 submit times. -1 for already disqualified
	private HashMap<String, Integer> sellerDisqualifiedCount = new HashMap<String, Integer>();
	
	// List of itemID and bidder name for temp sold item
	private HashMap<Integer, String> tempSold = new HashMap<Integer, String>();
	
	// Object used for instance synchronization if you need to do it at some point 
	// since as a good practice we don't use synchronized (this) if we are doing internal
	// synchronization.
	//
	private final Object instanceLock = new Object(); 

	public HashMap<Integer, Integer> getHighestBids() {
		return highestBids;
	}

	public HashMap<Integer, String> getHighestBidders() {
		return highestBidders;
	}
	
	public HashMap<Integer, Item> getItemsAndIDs() {
		return itemsAndIDs;
	}
	public List<Item> getUnbidItem() {
		return unbidItem;
	}	
	
	
	
	/*
	 *  The code from this point forward can and should be changed to correctly and safely 
	 *  implement the methods as needed to create a working multi-threaded server for the 
	 *  system.  If you need to add Object instances here to use for locking, place a comment
	 *  with them saying what they represent.  Note that if they just represent one structure
	 *  then you should probably be using that structure's intrinsic lock.
	 */

	 /*
	 Invariant:
	 1. serverCapacity >= maxSellerItems
	 2. lastListingID >= 0, revenue >= 0 , soldItemsCount >=0
	 3. itemsUpForBidding.size <= serverCapacity
	 4. every int in itemsPerSeller <= maxSellerItems
	 5. every int in itemsPerBuyer <= maxBidCount
	 */
	
	/*
	Precondition: all param passed in are not null, 1=<lowestBiddingPrice<=99
	Postcondition: if successfully submit item, return the item ID and add the item to itemsPerSeller and itemsUpForBidding 
				   if 1)the seller violate the rules 2)itemsUpForBidding.size reach the serverCapacity 
	             	  3) the item this seller have reach the maxSellerItems, return -1  
	Exception: if any param passed in is null, or lowestBiddingPrice is not in (1,99) return-1	 
	*/
	


	/**
	 * Attempt to submit an <code>Item</code> to the auction
	 * @param sellerName Name of the <code>Seller</code>
	 * @param itemName Name of the <code>Item</code>
	 * @param lowestBiddingPrice Opening price
	 * @param biddingDurationMs Bidding duration in milliseconds
	 * @return A positive, unique listing ID if the <code>Item</code> listed successfully, otherwise -1
	 */
	public int submitItem(String sellerName, String itemName, int lowestBiddingPrice, int biddingDurationMs)
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//   Make sure there's room in the auction site.
		//   If the seller is a new one, add them to the list of sellers.
		//   If the seller has too many items up for bidding, don't let them add this one.
		//   Don't forget to increment the number of things the seller has currently listed.
	

		//check Precondition
		if(sellerName!=null && itemName!=null && 0<lowestBiddingPrice && lowestBiddingPrice<100 && biddingDurationMs!=0) {
			synchronized(instanceLock) {
				synchronized(itemsUpForBidding) {
//					System.out.println("Thread: "+Thread.currentThread().getName()+"  "+"Seller name: "+sellerName+"  "+"");
					//add seller to itemsPerSeller and sellerDisqualifiedCount
					if(!itemsPerSeller.containsKey(sellerName)) itemsPerSeller.put(sellerName, 0);
					if(!sellerDisqualifiedCount.containsKey(sellerName)) sellerDisqualifiedCount.put(sellerName, 0);
					
					//check if the server have more space for item and if the seller reach the seller item limit
					if(itemsUpForBidding.size()<serverCapacity && itemsPerSeller.get(sellerName)<maxSellerItems) {
						//check if the seller is disqualified
						boolean disqualified = false;
						
						//Seller is disqualified if it submits three times in a row an item with opening price < $75, 
						//unless all the items that are left are in this range.
						
						//seller is already disqualified
						if (sellerDisqualifiedCount.get(sellerName)==-1) disqualified = true;
						
						//seller is not disqualified yet
						else {
							if(lowestBiddingPrice<75) {
						
								// but has 2 violation records
								if(sellerDisqualifiedCount.get(sellerName)==2) {
									boolean hasBigger = false;
									for(Item item: itemsAndIDs.values()) {
										if(item.seller()==sellerName) {
											if(item.lowestBiddingPrice()>=75) {
												hasBigger= true;
												break;
											}
										}
									}
									//disqualified the seller if one item >=75
									if(hasBigger) {
										disqualified = true;
										sellerDisqualifiedCount.put(sellerName, -1);
									}
								}
								//seller has less than 2 violation records
								else sellerDisqualifiedCount.put(sellerName, sellerDisqualifiedCount.get(sellerName)+1);
							}
							
								sellerDisqualifiedCount.put(sellerName,0);
								//Seller is disqualified if five or more of its items expire before anybody can bid.
								int disqualifiedCount2 =0;
								for(Item item: unbidItem) {
									if(item.seller().equals(sellerName)) {
										disqualifiedCount2++;
									}
								}
								if (disqualifiedCount2>=5) {
									disqualified = true;
									sellerDisqualifiedCount.put(sellerName, -1);
								}
							
						}
						//continue if the user is not disqualified
						if (!disqualified) {
							//update itemsUpForBidding itemsAndIDs itemsPerSeller accordingly
							lastListingID ++;
							Item item = new Item(sellerName, itemName, lastListingID, lowestBiddingPrice, biddingDurationMs);
							//update itemsUpForBidding
							itemsUpForBidding.add(item);
							
							//update itemsAndIDs
							itemsAndIDs.put(lastListingID,item);
	
							//update itemsPerSeller
							itemsPerSeller.put(sellerName,itemsPerSeller.get(sellerName)+1);
							
							System.out.println(sellerName+" submit successed, Item ID : "+lastListingID);
							return lastListingID;
						}
						else {
							System.out.println(sellerName+" submit failed due to disqualified Seller");
							return -1;
						}
					}
					else {
						System.out.println(sellerName+" submit failed due to reach the Serevr Capacity or Seller Item limit");
						List<Item> expiredItem = new ArrayList<Item>();
						for(Item item: itemsUpForBidding) {
							if(!item.biddingOpen()) {
								expiredItem.add(item);
							}
						}
						
						for(Item item: expiredItem) {
							itemsUpForBidding.remove(item);
							if(highestBids.containsKey(item.listingID())) {
								tempSold.put(item.listingID(), highestBidders.get(item.listingID()));
							}
							else {
								unbidItem.add(item);
								System.out.println( "The item: "+ item.listingID() +" is expired without bidding");
							}
						}
						
						return -1;
					}
				}// end of lock itemsUpForBidding
			}//end of lock instanceLock
		}
		//Exception
		else {
			System.out.println(sellerName+" submit failed due to opening price is not in 1 to 99");
			return -1;
		}
	}



	/*
	Precondition: no
	Postcondition: return list of item
	Exception: no
	*/
	/**
	 * Get all <code>Items</code> active in the auction
	 * @return A copy of the <code>List</code> of <code>Items</code>
	 */
	public List<Item> getItems()
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//    Don't forget that whatever you return is now outside of your control.
		synchronized(instanceLock) {
			List<Item> list = new ArrayList<Item>();
			for (Item item: itemsUpForBidding) {
				list.add(item);
			}
			return list;
		}
	}

	/*
	Precondition: all param passed in are not null
	Postcondition: return true if successfully submit, false otherwise
	Exception: if any param passed in is null return -1	
	*/
	/**
	 * Attempt to submit a bid for an <code>Item</code>
	 * @param bidderName Name of the <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @param biddingAmount Total amount to bid
	 * @return True if successfully bid, false otherwise
	 */
	public boolean submitBid(String bidderName, int listingID, int biddingAmount)
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//   See if the item exists.
		//   See if it can be bid upon.
		//   See if this bidder has too many items in their bidding list.
		//   Get current bidding info.
		//   See if they already hold the highest bid.
		//   See if the new bid isn't better than the existing/opening bid floor.
		//   Decrement the former winning bidder's count
		//   Put your bid in place
		
		if(bidderName!= null && listingID >=0 && biddingAmount>=0) {
			
			synchronized(instanceLock) {		
//				System.out.println("Thread: "+Thread.currentThread().getName()+"  "+"Bidder name: "+bidderName);
				
				synchronized(itemsUpForBidding) {
					synchronized(highestBidders) {
						synchronized(highestBids) {
							//check if the item exists
							boolean isExist = false;
							for(Item item: itemsUpForBidding) {
								if(item.listingID()==listingID) {
									isExist = true;
									break;
								}
							}
							if(isExist && itemsAndIDs.get(listingID).biddingOpen()) {
								//add the bidder to itemsPerBuyer if it's new
								if(!itemsPerBuyer.containsKey(bidderName)) {
									itemsPerBuyer.put(bidderName, 0);
								}
								//check if this bidder has too many items in its bidding list
								if(itemsPerBuyer.get(bidderName)<maxBidCount) {
									//Get current bidding info.
				
									synchronized(highestBidders) {
										//if they already hold the highest bid.
										if(itemUnbid(listingID) || !highestBidders.get(listingID).equals(bidderName)) {
											//if the new bid better than current price
											boolean isBetter =false;
											if(itemUnbid(listingID)) {
												if(itemsAndIDs.get(listingID).lowestBiddingPrice()<=biddingAmount) {
													isBetter = true;
												}
											}
											else {
												synchronized(highestBids) {
													if(highestBids.get(listingID)<biddingAmount) {
														isBetter = true;
														//Decrement the former winning bidder's count
														synchronized(itemsPerBuyer) {
															itemsPerBuyer.put(highestBidders.get(listingID), itemsPerBuyer.get(highestBidders.get(listingID))-1);
														}
													}
												}
											}
											if(isBetter) {
												//Put bid in place, update highestBids, highestBidders, itemsPerBuyer
												System.out.println(bidderName+" submit successed. Item ID: "+listingID+ ". New price: "+ biddingAmount);
												highestBids.put(listingID,biddingAmount);
												highestBidders.put(listingID, bidderName);
												itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName)+1);
												return true;
											}
											else {
												System.out.println(bidderName+" submit failed due to bid is lower than current price");
												return false;
											}
										}
										else {
											System.out.println(bidderName+" submit failed due to already hold the highest bid");
											return false;
										}
									}//end of lock highestBidders
								}
								else {
									System.out.println(bidderName+" submit failed due to too many items in bidding list");
									return false;
								}	
							}
							else {
								//check status
								//if the item is bid
								if(!itemUnbid(listingID)) {
									//if the bidder win	
									String name = highestBidders.get(listingID);
									//remove the item
									int index = 0;	
									for(Item item: itemsUpForBidding) {
										if(item.listingID()==listingID) break;
										index++;
									}
									if(index<itemsUpForBidding.size()) {
										//put the item in tempSold
										tempSold.put(listingID, name);
										//itemsUpForBidding.get(index)
										itemsUpForBidding.remove(index);
										System.out.println(bidderName+" failed on item "+ listingID+", the item was sold to "+name);	
									}
								}
								else {
									//remove the item
									int index = 0;	
									for(Item item: itemsUpForBidding) {
										if(item.listingID()==listingID) break;
										index++;
									}
									if(index<itemsUpForBidding.size()) {
										itemsUpForBidding.remove(index);
										unbidItem.add(itemsAndIDs.get(listingID));
										System.out.println(bidderName+" status check: the item: "+ listingID +" is expired without bidding");
									}
								}
								System.out.println(bidderName+" submit failed due to Item doesn't exist");
								return false;
							}
						}//end of lock highestBids
					}//end of lock highestBidders
				}//end of lock itemsUpForBidding
			}//end of lock instanceLock
		}
		//exception
		else {
			System.out.println(bidderName+" submit failed due to illegal Argument");
			return false;
		}
	}

	
	/*
	Precondition: all param passed in are not null
	Postcondition: return 1 if the bid over and the bidder win
				   return 2 if the bid is still running
				   return 3 if the bidder lose or listingID doesn't exist
				   if the item is expired, remove it
						if someone bid on the item, upgrade soldItemsCount, revenue, highestBids, highestBidders, itemsPerSeller, and itemsPerBuyer
	Exception: if any param passed in is null return -1
	*/	
	/**
	 * Check the status of a <code>Bidder</code>'s bid on an <code>Item</code>
	 * @param bidderName Name of <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return 1 (success) if bid is over and this <code>Bidder</code> has won<br>
	 * 2 (open) if this <code>Item</code> is still up for auction<br>
	 * 3 (failed) If this <code>Bidder</code> did not win or the <code>Item</code> does not exist
	 */
	public int checkBidStatus(String bidderName, int listingID)
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//   If the bidding is closed, clean up for that item.
		//     Remove item from the list of things up for bidding.
		//     Decrease the count of items being bid on by the winning bidder if there was any...
		//     Update the number of open bids for this seller

		if(bidderName!=null && listingID>=0) {
			synchronized(instanceLock) {
				synchronized(tempSold) {
					//check if the item is in tempSold
					if(tempSold.containsKey(listingID) && tempSold.get(listingID).equals(bidderName)) {
						soldItemsCount++;
						revenue+= highestBids.get(listingID);
						itemsPerSeller.computeIfPresent(itemsAndIDs.get(listingID).seller(), (k, v) -> v - 1);
						itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName)-1);
						System.out.println(bidderName+" win the bidding on item "+ listingID);
						return 1;	
					}
					else {
						synchronized(itemsUpForBidding) {
							boolean isExist = false;
							for(Item item: itemsUpForBidding) {
								if(item.listingID()==listingID) {
									isExist = true;
									break;
								}
							}
							if(isExist) {
								// if the item still open for bid
								if(!itemsAndIDs.get(listingID).biddingOpen()) {
									synchronized(highestBidders) {
										//if the item is bid
										if(!itemUnbid(listingID)) {
											//if the bidder win
											if(highestBidders.get(listingID).equals(bidderName)) {
												
												//remove the item
												int index = 0;	
												for(Item item: itemsUpForBidding) {
													if(item.listingID()==listingID) break;
													index++;
												}
												itemsUpForBidding.remove(index);
												//upgrade soldItemsCount, revenue, itemsPerSeller, and itemsPerBuyer
												soldItemsCount++;
												revenue+= highestBids.get(listingID);
												itemsPerSeller.computeIfPresent(itemsAndIDs.get(listingID).seller(), (k, v) -> v - 1);
												itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName)-1);
												System.out.println(bidderName+" win the bidding on item "+ listingID);
												return 1;
											}
											//if the bidder lose
											else {
												String name = highestBidders.get(listingID);
												//remove the item
												int index = 0;	
												for(Item item: itemsUpForBidding) {
													if(item.listingID()==listingID) break;
													index++;
												}
												//put the item in tempSold
												tempSold.put(listingID, name);
												//itemsUpForBidding.get(index)
												itemsUpForBidding.remove(index);
												System.out.println(bidderName+" failed on item "+ listingID+", the item was sold to "+name);
												return 3;										
											}
										}
										else {
											//remove the item
											int index = 0;	
											for(Item item: itemsUpForBidding) {
												if(item.listingID()==listingID) break;
												index++;
											}
											itemsUpForBidding.remove(index);
											unbidItem.add(itemsAndIDs.get(listingID));
											System.out.println(bidderName+" status check: the item: "+ listingID +" is expired without bidding");
											return 3;
										}
									}
								}
								//if the item is still open
								else {
									System.out.println(bidderName+" status check: the item: "+ listingID +" is still opening for bidding");
									return 2;
								}
							}
							else {
								System.out.println(bidderName+" failed on item "+ listingID+", the item doesn't exist anymore");
								return 3;
							}
						}//end of itemsUpForBidding
					}
				}//end of lock tempSold
			}//end of lock instanceLock
		}
		//exception
		else return -1;
	}

	
	/*
	Precondition: the listingID passed in should match the item on sale
	Postcondition: return the current price for the item
	Exception: if the listingID passed in doesn't match return -1
	*/	
	/**
	 * Check the current bid for an <code>Item</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return The highest bid so far or the opening price if no bid has been made,
	 * -1 if no <code>Item</code> exists
	 */
	public int itemPrice(int listingID)
	{
		// TODO: IMPLEMENT CODE HERE
		
		//if item exist in highestBids
		if(highestBids.containsKey(listingID)) {
			return highestBids.get(listingID);
		}
		else {
			if(itemsAndIDs.containsKey(listingID)) {
				return itemsAndIDs.get(listingID).lowestBiddingPrice();
			}
			else return -1;
		}
	}

	
	/*
	Precondition: no
	Postcondition: return false if item exist in highestBids,false otherwise
	Exception: no
	*/	
	/**
	 * Check whether an <code>Item</code> has been bid upon yet
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return True if there is no bid or the <code>Item</code> does not exist, false otherwise
	 */
	public Boolean itemUnbid(int listingID)
	{
		// TODO: IMPLEMENT CODE HERE
		
		if(highestBids.containsKey(listingID)) return false;
		else return true;
	}
}
 