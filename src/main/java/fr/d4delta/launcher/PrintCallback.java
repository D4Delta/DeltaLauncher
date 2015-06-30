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
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * The PrintCallback is basic callback that will send feedback in a given PrintStream.
 * @author d4delta
 */
public class PrintCallback extends Callback {

    final PrintStream err;
    final PrintStream out;
    
    public PrintCallback(PrintStream err, PrintStream out)  {
        this.err = err;
        this.out = out;
    }
    
    public PrintCallback() {
        this(System.err, System.out);
    }
    
    @Override
    public void dependecyUnavailableError(Dependency dependecy) {
        err.println(dependecy + " is unavailable in the repositories.");
    }

    @Override
    public void downloadError(IOException cause, URL originURL, File destination) {
        err.println("Exception while downloading " + originURL + " to " + destination.getAbsolutePath() + ":");
        cause.printStackTrace(err);
    }

    @Override
    public boolean equalVerificationError(URL urlToCompare, File fileToCompare, IOException cause) {
        err.println("Error while comparing " + urlToCompare + " with " + fileToCompare + ":");
        cause.printStackTrace(System.err);
        return false;
    }

    @Override
    public void pomLoadError(Dependency dependecy, Exception cause) {
        err.println(dependecy + " POM file is not a valid xml file:");
        cause.printStackTrace(err);
    }

    @Override
    public void noMainClassNotification() {
        err.println("Main is not defined. Check that you defined the \"delta.launcher.main\" property in your main pom file.");
    }

    @Override
    public void mainClassInvocationError(Exception cause, String mainClass) {
        err.println("Exception while main class invocation : ");
        cause.printStackTrace(err);
    }

    @Override
    public void readyToLaunchNotification(String mainClassPath, Class mainClass, Method main) {
        out.println(mainClassPath + " is ready to roll !");
    }

    @Override
    public void downloadStateNotification(URL origin, File destination, long fileSize, long downloaded) {
        out.println("Downloading " + origin + " to " + destination + " | " + (downloaded / fileSize * 100) + " %");
    }

    @Override
    public void addingRepositoryNotification(String id, String url) {
        out.println("Adding repository : " + id + " > " + url);
    }

    @Override
    public String[] addingPropertyNotification(String key, String value) {
        out.println("Adding property : " + key + " > " + value);
        return super.addingPropertyNotification(key, value);
    }

    @Override
    public void loadingDependecyNotification(Dependency dependecy) {
        out.println("Loading " + dependecy + "...");
    }

    @Override
    public void dependecyNotFoundInLocalRepo(Dependency dependecy) {
        out.println(dependecy + " hasn't been found in the local folder. Fetching from remote...");
    }

    @Override
    public void dependecyJarNotification(Dependency notification) {
        out.println(notification + " is a jar library.");
    }

    @Override
    public void dependecyNativeNotification(Dependency dependecy) {
        out.println(dependecy + "Dependecy is a native.");
    }

    @Override
    public void dependecyChecksumIncorrectNotification(Dependency dependecy, String checkSumType) {
        out.println(dependecy + "checksum (" + checkSumType + ")" + " differ from remote's checksum. Redownloading...");
    }   
}
