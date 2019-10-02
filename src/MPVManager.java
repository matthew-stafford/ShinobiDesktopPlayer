import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.mappedbus.MappedBusReader;

public class MPVManager implements Runnable {

	public static ArrayList<Process> processes = new ArrayList<Process>();
	public ArrayList<String> playlist = new ArrayList<String>();
	public String cmd;
	private Process process = null;
	private boolean userStopped = false;
	private Thread processMonitorThread = null;
	
	public String getValueFromResult(String json, String key) {
		try {
			System.out.println("Attempting to parse "+json);
			JsonElement jelement = new JsonParser().parse(json);
			if (jelement != null && !jelement.isJsonNull()) {
				JsonObject json_data = jelement.getAsJsonObject();
				if (json_data.get(key) != null && !json_data.get(key).isJsonNull()) {
					return json_data.get(key).getAsString();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public ArrayList<String> getPlaylist(String json) {
		ArrayList<String> playlist = new ArrayList<String>();
		try {
			JsonElement jelement = new JsonParser().parse(json);
			if (jelement != null && !jelement.isJsonNull()) {
				JsonObject  jobject = jelement.getAsJsonObject();
				JsonArray json_array = jobject.getAsJsonArray("data");
				for (int i = 0; i < json_array.size();i++) {
					JsonObject files = json_array.get(i).getAsJsonObject();
					playlist.add(files.get("filename").getAsString());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return playlist;
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
	        processes.add(p);
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
        while (!userStopped) {
	        try {
	             process = 
	                new ProcessBuilder(new String[] {"bash","-c", cmd})
	                    .redirectErrorStream(true)
	                    .start();
	            BufferedReader br = new BufferedReader(
	                    new InputStreamReader(process.getInputStream()));
	                String line = null;
	                processes.add(process);
	                while ( (line = br.readLine()) != null ) {
	                	
	                }
	                if (process.isAlive()) {
	                	process.destroyForcibly();
	                }
	        } catch (Exception e) {
	        	e.printStackTrace();
	        } finally {        	
	        	if (process != null && process.isAlive()) {
	        		process.destroyForcibly();
	        	}
	        }
	        if (!userStopped) {
	        	System.err.println("MPV process stopped. Restarting!");
	        } else {
	        	System.out.println("MPV process closing.");
	        }
        }
	}
	
	public void kill() {
		userStopped = true;
		if (process != null && process.isAlive()) {
			process.destroyForcibly();
		}
	}
	
	public void KillAll() {
		for (Process p : processes) {
			if (p.isAlive()) {
				p.destroyForcibly();
			}
		}
	}

	public boolean isStopped() {
		return userStopped;
	}
	
	
}