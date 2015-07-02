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
    public void dependencyUnavailableError(Dependency dependency) {
        err.println(dependency + " is unavailable in the repositories.");
    }

    @Override
    public void downloadError(IOException cause, URL originURL, File destination) {
        err.println("Exception while downloading " + originURL + " to " + destination.getAbsolutePath() + ":");
        cause.printStackTrace(err);
    }

    @Override
    public void pomLoadError(Dependency dependency, Exception cause) {
        err.println(dependency + " POM file is not a valid xml file:");
        cause.printStackTrace(err);
    }

    @Override
    public void noMainClassError() {
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
    public void loadingDependencyNotification(Dependency dependency) {
        out.println("Loading " + dependency + "...");
    }
    
    @Override
    public void dependencyJarNotification(Dependency notification) {
        out.println(notification + " is a jar library.");
    }

    @Override
    public void dependencyNativeNotification(Dependency dependency) {
        out.println(dependency + "dependency is a native.");
    }
}
