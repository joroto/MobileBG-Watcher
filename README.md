# MobileBG Watcher
MobileBG Watcher is a tool used to track new car listings on mobile.bg. It shows all the newest listings based on filters you set.
The listings are always sorted by newest created, edits of old listings are not pushed to top. Tool supports multiple filters active at once.

# How to use
<image width= '250' src='https://i.imgur.com/r5zM1zx.png'></image>
<br>This is the app's main screen, which you will see after loading. Here you have all adverts gathered for the filters you have set up,
sorted by date of publishing (newest first, edited adverts are not bumped to top).
<br>
<br> Right-clicking an advert instantly opens it in your browser.<br>
<br>
Double-clicking an advert will open up the advert info screen, where you can find more info about the car you are interested in, including more photos.
<br><image width= '500' src='https://i.imgur.com/j8Cb0y2.png'></image><br>
Here you have buttons that function as follows:
- "<" and ">" - load other photos.
- "GO TO ADVERT" - opens a browser window and navigate you to the specific advert.
- "FAVOURITE" button - adds the advert to your favourites list, and it will always be displayed at the top of the main screen list
as well as be highlighted in gold.

Additionally, you can click on the image to enlarge it and use the scroll wheel of your mouse to zoom in/out.

# Setting up
Requires Java 16 or higher!
1. Download the latest packaged JAR file from the [releases page](https://github.com/joroto/MobileBG-Watcher/releases).
2. Go to https://www.mobile.bg/ and select your filters (!**Sort by newest**!).<br>
   <image width= '450' src='https://i.imgur.com/HQGtDbc.png'></image>
3. Click search.
4. Copy the link you are redirected to.
5. Create a file named <code>car_requests.properties</code> in the same directory where you have the JAR file, open it with Notepad or similar text editor and set its content to: a variable (you may put model and brand for easier management, but it does not matter what exactly it is) that equals (=) the link you were on, like this:<br>
   <image src='https://i.imgur.com/bF2Bkbr.png'></image>
   **<br>(You can skip this and launch the app, but the first time it will show you a warning and will auto-generate the file with some default cars)**
6. Double-click the JAR file to start the tool.

#### To have multiple active filters you add lines to the <code>car_requests.properties</code> file exactly the same way you did the first car model<br>
You can enable logging by setting a property <code>logging_enabled = true</code> in the same <code>car_requests.properties</code> file. In which case the tool will create a folder named "LOGS" and start creating logs every time you close it. Disabled by default.