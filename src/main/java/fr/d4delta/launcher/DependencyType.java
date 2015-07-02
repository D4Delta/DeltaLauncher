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

import java.net.URL;
import java.util.List;
import org.jdom2.Element;

/**
 * A dependencyType is a class that will try to download a dependency.
 * For example, the jar dependency type will try to download the dependency if it's a jar.
 * @author d4delta
 */
public interface DependencyType {
    
    public boolean download(Dependency dependency, Element rootPom, List<URL> loader, Callback callback);

}
