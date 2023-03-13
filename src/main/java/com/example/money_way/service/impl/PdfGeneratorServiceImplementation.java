package com.example.money_way.service.impl;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.TransactionReceipt;
import com.example.money_way.exception.ResourceNotFoundException;
import com.example.money_way.model.Transaction;
import com.example.money_way.model.User;
import com.example.money_way.repository.TransactionRepository;
import com.example.money_way.service.PdfGeneratorService;
import com.example.money_way.utils.AppUtil;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PdfGeneratorServiceImplementation implements PdfGeneratorService {

     private final AppUtil appUtil;
     private final TransactionRepository transactionRepository;

     @Override
    public ApiResponse<Object> export(Long transactionId, HttpServletResponse response) throws IOException {

         User user = appUtil.getLoggedInUser();
         Long userId = user.getId();

         Optional<Transaction> transaction1 = transactionRepository.findByUserIdAndTransactionId(userId, transactionId);
         if(transaction1.isEmpty()){
              throw new ResourceNotFoundException("Transaction not found");
         }

         Document document = new Document(PageSize.A4);
         PdfWriter.getInstance(document, response.getOutputStream());

         document.open();
         Paragraph paragraph = new Paragraph("Transaction Receipt");
         paragraph.setAlignment(Paragraph.ALIGN_CENTER);
         Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
         headerFont.setColor(Color.BLUE);
         headerFont.setSize(20);
         Chunk chunk = new Chunk(String.valueOf(paragraph),headerFont);

          TransactionReceipt receipt = new TransactionReceipt();
         receipt.setDescription(transaction1.get().getDescription());
         receipt.setStatus(transaction1.get().getStatus());
         receipt.setUserId(transaction1.get().getUserId());
         receipt.setResponseMessage(transaction1.get().getResponseMessage());
         receipt.setTransactionId(transaction1.get().getTransactionId());
         receipt.setPaymentType(transaction1.get().getPaymentType());
         receipt.setVirtualAccountRef(transaction1.get().getVirtualAccountRef());
         receipt.setProviderStatus(transaction1.get().getProviderStatus());
         receipt.setAmount(transaction1.get().getAmount());
         receipt.setCreatedAt(transaction1.get().getCreatedAt());

          Paragraph paragraph1 = new Paragraph(String.valueOf(receipt));
          paragraph1.setAlignment(Paragraph.ALIGN_LEFT);
          Font fontParagraph = FontFactory.getFont(FontFactory.HELVETICA);
          fontParagraph.setSize(14);
          fontParagraph.setColor(Color.BLACK);

         //document.add(chunk);
         document.add(paragraph);
         document.add(paragraph1);
         document.close();

         return ApiResponse.builder()
                 .data(receipt)
                 .build();
    }

}
