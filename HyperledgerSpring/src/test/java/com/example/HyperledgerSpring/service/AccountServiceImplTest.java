package com.example.HyperledgerSpring.service;

import com.example.HyperledgerSpring.Account.AccountType;
import com.example.HyperledgerSpring.Account.Domain.Account;
import com.example.HyperledgerSpring.Account.Repository.FabricAccountRepository;
import com.example.HyperledgerSpring.Account.Service.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @InjectMocks
    AccountServiceImpl accountService;

    @Mock
    FabricAccountRepository fabricAccountRepository;

    List<Account> mockAccountListByCommission = new ArrayList<>();
    List<Account> mockAccountListByNotCommission = new ArrayList<>();

    @BeforeEach
    public void beforeEach(){
        accountService = new AccountServiceImpl(fabricAccountRepository);

        mockAccountListByCommission.clear();
        mockAccountListByNotCommission.clear();

        mockAccountListByCommission.add(
                Account.builder().accountId("1111")
                        .owner("A")
                        .type(AccountType.USER)
                        .amount(2000L)
                        .build());
        mockAccountListByCommission.add(
                Account.builder().accountId("2222")
                        .owner("B")
                        .type(AccountType.USER)
                        .amount(0L)
                        .build());
        mockAccountListByCommission.add(
                Account.builder().accountId("3333")
                        .owner("C")
                        .type(AccountType.USER)
                        .amount(0L)
                        .build());
        mockAccountListByCommission.add(
                Account.builder().accountId("4444")
                        .owner("C")
                        .type(AccountType.COMMISSION)
                        .amount(0L)
                        .build());


        mockAccountListByNotCommission.add(
                Account.builder().accountId("1111")
                        .owner("A")
                        .type(AccountType.USER)
                        .amount(2000L)
                        .build());
        mockAccountListByNotCommission.add(
                Account.builder().accountId("2222")
                        .owner("B")
                        .type(AccountType.USER)
                        .amount(0L)
                        .build());
        mockAccountListByNotCommission.add(
                Account.builder().accountId("3333")
                        .owner("C")
                        .type(AccountType.USER)
                        .amount(0L)
                        .build());
    }

    @Test
    @DisplayName("Commission Account 생성 에러 테스트")
    void createAccountByCommissionAccountException(){
        Account commissionAccount = Account.builder()
                .accountId("1111")
                .owner("A")
                .type(AccountType.COMMISSION)
                .amount(0L)
                .build();

        when(fabricAccountRepository.getAll())
                .thenReturn(this.mockAccountListByCommission);

        HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
                () -> accountService.createAccount(commissionAccount));

        assertThat(e.getMessage()).isEqualTo("500 계좌 생성 실패 \n409 이미 수수료 계좌가 존재합니다.");

    }


    @Test
    @DisplayName("transferAccount 동일한 계좌 테스트")
    void transferAccountBySameAccount(){
        String senderId = "1111";
        String receiverId = "1111";

        when(fabricAccountRepository.getAll())
                .thenReturn(this.mockAccountListByCommission);

        HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
                () -> accountService.tranferAccount(senderId, receiverId, "1000"));

        assertThat(e.getMessage()).isEqualTo("500 이체 실패 \n400 보내는 사람과 받는 사람이 동일합니다.");
    }

    @Test
    @DisplayName("transferAccount 존재하지 않은 계좌 에러 테스트")
    void transferAccountByNonExistentAccount(){
        String senderId = "1111";
        String receiverId = "99999";

        when(fabricAccountRepository.getAll())
                .thenReturn(this.mockAccountListByCommission);

        HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
                () -> accountService.tranferAccount(senderId, receiverId, "1000"));

        assertThat(e.getMessage()).isEqualTo("500 이체 실패 \n400 보내는 사람 혹은 받는 사람의 계좌가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("transferAccount 존재하지 않은 수수료 계좌 에러 테스트")
    void transferAccountByNonExistentCommissionAccount(){
        String senderId = "1111";
        String receiverId = "2222";

        when(fabricAccountRepository.getAll())
                .thenReturn(this.mockAccountListByNotCommission);

        HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
                () -> accountService.tranferAccount(senderId, receiverId, "1000"));

        assertThat(e.getMessage()).isEqualTo("500 이체 실패 \n409 유저간의 거래이지만 수수료 계좌가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("transferAccount 잔액 부족 에러 테스트")
    void transferAccountByNotEnoughAmount(){
        String senderId = "1111";
        String receiverId = "2222";

        when(fabricAccountRepository.getAll())
                .thenReturn(this.mockAccountListByCommission);

        HttpServerErrorException e = assertThrows(HttpServerErrorException.class,
                () -> accountService.tranferAccount(senderId, receiverId, "100000"));

        assertThat(e.getMessage()).isEqualTo("500 이체 실패 \n400 보내는 사람의 계좌 잔액이 부족합니다.");
    }
}
