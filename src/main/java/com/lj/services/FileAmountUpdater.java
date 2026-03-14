package com.lj.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lj.gen.json.mappings.transfer.TransfersystemSchema;
import com.lj.repository.AcctRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class FileAmountUpdater {

    private static final Logger logger = LoggerFactory.getLogger(FileAmountUpdater.class);
    private final File initDataFile;

    @Autowired
    private AcctRepo acctRepo;

    @Autowired
    public FileAmountUpdater(@Value("${updatedDataFile}") String initialFilePath) throws IOException {
        initDataFile = loadResource(initialFilePath);
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

    private File loadResource(String resourcePath) throws IOException {

        File resultFile;
        Resource resource = new DefaultResourceLoader().getResource(resourcePath);
        if (!resource.exists()) {
            logger.error("Resource not exist");
            throw new RuntimeException("Resource not exist");
        }

        if (!resource.isFile()) {
            logger.info("Resource is not file");
            resultFile = File.createTempFile("initFile", null);
            logger.info("Init file created from input stream: " + this.initDataFile.getPath());
            Files.copy(resource.getInputStream(), this.initDataFile.toPath(), REPLACE_EXISTING);
        } else {
            try {
                resultFile = resource.getFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return resultFile;
    }
}