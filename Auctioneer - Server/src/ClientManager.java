//Mehmet Gülþen
//2013400075
//mehmetgulsen95@hotmail.com
//CMPE436-Term

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;

public class ClientManager extends Thread{
	Socket connectionSocket;
	Lock lock;
	
	public ClientManager(Socket s, Lock l){
		connectionSocket = s;
		lock = l;
	}
	
	public void run(){
		
		try {
			//to read data from the client
			BufferedReader inFromClient =
					new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			
			//to send data to client 
			DataOutputStream outToClient= new DataOutputStream(connectionSocket.getOutputStream());
			
			
			while(true){
				System.out.println("Waiting for input");
				
				//First line of a client request is request type.
				//"0" : Get auction information
				//"1" : Make a new bid.
				//"2" : Create a new auction item
				//If the user closes the connection, this will give a
				//NullPointerException. We will handle it and terminate the thread.
				String requestType = inFromClient.readLine();
				System.out.println("request type is: " + requestType);
				
				if(requestType.equals("0")){	//auction info
					String auction_id = inFromClient.readLine();
					System.out.println("auction id: "+ auction_id);
					String query =  "SELECT * FROM auctions WHERE id = '"+auction_id+"'";
					
					//Don't let the writers in while reading.
					lock.startRead();

					DBConnect db = new DBConnect();
					
					//make the database query
					ResultSet rs = db.getData(query);
					
					//Data is read. No need to lock others out.
					lock.endRead();
					
					if(rs.next()){
						//information from the database
						String current_bid = rs.getString("current_bid");
						String minimum_bid = rs.getString("minimum_bid");
						String datetime = rs.getString("datetime");
						String item_owner = rs.getString("item_owner");
						String bid_owner = rs.getString("bid_owner");

						//Response status "0" means success
						outToClient.writeBytes("0\n");
						System.out.println("0");
						//Sending auction information
						outToClient.writeBytes(auction_id+"\n");
						System.out.println(auction_id);
						outToClient.writeBytes(current_bid+"\n");
						System.out.println(current_bid);
						outToClient.writeBytes(minimum_bid+"\n");
						System.out.println(minimum_bid);
						outToClient.writeBytes(datetime+"\n");
						System.out.println(datetime);
						outToClient.writeBytes(item_owner+"\n");
						System.out.println(item_owner);
						outToClient.writeBytes(bid_owner+"\n");
						System.out.println(bid_owner);
						
					}else{
						//database couldn't find the item.
						//Response status "1" means error.
						outToClient.writeBytes("1"+"\n");
						outToClient.writeBytes("Auction does not exist."+"\n");
					}
				}
				else if(requestType.equals("1")){	//making a bid
					
					//Current datetime of the server in YYYY-MM-DD HH:mm:SS format
					Calendar cal = Calendar.getInstance();
					Timestamp now = new Timestamp(cal.getTimeInMillis());
					
					//reading bid and auction information from client
					String auction_id = inFromClient.readLine();
					String last_bid = inFromClient.readLine();
					String bet =  inFromClient.readLine();
					String nickname =  inFromClient.readLine();
					
					//Lock everyone out
					lock.startWrite();
					//Query to find the auction item.
					String query =  "SELECT * FROM auctions WHERE id = '"+auction_id+"'";
					DBConnect db = new DBConnect();
					ResultSet rs = db.getData(query);
					
					//To bid, auction must exist
					if(!(rs.next())){
						System.out.println("1!Auction does not exist.");
						outToClient.writeBytes("1"+"\n");
						outToClient.writeBytes("Auction does not exist."+"\n");
						lock.endWrite();
						continue;
					}

					//The client must be up-to-date regarding the auction item.
					if(!last_bid.equals(rs.getString("current_bid"))){
						System.out.println("1!Someone else has bid before you.");
						outToClient.writeBytes("1"+"\n");
						outToClient.writeBytes("Someone else has bid before you."+"\n");
						lock.endWrite();
						continue;
					}
					int int_bet = Integer.parseInt(bet);
					int int_current_bid = Integer.parseInt(rs.getString("current_bid"));
					int int_minimum_bid = Integer.parseInt(rs.getString("minimum_bid"));
					
					//Bid must be high enough.
					if( int_bet < int_minimum_bid +int_current_bid){
						System.out.println("1!Your bet is too small.");
						outToClient.writeBytes("1"+"\n");
						outToClient.writeBytes("Your bet is too small."+"\n");
						lock.endWrite();
						continue;
					}
					
					String datetime = rs.getString("datetime");
					Timestamp ts = Timestamp.valueOf(datetime);
					
					//Cannot bid on expired auctions.
					if(now.after(ts)){
						System.out.println("1!This auction has expired.");;
						outToClient.writeBytes("1"+"\n");
						outToClient.writeBytes("This auction has expired."+"\n");
						lock.endWrite();
						continue;
					}
					
					//query to update the item
					query = "UPDATE `auctions` SET current_bid = '"+
							bet+"', bid_owner = '"+nickname+"' WHERE auctions.id = '"+auction_id+"'";
					db.runQuery(query);
					
					//get the updated info of the item.
					query =  "SELECT * FROM auctions WHERE id = '"+auction_id+"'";
					rs = db.getData(query);
					rs.next();
					
					String type = "0";
					String current_bid = rs.getString("current_bid");
					String minimum_bid = rs.getString("minimum_bid");
					datetime = rs.getString("datetime");
					String item_owner = rs.getString("item_owner");
					String bid_owner = rs.getString("bid_owner");
					lock.endWrite();

					//Send the updated auction information
					outToClient.writeBytes(type+"\n");
					outToClient.writeBytes(auction_id+"\n");
					outToClient.writeBytes(current_bid+"\n");
					outToClient.writeBytes(minimum_bid+"\n");
					outToClient.writeBytes(datetime+"\n");
					outToClient.writeBytes(item_owner+"\n");
					outToClient.writeBytes(bid_owner+"\n");				
				}
				else{		//auction creation
					
					//Read the item specifications.
					String starting_bid = inFromClient.readLine();;
					String minimum_bid = inFromClient.readLine();;
					String datetime =  inFromClient.readLine();
					String owner =  inFromClient.readLine();


					//lock everyone out
					lock.startWrite();
					DBConnect db = new DBConnect();

					//Make the database query to create the item.
					String query = "INSERT INTO `auctions` (`id`, `current_bid`, `minimum_bid`, `datetime`, `item_owner`, `bid_owner`) VALUES (NULL, '"+
							starting_bid +"', '"+minimum_bid+"', '"+datetime+"', '"+owner+"', '"+owner+"');";

					db.runQuery(query);
					
					//make the query to get the last created item.
					query = "SELECT * FROM auctions ORDER BY ID DESC LIMIT 1";
					ResultSet rs = db.getData(query);
					lock.endWrite();

					//Read the info of the item from database
					rs.next();
					String type = "0";
					String auction_id = rs.getString("id");
					starting_bid = rs.getString("current_bid");
					minimum_bid = rs.getString("minimum_bid");
					datetime = rs.getString("datetime");
					String item_owner = rs.getString("item_owner");
					String bid_owner = rs.getString("bid_owner");

					//Send the auction info to the client
					outToClient.writeBytes(type+"\n");
					outToClient.writeBytes(auction_id+"\n");
					outToClient.writeBytes(starting_bid+"\n");
					outToClient.writeBytes(minimum_bid+"\n");
					outToClient.writeBytes(datetime+"\n");
					outToClient.writeBytes(item_owner+"\n");
					outToClient.writeBytes(bid_owner+"\n");			
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
