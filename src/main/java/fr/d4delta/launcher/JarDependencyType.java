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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;

/**
 * The jar dependency type will try to download a jar dependency, and add the jar to the classpath for loading.
 * @author d4delta
 */
public class JarDependencyType extends DependencyType {
    
    public final String[] args;
    
    public JarDependencyType(String[] args) {
        this.args = args;
    }
    
    public static final String jarExt = ".jar";
    
    public List<URL> jarToLoad = new LinkedList<>();
    
    @Override
    public boolean download(Dependency dependency, Element rootPom, Callback callback) {
        
        URL remoteJarURL = null;
        
        URL remoteJarMD5URL = null;
        boolean jarHasMD5 = false;
        
        URL remoteJarSHA1URL = null;
        boolean jarHasSHA1 = false;
        
        try {
            
            remoteJarURL = new URL(dependency.remoteFolderURL + dependency.baseString + jarExt);
            
            remoteJarMD5URL = new URL(dependency.remoteFolderURL + dependency.baseString + jarExt + Dependency.md5Ext);
            jarHasMD5 = !Utils.is404(remoteJarMD5URL);
            
            remoteJarSHA1URL = new URL(dependency.remoteFolderURL + dependency.baseString + jarExt + Dependency.sha1Ext);
            jarHasSHA1 = !Utils.is404(remoteJarSHA1URL);
            
            if(Utils.is404(remoteJarURL))
                return false;
            
        } catch (MalformedURLException ex) {
            return false;
        }
        
        File jar = new File(dependency.folder, dependency.baseString + jarExt);
        File jarMD5 = new File(dependency.folder, dependency.baseString + jarExt + Dependency.md5Ext);
        File jarSHA1 = new File(dependency.folder, dependency.baseString + jarExt + Dependency.sha1Ext);
        
        callback.dependencyJarNotification(dependency);
        
        if(!jar.exists() || (jarHasMD5 && !Utils.equals(remoteJarMD5URL, jarMD5)) || (jarHasSHA1 && !Utils.equals(remoteJarSHA1URL, jarSHA1))) {
            
            jar.delete();
            Utils.downloadURL(remoteJarURL, jar, callback);
            
            if(jarHasMD5) {
                jarMD5.delete();
                Utils.downloadURL(remoteJarMD5URL, jarMD5, callback);
            }
            
            if(jarHasSHA1) {
                jarSHA1.delete();
                Utils.downloadURL(remoteJarSHA1URL, jarSHA1, callback);
            }
            
            
        }
        
        try {
            jarToLoad.add(jar.toURI().toURL());
        } catch (MalformedURLException ex) {}
        
        return true;
    }

    @Override
    public void done(Callback callback, List<DependencyType> dependenciesTypes) {
        URLClassLoader loader = new URLClassLoader(jarToLoad.toArray(new URL[jarToLoad.size()]));
        String mainClassPath = System.getProperty("delta.launcher.main");
        if(mainClassPath == null) {
            callback.noMainClassError();
        } else {
            try {
                Class mainClass = Class.forName(mainClassPath, true, loader);
                Method main = mainClass.getMethod("main", String[].class);
                callback.readyToLaunchNotification(mainClassPath, mainClass, main);
                main.invoke(null, (Object) args);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                callback.mainClassInvocationError(ex, mainClassPath);
            } 
        }
    }
}
