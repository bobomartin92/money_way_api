package com.example.money_way.model;

import com.example.money_way.enums.Type;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "beneficiary_tbl")
public class Beneficiary extends Base{
    private String name;
    private String accountNumber;
    private String virtualAcctRef;
    private String phoneNumber;
    private String email;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String bankName;
    @Column(nullable = false)
    private Long userId;
}
