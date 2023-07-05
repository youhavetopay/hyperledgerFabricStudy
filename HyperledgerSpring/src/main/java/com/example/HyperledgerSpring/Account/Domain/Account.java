package com.example.HyperledgerSpring.Account.Domain;

import com.example.HyperledgerSpring.Account.AccountType;
import com.example.HyperledgerSpring.Account.Controller.AccountInputForm;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class Account {

    private String accountId;
    private String owner;
    private Long amount;
    private AccountType type;

    public Account (){
        this.accountId = String.valueOf(System.currentTimeMillis());
        this.owner = "";
        this.amount = 0L;
        this.type = AccountType.USER;

    }

    public Account (AccountInputForm accountInput){
        this.accountId = String.valueOf(System.currentTimeMillis());
        this.owner = accountInput.getOwner();
        this.amount = accountInput.getAmount();
        this.type = accountInput.getType();
    }
}
