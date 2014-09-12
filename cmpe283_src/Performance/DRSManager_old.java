package Performance;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.vmware.vim25.ComputeResourceConfigSpec;
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

public class DRSManager_old {

	static ResourceBundle rb = ResourceBundle.getBundle("configuration.VMMachine");
	public static void main(String args[]) throws RemoteException{
		
		DRSManager_old drsMain = new DRSManager_old();
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
			if("host".equalsIgnoreCase(folName)){
				Folder hostFold = (Folder) me[i];
				HostConnectSpec hcs = new HostConnectSpec();
				hcs.setHostName("130.65.133.12");//"130.65.133.12"
				hcs.setUserName(rb.getString("sjsu.cmpe283.project2.hostUser"));
				hcs.setPassword(rb.getString("sjsu.cmpe283.project2.password"));
				hcs.sslThumbprint = rb.getString("sjsu.cmpe283.project2."+"130.65.133.12");
				connectHostToDataCenter(hostFold, hcs);
			}
		}
	}

	//Connect host and data center
	public static boolean connectHostToDataCenter(Folder hostFold, HostConnectSpec hcs){
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
		return bConnect;
	}


	//migrate a vm to another host
	public static void MigrateVirtualMachine(String newHostIP, String vmName){

		try {

			VMwareManagerMain vmmain = new VMwareManagerMain();
			vmmain.getConnection();
			vmmain.getAdminConnection();
			ServiceInstance si = vmmain.si;
			Folder rootFolder = si.getRootFolder();
			System.out.println("getting vm and host");
			HostSystem newHost = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", newHostIP);
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
	
	public boolean removeHost(HostSystem hs){
		boolean bSuccess = false;
		try {
			hs.disconnectHost();
		} catch (RuntimeFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bSuccess;
	}
	
	public void checkCPUUsage(String row){
		
	}

}
