package won.bot.meetingbot.model.openstreetmap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class OSMRequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OSMRequestBuilder.class);
    private String url;
    private String parameters = "?";

    public OSMRequestBuilder(String url) {
        this.url = url;
    }

    public <T> T executeForObject(Class<T> responseType, Object... uriVariables) throws RestClientException {
        logger.info("Requesting '{}'", this.url + this.parameters);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(this.url + this.parameters, responseType, uriVariables);
    }

}
