# Use Mayhem to completely secure your web applications

This example repo has both an external API as well as internal behavior. The following approaches demonstrate how someone might harness this codebase for testing with Mayhem.

## API testing / Full system testing

From the end user perspective, the only real way to interact with this application is via the API. We can easily point Mayhem at the API to start testing its robustness. 

### Start the API

From the `docker-compose` directory, run `docker-compose up` to start the API.

### Run Mayhem

Run `./mayhem/mapi.sh` from the root directory to test the API.

## Functional/Unit testing

The core banking service of this application has a few key functions that enable its behavior. We can use Mayhem with Jazzer's Autofuzz to specify and automatically test these functions.

### Build the application .jar

From the `core-banking-service` directory, run `gradle build shadowJar` to build the application.

### Dockerize the application

This isn't true containerization - we're just copying the jar into a base image that already has jazzer installed to make things easier for us. Run `docker build -t <your_image_name> . -f ./mayhem/Dockerfile` from the root directory. I'm pushing it to ghcr.io, so I'm using `ghcr.io/xansec/internet-banking-concept-microservices/core-banking-service:latest`.

### Create a Mayhemfile

This is a simple config file that tells Mayhem what to test. We combine this with Jazzer's Autofuzz to test the `fundTransfer` function. 

```yaml
image: ghcr.io/xansec/internet-banking-concept-microservices/core-banking-service:latest
project: internet-banking-concept-microservices
target: fund-transfer

cmds:
  - cmd: /app/jazzer --cp=/core-banking-service.jar --autofuzz=com.javatodev.finance.controller.TransactionController::fundTransfer --autofuzz_ignore=java.lang.NullPointerException
    libfuzzer: true
    env:
      MFUZZ_COMPAT_LEVEL: "1"
    timeout: 45
```

### Run Mayhem

From this directory, run `mayhem run . -f fundTransfer.Mayhemfile` to test the `fundTransfer` function.


## Integration testing

Functional testing is interesting, but it doesn't fully describe what the application is intended to do. For that, we need to write a harness that recreates this behavior, and see how it handles unintended inputs.

### Create a harness

We can write a simple harness that declares two bank accounts, and transfers funds from one account to the other. A simple integration test might look like this:

```java
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
            System.out.println("Transfer Successful: " + response.getReferenceNumber());
        } catch (EntityNotFoundException e) {
            System.out.println("Transfer Failed: " + e.getMessage());
        }
    }
```

Here, instead of specifying the ammounts and balances ourselves, we use Mayhem to generate targeted values based on the program's behavior. This allows us to test the application's behavior in a more realistic way.

### Build the Docker image

Same as before, but we create a shadowJar to include all of the code we need to run the harness.

### Create a Mayhemfile

This time, instead of using autofuzz, we'll specify our harness function, since it already handles external inputs.

```yaml
image: ghcr.io/xansec/internet-banking-concept-microservices/core-banking-service:latest
project: internet-banking-concept-microservices
target: core-banking-service-harness

cmds:
  - cmd: /app/jazzer --cp=/core-banking-service.jar --target_class=com.javatodev.finance.CoreBankingServiceHarness
    libfuzzer: true
    env:
      MFUZZ_COMPAT_LEVEL: "1"
    timeout: 45
```

### Run Mayhem

From this directory, run `mayhem run . -f cbsHarness.Mayhemfile` to test the `CoreBankingServiceHarness` function.


## Conclusion

That's it! We've now tested the application from three different perspectives, and have a good idea of how it behaves under different conditions. You're now ready to unleash Mayhem on your own codebase!

