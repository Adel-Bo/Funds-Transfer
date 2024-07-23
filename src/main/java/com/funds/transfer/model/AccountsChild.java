package com.funds.transfer.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountsChild extends Accounts {

   public void extendThing () {
       getThing();
   }

    @Override
    public void getThing() {
        log.info("success");
    }
}

