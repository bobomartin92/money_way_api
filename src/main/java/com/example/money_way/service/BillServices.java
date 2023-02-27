package com.example.money_way.service;

import com.example.money_way.dto.request.AirtimeRequestDto;
import com.example.money_way.dto.request.AirtimeRequestFE;
import com.example.money_way.dto.response.VTPassResponseDto;

public interface BillServices {
    VTPassResponseDto buyAirtime(AirtimeRequestFE airtimeRequestFE);
}
