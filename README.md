# White Label Event App

This project is an Android app that will be used as a showcase for Firebase
features.  It is also the basis of the companion app for [AnDevCon][1]
([fork][2]).

This app alone is "white label" in the sense that it is not a useful or
shippable app itself.  Rather, it is meant to be configured and skinned for
use with an actual event backed by data populated in Firebase Realtime
Database.  There is code available in a module here that populates a database
from data coming from an event created in EventMobi.


# Project Structure

This project contains multiple modules:

**app**

The Android app

**backend-util**

Some Java utilities that read and write the Firebase Realtime Database.  This
provides support for the App Engine backend, but the code here is much easier
to run and test in development outside of App Engine.

**backend**

An App Engine project that populates the database and downloads collected
feedback.

**core**

Some common bits of Java infrastructure

**eventmobi**

A collection of JavaBeans that are used for deserializing the results of API
calls to EventMobi.

**model**

Common JavaBeans that are used for database serialization and other general
purposes.


# Building and Running

This app should build with Gradle and import cleanly into Android Studio
without modification.  However, it won't run properly without the Firebase
Realtime Database populated with some data in the expected structure.  There
is a backend component that will read data out of an EventMobi event (requires
an API key to make calls to their service) and write it into the database.
Some time in the future, I am planning to provide a file with sample data to
import directly into the database to bootstrap a new installation without
having to copy data from EventMobi.


# Forking and Branding

The app was intended to be "branded" for real use by forking this repo and
using a build flavor to specify what should be different from the default
"stubbed" flavor.

To configure the app to use an event of your own creation, do the following:

1. Fork the project
2. Define a new productFlavor in app/build.gradle (e.g., 'myevent')
3. In android.productFlavors.myevent, set your own Android app config
4. In app/src/myevent (files that belong to your flavor), override the
   following resources with appropriate values:
  - app_name
  - event_name
  - event_hashtag (leave empty if no Twitter hashtag)

There are many other resources to override as well, such as styles, colors, and
Dagger module names.


# Configuring EventMobi

Documentation is woefully incomplete here!  It requires a fair amount of
configuration to build and run the backend components that can run a
process to copy data from EventMobi into Firebase.  In particular, you
will need to add a reference to some external resources used for
configuration.   You do this by setting the EVENT_APP_RESOURCES_DIR Gradle
property to a location that contains the resources to add to the build, and
that will get picked up by the backend-util build script for inclusion into
the built jar.

More details about this coming later.


# Integrating Twitter

The app supports a minimalistic integration with Twitter.  You will need to
know the hashtag defined for the event and have a Fabric account with a
Twitter API key and secret.

To get it integrated:

1. Obtain a fabric.properties file from Fabric and place it in the app
   directory.
2. Create another file under app and call it twitter_flavor.properties
   where "flavor" is the name of the flavor in your app fork.
3. Create two properties this in this file called twitterApiKey and
   twitterApiSecret with the appropriate values from the Fabric dashboard.
4. In your flavor's file space (e.g. app/src/yourflavor), override the
   string resource named event_hashtag with the hashtag including the leading
   pound sign.

Now, when you do a build, Fabric and Twitter properties will be injected
into the app, which allows Fabric to be initialized at startup.  When the app
is launched, a new option will be available in the sliding menu for tweets
which with invoke a fragment that displays the latest tweets on that hashtag.

Please note that fabric.properties and the twitter properties are flagged by
.gitignore to be omitted from commits.  If your app is open source, it's
probably a good idea not to advertise your API keys to the world.


# License

Apache 2.0 - see the LICENSE file in this project.

# Contributing

See CONTRIBUTING.md in this project.

# Author

Doug Stevenson - [@CodingDoug](https://twitter.com/CodingDoug)

# Disclaimer

This is not an official Google product.


[1]: http://www.andevcon.com/
[2]: https://github.com/BZMedia/andevcon-event-app
