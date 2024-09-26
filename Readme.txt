1.
Application that supports transfering funds out of and into specific accounts.

The application support the following features:

account storage
account debiting/crediting
accounts import/export


2. 
Start and close server:

mvn spring-boot:run -Dspring-boot.run.arguments="--fileWithAccountsInitPath=src/main/resources/AccountsMoneyBalance.json"
http://localhost:8082/close

3.
Starting ActiveMQ
ActiveMQ does not start automatically
Shall be started independently.
