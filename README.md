# Meeting-Bot
This Bot can be integrated in order to find an adequate location for meeting up. Given two locations (the locations of the interaction partners) we find you a venue for a meeting near the halfway point between you. 

How-To-Use (JSON):
1. start chat with the bot
2. write a message in the following form: `/json "lat1,lng1;lat2,lng2;..;latN,lngN/<category>"`
3. the bot writes a message back with a stringified json object containing the data of the found venue

category: pick a category from https://developer.foursquare.com/docs/resources/categories (english)
(e.g. for a soccer field pick "Socker Field")
the json message has the following form:
```
{
    "name": <name of the venue>,
    "address": <street and house number>,
    "city": <city>,
    "country": <country>,
    "postalCode": <postal code>,
    "formattedAddress": <the most important address data formatted in a readable way>
    "mapsLink": <the url of the place in google maps>
    "longitude": <longitude>
    "latitude": <latitude>
    
}
```
## Members
* [Alexander Melem](https://github.com/melemalex)
* [Fabian Windbacher](https://github.com/fabianwindbacher)
* [Max Podpera](https://github.com/MaxPodpera)