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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Alessandro Falappa
 */
@ConstrainedBinaryIndexer.Registration(
        mimeType = "text/x-json",
        requiredResource = "META-INF",
        indexerName = "SpringBootBinaryIndexer",
        indexVersion = 1
)
public class SpringBootBinaryIndexer extends ConstrainedBinaryIndexer {

    private static final Logger logger = Logger.getLogger(SpringBootBinaryIndexer.class.getName());

    @Override
    protected void index(Map<String, ? extends Iterable<? extends FileObject>> arg0, Context arg1) {
        logger.log(Level.INFO, "Root: {0}", arg1.getRootURI());
        for (Map.Entry<String, ? extends Iterable<? extends FileObject>> entry : arg0.entrySet()) {
            logger.log(Level.INFO, "{0}", entry.getKey());
            for (Object object : entry.getValue()) {
                FileObject fob = (FileObject) object;
                logger.log(Level.INFO, "\t{0}", FileUtil.getFileDisplayName(fob));
            }
        }
    }

}
