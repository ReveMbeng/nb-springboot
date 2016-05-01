/*
 * Copyright 2016 sasha.
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
package com.github.alexfalappa.nbspringboot.projects.initializr;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.JsonNode;

import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_ARTIFACT;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_BOOT_VERSION;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_DEPENDENCIES;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_DESCRIPTION;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_GROUP;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_JAVA_VERSION;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_LANGUAGE;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_METADATA;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_NAME;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_PACKAGE;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_PACKAGING;
import static com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectProps.WIZ_PROJ_LOCATION;

@TemplateRegistration(
        folder = "Project/Maven2",
        displayName = "#InitializrSpringbootProject_displayName",
        description = "InitializrSpringbootProjectDescription.html",
        iconBase = "com/github/alexfalappa/nbspringboot/projects/initializr/InitializrSpringbootProject.png",
        content = "InitializrSpringbootProjectProject.zip")
@Messages("InitializrSpringbootProject_displayName=Spring Boot project (from Spring Initializr)")
public class InitializrProjectWizardIterator implements WizardDescriptor./*Progress*/InstantiatingIterator {

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;
    private final InitializrService initializrService = new InitializrService();

    public InitializrProjectWizardIterator() {
    }

    public static InitializrProjectWizardIterator createIterator() {
        return new InitializrProjectWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[]{
            new InitializrProjectWizardPanel1(),
            new InitializrProjectWizardPanel2(),
            new InitializrProjectWizardPanel3()
        };
    }

    private String[] createSteps() {
        return new String[]{
            NbBundle.getMessage(InitializrProjectWizardIterator.class, "LBL_BasePropsStep"),
            NbBundle.getMessage(InitializrProjectWizardIterator.class, "LBL_DependenciesStep"),
            NbBundle.getMessage(InitializrProjectWizardIterator.class, "LBL_CreateProjectStep")
        };
    }

    @Override
    public Set<FileObject> instantiate(/*ProgressHandle handle*/) throws IOException {
        System.out.println("com.github.alexfalappa.nbspringboot.projects.initializr.InitializrProjectWizardIterator.instantiate()");
        Set<FileObject> resultSet = new LinkedHashSet<>();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty(WIZ_PROJ_LOCATION));
        dirF.mkdirs();
        FileObject dir = FileUtil.toFileObject(dirF);
        // prepare service invocation params
        String botVersion = (String) wiz.getProperty(WIZ_BOOT_VERSION);
        String mvnGroup = (String) wiz.getProperty(WIZ_GROUP);
        String mvnArtifact = (String) wiz.getProperty(WIZ_ARTIFACT);
        String mvnName = (String) wiz.getProperty(WIZ_NAME);
        String mvnDesc = (String) wiz.getProperty(WIZ_DESCRIPTION);
        String lang = (String) wiz.getProperty(WIZ_LANGUAGE);
        String javaVersion = (String) wiz.getProperty(WIZ_JAVA_VERSION);
        String deps = (String) wiz.getProperty(WIZ_DEPENDENCIES);
        // TODO invoke initializr webservice and unzip response
        // unZipFile(resttemplate.getInputStream(), dir);
        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration<? extends FileObject> e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }
        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }
        return resultSet;
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        String[] steps;
        try {
            // invoke initializr service to get metadata
            JsonNode metadata = initializrService.getMetadata();
            this.wiz.putProperty(WIZ_METADATA, metadata);
            // create the normal panels
            panels = createPanels();
            // Make sure list of steps is accurate.
            steps = createSteps();
        } catch (Exception ex) {
            // create an error panel to indicate service access problems
            final ErrorWizardPanel errorWizardPanel = new ErrorWizardPanel();
            errorWizardPanel.setError(ex.getMessage());
            panels = new WizardDescriptor.Panel[]{errorWizardPanel};
            steps = new String[]{NbBundle.getMessage(InitializrProjectWizardIterator.class, "LBL_ErrorStep")};
            Exceptions.printStackTrace(ex);
        }
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(WIZ_NAME, null);
        this.wiz.putProperty(WIZ_GROUP, null);
        this.wiz.putProperty(WIZ_ARTIFACT, null);
        this.wiz.putProperty(WIZ_DESCRIPTION, null);
        this.wiz.putProperty(WIZ_PACKAGING, null);
        this.wiz.putProperty(WIZ_PACKAGE, null);
        this.wiz.putProperty(WIZ_JAVA_VERSION, null);
        this.wiz.putProperty(WIZ_LANGUAGE, null);
        this.wiz.putProperty(WIZ_DEPENDENCIES, null);
        panels = null;
    }

    @Override
    public String name() {
        return MessageFormat.format("{0} of {1}", new Object[]{index + 1, panels.length});
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    private static void unZipFile(InputStream source, FileObject projectRoot) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
                    if ("nbproject/project.xml".equals(entry.getName())) {
                        // Special handling for setting name of Ant-based projects; customize as needed:
                        filterProjectXML(fo, str, projectRoot.getName());
                    } else {
                        writeFile(str, fo);
                    }
                }
            }
        } finally {
            source.close();
        }
    }

    private static void writeFile(ZipInputStream str, FileObject fo) throws IOException {
        try (OutputStream out = fo.getOutputStream()) {
            FileUtil.copy(str, out);
        }
    }

    private static void filterProjectXML(FileObject fo, ZipInputStream str, String name) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(str, baos);
            Document doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, false, null, null);
            NodeList nl = doc.getDocumentElement().getElementsByTagName("name");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    if (el.getParentNode() != null && "data".equals(el.getParentNode().getNodeName())) {
                        NodeList nl2 = el.getChildNodes();
                        if (nl2.getLength() > 0) {
                            nl2.item(0).setNodeValue(name);
                        }
                        break;
                    }
                }
            }
            try (OutputStream out = fo.getOutputStream()) {
                XMLUtil.write(doc, out, "UTF-8");
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            writeFile(str, fo);
        }

    }

}
