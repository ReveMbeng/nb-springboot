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

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Alessandro Falappa
 */
public class SpringBootIndexer extends CustomIndexer {

    private static final Logger logger = Logger.getLogger(SpringBootIndexer.class.getName());

    public SpringBootIndexer() {
    }

    @Override
    protected void index(Iterable<? extends Indexable> arg0, Context arg1) {
        logger.log(Level.INFO, "Root: {0}", arg1.getRootURI());
        for (Indexable indexable : arg0) {
            logger.log(Level.INFO, "{0}", indexable.toString());
            try {
                IndexingSupport is = IndexingSupport.getInstance(arg1);
                IndexDocument doc = is.createDocument(indexable);
                doc.addPair("prova", "valore", false, true);
                is.addDocument(doc);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @MimeRegistration(mimeType = "text/x-json", service = SpringBootIndexerFactory.class)
    public static class SpringBootIndexerFactory extends CustomIndexerFactory {

        private static final Logger logger = Logger.getLogger(SpringBootIndexerFactory.class.getName());

        @Override
        public CustomIndexer createIndexer() {
            return new SpringBootIndexer();
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> arg0, Context arg1) {
            logger.info("Deleted some files");
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> arg0, Context arg1) {
            logger.info("Some files are dirty");
        }

        @Override
        public String getIndexerName() {
            return "SpringBootIndexer";
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

    }
}
