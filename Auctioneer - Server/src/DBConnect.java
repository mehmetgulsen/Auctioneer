//Mehmet Gülþen
//2013400075
//mehmetgulsen95@hotmail.com
//CMPE436-Term

import java.sql.*;

/*
 * This class is used for connecting to the database
 */
public class DBConnect {
	private Connection con;
	private Statement st;
	private ResultSet rs;
	
	public DBConnect(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/auctioneer", "java", "123456");
			System.out.println("database connected!");
			st = con.createStatement();
			
		} catch (Exception e) {
			System.out.println("Error: "+e);
		}
	}
	
	public ResultSet getData(String query){
		try {
			rs = st.executeQuery(query);
			return rs;
			
			
		} catch (Exception e) {
			System.out.println("Error: "+e);
		}
		return rs;
	}
	
	public void runQuery(String query){
		try {

			st.executeUpdate(query);
			
		} catch (Exception e) {
			System.out.println("Error: "+e);
		}
	}
}
