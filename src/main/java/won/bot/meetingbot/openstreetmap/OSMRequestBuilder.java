package won.bot.meetingbot.openstreetmap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import won.bot.meetingbot.foursquare.FSRequestBuilder;

public class OSMRequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger(FSRequestBuilder.class);
    private String url;
    private String parameters = "?";

    public OSMRequestBuilder(String url) {
        this.url = url;
    }

    public OSMRequestBuilder withParameter(String parameter, String value) {
        if(this.parameters.length() > 1) {
            this.parameters += "&";
        }
        this.parameters += parameter + "=" + value;
        return this;
    }

    public OSMRequestBuilder withAddress(String address) {
        return this.withParameter("q", address);
    }

    public <T> T executeForObject(Class<T> responseType, Object... uriVariables) throws RestClientException {
        logger.info("Requesting '{}'", this.url + this.parameters);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(this.url + this.parameters, responseType, uriVariables);
    }

}
