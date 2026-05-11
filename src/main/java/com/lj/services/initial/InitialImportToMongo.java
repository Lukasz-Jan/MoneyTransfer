package com.lj.services.initial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lj.gen.json.mappings.transfer.TransfersystemSchema;
import com.lj.mongo.repository.documents.AccountDoc;
import com.lj.services.FileFetchService;
import com.mongodb.MongoBulkWriteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class InitialImportToMongo {

    private static final Logger logger = LoggerFactory.getLogger(InitialImportToMongo.class);
    private static final String TRANSFER_COLLECTION = "transfers";
    private final MongoTemplate mongo;
    private final FileFetchService fileRetriever;
    private final File linesFile;

    @Autowired
    public InitialImportToMongo(MongoTemplate mongo, FileFetchService fileRetriever,
                                @Value("${initFileLinesJson}") String jsonLinesFileName,
                                @Value("${initDataFile}") String initialDataFileName) throws IOException {
        this.mongo = mongo;
        this.fileRetriever = fileRetriever;
        File initFile = this.fileRetriever.fetchFile(initialDataFileName);
        this.linesFile = this.fileRetriever.fetchFile(jsonLinesFileName);
        List<AccountDoc> accounts = jsonObjectToJsonLines(initFile);
        importToDb(accounts);
    }

    private List<AccountDoc> jsonObjectToJsonLines(File initFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TransfersystemSchema transfer;
        try (InputStream inputStream = new FileInputStream(initFile)) {
            transfer = mapper.readValue(inputStream, TransfersystemSchema.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> lines = new ArrayList<>();
        List<AccountDoc> accountsList = new ArrayList<>();
        for(com.lj.gen.json.mappings.transfer.Account acc: transfer.getAccounts()) {
            AccountDoc accountDocument = new AccountDoc(acc.getAccountNumber(), acc.getCurrencyAmounts());
            accountsList.add(accountDocument);
            lines.add(mapper.writeValueAsString(acc));
        }
        writeLinesToFile(lines);
        return accountsList;
    }

    private void writeLinesToFile(List<String> lines) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(linesFile.getPath()))) {
            for(String line: lines) {
                writer.append(line + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void importToDb(List<AccountDoc> docs) {
        int inserts = insertInto(docs);
        logger.info(inserts + " documents added to " + TRANSFER_COLLECTION + " collecion");
    }

    private int insertInto(List<AccountDoc> mongoDocs) {
        try {
            Collection<AccountDoc> inserts = mongo.insert(mongoDocs, TRANSFER_COLLECTION);
            return inserts.size();
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof MongoBulkWriteException) {
                return ((MongoBulkWriteException) e.getCause())
                        .getWriteResult()
                        .getInsertedCount();
            }
            return 0;
        }
    }
}
