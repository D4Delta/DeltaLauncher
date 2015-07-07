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
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jdom2.Element;

/**
 * Will download and add to the classpath maven-native dependency.
 * @author d4delta
 */
public class NativeDependencyType extends DependencyType{

    public final String natives = "natives";

    private enum HackType {Usr, Sys}
    private final HackType hackType;
    
    private String sysLibrariesPaths;
    private List<String> usrLibrariesPaths;
    
    
    public NativeDependencyType() throws NativeHackException {
        if(canUsrHack()) {
            hackType = HackType.Usr;
            usrLibrariesPaths = new LinkedList<>();
        } else if(canSysHack()) {
            hackType = HackType.Sys;
            sysLibrariesPaths = System.getProperty("java.library.path");
        } else {
            throw new NativeHackException();
        }
    }
    
    @Override
    public boolean download(Dependency dependency, Element rootPom, Callback callback) {
        URL remotePackURL = null;
        URL remotePackMD5URL = null;
        URL remotePackSHA1URL = null;
        
        String baseString = dependency.baseString + "-" + natives + "-" + Utils.OS;
        String baseURL = dependency.remoteFolderURL + baseString;
        
        try {
            remotePackURL =  new URL(baseURL + JarDependencyType.jarExt);
            remotePackMD5URL = new URL(baseURL + JarDependencyType.jarExt + Dependency.md5Ext);
            remotePackSHA1URL = new URL(baseURL + JarDependencyType.jarExt + Dependency.sha1Ext);
        } catch(MalformedURLException ex) {
            return false;
        }
        
        if(Utils.is404(remotePackURL)) {
            return false;
        }
        
        boolean hasMD5 = !Utils.is404(remotePackMD5URL);
        boolean hasSHA1 = !Utils.is404(remotePackSHA1URL);
        
        //File packFile = new File(dependency.folder, baseString + JarDependencyType.jarExt);
        File packMD5File = new File(dependency.folder, baseString + JarDependencyType.jarExt + Dependency.md5Ext);
        File packSHA1File = new File(dependency.folder, baseString + JarDependencyType.jarExt + Dependency.sha1Ext);
        File extractedPack = new File(dependency.folder, "natives-" + Utils.OS);
        
        if(!extractedPack.exists() || (hasMD5 && !Utils.equals(remotePackMD5URL, packMD5File)) || (hasSHA1 && !Utils.equals(remotePackSHA1URL, packSHA1File))) {
            
            Utils.purge(extractedPack);
            extractedPack.mkdirs();
            
            if(hasMD5) {
                packMD5File.delete();
                Utils.downloadURL(remotePackMD5URL, packMD5File, callback);
            }
            
            if(hasSHA1) {
                packSHA1File.delete();
                Utils.downloadURL(remotePackSHA1URL, packSHA1File, callback);
            }
            
            try(ZipInputStream zipPackIn = new ZipInputStream(remotePackURL.openStream())) {
                //Extract the remote jar into the extract folder
                ZipEntry entry;
                while((entry = zipPackIn.getNextEntry()) != null) {
                    File outFile = new File(extractedPack, entry.getName());
                    outFile.getParentFile().mkdirs();
                    try(FileOutputStream out = new FileOutputStream(outFile)) {
                        int read;
                        byte[] buffer = new byte[Utils.downloadBufferSize];
                        while ((read = zipPackIn.read(buffer)) > 0) {
                            out.write(buffer, 0, read);
                        }
                    }
                }
            } catch (IOException ex) {
                callback.extractException(dependency, remotePackURL, extractedPack, ex);
            }
            
            addToPath(extractedPack.getPath());
        }
        
        return true;
    }
    
    @Override
    public void done(Callback callback, List<DependencyType> dependencyTypes) {
        try {
            switch(hackType) {
                case Sys:
                    sysHackReload();
                    break;        
                case Usr:
                    Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
                    usrPathsField.setAccessible(true);
                    usrPathsField.set(null, Utils.concatenate((String[])usrPathsField.get(null), usrLibrariesPaths.toArray(new String[0])));
            }
        } catch(Exception e) {
            callback.nativeHackError(new NativeHackException(e));
        }
    }
    
    private void addToPath(String libPath) {
        switch(hackType) {
            case Sys:
                sysLibrariesPaths += File.pathSeparator + libPath;
                break;
            case Usr:
                usrLibrariesPaths.add(libPath);
        }
    }
    
    private boolean canUsrHack() {
        try {
            Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);
            String[] paths = (String[])usrPathsField.get(null);
            usrPathsField.set(null, paths);
            return true;
        } catch(Exception ex) {
            return false;
        }    
    }
    
    private void reloadUsr() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);
        usrPathsField.set(null, Utils.concatenate((String[])usrPathsField.get(null), usrLibrariesPaths.toArray(new String[0])));
    }
    
    private boolean canSysHack() {
        try {
            sysHackReload();
            return true;
        } catch(Exception ex) {
            return false;
        }
    }
    
    private void sysHackReload() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
        sysPathsField.setAccessible(true);
        sysPathsField.set(null, null);
    }
    
    
}
