##inSync

>inSync is an application that attempts to keep mp3 playback synced across numerous users and devices. There is currently a web version and an Android version.

>>Compatible with Google Chrome and Safari

>>Not compatible with Firefox

Hosted on: http://ml7.stuyc.org:8888 <br>
Android APK: https://github.com/stuycs-softdev/inSync/tree/master/inSync

##How to Use (Web Version):
1. Login with Facebook

###Enter Room
If you want to listen to a stream, enter the room name
  * Do not interact with the audio-player or you will not be in sync with the rest of the users in the stream
  
###Enter Room as Admin
If you want to control a stream, enter a room as admin with "Enter Room as Admin"
  * Do not press the play/pause button built into the audio-player as that event is not broadcasted to the stream.
      * Instead, use the Play/Pause below the audio-player
  * The following buttons broadcast respective events to everyone connected to your stream
      * Play/Pause button toggles play/pause 
      * Skip Forward button skips the stream forward by 10 seconds 
      * Reset button brings the position in the track back to 0:00
      * Using the slider to change the time will change the time for all users
  * Upload
      * Upload an mp3 file to the server to be played in the stream (see next section)
  * Select Music
      * Select an mp3 file to be played in your room's stream 
        * Default mp3 being played in all rooms before music is selected is **Brandenburg Concertos No. 2**
      
###Create a room
1. Enter the name of the room you want to create
2. Enter an admin password unless you want anyone to be able to control the stream
 
  
##Project Contributors
Brian Lam
+ General Leader
+ Overall Android Overwatch

Steven Huang
+ Making sure Brian doesn't do something stupid

Raymond Zeng
+ Web Overseer
+ Assistant to Making sure Brian doesn't do something stupid

Jason Peng
+ Graphic Design Overseer

##Known Issues
1. Select from URL
  + You must first have selected something from the server before being able to select with a URL. 
  Otherwise you get kicked out of the room but everyone else in the room recieves the music. 
  Once you have selected from the server, you can select as many URLs until you refresh the page. 
  + Temp Solution:
     + 'notasong.mp3' has been added to the server for your convienence of selecting something arbitrary from the server.
  
  + Selecting from URL is not very compatible with mobile
  
2. Audio Player Loading Issues
  + The html5 audio player tends to 'blacked-out' and become unclickable when multiple users are accessing the same mp3 src.
    The first person to log into the room usually has a functioning player while every subsequent login is blacked-out.
  + Temp Solution:
     + If the people without blacked-out players press play and then pause again, everyone else's player becomes un-blacked
     + Alternatively, the host can just toggle play/pause twice to hopefully get everyones player un-blacked
     + Once everyone in the room has a functioning player (the slider should be one color: gray), the host can reset and afterwards everything will work
