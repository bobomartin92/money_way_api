package com.example.money_way.model;

import com.example.money_way.enums.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.PrePersist;
import javax.persistence.TemporalType;
import javax.persistence.Temporal;
import javax.persistence.PreUpdate;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "transaction_tbl")
public class Transaction {

    @Id
    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private String currency;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private String virtualAccountRef;
    private String description;
    private Status status;
    private String responseMessage;
    private String providerStatus;
    private String paymentType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdAt")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_updated")
    private Date updatedAt;


    @PrePersist
    public void createdAt(){

        this.createdAt = new Date();
    }

    @PreUpdate
    public void updatedAt(){

        this.updatedAt = new Date();
    }


}
