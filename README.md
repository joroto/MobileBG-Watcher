# MobileBG Watcher
MobileBG Watcher is a tool used to track new car listings on mobile.bg. It shows all the newest listings based on filters you set.
The listings are always sorted by newest created, edits of old listings are not pushed to top. Tool supports multiple filters active at once.

<image width= '250' src='https://i.imgur.com/lQMpgGA.png'></image>

# How to use
Requires Java 16 or higher!
1. Download the latest packaged JAR file from the releases page
2. Create a folder and place the JAR file in it
3. Go to https://www.mobile.bg/pcgi/mobile.cgi and select your filters (!**Sort by newest**!)<br>
   <image width= '450' src='https://i.imgur.com/HQGtDbc.png'></image>
4. Before you click search open your browser's developer tools, network tab
5. Click search
6. Find the network request to <code>mobile.cgi</code> and go to Payload -> View source, copy everything from there<br>
<image src='https://i.imgur.com/1oLF4o0.png'></image>
7. Create a file named <code>car_requests.properties</code> in the folder where you placed the JAR file, open it with Notepad or similar text editor and set its content to: a variable (you may put model and brand for easier management) that equals (=) what you copied from the request, like this:<br>
   <image src='https://i.imgur.com/aQMByxq.png'></image>
8. Double-click the JAR file to start the tool

#### Double clicking a picture or text will open the corresponding listing in your web browser

#### To have multiple active filters you add lines to the <code>car_requests.properties</code> file like this:<br>
<image src='https://i.imgur.com/ar1iqCL.png'></image>

#### Default refresh interval is 10 minutes but you can change it through the same <code>car_requests.properties</code> file by adding a line containing <code>refresh_interval=</code>, uses minutes
The tool will create a folder named "LOGS" for logging which should not concern you, it's for debugging.