package com.example.money_way.service.impl;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataResponse;
import com.example.money_way.service.BillService;
import com.example.money_way.utils.EnvironmentVariables;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class BillServiceImpl implements BillService {
    private final EnvironmentVariables environmentVariables;
    private final RestTemplate restTemplate;

    @Override
    public ApiResponse<DataResponse> fetchDataVariations(String dataServiceProvider) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("api-key", environmentVariables.getApiKey());
        headers.add("public-key", environmentVariables.getPublicKey());
        headers.setContentType((MediaType.APPLICATION_JSON));

        HttpEntity<ApiResponse> entity = new HttpEntity<ApiResponse>(headers);

        DataResponse response = restTemplate.exchange(environmentVariables.getFetchDataVariations()+"serviceID="+dataServiceProvider+"-data",
                HttpMethod.GET, entity, DataResponse.class).getBody();
         return  new ApiResponse<>("Success", null, response);
    }

}
