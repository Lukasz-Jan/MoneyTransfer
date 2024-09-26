
Application that supports transfering funds out of and into specific accounts.

The application support the following features:

* account storage
* account debiting/crediting
* accounts import/export

## Feature Details

### Account Storage

Internally, the application stores information on accounts.

#### Account Number

Each account is uniquely identified by an account number - a non-empty string containing only digits. 
No two accounts stored by the application are allowed to have the same account number.

#### Currency Holdings

Along with each account, the application stores the account's holdings - by currency. 
For example, an account could have 50 PLN, and 140 USD. 
If an account does not contain a record of the amount for a given currency, 
then it is understood as the account not being allowed to hold that currency.

### Account Debiting/Crediting

The application supports a MQ (message queue) based request/response interface 
through which a specific account can be credited or debited some amount in a given currency. 

#### Requests

The application consumes messages from a request queue 
(the name of this queue is configurable). 
These messages have a XML payload matching the TransferRequest schema contained in [transfer-request-response.xsd]
(transfer-request-response.xsd). 
An example of a conformant XML can be found in [example-transfer-request.xml](example-transfer-request.xml).

#### Responses

The application produce messages to a response queue (the name of this queue is configurable).
These messages have a XML payload matching the TransferResponse schema contained in [transfer-request-response.xsd](transfer-request-response.xsd).


#### Logic

The application accepts the request message if it matches the TransferRequest schema and the following conditions are met:

 * the application has in its storage an account with account number matching the value in the *TargetAccountNumber* field,
 * the account is allowed to hold the currency specified in the *Currency* field,
 * if the value in the *Action* field is `DEBIT`, then the account must hold at least the amount specified in the *Quantity* field.

If these conditions are met, then:

 * For *Action* = `CREDIT`, the amount specified by field *Quantity* of currency specified by field *Currency* is added to the account.
 * For *Action* = `DEBIT`, the amount specified by field *Quantity* of currency specified by field *Currency* is subtracted from the account.
 
 Additionally, a reponse message is sent, with the value `ACCEPT` in the *Outcome* field. 
 All other fields have values matching the equivalent fields from the incoming request message. 

In all other situations, the request message is rejected. If the rejected request message is malformed (does not match the TransferRequest schema), 
the error is only logged. 
Otherwise, a response message is sent, with value `REJECT` in the *Outcome* field. 
All other fields have values matching the equivalent fields from the incoming request message.

### Account Import/Export

On startup, the application imports the account information from a JSON file. 
A path to the file is specified as a commandline argument to the applicatio`n. 
On each update to the account information stored in the application is updated too.

#### JSON Schema

The file used for import and export of account information conforms 
to the schema contained in [transfersystem.schema.json](transfersystem.schema.json).
 
An example of a conformant JSON file can be found in [example-transfer-system.json](example-transfer-system.json). 
