package com.example.HyperledgerSpring.Account.Controller;

import com.example.HyperledgerSpring.Account.Domain.Account;
import com.example.HyperledgerSpring.Account.Service.AccountService;
import com.example.HyperledgerSpring.Account.Service.AccountServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("account")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountServiceImpl accountService){
        this.accountService = accountService;
    }


    @GetMapping("list")
    public ResponseEntity<List<Account>> getAllList(){
        List<Account> accounts = this.accountService.getAllAccount();
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("create")
    public ResponseEntity<Map> createAccount(final @Valid @RequestBody AccountInputForm accountInput){
        Account account = new Account(accountInput);
        this.accountService.createAccount(account);

        Map<String, String> result = new HashMap<>();
        result.put("message", "생성완료");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("transfer")
    public ResponseEntity<Map> transferAccount(final @Valid @RequestBody TransferInputForm transferInputForm){

        String senderId = transferInputForm.getSender().toString();
        String receiverId = transferInputForm.getReceiver().toString();
        String sendAmount = transferInputForm.getSendAmount().toString();

        String transferResult = this.accountService.tranferAccount(senderId, receiverId, sendAmount);
        Map<String, String> result = new HashMap<>();
        result.put("message", transferResult);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}
