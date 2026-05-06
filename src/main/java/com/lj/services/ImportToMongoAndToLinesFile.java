package com.lj.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lj.gen.json.mappings.transfer.TransfersystemSchema;
import com.mongodb.MongoBulkWriteException;
import org.bson.Document;
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
public class ImportToMongoAndToLinesFile {

    private static final Logger logger = LoggerFactory.getLogger(ImportToMongoAndToLinesFile.class);
    private static final String TRANSFER_COLLECTION = "transfers";

    private final MongoTemplate mongo;
    private final FileFetchService fileRetriever;
    private final File linesFile;

    @Autowired
    public ImportToMongoAndToLinesFile(MongoTemplate mongo, FileFetchService fileRetriever,
                                       @Value("${initFileLinesJson}") String jsonLinesFileName,
                                       @Value("${initDataFile}") String initialDataFileName) throws IOException {
        this.mongo = mongo;
        this.fileRetriever = fileRetriever;
        File initFile = this.fileRetriever.fetchFile(initialDataFileName);
        this.linesFile = this.fileRetriever.fetchFile(jsonLinesFileName);

        List<String> lines = jsonObjectToJsonLines(initFile);
        writeLinesToFile(lines);
        importToDb(TRANSFER_COLLECTION, lines);
    }

    private List<String> jsonObjectToJsonLines(File initFile) throws IOException {

        System.out.println("initial file path: " + initFile.getPath());

        ObjectMapper mapper = new ObjectMapper();
        TransfersystemSchema transfer;
        try (InputStream inputStream = new FileInputStream(initFile)) {
            transfer = mapper.readValue(inputStream, TransfersystemSchema.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> lines = new ArrayList<>();
        for(com.lj.gen.json.mappings.transfer.Account acc: transfer.getAccounts()) {
            String line = mapper.writeValueAsString(acc);
            lines.add(line);
        }
        writeLinesToFile(lines);
        return lines;
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

    private void importToDb(String destinationCollectionName, List<String> jsonLines) {

        List<Document> mongoDocs = generateMongoDocumentsPerLine(jsonLines);
        int inserts = insertInto(destinationCollectionName, mongoDocs);
        logger.info(inserts + " documents added to " + TRANSFER_COLLECTION + " collecion");
    }

    private List<Document> generateMongoDocumentsPerLine(List<String> lines) {

        List<Document> mongoDocuments = new ArrayList<>();
        for (String json : lines) {
            mongoDocuments.add(Document.parse(json));
        }
        return mongoDocuments;
    }

    private int insertInto(String destinationCollectionName, List<Document> mongoDocs) {
        try {
            Collection<Document> inserts = mongo.insert(mongoDocs, destinationCollectionName);
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
