# Setup your launcher

####Create the project

Create a new maven project, and then edit your pom.xml to add my repository, and the DeltaLauncher dependency.

```
<project>
...
	<repositories>
		<repository>
			<id>delta-repo</id>
			<url>http://deltarepo.olympe.in/</url>
		</repository>
	</repositories>

	<dependencies>
		<dependency>
			<groupId>fr.d4delta</groupId>
			<artifactId>DeltaLauncher</artifactId>
			<version>1.0.0</version>
		</dependency>
	</dependencies>
</project>
```
Clean and build the project, and you should see DeltaLauncher in the list of the dependecies.

#### Creating the launcher
Your launcher can contains all you wants : 3d stuff, a web page, or even nothing at all : It just had to create a new Launcher instance, and launch it using the `run()` method if you want to launch it on the same thread, or the `start()` method if you need to launch it on a separate thread.

To have feedback about what DeltaLauncher is doing, implements your own Callback. For example this callback will show up a dialog when the update process is done:
```java
public class ExampleCallback extends Callback {
	@Override
	public void readyToLaunchNotification(String mainClassPath, Class mainClass, Method main) {
		JOptionPane.showMessageDialog(null, "The update is finished, the program will now launch.");	
	}
}
```
For example, your launcher might look like this:
```java
public class Launcher {
	public static void main(String[] args) {
		Launcher deltalauncher = new Launcher(new File("YourAppName"), new URL("http://yourftp.com/mainpom.xml").openStream(), new ExampleCallback()); 
		deltalauncher.run();
	}
}
```

You now got a working launcher, that you can give to your users. Well not for the moment because your application is not yet deployed, but we'll cover that in next section.

#### Deploying your  application

To update your application, your have to create your own repository and deploy your maven application on it.

First you have to get a ftp. There is a lot of free ftp on internet, just search for the one that fit you.

Now you have to deploy your application on this ftp.
Just edit your application's pom.xml, and add your ftp server:

```xml
<project>
	...
	<!-->This will add your server<-->
	<distributionManagement>
		<repository>
			<id>YourId</id>
			<url>ftp://yourftp.com</url>
		</repository>
	</distributionManagement>

	<!-->This will enable the ftp support on your dependecy.<-->
	<build>
		<extensions>
			<extension>
				<groupId>org.apache.maven.wagon</groupId>
				<artifactId>wagon-ftp</artifactId>
				<version>1.0-beta-6</version>
			</extension>
		</extensions>
	</build>
</project>	
```

After you done this you have to (create and) edit your settings.xml, to add your password and username to your ftp.
The settings.xml is located on `C:\Users\You\.m2\settings.xml` on windows, and in `/home/you/.m2/settings.xml` on linux.

After your located and created settings.xml if it doesn't exists, add these lines:

```xml
<settings>
	<servers>
		<server>
			<id>YourId</id>
			<username>ftpUsername</username>
			<password>ftpPassword</password>
		</server>
	</servers>
</settings>
```
Your application is ready to be deployed. Use `mvn deploy` to deploy it, or follow [this instructions](http://maxrohde.com/2013/02/11/deploy-maven-project-with-netbeans/) to deploy it using netbeans.

Congratulations ! You now have your own maven repository. Now we have to create the main pom.

####Creating the main pom

First, what is the main pom ? The main pom is a xml file that contains the data needed by delta launcher to download your application : Your repository, and your application's groupId and artifactId. I recommend uploading this file on a ftp (it can be your repository ftp), but if you want, you can directly add it to your project, and load it using ```getResourceAsStream("mainpom.xml")```.

But let's create that pom :

```xml
<project>
	<repositories>
		<repository>
			<id>YourId</id>
			<url>ftp://yourftp.com</url>
		</repository>
	</repositories>

	<properties>
		<!--> The java path to the main class.<-->
		<delta.launcher.main>your.package.yourapplication.YourMainClass</delta.launcher.main>
	</properties>

	<!-->This should contains one dependency : your application <-->
	<dependencies>
		<dependency>
			<groupId>your.package</groupId>
			<artifactId>yourapplication</artifactId>
			<version>yourversion</version>
		</dependency>
	</dependencies>
</project>
```
Important : This pom should contains **one** dependency : your application.
Note that ```<delta.launcher.main>``` could be in your application's pom properties.

After your done this file, you should upload it on your ftp and give the link to the `Launcher` instance, like on the Launcher example:

```java
public class Launcher {
	public static void main(String[] args) {
		Launcher deltalauncher = new Launcher(new File("YourAppName"), new URL("http://yourftp.com/mainpom.xml").openStream(), new ExampleCallback()); 
		deltalauncher.run();
	}
}
```
(Replace "http://yourftp.com/mainpom.xml" by your main pom link)

Your launcher should now work without problem: If you redeploy your application and if there is change in the newly deployed application,  the users will download the new version when the launcher start. (Awesome isn't it ?). 

Also, note that not only your application is updated : All your application's dependencies are also updated. It's means if there is a major security leak in one of your dependencies, the client will download the updated version automatically.

#### Contact
You can send feedback to [d4delta@outlook.fr](d4delta@outlook.fr)
