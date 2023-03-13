package com.example.money_way.service;

import com.example.money_way.dto.response.ApiResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface PdfGeneratorService {

    ApiResponse<Object> export(Long transactionId, HttpServletResponse response) throws IOException;


}
