# DynAlarm 

DynAlarm :alarm_clock: is my final year project for Computer Applications in Dublin City University.

## Idea 

DynAlarm is a dynamic alarm clock application made for Android. It analyses traffic delays to help wake up users in situations when they are going to be delayed. In addition to this, it also uses the built-in accelerometer sensor in the phone to read body movements during sleep. Using a specified wake up time-frame, the application can get frequent updates on the traffic data and the body movements near the end of the sleep. If the application sees that the user will be late to their destination, it will update the alarm to wake up the user immediately. If the user is also moving a lot, the application will take this to mean they are waking up, and it updates the alarm so they don’t fall back to sleep. You do not need to use the traffic data if it is not applicable to you. In this case, users are just woken up based on movement. Users can also specify “routines” for the morning, these routines are factored into calculating the traffic delays. 

### Frameworks & APIs Used

- TomTom Online Routing API for Traffic Data
- MPAndroidChart for Analysis Section
- Realm Database
- Stetho for Realm Database Debugging 

### Screenshots 



--- | ---
![Alarm Screen](images/alarm_frag.png?raw=true "Alarm Screen")|![Analysis Screen](images/analysis.png?raw=true "Analysis Screen")
![Settings Screen](images/settings.png?raw=true "Settings Screen")|![Time Picker](images/time_picker.png?raw=true "Time Picker")
![Summaries](images/summaries.png?raw=true "Sleep Summaries")|![Alarm Wakeup](images/alarm.png?raw=true "Alarm Wakeup Dialog")






