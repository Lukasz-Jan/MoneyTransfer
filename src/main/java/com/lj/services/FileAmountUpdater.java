package com.lj.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lj.gen.json.mappings.transfer.TransfersystemSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.math.BigDecimal;

@Service
public class FileAmountUpdater {

    private static final Logger logger = LoggerFactory.getLogger(FileAmountUpdater.class);
    private final File initDataFile;
    private final FileFetchService fileService;

    @Autowired
    public FileAmountUpdater(@Value("${updatedDataFile}") String initialFilePath,
                             FileFetchService fileService) throws IOException {

        this.fileService = fileService;
        this.initDataFile = this.fileService.fetchFile(initialFilePath);
    }

    public void changeAmountForAccountInInitDataFile(String accountToUpdate, String currencyToUpdate, BigDecimal newAmount) {

        ObjectMapper mapper = new ObjectMapper();
        TransfersystemSchema transferSchemaRoot;

        try (InputStream inputStream = new FileInputStream(initDataFile)) {
            transferSchemaRoot = mapper.readValue(inputStream, TransfersystemSchema.class);
            logger.info("Json root loaded, search for account " + accountToUpdate + " " + currencyToUpdate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        transferSchemaRoot
                .getAccounts()
                .stream()
                .filter(a -> a.getAccountNumber().equals(accountToUpdate))
                .anyMatch(foundAccountToUpdate -> {
                            foundAccountToUpdate
                                    .getCurrencyAmounts()
                                    .stream()
                                    .filter(transferCurrency -> transferCurrency.getCurrency().equals(currencyToUpdate))
                                    .anyMatch(foundtransferCurrency -> {
                                        foundtransferCurrency.setAmount(newAmount.doubleValue());
                                        return true;
                                    });
                            return true;
                        }
                );
        try {
            mapper.writeValue(initDataFile, transferSchemaRoot);
        } catch (IOException e) {
            logger.error("Money not updated in file for acc: " + accountToUpdate);
            throw new RuntimeException("Exception in changeAmountForAccountInInitDataFile");
        }
    }
}