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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

/**
 * The utils class contains stuff that I use for DeltaLauncher.
 * Just don't use it, because I may delete or change a method.
 * @author d4delta
 */
public class Utils {

    public static void purge(File file) {
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                purge(f);
            }
        }
        file.delete();
    }
    
    public static boolean is404(URL url) {
        
        URLConnection connection;
        InputStream urlIn = null;
        try {
            connection = url.openConnection();
            
            //If the connection is http, let's try if the response code is an error (>= 400)
            if(connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                return httpConnection.getResponseCode() >= 400;
            }
            
            //In this case, the connection is not http, so it could be file://, or an other unkown protocol.
            //we'll just try to open an input stream and read ; if we manage to do this without IOException, then false will be returned.
            //Otherwise, an IOException will be threw up and true will be returned.
            urlIn = connection.getInputStream();
            urlIn.read();
            
        } catch (IOException ex) {
            return true;
        } finally {
            try {
                if(urlIn != null) {
                    urlIn.close();
                }
            } catch(IOException e) {}
        }
        
        return false;
    }
    private static boolean follows(InputStream in, char[] chars) throws IOException {
        for(int i = 0 ; i < chars.length; i++) {
            if(chars[i] != in.read()) {
                return false;
            }
        }
        return true;
    }
    
    static final int downloadBufferSize = 16384;
    public static void downloadURL(URL url, File destination, Callback callback) {
        
        destination.getParentFile().mkdirs();
        try (final FileOutputStream out = new FileOutputStream(destination); final InputStream in = url.openStream()) {
            long totalSize = url.openConnection().getContentLengthLong();
            long downloaded = 0;
            int read;
            byte[] data = new byte[downloadBufferSize];
            while ((read = in.read(data, 0, data.length)) != -1) {
                out.write(data, 0, read);
                downloaded += read;
            }
        } catch(IOException ex) {
            destination.delete();
            callback.downloadError(ex, url, destination);
        }
    }
    
    public static boolean equals(URL url, File file) {
        try(InputStream urlIn = url.openStream(); InputStream fileIn = new FileInputStream(file)) {
            return equals(fileIn, urlIn);
        } catch(IOException ex) {
            return false;
        }
    }
    
    public static boolean equals(InputStream in1, InputStream in2) throws IOException {
        int read1;
        int read2;
        
        while((read1 = in1.read()) + (read2 = in2.read()) > -2 && read1 == read2);
        return read1 == read2;
    }
    
    public static String substituteMaven(String str) {
        return substitute(str, "${", "}");
    }
    
    public static String substitute(String str, String startVar, String endVar) {
        int beginPos;
        int endPos;
        
        while((beginPos = str.indexOf(startVar)) < (endPos = str.indexOf(endVar, beginPos)) && endPos != -1) {
            str = substitute(str, beginPos, endPos, startVar, endVar);
        }
        
        return str;
    }
    
    private static String substitute(String str, int beginPos, int endPos, String startVar, String endVar) {
        int subIndex = str.indexOf(startVar, beginPos+startVar.length());
        if(subIndex != -1 && subIndex < endPos) {
            return substitute(str, subIndex, endPos, startVar, endVar);
        } else {
            String var = str.substring(beginPos, endPos);
            String property = System.getProperty(var.substring(startVar.length()), "");
            return str.replaceAll(Pattern.quote(var + endVar), property);
        }
    }
    
    //From http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java
    public static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen+bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
    
    public static String OS;
    
    static {
        
        String fullOSname = System.getProperty("os.name").toLowerCase();
        
        if(fullOSname.contains("win")) {
            OS = "windows";
        } else if(fullOSname.contains("mac")) {
            OS = "osx";
        } else  if(fullOSname.contains("nux") || fullOSname.contains("nix")  || fullOSname.contains("aix")) {
            OS = "linux";
        }
       
    }
}
