import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;


public class MongoExport {

	public static void main(String[] args) {
		Timer timer = new Timer();

		TimerTask t = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("starting thread.........................");
				MongoClient mongoClient;
				try {
					//mongo connection
					mongoClient = new MongoClient( "localhost" , 27017 );
					DB db = mongoClient.getDB( "db" );
					DBCollection coll = db.getCollection("host1");
					

					//sql connection
					Connection con = DriverManager.getConnection("jdbc:mysql://cmpe272.cn1huhw7fmgm.us-west-1.rds.amazonaws.com:3306/CMPE283", "root", "rootcmpe");

					BasicDBObject whereQuery = new BasicDBObject();
					whereQuery.put("vmname", "Team13_VM2");
					whereQuery.put("processed", "false");
					DBCursor cursor = coll.find(whereQuery);
					List<String> listOfUpdatedIds = new ArrayList<String>();
					while(cursor.hasNext()) {
						DBObject currentRow = cursor.next();
						insertIntoSQL(currentRow, con);
						System.out.println("inserted into DB, updating mongo");
						String idString = currentRow.get("_id").toString();;
						listOfUpdatedIds.add(idString);

						System.out.println(currentRow);
					}
					
					System.out.println("updating ids.." + listOfUpdatedIds.size());
					
					
					DBCollection coll2 = db.getCollection("host1");

					for (String idString : listOfUpdatedIds) {
						//update processed flag in mongodb
						BasicDBObject newDocument = new BasicDBObject();
						newDocument.append("$set", new BasicDBObject().append("processed", "true"));				
						DBObject searchById = new BasicDBObject("_id", new ObjectId(idString));
						coll2.update(searchById, newDocument);				
						System.out.println("updated mongo");
						
					}
					
					System.out.println("closing sql and mongo conneciton");
					con.close();
					mongoClient.close();
				

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}; 
		
		
		timer.schedule(t, 0, 1000*30);
			}

	public static void insertIntoSQL(DBObject currentRow, Connection con) throws SQLException
	{
		String sql = "INSERT INTO host1 (version, time_stamp, host_name, path, date_val, time_val, vmname, cpu_usagemhz, cpu_usage, datastore_totalReadLatency, datastore_totalWriteLatency, disk_maxTotalLatency, mem_granted, mem_vmmemctl, mem_consumed, mem_active, net_transmitted, net_received, net_usage, power_power, sys_uptime)" +
				"VALUES (?, ?, ?,?, ?, ?,?, ?, ?,?, ?, ?,?, ?, ?,?, ?, ?,?, ?, ?)";
		PreparedStatement preparedStatement = con.prepareStatement(sql);


		preparedStatement.setString(1, currentRow.get("@version").toString());
		preparedStatement.setDate(2, null);
		preparedStatement.setString(3, currentRow.get("host").toString());
		preparedStatement.setString(4, currentRow.get("path").toString());
		preparedStatement.setString(5, currentRow.get("date").toString());
		preparedStatement.setString(6, currentRow.get("time").toString());
		preparedStatement.setString(7, currentRow.get("vmname").toString());
		preparedStatement.setString(8, currentRow.get("cpu_usagemhz").toString());
		preparedStatement.setString(9, currentRow.get("cpu_usage").toString());
		preparedStatement.setString(10, currentRow.get("datastore_totalReadLatency").toString());
		preparedStatement.setString(11, currentRow.get("datastore_totalWriteLatency").toString());
		preparedStatement.setString(12, currentRow.get("disk_maxTotalLatency").toString());
		preparedStatement.setString(13, currentRow.get("mem_granted").toString());
		preparedStatement.setString(14, currentRow.get("mem_vmmemctl").toString());
		preparedStatement.setString(15, currentRow.get("mem_consumed").toString());
		preparedStatement.setString(16, currentRow.get("mem_active").toString());
		preparedStatement.setString(17, currentRow.get("net_transmitted").toString());
		preparedStatement.setString(18, currentRow.get("net_received").toString());
		preparedStatement.setString(19, currentRow.get("net_usage").toString());
		preparedStatement.setString(20, currentRow.get("power_power").toString());
		preparedStatement.setString(21, currentRow.get("sys_uptime").toString());
		preparedStatement.executeUpdate(); 

	}

}
