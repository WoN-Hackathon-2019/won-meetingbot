# Meeting-Bot
This Bot can be integrated in order to find an adequate location for meeting up. Given two locations (the locations of the interaction partners) we find you a venue for a meeting near the halfway point between you. 

We also have a [PartnerBot-Demo](https://github.com/WoN-Hackathon-2019/won-meetingbot-partner) you can check out if you want to use our bot!
## How-To-Use
### General

You can use this bot either as a chatbot or integrate it into your service and have a JSON api.
If you want to use categories for either situation you'll need to check what categories are supported:

pick categories from https://developer.foursquare.com/docs/resources/categories (english)
(e.g. for a soccer field pick "Socker Field")
or use the command /Category <search-term>  in the chat to find the available categories:
 
 e.g. ```/Category Soccer```
 
will return ```College Soccer Field, Soccer Stadium, Soccer Field```


### Chat
1. start chat with the bot
2. write a message in the following form ```loc1;loc2;..;locN/category1;category2;..;categoryN```
3. get the name and address of a place fitting your criteria

loc can have one of two forms:
* latitude, longitude (e.g. ```48.210033,16.363449```)
* natural language address (e.g ```Taubstummengasse 1, 1040 Wien ```)
 
### JSON
1. start chat with the bot
2. write a message in the following form: e.g. ```/json "{\"locations\": [[48.202934, 16.354662], [48.200842, 16.382923]], \"categories\": [\"Socker Field\", \"Socker Stadium\"]}" ``` (first a list of locations ( a location is \[latitude, longitude]), then a list of categories (\[category1, category2..]))
3. the bot writes a message back with a stringified json object containing the data of the found venue

the returned json message has the following form:
```
{
    "name": (String) <name of the venue>,
    "address": (String) <street and house number>,
    "city": (String) <city>,
    "country": (String) <country>,
    "postalCode": (String) <postal code>,
    "formattedAddress": (String) <the most important address data formatted in a readable way>
    "mapsLink": (String) <the url of the place in google maps>
    "longitude": (Double) <longitude>
    "latitude": (Double) <latitude>
    
}
```

## Running the Bot yourself

You can use the Bot just like the [Bot-Skeleton](https://github.com/researchstudio-sat/bot-skeleton).
## Collaboration
We cooperated with the CoopTeam: 
 * We used and even made minor improvements to their [EasyCLI](https://github.com/WoN-Hackathon-2019/easycli)
 * They are using our MeetingBot to find a good meeting spot in their [CoopBot](https://github.com/WoN-Hackathon-2019/won-coopbot)
## Members
* [Alexander Melem](https://github.com/melemalex)
* [Fabian Windbacher](https://github.com/fabianwindbacher)
* [Max Podpera](https://github.com/MaxPodpera)