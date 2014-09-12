import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;


public class HostPeformanceCollector {

	private static HashMap<Integer, PerfCounterInfo> headerInfo = new HashMap<Integer, PerfCounterInfo>();
	private int maxSamples;
	private String username;
	private String password;
	private URL url;
	private List<VirtualMachine> hostList;

	public static final List<String> METRIC_LIST = new ArrayList<String>(
			Arrays.asList("cpu","datastore","disk","mem","net","power","sys"));
	public static String URL = "https://130.65.133.10/sdk";
	public static String ADMINURL = "https://130.65.132.13/sdk";
	public static String ADMIN_USER_NAME = "administrator";
	public static String ADMIN_PASSWORD = "12!@qwQW";
	public static List<String> PARAMETER_LIST = new ArrayList<String>(
			Arrays.asList("cpu_usage","cpu_usagemhz",
					"datastore_totalWriteLatency", "datastore_totalReadLatency",
					"disk_write", "disk_read", "disk_maxTotalLatency",  "disk_usage",
					"mem_granted", "mem_consumed","mem_active","mem_vmmemctl",
					"net_usage","net_received","net_transmitted",
					"power_power",
			"sys_uptime"));
	public List<VirtualMachine> getHostList() {
		return hostList;
	}

	public void setHostList(List<VirtualMachine> hostList) {
		this.hostList = hostList;
	}

