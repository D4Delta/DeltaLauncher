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

import java.util.List;
import org.jdom2.Element;

/**
 * A dependencyType is a class that will try to download a dependency.
 * For example, the jar dependency type will try to download the dependency if it's a jar.
 * @author d4delta
 */
public abstract class DependencyType {
    /**
     * This method will try to use the DependencyType for a dependency.
     * @param dependency The dependency you should test & download.
     * @param rootPom The root element of the dependency's xml
     * @param callback The callback of the launcher
     * @return If the dependency belong to this type
     */
    public abstract boolean download(Dependency dependency, Element rootPom, Callback callback);
    
    /**
     * This method will be called when there is no more dependency to download.
     * @param callback The callback of the launcher.
     * @param dependencyTypes The dependency types of the launcher, to interact with them for example. 
     */
    public void done(Callback callback, List<DependencyType> dependencyTypes) {};
}
