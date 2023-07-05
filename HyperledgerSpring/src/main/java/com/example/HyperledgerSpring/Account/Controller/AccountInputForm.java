package com.example.HyperledgerSpring.Account.Controller;


import com.example.HyperledgerSpring.Account.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountInputForm {

    @NotNull
    public String owner;
    @NotNull
    public Long amount;
    @NotNull
    public AccountType type;
}
