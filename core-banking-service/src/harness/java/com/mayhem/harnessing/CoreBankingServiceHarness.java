package com.javatodev.finance;

import com.javatodev.finance.repository.BankAccountRepository;
import com.javatodev.finance.repository.UtilityAccountRepository;
import com.javatodev.finance.repository.TransactionRepository;
import com.javatodev.finance.model.dto.BankAccount;
import com.javatodev.finance.model.dto.request.FundTransferRequest;
import com.javatodev.finance.model.dto.response.FundTransferResponse;
import com.javatodev.finance.service.TransactionService;
import com.javatodev.finance.service.AccountService;
import java.math.BigDecimal;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.javatodev.finance.exception.EntityNotFoundException;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
import org.mockito.Mockito;

// @Slf4j
// @Service
// @RequiredArgsConstructor
public class CoreBankingServiceHarness {

    private static final BankAccountRepository bankAccountRepository = Mockito.mock(BankAccountRepository.class);
    private static final UtilityAccountRepository utilityAccountRepository = Mockito.mock(UtilityAccountRepository.class);
    private static final TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);

    private static final AccountService accountService = new AccountService(bankAccountRepository, utilityAccountRepository);
    private static final TransactionService transactionService = new TransactionService(accountService, bankAccountRepository, transactionRepository);


    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        String accountNumber1 = data.consumeString(10);
        String accountNumber2 = data.consumeString(10);
        
        BigDecimal initialBalance1 = BigDecimal.valueOf(data.consumeRegularDouble(100, 10000));
        BigDecimal initialBalance2 = BigDecimal.valueOf(data.consumeRegularDouble(100, 10000));

        BankAccount account1 = new BankAccount();
        BankAccount account2 = new BankAccount();
        account1.setNumber(accountNumber1);
        account1.setActualBalance(initialBalance1);
        account2.setNumber(accountNumber2);
        account2.setActualBalance(initialBalance2);

        for (int i = 0; i < data.consumeInt(1,20); i++) { 
            BigDecimal transferAmount = BigDecimal.valueOf(data.consumeRegularDouble(1, 5000));
            FundTransferRequest transferRequest = new FundTransferRequest();
            transferRequest.setFromAccount(accountNumber1);
            transferRequest.setToAccount(accountNumber2);
            transferRequest.setAmount(transferAmount);
            try {
                FundTransferResponse response = transactionService.fundTransfer(transferRequest);
                // System.out.println("Transfer Successful: " + response.getReferenceNumber());
            } catch (EntityNotFoundException e) {
                // System.out.println("Transfer Failed: " + e.getMessage());
            }
        }

        // BankAccount finalAccount1 = accountService.readBankAccount(accountNumber1);
        // BankAccount finalAccount2 = accountService.readBankAccount(accountNumber2);

        // System.out.println("Final Balance of Account 1: " + finalAccount1.getActualBalance());
        // System.out.println("Final Balance of Account 2: " + finalAccount2.getActualBalance());
    }
}
