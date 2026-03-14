FROM ubuntu:22.04

RUN apt-get update

RUN apt install -y openjdk-17-jdk

RUN mkdir -p /usr/app/transfer
RUN mkdir -p /usr/app/transfer/data

COPY /src/main/resources/data/AccountsData.json /usr/app/transfer/data

COPY target/MoneyTransfer-1.0.jar /usr/app/transfer


CMD ["java", "-jar", "/usr/app/transfer/MoneyTransfer-1.0.jar", "--initDataFile=/usr/app/transfer/data/AccountsData.json"]












#CMD pwd

#CMD ["/bin/bash", "-c", "ls -la"]

#CMD ["sh", "-c", "django-admin startproject $PROJECTNAME"]

#CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005", "-jar", "/usr/app/transfer/MoneyTransfer-1.0.jar"]

#CMD ["java", "-jar", "/usr/app/transfer/MoneyTransfer-1.0.jar", "AccountsData.json"]

#RUN java -jar /usr/app/transfer/MoneyTransfer-1.0.jar

#java -jar /usr/app/transfer/MoneyTransfer-1.0.jar --initDataFile=/usr/app/transfer/data/AccountsData.json
