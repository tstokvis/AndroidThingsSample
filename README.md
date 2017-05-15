# AndroidThingsSample
Sample app for used for Raspberry Pi talk for Android guild

Project comprised of two simple applications, the Android Things application (to be loaded onto a Raspberry Pi) and a companion application targeted for a nougat device. Used as a sample for a talk to show how Android Things works at an Android Guild at Hootsuite.

### Raspberry Pi Application

  - Writes to Pin BCM20 for LED light
  - Reads Pin BCM21 for button
  - When button pressed (either on breadboard or on Android things UI), updates firebase database with new value of Pin BCM12
  - Keeps track of firebase value, updates BCM12 pin if there is a change
  
### Raspberry Companion App

  - Reads value of pin from firebase
  - Indicates wether the light is on or off on application
  - Includes a button which writes a value to firebase if pressed
