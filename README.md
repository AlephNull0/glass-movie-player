Glass Movie Player
==========================
Browse and watch movies on Google Glass.

Note: With the XE16/17 updates, fetching video thumbnails from the content provider is noticeably slow. If anyone knows how to fix this, please let me know!

Usage
-----
1.  Install the apk using adb:  
```
cd WhereverYouPutTheApk
adb install GlassMoviePlayer.apk  
```
2.  Use the OK Glass menu to start:
```
OK Glass... watch a movie
```


Acquiring Videos
----------------
Glass Movie Player will detect all video files found in google glass's storage, including videos that you have recorded.  

You may want to upload videos from your computer to google glass. My preferred method is using adb of course:  

```
adb push my_video.mp4 /sdcard/Movies/  
```

You can upload videos anywhere in the /sdcard directory, but I like using the default Movies subdirectory.

Note: If you uploaded videos to Glass with adb and GlassMoviePlayer can't find them, restart Glass.


Watching Videos
---------------
Browsing and picking a video to watch is self-explanatory. Once you start the application, scroll through the folders and videos by swiping on the trackpad as if you were navigating through your timeline items. Press the trackpad to select a video.  

![Every video is displayed as a card](assets/picker1.png?raw=true)
![Browse videos by swiping--just like navigating your timeline](assets/picker2.png?raw=true)

Once you are watching a video, press the trackpad to pause or resume. You can seek (that is, rewind or fast-forward) by swiping on the trackpad. 

![Press the trackpad to pause or resume video](assets/player_pause.png?raw=true)
![Swipe with two fingers to seek (that is, reverse or fast forward)](assets/player_seek.png?raw=true)


Why a Movie Player?
----------------------------------------
Glass is designed for micro-interactions so why would anyone want a movie player? Because it works really well in certain situations. I have watched Android design in action videos while on a treadmill and it works great. Videos that don't require constant visual attention are great to watch on glass while on the go. I would not recommend watching any crazy movies such as Memento where you just can't keep your eyes off the screen.

Note that the volume may be a bit too low with bone conduction. I recommend using a headset if you run into this issue.
