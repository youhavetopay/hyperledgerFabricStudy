package com.example.HyperledgerSpring.Account.Repository;

import com.example.HyperledgerSpring.Account.Controller.TransferInputForm;
import com.example.HyperledgerSpring.Account.Domain.Account;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hyperledger.fabric.client.*;

import java.util.List;

public interface AccountRepository {

    void createAccount(Account account) throws EndorseException, CommitException, SubmitException, CommitStatusException;

    List<Account> getAll() throws GatewayException, JsonProcessingException;

    String transfer(String senderId, String receiverId, String sendAmount);

}
