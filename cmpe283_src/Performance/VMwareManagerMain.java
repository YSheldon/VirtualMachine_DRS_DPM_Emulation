package Performance;
/**
 * 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.oracle.webservices.internal.literal.Map;
import com.vmware.vim25.VirtualMachineCapability;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author Madan
 *
 */
public class VMwareManagerMain {

	/**
	 * @param args
	 */

	static ManagedEntity[] managedEntitiesVMs;
	static ManagedEntity[] managedEntitiesHosts;
	static ManagedEntity managedEntityRP;
	static ServiceInstance si;
	static ServiceInstance siAdmin;
	static HashMap hmFailedVMs = new HashMap<>();
	static ArrayList alPoweredOffVMs = new ArrayList<>();
	VMwareManagerMain(){
		getConnection();
	}
	public static void main(String[] args) {
		ResourceBundle rb = ResourceBundle.getBundle("com.sjsu.project1.VMMachine");
		//Timer for Ping thread
	}
	public void getConnection(){
		try{
			URL url = new URL("https://130.65.133.10/sdk");
			si = new ServiceInstance(url, "administrator", "12!@qwQW", true);
			Folder rootFolder = si.getRootFolder();	
			this.managedEntitiesVMs = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
			this.managedEntitiesHosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.out.println("Exception in getConnection::::"+ex.getMessage());
		}
	}
	public void getAdminConnection(){
		try{
			URL url = new URL("https://130.65.132.13/sdk");
			siAdmin = new ServiceInstance(url, "administrator", "12!@qwQW", true);
			Folder rootFolder = siAdmin.getRootFolder();	
			this.managedEntityRP = new InventoryNavigator(rootFolder).searchManagedEntity("ResourcePool", "Part2_Team13_vHOSTS");
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.out.println("Exception in getConnection::::"+ex.getMessage());
		}
	}
	public static ManagedEntity[] getManagedEntitiesVMs() {
		return managedEntitiesVMs;
	}
	public static void setManagedEntitiesVMs(ManagedEntity[] managedEntitiesVMs) {
		VMwareManagerMain.managedEntitiesVMs = managedEntitiesVMs;
	}
	public static ManagedEntity[] getManagedEntitiesHosts() {
		return managedEntitiesHosts;
	}
	public static void setManagedEntitiesHosts(ManagedEntity[] managedEntitiesHosts) {
		VMwareManagerMain.managedEntitiesHosts = managedEntitiesHosts;
	}
	public static ManagedEntity getManagedEntitiesRP() {
		return managedEntityRP;
	}
	public static void setManagedEntitiesRP(ManagedEntity managedEntitiesRP) {
		VMwareManagerMain.managedEntityRP = managedEntitiesRP;
	}
}
