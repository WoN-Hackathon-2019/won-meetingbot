package won.bot.meetingbot.foursquare;


import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class FSRequestBuilder {

    private String url;
    private String parameters = "?";

    public FSRequestBuilder(String url) {
        this.url = url;
        this.withParameter("client_id", "SBWQBQOWDTTPUSODUWMVH21REXVOWTN1FJJR3AZSPZ5PREHM");
        this.withParameter("client_secret", "IOUQ3ZIUETLMLEU0MLEJ5BMAASMEPLCHHTTEPDFKBPNB2OIY");
        this.withParameter("v", "20191209");
    }

    public FSRequestBuilder withParameter(String parameter, String value) {
        if(this.parameters.length() > 1) {
            this.parameters += "&";
        }
        this.parameters += parameter + "=" + value;
        return this;
    }

    public <T> T executeForObject(Class<T> responseType, Object... uriVariables) throws RestClientException {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(this.url + this.parameters, responseType, uriVariables);
    }

}
