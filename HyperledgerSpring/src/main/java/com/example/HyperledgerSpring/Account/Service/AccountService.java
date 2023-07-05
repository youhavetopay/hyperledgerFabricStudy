package com.example.HyperledgerSpring.Account.Service;


import com.example.HyperledgerSpring.Account.Controller.TransferInputForm;
import com.example.HyperledgerSpring.Account.Domain.Account;

import java.util.List;

public interface AccountService {

    void createAccount(Account account);

    List<Account> getAllAccount();

    String tranferAccount(String senderId, String receiverId, String sendAmount);
}
