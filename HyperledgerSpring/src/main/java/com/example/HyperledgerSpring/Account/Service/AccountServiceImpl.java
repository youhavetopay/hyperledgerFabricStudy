package com.example.HyperledgerSpring.Account.Service;

import com.example.HyperledgerSpring.Account.AccountType;
import com.example.HyperledgerSpring.Account.Domain.Account;
import com.example.HyperledgerSpring.Account.Repository.AccountRepository;
import com.example.HyperledgerSpring.Account.Repository.FabricGateWay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService{

    private static AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository fabricAccountRepository) {
        accountRepository = fabricAccountRepository;
    }

    @Override
    public void createAccount(Account account){

        try {
            List<Account> accounts = accountRepository.getAll();

            if (checkInCommissionAccount(accounts) && account.getType() == AccountType.COMMISSION){
                throw new HttpServerErrorException(HttpStatus.CONFLICT, "이미 수수료 계좌가 존재합니다.");
            }

            this.accountRepository.createAccount(account);
        } catch (Exception e){
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "계좌 생성 실패 \n" + e.getMessage());
        }

    }

    @Override
    public List<Account> getAllAccount(){

        List<Account> accounts = new ArrayList<>();

        try {
            accounts = this.accountRepository.getAll();
        } catch (Exception e){
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "계좌 조회 실패 \n" + e.getMessage());
        }

        return accounts;
    }

    @Override
    public String tranferAccount(String senderId, String receiverId, String sendAmount) {
        String tranferResult = "";

        try {
            List<Account> accounts = accountRepository.getAll();

            if (senderId.equals(receiverId)){
                throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "보내는 사람과 받는 사람이 동일합니다.");
            }

            if (!checkInAccountByAccountId(senderId, accounts) || !checkInAccountByAccountId(receiverId, accounts)){
                throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "보내는 사람 혹은 받는 사람의 계좌가 존재하지 않습니다.");
            }

            Account senderAccount = getAccountById(senderId, accounts);
            Account receiverAccount = getAccountById(receiverId, accounts);

            if (checkTransactionBetweenUsers(senderAccount.getType(), receiverAccount.getType()) && !checkInCommissionAccount(accounts)){
                throw new HttpServerErrorException(HttpStatus.CONFLICT, "유저간의 거래이지만 수수료 계좌가 존재하지 않습니다.");
            }

            checkSenderAmount(senderAccount, receiverAccount, Long.parseLong(sendAmount));

            tranferResult = this.accountRepository.transfer(senderId, receiverId, sendAmount);

        } catch (Exception e){
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "이체 실패 \n" + e.getMessage());
        }

        return tranferResult;
    }

    public boolean checkInCommissionAccount(List<Account> accounts){
        for (Account account : accounts){
            if (account.getType() == AccountType.COMMISSION){
                return true;
            }
        }

        return false;
    }

    public boolean checkInAccountByAccountId(String checkAccountId, List<Account> accounts){
        for (Account account : accounts){
            if (account.getAccountId().equals(checkAccountId)){
                return true;
            }
        }

        return false;
    }

    public Account getAccountById(String accountId, List<Account> accounts){
        for (Account account : accounts){
            if (account.getAccountId().equals(accountId)){
                return account;
            }
        }

        return null;
    }

    public void checkSenderAmount(Account sender, Account receiver, long sendAmount){

        float commissionPercent = 0.0f;

        if (checkTransactionBetweenUsers(sender.getType(), receiver.getType())){
            commissionPercent = 0.001f;
        }

        long commission = (long) (sendAmount * commissionPercent);
        long totalSendAmount = sendAmount + commission;

        if (sender.getAmount() < totalSendAmount){
            throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "보내는 사람의 계좌 잔액이 부족합니다.");
        }
    }

    public boolean checkTransactionBetweenUsers(AccountType senderType, AccountType receiverType){
        if (senderType == receiverType && senderType == AccountType.USER){
            return true;
        }

        return false;
    }
}
