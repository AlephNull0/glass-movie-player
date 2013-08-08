Glass Movie Player
==========================
Browse movies in google glass's storage and watch them using a simple interface. 

Usage
-----
1) Install the apk using adb:  
```
adb install GlassMoviePlayer.apk  
```

2) Start Glass Movie Player turning on the screen and then using adb:
```
adb shell am start -n com.ocd.dev.glassmovieplayer/.MoviePickerActivity  
```

If the screen is not on when you type this command, it will not work. Google will likely make it easier to start native apps in the future (perhaps when the GDK comes out), but for now there's no nice and simple method.


Alternatively, you can use Launchy to start the movie player.


Acquiring Videos
----------------
Glass Movie Player will detect any video in google glass's storage, including videos that you have recorded.  

You may want to upload videos from your computer to google glass. My preferred method is using adb of course:  

```
adb push my_video.mp4 /sdcard/Movies  
```

You can upload videos anywhere in the /sdcard directory, but I just like using the Movies subdirectory.


Watching Videos
---------------
Browsing and picking a video to watch is self-explanatory. Once you start the application, just swipe the trackpad as if you were navigating through your timeline items. Press the trackpad to select a video.  

Once you are watching a video, press the trackpad to pause or resume. You can seek (that is, rewind or fast-forward) by swiping on the trackpad using two fingers at the same time. 


Whisky Tango Foxtrot Why a Movie Player?
----------------------------------------
Glass is designed for micro-interactions so why would anyone want a movie player? Because it works really well with some movies. I have watched Android design in action videos while on a treadmill and it works great. Videos that don't require constant visual attention are great to watch on glass while on the go. I would not recommend watching any crazy movies such as Memento where you just can't keep your eyes off the screen. Feel free to experiment what type of videos work in what situations. At the moment, volume is a bit of an issue, but as a temporary fix, I just wear an ear-plug in my right ear to let the bone conduction do its thing.
