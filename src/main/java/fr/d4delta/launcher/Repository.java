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

/**
 * The repository object contains data about a repository (id and url).
 * @author d4delta
 */
public class Repository {
    
    public final String id;
    public final String url;
    
    public Repository(String id, String url) {
        this.id = id;
        this.url = url;
    }

}
