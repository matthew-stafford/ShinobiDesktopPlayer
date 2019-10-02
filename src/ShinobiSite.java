import java.util.TreeMap;

public class ShinobiSite {

	public String name, host,apiKey, groupKey;
	public int port;
	public boolean https;
	private ShinobiAPI api = null;
	
	private TreeMap<String, ShinobiMonitor> monitors = new TreeMap<String, ShinobiMonitor>();
	
	public void loadMonitors() {
		System.out.println("Loading monitors for "+name);
		if (api == null) {
			api = new ShinobiAPI(this);
		}
		monitors = api.checkAPIValidAndGetMonitors();
	}
	
	// generates the base URL string such as http(s)://host:8080
	public String getBaseURL() {
		return (https?"https://":"http://")+host+":"+port;
	}

	public TreeMap<String, ShinobiMonitor> getMonitors() {
		return monitors;
	}
}
