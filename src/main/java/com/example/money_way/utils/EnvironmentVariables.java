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

    @Value("${app.buy_data}")
    private String dataPurchaseUrl;

    @Value("${app.purchase_data}")
    private String purchaseDataUrl;

    @Value("${app.vt-pass-api-key}")
    private String vtPassApiKey;

    @Value("${app.vt-pass-secret-key}")
    private String vtPassSecretKey;

    @Value("${app.verify_transaction_endpoint}")
    private String verifyTransactionEndpoint;

    @Value("${app.WEBHOOK_VERIFY_HASH}")
    private String WEBHOOK_VERIFY_HASH;
}
