# ParkingSpotLocator
Video Demonstration (click):
[![Parking Spot Locator Video Demo](https://i.imgur.com/7fVMAxx.png)](https://drive.google.com/file/d/1YiNe6yxsFiGkldWOU6orBqKrjwWxbeCF/view?usp=sharing)

• Developed a mobile application that reduces the amount of time it takes to find a parking spot at predefined parking locations. The application sends a real-time notification to user’s before arriving at their destination. The notification displays and recommends available parking lots based on the user’s personal preference, distance from lot, and the number of spots open at each lot.

• **Python** and **OpenCV**’s image processing libraries are needed to process the live video feeds, set up at each location. This was crucial in determining whether parking spaces are occupied or vacant.

• **YAML** is being utilized to store generated coordinates, in order to define existing parking spaces.

• Google’s real-time database, **Firebase** handles the storage of available spots at each parking lot.

• **Java** and **XML** were used to construct the mobile application within **Android Studios**. **Geofencing** services are used to set up virtual geofences around predefined surrounding parking lots. This helps track when a user is within a certain safe distance of parking, allowing for a time-appropriate notification to be sent back to the user with relative information.

• Incorporates **Google Maps API** to allow users to view their current location, relative to where the virtual geofences are placed. The map also helps visualize when a geofence transition event occurs.
