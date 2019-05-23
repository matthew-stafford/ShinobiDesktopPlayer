import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MPVManager implements Runnable {

	public static ArrayList<Process> processes = new ArrayList<Process>();
	public String cmd;
	private Process process = null;
	
	public String getValueFromResult(String json, String key) {
		JsonElement jelement = new JsonParser().parse(json);
		JsonObject json_data = jelement.getAsJsonObject();
		if (json_data.get(key) != null && !json_data.get(key).isJsonNull()) {
			return json_data.get(key).getAsString();
		}
		return "";
	}
	
	public String sendCommand(String cmd) {
		Process p = null;
		String result = "";
		try {
			p = new ProcessBuilder(new String[] {"bash","-c", cmd})
	                .redirectErrorStream(true)
	                .start();
	        BufferedReader br = new BufferedReader(
	                new InputStreamReader(p.getInputStream()));
	            String line = null;
	            while ( (line = br.readLine()) != null ) {
	            	result = result+line;
	            	System.out.println(line);
	            }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (p != null && p.isAlive()) {
				p.destroy();
			}
		}
		return result;
	}
	
	
	public MPVManager(String cmd) {
		// record instance of MPV so it can be killed when necessary
		this.cmd = cmd;
	}
	
	public void Start() {
		Thread t1 = new Thread(this);
		t1.start();
	}
	
	@Override
	public void run() {
        System.out.println(cmd);
        try {
             process = 
                new ProcessBuilder(new String[] {"bash","-c", cmd})
                    .redirectErrorStream(true)
                    .start();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
                String line = null;
                while ( (line = br.readLine()) != null ) {
                	
                }
                if (process.isAlive()) {
                	process.destroy();
                }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {        	
        	if (process != null && process.isAlive()) {
        		process.destroy();
        	}
        }
	}
	
	public void kill() {
		if (process != null && process.isAlive()) {
			process.destroy();
		}
	}
	
	public void KillAll() {
		for (Process p : processes) {
			if (p.isAlive()) {
				p.destroy();
			}
		}
	}
	
	
}
