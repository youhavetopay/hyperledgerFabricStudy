package com.example.HyperledgerSpring.Account.Controller;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferInputForm {

    @NotNull
    Long sender;
    @NotNull
    Long receiver;
    @NotNull
    Long sendAmount;

}
