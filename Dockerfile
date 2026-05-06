FROM ubuntu:22.04

RUN apt-get update

RUN apt install -y openjdk-21-jdk

RUN mkdir -p /usr/app/transfer
RUN mkdir -p /usr/app/transfer/data

COPY /src/main/resources/data/AccountsData.json /usr/app/transfer/data
COPY /src/main/resources/data/AccountsData.jsonl /usr/app/transfer/data

COPY target/MoneyTransfer-1.0.jar /usr/app/transfer


CMD ["java", "-jar", "/usr/app/transfer/MoneyTransfer-1.0.jar", "--initDataFile=/usr/app/transfer/data/AccountsData.json"]