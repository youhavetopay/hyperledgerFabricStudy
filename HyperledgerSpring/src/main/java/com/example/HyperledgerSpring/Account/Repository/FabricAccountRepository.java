package com.example.HyperledgerSpring.Account.Repository;

import com.example.HyperledgerSpring.Account.Domain.Account;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class FabricAccountRepository implements AccountRepository{

    private static final String CHANNEL_NAME = "mychannel";
    private static final String CHAINCODE_NAME = "basic";

    private final Contract contract;


    @Autowired
    public FabricAccountRepository (FabricGateWay fabricGateWay) {
        var gateway = fabricGateWay.connection();
        var network = gateway.getNetwork(CHANNEL_NAME);
        contract = network.getContract(CHAINCODE_NAME);
    }

    @Override
    public void createAccount(Account account) {

        try {
            contract.submitTransaction("CreateAsset", account.getAccountId(), account.getOwner(), String.valueOf(account.getAmount()), String.valueOf(account.getType()));
        } catch (EndorseException e) {
            e.printStackTrace();
            throw new RuntimeException("트랜잭션 제안 보증 실패(트랜잭션 실행 중 에러뜸)", e);
        } catch (CommitException e) {
            e.printStackTrace();
            throw new RuntimeException("트랜잭션 commit 실패", e);
        } catch (SubmitException e) {
            e.printStackTrace();
            throw new RuntimeException("트랜잭션 order 에게 전달 실패", e);
        } catch (CommitStatusException e) {
            e.printStackTrace();
            throw new RuntimeException("트랜잭션 commit 상태 조회 실패", e);
        }

    }

    @Override
    public List<Account> getAll(){
        List<Account> accounts = new ArrayList<>();

        try {
            var result = contract.evaluateTransaction("GetAllAssets");
            accounts = getStringJsonToList(new String(result));
        } catch (GatewayException e) {
            throw new RuntimeException("GetAllAssets 트랜잭션 실패", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }

        return accounts;
    }

    @Override
    public String transfer(String senderId, String receiverId, String sendAmount) {
        String tranferResult = "";

        try {
            var result = contract.submitTransaction("TransferAccount", senderId, receiverId, sendAmount);
            tranferResult = new String(result);

        } catch (EndorseException e) {
            e.printStackTrace();
            throw new RuntimeException("트랜잭션 제안 보증 실패(트랜잭션 실행 중 에러뜸)", e);
        } catch (CommitException e) {
            e.printStackTrace();
            throw new RuntimeException("트랜잭션 commit 실패", e);
        } catch (SubmitException e) {
            e.printStackTrace();
            throw new RuntimeException("트랜잭션 order 에게 전달 실패", e);
        } catch (CommitStatusException e) {
            e.printStackTrace();
            throw new RuntimeException("트랜잭션 commit 상태 조회 실패", e);
        }

        return tranferResult;
    }

    private List<Account> getStringJsonToList(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(json, Account[].class));
    }

}
