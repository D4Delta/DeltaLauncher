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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.jdom2.Element;

/**
 * The jar dependency type will try to download a jar dependecy, and add the jar to the classpath for loading.
 * @author d4delta
 */
public class JarDependencyType implements DependencyType {

    public static final String jarExt = ".jar";
    
    @Override
    public boolean download(Dependency dependecy, Element rootPom, List<URL> loader, Callback callback) {
        
        URL remoteJarURL = null;
        
        URL remoteJarMD5URL = null;
        boolean jarHasMD5 = false;
        
        URL remoteJarSHA1URL = null;
        boolean jarHasSHA1 = false;
        
        try {
            
            remoteJarURL = new URL(dependecy.remoteFolderURL + dependecy.baseString + jarExt);
            
            remoteJarMD5URL = new URL(dependecy.remoteFolderURL + dependecy.baseString + jarExt + Dependency.md5Ext);
            jarHasMD5 = !Utils.is404(remoteJarMD5URL);
            
            remoteJarSHA1URL = new URL(dependecy.remoteFolderURL + dependecy.baseString + jarExt + Dependency.sha1Ext);
            jarHasSHA1 = !Utils.is404(remoteJarSHA1URL);
            
            if(Utils.is404(remoteJarURL))
                return false;
            
        } catch (MalformedURLException ex) {
            return false;
        }
        
        File jar = new File(dependecy.folder, dependecy.baseString + jarExt);
        File jarMD5 = new File(dependecy.folder, dependecy.baseString + jarExt + Dependency.md5Ext);
        File jarSHA1 = new File(dependecy.folder, dependecy.baseString + jarExt + Dependency.sha1Ext);

        if(!jar.exists() || (jarHasMD5 && !Utils.equals(remoteJarMD5URL, jarMD5)) || (jarHasSHA1 && !Utils.equals(remoteJarSHA1URL, jarSHA1))) {
            
            dependecy.folder.mkdirs();
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
            loader.add(jar.toURI().toURL());
        } catch (MalformedURLException ex) {}
        
        return true;
    }
}
