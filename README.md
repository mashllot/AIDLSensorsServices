Android_AIDL_Sensors_Plus
====================

@Author: Zak Chapman,	      HTTP://CODE.NEWTECHJP.COM

Copyright (C) Zak Chapman.


Table of contents
-----------------

I   ............ Description

II  ............ File overview

III ............ License

IV  ............ Screen Capture

I Description
-------------

This project is handling many features

① Sensors : Accelerometer, Gyroscope, Magnetic and Light.

    1.1 All these sensors are running in a Services with a LogFile of each sensor by a String title gotten from the Delay chosen from MainActivity...
    
② Services : Each Sensor run on different service with recording Data into .csv file titled by now time and chosen Delay

③ AIDL : Each Sensor send the data of x, y, z and Delay to Main Activity using AIDL...

④ Setting : at Main Activity 

    4.1 You can choose within to keep the Services running on the background or to stop them at a specific time (upTime) after the Start button pressed.
    4.2 you can choose to show or not the Notification when the service is running.
    4.3 Then you can choose the upTime when the services should stop using Alarm Manager at a specific time.
    
 Notification : Using RemoteView from XML file with bigView.



II File overview
---------------

MainActivity: 
    - MainActivity
    
Services: 
    - AccelService
    - GyroService
    - MagneService
    - LightService
    
AIDLs: 
    - IGETAccelData
    - IGETGyroData
    - IGETMagneData
    - IGETLightData
    
Setting: 
    - FragmentPreferences
    - PreferencesActivity
    - UserPreferenceFragment



       Android_AIDL_Sensors_Plus
------------------------------------------------------------
			 
       +---------------+
		   |  MainActivity |
		   +---------------+    \   +---------------+  \   +---------------+
		          |                 |  Preferences  |      |    Setting    |
		          |                 +---------------+	     +---------------+
		          |
		     +---------------+
		     |Sensor_Services|
		     +---------------+		 
		      /          \ 		  
		  +---------+       +--------+	  |  Display       |
		  |Log Files|  ...  | AIDLs  | -- |	 Alarm Manager |
		  +---------+       +--------+    |	 Broadcast     |
		                                  |	 Notification  |
		     
------------------------------------------------------------


III License
---------

The license for all code in this project is belong to Newtechjp Ltd,
Please refer to us for further details.


IV Screen Capture
----------------

http://code.newtechjp.com/Coding/uploads/2013/12/Screenshot_2013-12-30-00-24-49.png