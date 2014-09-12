package Performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstantUtil {

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
}
