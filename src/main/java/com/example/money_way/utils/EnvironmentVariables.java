package com.example.money_way.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class EnvironmentVariables {

    @Value("${app.FLW_SECRET_KEY}")
    private String FLW_SECRET_KEY;

    @Value("${app.create_wallet}")
    private String createWalletUrl;

    @Value("${app.verify_transaction_endpoint}")
    private String verifyTransactionEndpoint;

    @Value("${app.WEBHOOK_VERIFY_HASH}")
    private String WEBHOOK_VERIFY_HASH;

    @Value("${app.fetch_data_variations}")
    private String fetchDataVariations;

    @Value("${app.api_key}")
    private String apiKey;

    @Value("${app.public_key}")
    private String publicKey;
}
