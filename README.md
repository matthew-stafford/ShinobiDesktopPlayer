# Shinobi Desktop Player
Shinobi CCTV player for linux desktops. Allows playback of live streams and recorded video files using Shinobi Systems CCTV API.

<h2>System Requirements</h2>
must have the following installed:

socat (used for IPC pipe between application and mpv), xwininfo (used to get windowID of java canvas) & mpv (for playback of the video files)

sudo apt install socat 
sudo apt install xwininfo
sudo apt install mpv

<h2>Instructions</h2>
Download latest release from <a href="https://github.com/matthew-stafford/ShinobiDesktopPlayer/releases" target="_blank">here</a> and extract .jar file. Open a terminal and ensure it is executable by typing 'chmod +x /path/to/ShinobiDesktopPlayer.jar'

Double click .jar file to open or type 'java -jar ShinobiDesktopPlayer.jar' in terminal to launch.

Upon application launch you will be prompted for your host, apikey and groupkey.

Host: this is the location of your shinobi instance i.e. http://127.0.0.1:8080/
Api Key: Select your email address in the top left hand corner of your shinobi instance. Select API from the drop down menu. Set the IP address to 0.0.0.0 and then press the '+ Add' button in the bottom right corner. The API key will now be created and you can copy into this software.
Group Key: Select your email address in the top left hand corner of your shinobi instance. Select Settings from the drop down menu. Group Key will be an option under your email address.

Now the application will open.
