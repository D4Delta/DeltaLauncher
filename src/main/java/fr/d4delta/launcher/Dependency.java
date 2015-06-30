/* 
This file is part of DeltaLauncher.
DeltaLauncher is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

DeltaLauncher is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with DeltaLauncher.  If not, see <http://www.gnu.org/licenses/>.
*/

package fr.d4delta.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * The dependecy class contains information about a dependecy reference, and some methods to interact with it.
 * You can retrieve information about the dependecy, like the groupId, the version, the pom file, or if the remote dependecy support sha1 for example.
 * @author d4delta
 */
public class Dependency {
    
    /**Constants**/
    
    public static final String sha1Ext = ".sha1";
    public static final String md5Ext = ".md5";
    public static final String pomExt = ".pom";
    
    /** **/
    
    public final String groupId;
    public final String artifactId;
    public final String version;
    public final File rootFolder;
    public final Callback callback;
    
    public Dependency(String groupId, String artifactId, String version, File rootFolder, Callback callback) {
        this.callback = callback;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.rootFolder = rootFolder;
        
        relativePath = genRelativePath();
        baseString = genBaseString();
        folder = genFolderFile();
        pom = genPomFile();
        pomSHA1 = getPomSHA1File();
        pomMD5 = genPomMD5File();
        preferedRepo = genPreferedRepoFile();
        
        pushProps();
    }
    
    public Dependency(Element reference, File rootFolder, Callback callback, Namespace namespace) {
        this(Utils.substituteMaven(reference.getChildText("groupId", namespace)), Utils.substituteMaven(reference.getChildText("artifactId", namespace)), Utils.substituteMaven(reference.getChildText("version", namespace)), rootFolder, callback);
    }
    
    private void pushProps() {
        System.setProperty("project.artifactId", artifactId);
        System.setProperty("project.groupId", groupId);
        System.setProperty("project.version", version);
    }
    
    @Override
    public String toString() {
        return groupId + "." + artifactId + "-" + version;
    }
    
    public final String relativePath;
    public final String genRelativePath() {
        return groupId.replaceAll(Pattern.quote("."), "/") + "/" + artifactId + "/" + version + "/";
    }
    
    public final String baseString;
    public final String genBaseString() {
        return artifactId + "-" + version;
    }
    
    public final File folder;
    public final File genFolderFile() {
        return new File(rootFolder, relativePath);
    }
    
    public final File pom;
    public final File genPomFile() {
        return new File(folder, baseString + pomExt);
    }
    
    public final File pomMD5;
    public final File genPomMD5File() {
        return new File(folder, baseString + pomExt + md5Ext);
    }
    
    public final File pomSHA1;
    public final File getPomSHA1File() {
        return new File(folder, baseString + pomExt + sha1Ext);
    }
    
    public final File preferedRepo;
    public final File genPreferedRepoFile() {
        return new File(folder, "_maven.repositories");
    }
    
    public Repository source;
    public boolean genRemoteURLS(Repository repo) {
        source = repo;
        genRemoteFolderURL();
        genRemotePomURL();
        
        if(Utils.is404(remotePomURL)) {
            source = null;
            remoteFolderURL = null;
            remotePomURL = null;
            return false;
        }
            
        genRemotePomMD5URL();
        genRemotePomSHA1URL();
        
        return true;
    }
    
    public String remoteFolderURL;
    public void genRemoteFolderURL() {
        remoteFolderURL = source.url + relativePath;
    }
    
    public URL remotePomURL;
    public void genRemotePomURL() {
        try {
            remotePomURL = new URL(remoteFolderURL + baseString + pomExt);
        } catch (MalformedURLException ex) {}
    }
    
    public URL remotePomSHA1URL;
    public boolean remotePomHasSHA1;
    public void genRemotePomSHA1URL() {
        try {
            remotePomSHA1URL = new URL(remoteFolderURL + baseString + pomExt + sha1Ext);
            remotePomHasSHA1 = !Utils.is404(remotePomSHA1URL);
        } catch (MalformedURLException ex) {}
    }
    
    public URL remotePomMD5URL;
    public boolean remotePomHasMD5;
    public void genRemotePomMD5URL() {
        try {
            remotePomMD5URL = new URL(remoteFolderURL + baseString + pomExt + md5Ext);
            remotePomHasMD5 = !Utils.is404(remotePomMD5URL);
        } catch (MalformedURLException ex) {}
    }
    
    
    public void downloadPom() {
        
        if(!pom.exists() || (remotePomHasSHA1 && pomSHA1.exists() && !Utils.equals(remotePomSHA1URL, pomSHA1)) || (remotePomHasMD5 && pomMD5.exists() && !Utils.equals(remotePomMD5URL, pomMD5))) {
            Utils.downloadURL(remotePomURL, pom, callback);
            if(remotePomHasSHA1)
                Utils.downloadURL(remotePomSHA1URL, pomSHA1, callback);
            if(remotePomHasMD5)
                Utils.downloadURL(remotePomMD5URL, pomMD5, callback);
        }
    
    }
    
    final String mavenComment = "#NOTE: This is an internal implementation file, its format can be changed without prior notice.";
    public void updatePreferedRepoFile() {
        try(PrintWriter pw = new PrintWriter(preferedRepo)) {
            pw.write(mavenComment + System.lineSeparator() + "#" + Calendar.getInstance().getTime() + System.lineSeparator());
            File[] in = folder.listFiles();
            for(File f: in) {
                if(!f.equals(preferedRepo)) {
                    pw.write(f.getName() + ">" + source.id + "=" + System.lineSeparator());
                }
            }
        } catch (IOException ex) {}
    }
    
    public String getPreferedRepoId() {
        try(Scanner scanner = new Scanner(preferedRepo)) {
            String current;
            while((current = scanner.nextLine()).startsWith("#"));
            return current.substring(current.indexOf(">"), current.length()-1);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }
    
}
