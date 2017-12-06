//Mehmet Gülþen
//2013400075
//mehmetgulsen95@hotmail.com
//CMPE436-Term

import java.net.ServerSocket;
import java.net.Socket;

public class Main {

	public static void main(String argv[]) throws Exception {
		Lock lock = new Lock();
				
		
		ServerSocket welcomeSocket = new ServerSocket(9090);

		//Generate a ThreadManager for each client
		//all clients use the same lock
		while (true) {
			System.out.println("Server is waiting for client.");
			Socket connectionSocket = welcomeSocket.accept();
			System.out.println("Client arrival");

			new ClientManager(connectionSocket,lock).start();
			
		}
		
		
		
	}

}
