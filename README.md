# White Label Event App

This project is an Android app that will be used as a showcase for Android
programming techniques and Google APIs.  It is also the basis of the companion
app for AnDevCon.

This app alone is "white label" in the sense that it is not a useful or
shippable app itself.  Rather, it is meant to be configured and skinned for
use with an actual event backed by event data hosted by EventMobi and
accessible via their web APIs.

# Building and Running

This app should build with Gradle and import cleanly into Android Studio
without modification.

The meat of this app is driven by the EventMobi third party API, but you
don't need an account to be able to run the app.  The one build flavor
configured for this app is called "stubbed" and it's configured to use
canned data from Java resources to simulate actual event data from the API.

To configure the app to use an event of your own creation, do the following:

1. Fork the project
2. Define a new productFlavor in app/build.gradle (e.g., 'myevent')
3. In android.productFlavors.myevent, set your own Android app config
4. In app/src/myevent (files that belong to your flavor), override the
   following resources with appropriate values:
  - app_name
  - event_name
  - event_hashtag (leave empty if no Twitter hashtag)
  - eventmobi_api_key
  - eventmobi_event_name (the unique id for your event)

There are many other resources to override as well, such as styles, colors, and
Dagger module names.

# Configuring Eventmobi

When you create an app flavor in your fork, create a new file called
eventmobi_flavor.properties in the app directory where "flavor" is the name of
the flavor.  In this file, define one property called eventmobiApiKey with the
value of the API key for your event.

When you do a build, this value will be injected into the app, which will then
be used by the app to make API requests to Eventmobi.

The properties file is flagged by .gitignore to be commited from commits.  If
you would like to check in the file anyway, make the necessary change to
.gitignore.

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

# Example Showcase

The following is a list of things to be seen here:

- Application init
- Dependency Injection with Dagger 2
- Logging with Java APIs
- Strategies for white label app architecture
- Event bus usage
- Web service fetch and JSON parsing
- Proper Android date and time formatting
- Strategy for simulating different wall clock times in-app
- Data "cooking"
- RecyclerView
- Some material design

And many more things to come!

# License

Apache 2.0 - see the LICENSE file in this project.

# Contributing

See CONTRIBUTING.md in this project.

# Author

Doug Stevenson - [@CodingDoug](https://twitter.com/CodingDoug)

# Disclaimer

This is not an official Google product.
