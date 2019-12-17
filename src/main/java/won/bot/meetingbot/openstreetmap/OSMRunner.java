package won.bot.meetingbot.openstreetmap;


import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;

public class OSMRunner {

    public static void main(String[] args) throws IOException {


        RestTemplate restTemplate = new RestTemplate();

        String y = restTemplate.getForObject("https://nominatim.openstreetmap" + ".org/search?format=json" +
                "&addressdetails=0&q=Stelzhamerstraße 4, 4701 Bad Schallerbach", String.class);

        System.out.println(y);

        OSMLocation[] x = restTemplate.getForObject("https://nominatim.openstreetmap" + ".org/search?format=json" +
                "&addressdetails=0&q=Stelzhamerstraße 4, 4701 Bad Schallerbach", OSMLocation[].class);

        System.out.println(x);


        OSMLocation[] result = new OSMRequestBuilder("https://nominatim.openstreetmap.org/search?format=json" +
                "&addressdetails=0&q=Stelzhamerstraße 4, 4701 Bad Schallerbach").executeForObject(OSMLocation[].class);


        System.out.println(result.length);
        System.out.println(Arrays.toString(result));

    }
}
