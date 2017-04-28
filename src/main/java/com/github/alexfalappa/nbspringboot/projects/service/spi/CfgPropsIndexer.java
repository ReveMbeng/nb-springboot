/*
 * Copyright 2017 Alessandro Falappa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.alexfalappa.nbspringboot.projects.service.spi;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.util.Exceptions;

import static java.util.logging.Level.INFO;

public class CfgPropsIndexer extends EmbeddingIndexer {

    private static final Logger logger = Logger.getLogger("CfgPropsIndexer");

    public CfgPropsIndexer() {
    }

    @Override
    protected void index(Indexable arg0, Parser.Result arg1, Context arg2) {
        logger.log(Level.INFO, "Root: {0}", arg2.getRootURI());
        logger.log(Level.INFO, "Indexable: {0}", arg0.toString());
        logger.log(Level.INFO, "Snapshot: {0}", arg1.getSnapshot().getText());
        try {
            IndexingSupport is = IndexingSupport.getInstance(arg2);
            IndexDocument doc = is.createDocument(arg0);
            doc.addPair("prova", "valore", false, true);
            is.addDocument(doc);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static class CfgPropsIndexerFactory extends EmbeddingIndexerFactory {

        private static final Logger logger = Logger.getLogger(CfgPropsIndexerFactory.class.getName());

        @Override
        public EmbeddingIndexer createIndexer(Indexable arg0, Snapshot arg1) {
            return new CfgPropsIndexer();
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> arg0, Context arg1) {
            for (Indexable indexable : arg0) {
                logger.log(INFO, "Deleted {0}", indexable.toString());
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> arg0, Context arg1) {
            for (Indexable indexable : arg0) {
                logger.log(INFO, "Dirty {0}", indexable.toString());
            }
        }

        @Override
        public String getIndexerName() {
            return "CfgPropsIndexer";
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

    }
}
