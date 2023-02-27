package com.example.money_way.service.impl;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataVariationsResponse;
import com.example.money_way.service.BillService;
import com.example.money_way.utils.EnvironmentVariables;
import com.example.money_way.utils.RestTemplateUtil;
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

    private final RestTemplateUtil restTemplateUtil;

    @Override
    public ApiResponse<DataVariationsResponse> fetchDataVariations(String dataServiceProvider) {
        DataVariationsResponse response = restTemplateUtil.fetchDataVariations(dataServiceProvider);
         return  new ApiResponse<>("Success", null, response);
    }

}
