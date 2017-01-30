/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

public class BuildStopwords {
    public static void main(String[] args) throws IOException {
        final Map<Long, String> docId2Name = TfIdfStreams.buildDocumentInventory();
        final long docCount = docId2Name.size();
        System.out.println("Analyzing documents");
        final Map<String, Set<Long>> wordDocs = docId2Name
                .entrySet()
                .parallelStream()
                .flatMap(TfIdfStreams::docLines)
                .flatMap(TfIdfStreams::tokenize)
                .collect(groupingBy(Entry::getValue, mapping(Entry::getKey, toSet())));
        final File stopwordsFile = new File("stopwords.txt");
        System.out.println("Writing the stopwords file " + stopwordsFile.getAbsolutePath());
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(stopwordsFile), UTF_8))) {
            wordDocs.entrySet()
                    .stream()
                    .map(e -> new SimpleImmutableEntry<>(e.getKey(), e.getValue().size()))
                    .filter(e -> e.getValue() == docCount)
                    .sorted(comparing(Entry::getKey))
                    .map(Entry::getKey)
                    .forEach(w::println);
        }
    }
}
