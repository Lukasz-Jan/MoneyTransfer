
##### Simple bank application
Allows to transer funds into and out of accounts.
In/out requests come from activeMQ.
More detailed description in file description.md.

##### How to start?
######  Start with data initialized in application property
 - java -jar MoneyTransfer-1.0.jar
######  Start with data initialized from inner resource
 - java -jar MoneyTransfer-1.0.jar --initDataFile=AccountsData.json
######  Starting with init data from file system
 - java -jar MoneyTransfer-1.0.jar --initDataFile=/usr/app/transfer/data/AccountsData.json
######  Maven start:
 - mvn spring-boot:run
 - mvn spring-boot:run -Dspring-boot.run.arguments="--initDataFile=AccountsData.json" 
 - mvn spring-boot:run -Dspring-boot.run.arguments="--initDataFile=/usr/app/transfer/data/AccountsData.json"



######  Tests run:

 - mvn test -Dtest=AddAccountServiceIT
 - mvn test -Dtest=TransactionServiceIT
 - mvn test -Dtest=AddAccountServiceChangeAmountForAccountTest
 - mvn test -Dtest=AddAccountServiceChangeAmountForAccountTest#function_changeAmountForAccount_AccountOK_CURRENCY_NOT_OK


###### ActiveMq
- Request queue name and response queue name are defined in application properties
- ActiveMq shall be installed and started separately, web description https://activemq.apache.org/
