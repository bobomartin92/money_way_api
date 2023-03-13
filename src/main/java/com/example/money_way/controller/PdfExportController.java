package com.example.money_way.controller;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.TransactionReceipt;
import com.example.money_way.service.PdfGeneratorService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/transactions/")
public class PdfExportController {

    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping("generate-receipt/{transactionId}")
    public ApiResponse<Object> export(@PathVariable Long transactionId, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy:hh:mm:ss");
        String currentDateTime = dateFormat.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment: Transaction Receipt " + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        return pdfGeneratorService.export(transactionId,response);
    }
}
