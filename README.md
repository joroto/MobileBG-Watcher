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
- "<" and ">" - to load other photos.
- "GO TO ADVERT" - button that will open a browser window and 
navigate you to the specific advert.
- "FAVOURITE" button - when clicked will add the advert to your favourites list, and it will always be displayed at the top of the main screen list
as well as be highlighted in gold.

Additionally, you can click on the image to enlarge it and use the scroll wheel of your mouse to zoom in/out.

# Setting up
Requires Java 16 or higher!
1. Download the latest packaged JAR file from the [releases page](https://github.com/joroto/MobileBG-Watcher/releases).
2. Create a folder and place the JAR file in it.
3. Go to https://www.mobile.bg/ and select your filters (!**Sort by newest**!).<br>
   <image width= '450' src='https://i.imgur.com/HQGtDbc.png'></image>
4. Click search.
5. Copy the link you are redirected to.
7. Create a file named <code>car_requests.properties</code> in the folder where you placed the JAR file, open it with Notepad or similar text editor and set its content to: a variable (you may put model and brand for easier management, but it does not matter what exactly it is) that equals (=) the link you were on, like this:<br>
   <image src='https://i.imgur.com/bF2Bkbr.png'></image>
   **<br>(You can skip this and launch the app, but the first time it will not start correctly, as it has no data for cars, but it will auto-generate the file which you can edit later)**
8. Double-click the JAR file to start the tool.

#### To have multiple active filters you add lines to the <code>car_requests.properties</code> file exactly the same way you did the first car model<br>

The tool will create a folder named "LOGS" for logging which should not concern you, it's for debugging.