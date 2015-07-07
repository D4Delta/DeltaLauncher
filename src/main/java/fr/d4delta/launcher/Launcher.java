/* 
    This file is part of DeltaLauncher.

    DeltaLauncher is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DeltaLauncher is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the Lesser GNU General Public License
    along with DeltaLauncher.  If not, see <http://www.gnu.org/licenses/>.
*/

package fr.d4delta.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

/**
 * The launcher allows you to update and launch a maven java application.
 * You just have to create a new instance and run().
 * @author d4delta
 */
public class Launcher extends Thread {
    
    private final File rootFolder;
    private final Callback callback;
    private final String[] args;
    
    private final Element root;
    
    HashMap<String,Repository> repositories = new HashMap<>();
    HashMap<String, Boolean> isLoaded = new HashMap<>();
    
    public List<DependencyType> types = new LinkedList<>();
    
    /**
     * Main constructor of the launcher class.
     * @param rootFolder The folder where the dependecies will be downloaded (and loaded)
     * @param source The source pom. For example, if it's a remote pom, use "new URL("http://example.com/yourpom.xml").openStream()";
     * @param callBack The callback. The callback object receive feedback about the launch process.
     * @param args The args to give to the main methods.
     * @throws JDOMException if the given pom is an incorrect xml file.
     * @throws IOException  if something goes wrong while reading the file.
     */
    public Launcher(File rootFolder, InputStream source, Callback callBack, String[] args) throws JDOMException, IOException {
        this.rootFolder = rootFolder;
        this.callback = callBack;
        this.args = args;
        root = new SAXBuilder().build(source).getRootElement();
        rootFolder.mkdirs();
        
        //Default types initialization
        types.add(new JarDependencyType(args));
    }
    
    /**
     * 
     * @param rootFolder The folder where the dependecies will be downloaded (and loaded)
     * @param source The source pom. For example, if it's a remote pom, use "new URL("http://example.com/yourpom.xml").openStream()";
     * @param args The args to give to the main methods.
     * @throws JDOMException if the given pom is an incorrect xml file.
     * @throws IOException if something goes wrong while reading the file.
     */
    public Launcher(File rootFolder, InputStream source, String[] args) throws JDOMException, IOException {
        this(rootFolder, source, new PrintCallback(), args);
    }

    /**
    * 
    * @param rootFolder The folder where the dependecies will be downloaded (and loaded)
    * @param source The source pom. For example, if it's a remote pom, use "new URL("http://example.com/yourpom.xml").openStream()";
    * @param callback The callback. The callback object will receive feedback about the launch process.
    * @throws JDOMException if the given pom is an incorrect xml file.
    * @throws IOException if something goes wrong while reading the file.
    */
    public Launcher(File rootFolder, InputStream source, Callback callback) throws JDOMException, IOException {
        this(rootFolder, source, callback, new String[0]);
    }
    
    /**
     * 
     * @param rootFolder The folder where the dependecies will be downloaded (and loaded)
     * @param source The source pom. For example, if it's a remote pom, use "new URL("http://example.com/yourpom.xml").openStream()";
     * @throws JDOMException if the given pom is an incorrect xml file.
     * @throws IOException if something goes wrong while reading the file.
     */
    public Launcher(File rootFolder, InputStream source) throws JDOMException, IOException {
        this(rootFolder, source, new String[0]);
    }
    
    /**
     * If you wish to update without starting a new thread, use this method.
     * But if you want to update asynchronously, use start() instead.
     */
    @Override
    public void run() {
        repositories.put("central", new Repository("central", "http://central.maven.org/maven2/"));

        Namespace rootNamespace = root.getNamespace();
        loadProperties(root, rootNamespace);
        registerRepositories(root, rootNamespace);
        loadReferences(root, rootNamespace);

        launch();
    }
    
    private void launch() {
        for(int i = types.size(); i > 0; i--) {
            types.get(i).done(callback, types);
        }
    }
    
    private void loadProperties(Element pom, Namespace namespace) {
        List<Element> properties;
        try {
            properties = pom.getChild("properties").getChildren();
            
            for(Element e: properties) {
                String[] res = callback.addingPropertyNotification(e.getName(), Utils.substituteMaven(e.getValue()));
                if(res != null && res.length == 2) {
                    System.setProperty(res[0], res[1]);
                }
            }
        } catch(NullPointerException ex) {}
    }
    
    private void registerRepositories(Element pom, Namespace namespace) {
        List<Element> repos;
        try {
            repos = pom.getChild("repositories", namespace).getChildren("repository", namespace);
        } catch(NullPointerException ex) {
            return;
        }    
        
        for(Element e: repos) {
            String id = Utils.substituteMaven(e.getChildText("id", namespace));
            String url = Utils.substituteMaven(e.getChildText("url", namespace));
            callback.addingRepositoryNotification(id, url);
            repositories.put(id,new Repository(id, url));
        }
    }
    
    private void loadReferences(Element root, Namespace namespace) {
        List<Element> dependecies;
        try {
            dependecies = root.getChild("dependencies", namespace).getChildren("dependency", namespace);
        } catch(NullPointerException ex) {
            return;
        }
        
        for(Element e: dependecies) {
            loadReference(e, namespace);
        }
    }
    
    private boolean findRemote(Dependency dependency) {
        
        String preferedId = dependency.getPreferedRepoId();
        if(preferedId != null) {
            Repository preferedRepo = repositories.get(preferedId);
            if(preferedRepo != null && dependency.genRemoteURLS(preferedRepo)) {
                return true;
            }
        }
        
        Set<Entry<String,Repository>> entries = repositories.entrySet();
        for(Entry<String, Repository> e: entries) {
            if(dependency.genRemoteURLS(e.getValue())) {
                return true;
            }
        }
        return false;
    }
    
    private void loadReference(Element reference, Namespace namespace) {
        
        {
            String scope = reference.getChildText("scope", namespace);
            if(scope != null && (!scope.equals("runtime") || !scope.equals("compile")))
                return;
            
            String optional = reference.getChildText("optional", namespace);
            if(optional != null && optional.equals("true"))
                return;
        }
        
        Dependency dependency = new Dependency(reference, rootFolder, callback, namespace);
        
        if(isLoaded.get(dependency.groupId + dependency.artifactId + dependency.version) != null) {
            return;
        }
        
        callback.loadingDependencyNotification(dependency);
        
        if(!findRemote(dependency)) {
            callback.dependencyUnavailableError(dependency);
        } else {
            downloadDependency(dependency);
        }
    }
    
    private void downloadDependency(Dependency dependency)  {
        
        dependency.downloadPom();
        
        Element pomRoot;
        try {
            pomRoot = new SAXBuilder().build(dependency.pom).getRootElement();
        } catch (JDOMException | IOException ex) {
            callback.pomLoadError(dependency, ex);
            Utils.purge(dependency.folder);
            return;
        }
        Namespace namespace = pomRoot.getNamespace();
        
        
        loadProperties(pomRoot, namespace);
        registerRepositories(pomRoot, namespace);
        
        for(DependencyType t: types) {
            if(t.download(dependency, pomRoot, callback)) {
                dependency.updatePreferedRepoFile();
                isLoaded.put(dependency.groupId+dependency.artifactId+dependency.version, Boolean.TRUE);
                loadReferences(pomRoot, namespace);
                return;
            }
        }
            
    }
    
}
