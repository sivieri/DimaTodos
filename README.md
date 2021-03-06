DimaTodos
=========

Android app tutorial for DIMA 2013 @ PoliMi

# Lesson 1
* IDE overview
* DimaTodos v1 overview
* DimaTodos main activity
* DimaTodos SQLite helper
* DimaTodos content provider (introduction)

This version requires the Android Support Library v4, which is automatically added by Eclipse ADT when a new project is created.

# Lesson 2
* DimaTodos content provider (continued)
* DimaTodos note editor
* GPS and Location API (Google Play Services)
* External intents through URIs

Please, notice that to use this version (and the next ones) you need the Google Play Services library, which is obtained as explained in the [official tutorial](http://developer.android.com/google/play-services/setup.html); once you have the project in your Eclipse workspace, you need to add a reference to it (the current version of the project may have the reference from my workspace), as explained in [this other tutorial](http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject).

# Lesson 3
* External intents: camera (send and receive data)
* DimaTodos note editor (reprise)
* External content providers: calendar
* Use functionality for specific API versions
* Asynchronous tasks
* Localization

# Lesson 4
* Add the ActionBar (useful for the data sharing widgets)
* Share note content with other applications
* Receive shared input from other applications as note content
* Moved application to fragments
* Added new layout for tablets
* Added sidebar for online dictionary search

This version requires the Android Support Library v7-appcompat, which has to be downloaded using the Android SDK Manager and imported in Eclipse ADT. Version 7 automatically includes version 4 (which can be removed from the dependency list).

# Lesson 5
* Open permissions for the notes content provider, to implement the widget
* Added an [example widget](https://github.com/sivieri/DimaWidget)

# Lesson 6
* Google AppEngine backend (insertion only)
* Added an [example backend](https://github.com/sivieri/DimaCloud)
