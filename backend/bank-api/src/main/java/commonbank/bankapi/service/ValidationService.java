package commonbank.bankapi.service;

import commonbank.bankapi.client.BlockchainApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {
    private BlockchainApiClient apiClient;

    @Autowired
    public ValidationService(BlockchainApiClient apiClient){this.apiClient = apiClient;};

    public void verifyBurnRequest(){
        
    }
}
