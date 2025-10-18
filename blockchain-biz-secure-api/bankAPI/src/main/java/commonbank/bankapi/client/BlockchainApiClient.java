package commonbank.bankapi.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BlockchainApiClient {

    private final RestTemplate restTemplate;

    private final String blockchainApiBaseUrl = "http://logistics_api.furnituresmakers.com:8080/api/network/"; // Zmień na właściwy URL API banku

    public BlockchainApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> getConfirmationOfBurnRequest(String burnRequestId) {
        String url = blockchainApiBaseUrl + "/getBurnRequest" + burnRequestId;
        return restTemplate.postForEntity(url, burnRequestId, String.class);
    }
}
