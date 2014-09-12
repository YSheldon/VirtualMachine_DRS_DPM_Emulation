package Performance;

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Scanner;

import com.vmware.vim25.HostConnectFault;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.InvalidLogin;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class DRSManager {

	static ResourceBundle rb = ResourceBundle.getBundle("configuration.VMMachine");
	public static ArrayList<String> alAvaiHost =  new ArrayList<String>();
	static int MAX_LOAD = 75;
	static int MIN_LOAD = 30;
	public DRSManager(){
		alAvaiHost.add(rb.getString("sjsu.cmpe283.project2.host1"));
	}
	public static void main(String args[]) throws RemoteException{

		try {
			Scanner scr = new Scanner(System.in);
			System.out.println("Welcome to Team 13 Custom DRS DPM application\n");
			System.out.println("Kindly Enter the maximum load allowed on Virtual Host:[75]\n");
			
			MAX_LOAD = scr.nextInt();
			if(MAX_LOAD == 0){
				MAX_LOAD = 75;
			}
			System.out.println("Kindly Enter the maximum load allowed on Virtual Host:[30]\n");
			MIN_LOAD = scr.nextInt();
			if(MIN_LOAD == 0){
				MIN_LOAD = 30;
			}
			
			checkCPUUsageHost(true);
			checkCPUUsageHost(false);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	//Connect host and data center
	public static String connectHostToDataCenter(){
		String hostName = "";
		Folder hostFold = null;
		HostConnectSpec hcs = null;
		DRSManager drsMain = new DRSManager();
		VMwareManagerMain vmmain = new VMwareManagerMain();
		vmmain.getConnection();
		vmmain.getAdminConnection();
		ServiceInstance si = vmmain.si;
		Folder rootFolder = si.getRootFolder();	
		ManagedEntity[] me = null;
		try {
			me = new InventoryNavigator(rootFolder).searchManagedEntities("Folder");
		} catch (InvalidProperty e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i = 0;me !=null && i<me.length;i++){
			System.out.println(me[i].getName());
			String folName = me[i].getName();
			hostName = alAvaiHost.get(0);
			if("host".equalsIgnoreCase(folName)){
				hostFold = (Folder) me[i];
				hcs = new HostConnectSpec();
				hcs.setHostName(hostName);//"130.65.133.12"
				hcs.setUserName(rb.getString("sjsu.cmpe283.project2.hostUser"));
				hcs.setPassword(rb.getString("sjsu.cmpe283.project2.password"));
				hcs.sslThumbprint = rb.getString("sjsu.cmpe283.project2."+hostName);// feed prop for 13 ssh
			}
		}
		boolean bConnect = false;
		try {
			Task task = hostFold.addStandaloneHost_Task(hcs, null, true);						
			TaskInfo ti = task.getTaskInfo();
			Object obj = ti.getResult();
			bConnect = true;
		} catch (InvalidLogin e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HostConnectFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hostName;
	}


	//migrate a vm to another host
	public static void migrateVirtualMachine(String hostName, String vmName){

		try {
			String name = hostName;
			String ip = "130.65."+name.substring(name.length()-6);

			VMwareManagerMain vmmain = new VMwareManagerMain();
			vmmain.getConnection();
			vmmain.getAdminConnection();
			ServiceInstance si = vmmain.si;
			Folder rootFolder = si.getRootFolder();
			System.out.println("getting vm and host");
			HostSystem newHost = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", ip);
			VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);
			System.out.println(vm.getName());
			System.out.println(newHost.getName());
			ComputeResource cr = (ComputeResource) newHost.getParent();
			//newHost.getLicenseManager();
			String[] checks = new String[] {"cpu", "software"};
			HostVMotionCompatibility[] vmcs = si.queryVMotionCompatibility(vm, new HostSystem[] {newHost},checks );

			String[] comps = vmcs[0].getCompatibility();
			if(checks.length != comps.length)
			{
				System.out.println("CPU/software NOT compatible. Exit.");
				si.getServerConnection().logout();
				return;
			}

			Task task = vm.migrateVM_Task(cr.getResourcePool(), newHost,VirtualMachineMovePriority.highPriority, VirtualMachinePowerState.poweredOn);
			if(task.waitForMe()==Task.SUCCESS)
			{
				System.out.println("VMotioned!");
			}
			else
			{
				System.out.println("VMotion failed!");
				TaskInfo info = task.getTaskInfo();
				System.out.println(info.getError().getFault());
			}
			si.getServerConnection().logout();
		} catch (InvalidProperty e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static boolean removeHost(String strHostName){
		boolean bSuccess = false;
		try {
			VMwareManagerMain vmmain = new VMwareManagerMain();
			vmmain.getConnection();
			ServiceInstance si = vmmain.si;
			Folder rootFolder = si.getRootFolder();
			HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", strHostName);
			host.disconnectHost();
		} catch (RuntimeFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bSuccess;
	}


	public static String checkCPUUsageVM(String hostName) throws SQLException, InvalidProperty, RuntimeFault, RemoteException
	{
		String name = hostName;
		String ip = "130.65."+name.substring(name.length()-6);

		Connection con = DriverManager.getConnection("jdbc:mysql://cmpe272.cn1huhw7fmgm.us-west-1.rds.amazonaws.com:3306/CMPE283", "root", "rootcmpe");
		VMwareManagerMain vmmain = new VMwareManagerMain();
		vmmain.getConnection();
		vmmain.getAdminConnection();
		ServiceInstance si = vmmain.si;
		Folder rootFolder = si.getRootFolder();
		System.out.println("getting host.....");
		HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", ip);
		VirtualMachine[] vms = host.getVms();

		int minLoadVm = 0;
		int maxLoadVm = 0;
		String minVmName = "";
		String maxVmName = "";
		String query2 = "";
		Statement stmt = con.createStatement();
		ResultSet rs = null;


		for(int i=0;i<vms.length;i++)
		{
			System.out.println(vms[i]);
			int cpuUsage = 0;
			int count = 0;
			query2 = "select id, cpu_usage, vmname from CMPE283.VM_Usage where vmname like '"+ vms[i].getName() +"' order by id desc limit 5";
			rs = stmt.executeQuery(query2);
			while(rs.next())
			{
				count++;
				String cpu_usage = rs.getString("cpu_usage");
				cpuUsage = cpuUsage + Integer.parseInt(cpu_usage);
			}
			int avgCpu_usage = cpuUsage/count;

			if(minLoadVm == 0){
				maxLoadVm= minLoadVm = avgCpu_usage;
				minVmName = maxVmName = vms[i].getName();
			}

			//getting host which has min cpu usage 
			if(minLoadVm > avgCpu_usage)
			{
				minLoadVm = avgCpu_usage;
				minVmName = vms[i].getName();
			}

			//getting vm which has max cpu usage 
			if(maxLoadVm < avgCpu_usage )
			{
				maxLoadVm = avgCpu_usage;
				maxVmName = vms[i].getName();
			}
			System.out.println(avgCpu_usage);
		}

		return minVmName;

	}

	public static void checkCPUUsageHost(boolean bDRS) throws SQLException, InvalidProperty, RuntimeFault, RemoteException
	{
		Connection con = DriverManager.getConnection("jdbc:mysql://cmpe272.cn1huhw7fmgm.us-west-1.rds.amazonaws.com:3306/CMPE283", "root", "rootcmpe");
		String quer1 = "select distinct vmname from CMPE283.VHOST_Usage";
		String query2 = "";

		HashMap<Integer, String> hmPerf = new HashMap<Integer, String>();
		ArrayList<Integer> arrPerf = new ArrayList<Integer>();;
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(quer1);
		ArrayList<String> alVmnames = new ArrayList<String>();
		boolean bSuccess = false;
		while(rs.next())
		{
			String vmname = rs.getString("vmname");
			alVmnames.add(vmname);
		}
		int minLoadHost = 0;
		int maxLoadHost = 0;
		int prevMinHost = 0;
		String prevHostName = "";
		String minHostName = "";
		String maxHostName = "";

		for(int i=0;i<alVmnames.size();i++)
		{
			System.out.println(alVmnames.get(i));
			int cpuUsage = 0;
			int count = 0;
			query2 = "select id, cpu_usage, vmname from CMPE283.VHOST_Usage where vmname like '"+ alVmnames.get(i)+"' order by id desc limit 5";
			rs = stmt.executeQuery(query2);
			while(rs.next())
			{
				count++;
				String cpu_usage = rs.getString("cpu_usage");
				cpuUsage = cpuUsage + Integer.parseInt(cpu_usage);
			}
			int avgCpu_usage = cpuUsage/count;
			hmPerf.put(avgCpu_usage, alVmnames.get(i));
			arrPerf.add(avgCpu_usage);

			if(minLoadHost == 0){
				maxLoadHost = minLoadHost = avgCpu_usage;
				minHostName = maxHostName = alVmnames.get(i);
			}
			//getting host which has min cpu usage 
			if(minLoadHost > avgCpu_usage)
			{
				//prevMinHost = minLoadHost;
				//prevHostName = minHostName;
				minLoadHost = avgCpu_usage;
				minHostName = alVmnames.get(i);
			}

			//getting host which has max cpu usage 
			if(maxLoadHost < avgCpu_usage )
			{
				//prevMinHost = maxLoadHost;
				//prevHostName = maxHostName;
				maxLoadHost = avgCpu_usage;
				maxHostName = alVmnames.get(i);
			}
			System.out.println(avgCpu_usage);
		}

		if(bDRS){
			if(maxLoadHost/100 > MAX_LOAD)//Logic to check if the max of host cpus are 75% loaded
			{
				//check vms usage
				if(maxHostName.equalsIgnoreCase(minHostName) || (minLoadHost/100 > 75)){
					//add a host
					minHostName = connectHostToDataCenter();
					if(alAvaiHost.contains(alAvaiHost))
						alAvaiHost.remove(minHostName);
				}
				String minLoadVm  = checkCPUUsageVM(maxHostName);//to find minimum loaded vm
				if(minHostName != null)
					DRSManager.migrateVirtualMachine(minHostName, minLoadVm);
			}
		}
		else{
			if(minLoadHost/100 < MIN_LOAD)//Logic to check if the min load is less than 30 %
			{
				//minHostName
				if(!maxHostName.equalsIgnoreCase(minHostName)){
					//sort arrr and take the value from map
					Collections.sort(arrPerf);
					if(arrPerf.size() > 1){
						int cpu_usage_min = (Integer)arrPerf.get(0);
						String sourceHostIp = hmPerf.get(cpu_usage_min);
						int cpu_usage = (Integer)arrPerf.get(1);//check if greater than 75
						String targetHostName = hmPerf.get(cpu_usage);
						String name = targetHostName;
						String ip = "130.65."+name.substring(name.length()-6);
						sourceHostIp = "130.65."+sourceHostIp.substring(name.length()-6);
						
						VMwareManagerMain vmmain = new VMwareManagerMain();
						vmmain.getConnection();
						ServiceInstance si = vmmain.si;
						Folder rootFolder = si.getRootFolder();
						HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", sourceHostIp);
						VirtualMachine[] vms = host.getVms();
						for(int i = 0;i<vms.length;i++)
							DRSManager.migrateVirtualMachine(ip, vms[i].getName());
						//DRSManager.removeHost(ip);
					}
				}
			}
		}
	}
}