	/**
	 * @param args
	 */
	public static void startCollection(HostPeformanceCollector perColl) {

		try {
			while (true) {
				for (VirtualMachine vm : perColl.getHostList()) {
					Date date = new Date(System.currentTimeMillis());
					SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
					StringBuffer str = new StringBuffer();
					str.append("timestamp:" + format.format(date));
					str.append(",vmname:" + vm.getName());
					HashMap<String, HashMap<String, String>> metricsMap = null;
					metricsMap = perColl.getPerformanceMetrics(vm);

					for (String metricNam : METRIC_LIST) {
						HashMap<String, String> metricProps = metricsMap.get(metricNam);
						for (String p : metricProps.keySet()) {
							if (PARAMETER_LIST.contains(p)) {
								str.append("," + p + ":" + metricProps.get(p));
							}
						}
					}

					String fName = vm.getName()+".log";
					try {
						//File file = new File("D:\\Madan\\SJSU\\Second_semester_spring2014\\CMPE_283\\Project2\\Logs\\logging"+vm.getName()+".log");
						//if (!file.exists()) {
						//file.createNewFile();
						//}

						File fin = new File("/home/team13/TempLogs/Hosts");//ubuntu
						//File fin = new File("D:\\Madan\\SJSU\\Second_semester_spring2014\\CMPE_283\\Project2\\TempLogs");
						File fout = new File("/home/team13/Logs/Hosts");//ubuntu
						//File fout = new File("D:\\Madan\\SJSU\\Second_semester_spring2014\\CMPE_283\\Project2\\Logs");

						if(!fin.exists()){
							fin.mkdirs();
						}
						if(!fout.exists()){
							fout.mkdirs();
						}
						fin = new File(fin.getPath()+"/"+fName);
						fout = new File(fout.getPath()+"/"+fName);

						FileWriter fw = new FileWriter(fin,true);
						BufferedWriter writer = new BufferedWriter(fw);
						String strHeader = str.toString();
						if(strHeader!= null){
							writer.write(str.toString());
							writer.write("\n");
							writer.flush();
							double bytes = fin.length();
							double kilobytes = (bytes / 1024);
							double megabytes = (kilobytes / 1024);
							if(kilobytes > 1){
								copyFiles(fin.getParentFile(), fout.getParentFile());
								writer.close();
								//System.out.println(fin.getAbsolutePath());
								fin.delete();
								fin = new File(fin.getPath());
								fw = new FileWriter(fin);
								writer = new BufferedWriter(fw);
								System.out.println("##############File: "+fName+" Sent to Logstash##############");
							}
						}
						//FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
						//BufferedWriter bw = new BufferedWriter(fw);
						//bw.append(str.toString());
						//bw.append("\n");
						//bw.flush();
						//bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				Thread.currentThread().sleep(5000);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void copyFiles(File sourceLocation , File targetLocation)
	throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}
			File[] files = sourceLocation.listFiles();
			for(File file:files){
				InputStream in = new FileInputStream(file);
				OutputStream out = new FileOutputStream(targetLocation+"/"+file.getName());

				// Copy the bits from input stream to output stream
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}            
		}
	}

	public HostPeformanceCollector(URL url, String username, String password,
			int maxSamples) throws RemoteException, MalformedURLException {
		this.url = url;
		this.username = username;
		this.password = password;
		this.maxSamples = maxSamples;
		this.hostList = new ArrayList<VirtualMachine>();
		ServiceInstance si = new ServiceInstance(url, username, password, true);
		ServiceInstance siClient = new ServiceInstance(new URL(ConstantUtil.URL), 
				ConstantUtil.ADMIN_USER_NAME, ConstantUtil.ADMIN_PASSWORD, true);
		Folder rootFolder = siClient.getRootFolder();

		ManagedEntity managedEntityRP = new 
		InventoryNavigator(si.getRootFolder()).searchManagedEntity("ResourcePool", 
		"Part2_Team13_vHOSTS");
		ManagedEntity[] hostsEntities = new 
		InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		ResourcePool rp = (ResourcePool) managedEntityRP;
		//System.out.println(rp.getName());
		VirtualMachine[] vms = rp.getVMs();
		ArrayList<String> alHosts = new ArrayList<String>();
		for(int h= 0 ;h< hostsEntities.length;h++){
			HostSystem hs = (HostSystem) hostsEntities[h];
			alHosts.add(hs.getName());
		}
		if(vms != null && vms.length > 0){
			for(int i=0;i<vms.length;i++){
				VirtualMachine virtualMachine = (VirtualMachine) vms[i]; 
				String name = virtualMachine.getName();
				String ip = "130.65."+name.substring(name.length()-6);
				if(alHosts.contains(ip)){
					hostList.add((VirtualMachine) virtualMachine);

				}
			}
		}
		// Make a list of all the VMs
		PerformanceManager performanceManager = si.getPerformanceManager();
		PerfCounterInfo[] infos = performanceManager.getPerfCounter();
		for (PerfCounterInfo info : infos) {
			headerInfo.put(new Integer(info.getKey()), info);
		}

	}

	protected HashMap<String, HashMap<String, String>> getPerformanceMetrics(
			VirtualMachine virtualMachine) throws Exception {

		PerfEntityMetricBase[] pValues = null;
		ServiceInstance serviceInstance = new ServiceInstance(url, username, password, true);
		PerformanceManager performanceManager = serviceInstance.getPerformanceManager();
		PerfQuerySpec perfQuerySpec = new PerfQuerySpec();
		perfQuerySpec.setEntity(virtualMachine.getMOR());
		perfQuerySpec.setMaxSample(new Integer(maxSamples));
		perfQuerySpec.setFormat("normal");

		PerfProviderSummary pps = performanceManager.queryPerfProviderSummary(virtualMachine);
		perfQuerySpec.setIntervalId(new Integer(pps.getRefreshRate().intValue()));
		pValues = performanceManager.queryPerf(new PerfQuerySpec[] { perfQuerySpec });
		if (pValues != null) {
			return generatePerformanceResult(pValues);
		} else {
			throw new Exception("No values found!");
		}

	}

	private HashMap<String, HashMap<String, String>> generatePerformanceResult(
			PerfEntityMetricBase[] pValues) {
		HashMap<String, HashMap<String, String>> propertyGroups = new HashMap<String, HashMap<String, String>>();
		for (PerfEntityMetricBase p : pValues) {
			PerfEntityMetric pem = (PerfEntityMetric) p;
			PerfMetricSeries[] pms = pem.getValue();
			for (PerfMetricSeries pm : pms) {
				int counterId = pm.getId().getCounterId();
				PerfCounterInfo info = headerInfo.get(new Integer(counterId));

				String value = "";

				if (pm instanceof PerfMetricIntSeries) {
					PerfMetricIntSeries series = (PerfMetricIntSeries) pm;
					long[] values = series.getValue();
					long result = 0;
					for (long v : values) {
						result += v;
					}
					result = (long) (result / values.length);
					value = String.valueOf(result);
				} else if (pm instanceof PerfMetricSeriesCSV) {
					PerfMetricSeriesCSV seriesCsv = (PerfMetricSeriesCSV) pm;
					value = seriesCsv.getValue() + " in "
					+ info.getUnitInfo().getLabel();
				}

				HashMap<String, String> properties;
				if (propertyGroups.containsKey(info.getGroupInfo().getKey())) {
					properties = propertyGroups.get(info.getGroupInfo()
							.getKey());
				} else {
					properties = new HashMap<String, String>();
					propertyGroups
					.put(info.getGroupInfo().getKey(), properties);
				}

				String propName = String.format("%s_%s", info.getGroupInfo()
						.getKey(), info.getNameInfo().getKey());
				properties.put(propName, value);
			}
		}
		return propertyGroups;

	}

}
