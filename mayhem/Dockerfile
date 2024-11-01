FROM cifuzz/jazzer

COPY ./core-banking-service/build/libs/core-banking-service-0.0.1-SNAPSHOT-all.jar /core-banking-service.jar

# ENTRYPOINT ["/app/jazzer", "--cp=/core-banking-service.jar", "--autofuzz=com.javatodev.finance.service.TransactionService::internalFundTransfer", "--autofuzz_ignore=java.lang.NullPointerException"]
# ENTRYPOINT ["/app/jazzer", "--cp=/core-banking-service.jar", "--target_class=com.javatodev.finance.CoreBankingServiceHarness"]