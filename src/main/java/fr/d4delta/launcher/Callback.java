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
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * The callback interface is used by DeltaLauncher to send feedback to the main program.
 * You may implements your own callback directly from this class, 
 * however I recommend to extend PrintCallback instead, because you
 * don't need to implement all the methods in 99% of the case
 * @author d4delta
 */
public interface Callback {
    
    //ERRORS
    
    public void dependecyUnavailableError(Dependency dependecy);
    
    public void downloadError(IOException cause, URL originURL, File destination);
   
    public boolean equalVerificationError(URL urlToCompare, File fileToCompare, IOException cause);
    
    public void pomLoadError(Dependency dependecy, Exception cause);
    
    public void mainClassInvocationError(Exception cause, String mainClass);
    
    //NOTIFICATIONS
    
    public void noMainClassNotification();
    
    public void readyToLaunchNotification(String mainClassPath, Class mainClass, Method main);
    
    public void downloadStateNotification(URL origin, File destination, long fileSize, long downloaded);
    
    public void addingRepositoryNotification(String id, String url);
    
    public String[] addingPropertyNotification(String key, String value);
    
    public void loadingDependecyNotification(Dependency dependecy);
    
    public void dependecyNotFoundInLocalRepo(Dependency dependecy);
    
    public void dependecyJarNotification(Dependency notification);
    
    public void dependecyNativeNotification(Dependency dependecy);
    
    public void dependecyChecksumIncorrectNotification(Dependency dependecy, String checkSumType);
}
