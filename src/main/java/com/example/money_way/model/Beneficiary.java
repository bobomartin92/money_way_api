package com.example.money_way.model;

import com.example.money_way.enums.Type;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.Column;


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
    private String phoneNumber;
    private String email;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String bankName;
    @Column(nullable = false)
    private Long userId;
}
