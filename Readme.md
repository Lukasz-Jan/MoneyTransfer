
##### Simple bank application
Allows to transer funds into and out of accounts.
In/out requests come from activeMQ.
More detailed description in file description.md.

##### How to start?
######  Start with data initialized in application property
###### MongoDb address shall be given as parameter

 - java -jar MoneyTransfer-1.0.jar
 - java -jar MoneyTransfer-1.0.jar --mongoConnString=mongodb://127.0.0.1:27017/test
######  Start with data initialized from inner resource
 - java -jar MoneyTransfer-1.0.jar --initDataFile=AccountsData.json --mongoConnString=mongodb://127.0.0.1:27017/test
######  Starting with init data from file system
 - java -jar MoneyTransfer-1.0.jar --initDataFile=/usr/app/transfer/data/AccountsData.json --mongoConnString=mongodb://127.0.0.1:27017/test

######  Maven start, mongoDb shall be started before:

 - mvn spring-boot:run -Dspring-boot.run.arguments="--mongoConnString=mongodb://127.0.0.1:27017/test"
 - mvn spring-boot:run -Dspring-boot.run.arguments="--mongoConnString=mongodb://127.0.0.1:27017/test --initDataFile=AccountsData.json"

######  Docker start (all in one: transfer app, aciveMq, mongoDb, H2):
- with script startApplication.sh

debug:


    java "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" -jar MoneyTransfer-1.0.jar
    java "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005" -jar MoneyTransfer-1.0.jar 
    java "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005" -jar MoneyTransfer-1.0.jar
    java "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005" -jar MoneyTransfer-1.0.jar --initDataFile=/usr/app/transfer/data/AccountsData.json


    


######  Tests run:

 - mvn test -Dtest=AddAccountServiceIT
 - mvn test -Dtest=TransactionServiceIT
 - mvn test -Dtest=FileAmountUpdaterTest
 - mvn test -Dtest=FileAmountUpdaterTest#havingDifferentAccounts_changeAmounts_InFile


###### ActiveMq
- Request queue name and response queue name are defined in application properties
- ActiveMq shall be installed and started separately 
  if running outside docker,
  web description https://activemq.apache.org/

###### Docker available
- Possible to start transfer server with activeMq via
  docker compose - script startApplication.sh

###### mongo db 
- Initial data (initial amounts) 
  written to mongodb as well as to H2 