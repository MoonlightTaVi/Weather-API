# Weather-API
Application Programming Interface to fetch the weather data from VisualCrossing

## Summary
This is a project made for Roadmap.sh, but at the moment I am not able to:
- Visit Roadmap.sh
- Visit VisualCrossing's web service

The project seems to be stable (just a few bugs left), but I can't fix them, because the connection simply times out.

If you are able to, with this project you can:
- (technically) Use the artifact to fetch the data you want in your own applications
- OR use this application as a standalone app

It even has my API key inside, it seems (I absolutely forgot about it after all this connection issues).

I will later rebase it out, but only if the Visual Crossing would be so kind as to let me keep working with their service further (even though this may not be their problem, in the end).
If that doesn't happen, feel free to do whatever you want; I may delete the Visual Crossing account as well at that moment.

## About
The Application lets you fetch the weather forecast for 2 days (inculding today; i.e. today and tommorow). To do this:
- Enter any location (white spaces are allowed, punctuation signs aren't; but I wasn't let to fix it)
- Press submit
- Wait and see the information on:
  - Average temperature
  - Minimum (night) temperature
  - Maximum (day) temperature
  - Textual conditions (rain, fog, etc.)
  - A little bit more detailed description (compared to "conditions")
  - Also you'll see:
    - Last update date
    - Dates of the forecast (so that you know that "today" in the forecast is some outdated "today")
    - Resolved address (if the service has not understood you and given you a wrong address, you'll know it)

Also, the application features:
- Caching to Redis (if connected)
  - Caching to a ".json" file, if couldn't connect
- Ability to specify the unit system (metric, US, UK,..)
  - As well as the application's and response's text language
    - You can make your own locale; just add your columns to the ".csv" file (if you need to put a comma, use the "{c}" escape sequence
  - All the setting are cached along with last responses; BUT they are deleted after 12 hours either! Again, I am not able to fix it because of connection issues

## Some screenshots

_TBA_ (again, I can't fetch anything!)
