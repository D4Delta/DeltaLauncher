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
import java.lang.reflect.Method;
import java.net.URL;

/**
 * The callback class is used by DeltaLauncher to send feedback to the main program.
 * It act like a "bridge" between DeltaLauncher and your gui.
 * @author d4delta
 */
public class Callback {
    
    //ERRORS
    
    public void dependencyUnavailableError(Dependency dependency) {
        //The program will continue to update even if one dependency is unavailable. To stop the program, just throw a new RuntimeException.
    };
    
    public void downloadError(IOException cause, URL originURL, File destination) {
        //The program could continue to run, but in the most of the cases it lead to an other exception, So by default the program stop.
        throw new RuntimeException(cause);
    };
    
    public void pomLoadError(Dependency dependency, Exception cause) {
        //The program will continue to update even if the pom is an incorrect xml file. To stop the program just throw a new RuntimeException.
    };
    
    public void extractException(Dependency dependency, URL remotePackURL, File extractFolder, IOException cause) {                                                           
        //The program will continue to run and clean the extracted                                                                                                                                                                                                                                                                                                                                                                  
    }
    
    public void mainClassInvocationError(Exception cause, String mainClass) {};
    
    public void noMainClassError() {};
    
    public void nativeHackError(NativeHackException exception) {}
    
    //NOTIFICATIONS
    
    public void readyToLaunchNotification(String mainClassPath, Class mainClass, Method main) {};
    
    public void downloadStateNotification(URL origin, File destination, long fileSize, long downloaded) {};
    
    public void addingRepositoryNotification(String id, String url) {};
    
    public String[] addingPropertyNotification(String key, String value) {return new String[] {key, value};};
    
    public void loadingDependencyNotification(Dependency dependency) {};
    
    public void dependencyJarNotification(Dependency notification) {};
    
    public void dependencyNativeNotification(Dependency dependency) {};
}
