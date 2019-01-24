# The definite guide of reading files in Java

## 1. When we need a java.io.File object
It is very common to introduce a `java.io.File`  object in our project and in an interview, and more common is the case of reading a text file, because the BufferedReader needs a `java.io.File` object, as stated in its constructor:

    BufferedReader bfr = new BufferedReader(new FileReader(f))
    
. So, the best and most convenient way to do it, is?

We must remember that `java.io.File` is determined by a string-based path. This path has two forms, just same as the files in our file system, both in Windows and in Linux:

 - absolute path (in the form of `/usr/bin` or `C:\\new.csv`, quoted and escaped as in a standard Java string)
 - relative path (in the form of `src/files/new.csv`, without the leading `/`)

The first form is seen everywhere in online tutorials, like that of Mkyong ([How to read file from java BufferedReader](https://www.mkyong.com/java/how-to-read-file-from-java-bufferedreader-example/)), or TutorialPoint ([Java - Files and I/O](https://www.tutorialspoint.com/java/java_files_io.htm)). But, there is a big difference between these two approaches: where to put the file? Or, if we have had some experience in Java programming, we may ask ourselves: is that really reliable in all the kinds of Java project, like, Java EE project organised by Maven? In a WAR? What if the application is deployed as a JAR file and I want to read an internal or external file? 

Okay okay, so let's wait a minute and start from the beginning, or, the most trustful friend, who saved us a million of times: the Javadoc of `java.io.File`: (Java 8)

> User interfaces and operating systems use system-dependent _pathname strings_ to name files and directories. This class presents an abstract, system-independent view of hierarchical pathnames. An _abstract pathname_ has two components:
> 
> 1.  An optional system-dependent _prefix_ string, such as a disk-drive specifier, `"/"` for the UNIX root directory, or `"\\\\"` for a
> Microsoft Windows UNC pathname, and
> 2.  A sequence of zero or more string _names_.
> 
> The first name in an abstract pathname may be a directory name or, in the case of Microsoft Windows UNC pathnames, a hostname. Each subsequent name in an abstract pathname denotes a directory; the last name may denote either a directory or a file. The _empty_ abstract pathname has no prefix and an empty name sequence.
> ...
> A pathname, whether abstract or in string form, may be either _absolute_ or _relative_. An absolute pathname is complete in that no other information is required in order to locate the file that it denotes. A relative pathname, in contrast, must be interpreted in terms of information taken from some other pathname. By default the classes in the `java.io` package always resolve relative pathnames against the current user directory. This directory is named by the system property `user.dir`, and is typically the directory in which the Java virtual machine was invoked.

So, it states clearly that: 
 - If we have an absolute path, it should denote the file without confusion, so it must be a complete path beginning with the root of the file system tree. Actually, if it starts with `/` in Linux, or has the form of `X:\\` in Windows, it will be recognised as absolute.
 - Or, in the opposite case, it starts with a name of a file or a directory, Java will resolve it starting from a proper "root" directory for him. So where would be this "root dir"? In the simplest case, if we have a standard Java project on the disk, it will be where the first level of content of project resides in. If we have this project:

```
Tester
    ├ bin 
    |  └ io
    |     └ westerngun
    |        └ ReadingFiles.class
    ├ files 
    |  └ new.csv
    └ src 
       └ io
          └ westerngun
             └ ReadingFiles.java

```
Then the `bin`, `files` and `src` folders are on the same level, under the "project root". (We assume the project is under `C:\programming\workspace`.

Thus, we have the first solution, not so reliable but the quickest one, specially when you are asked to complete a speedrun style Java programming quiz, or just fiddling your own small test projects: 

### Solution 1: in a plain Java project, put your text files under the "project root" and find them with relative path:
```
File file = new File("files/new.csv"); // in relative path, it works in Windows and Linux
```
Or, 
```
File file = new File("files\\new.csv"); // only valid in Windows
```
Can we use the absolute path? Of course. It is ugly, but it works: 
```
File file = new File("C:\\programming\\workspace\\files\\new.csv"); // in Windows only double backslashes works in absolute path
```
Or, 
```
File file = new File("/home/documents/new.csv");
// in Linux, absolute paths begin with "/".
```

(People call `\` a "backslash", but I name it "right hand slash", because when you clasp your hands, to make the fingertips meet and the palm separated to form a small pyramid, it is the right half; so the Linux path separator for me is "left hand slash". Quite graphical, isn't it?) 

> Note: you must use double backslashes in Windows in absolute path and relative path, or you **can** use single slash in a relative path, even in Windows, as I did above. 
But, never use one `\` to separate path, like <s>`files\new.csv`</s>! It is valid Java string, but will never find a file!!! Or slash, or double backslashes in Windows. In Linux, only slash is valid. 


| &nbsp; |  Windows | Linux |
| - | - | - |
| absolute |  `\\` | `/` (and begin with `/`) |
| relative   | `/` or `\\` | `/` (and not begin with `/`) | 

Conclusion: use Linux. (not a joke)

> Note that we haven't mention in solution 1 the word "classpath", because `java.io.File` loading `File` has nothing to do with classpath settings. 


Solution 1 has some drawbacks:
 - Absolute path is not portable: it changes according to the environment; what if the dev environment is Windows and the production environment is a Linux machine? Or other OS, like Solaris? Also, how do you find the project root in the code? What about net drive paths?
 - Relative path will not work with JAR files, even when you package them into the jar file. __It is because `java.io.File` will not resolve the path within the JAR; so, you have to move the file out of the jar and put it at the same level of the jar.__ Even though, we cannot ensure the file would be found: if you launch the application with a script and from a different location, the file root will be where you run the script, which is not necessarily where the file is stored. _Program variable `-Duser.dir` will not help in this case_; it only ensures Java can find the Main class and has nothing to do with the `File` directory root resolution. 

A very simple example: 
```sh
> pwd
/root
> touch test.txt
> vi Main.java
```
And the class is like:
```java
import java.io.File;

class Main {
    public static void main(String[] args) {
        File file = new File("test.txt");
        System.out.println(file.exists());
    }
}
```
We can test like:
```bash
> javac Main.java
> java Main
true
> cd /opt
> java -Duser.dir=/root Main
false
```
This is because `test.txt` is absent under `/opt`. The java class is executed, so we get a output; that means the Main class is found, while the file is not.

So, or we just specify every time the absolute path(hard-coded or in properties file as you like, but will never work in JAR), or we just change to solution 2: _load resources with relative path resolved from the class itself as `InputStream`, not as `File` due to the limitation we have mentioned above_.

## 2. When we need to read the content of any resources(text file, images, binary files, etc.)
But, in real life we don't always need a `File` object. Think about it: from the definition of `java.io.File` in Java references and Javadocs, we can say a `File` is a resource described by a string which represents an absolute or relative path, it can exist or not. The `File` is like a pointer, a variable; at any moment before checking its existence, we have no guarantee if the "file pointer" has some content or not. And, due to its limitation mentioned above, i.e, reading a file is only tangible if the project has the structure of a plain Java project, not with a JAR neither a WAR file, I would say that the class was designed more from the angle of file system, not from the angle of Java, thus, not very portable.

Thus, when we want to access some "resources", we usually use another approach, which we will explain in detail in the following part. This approach is more widely adopted because it works in:
 - plain command line Java program
 - Java projects created in IDE
 - Maven project(thus Gradle projects as well, for they have identical structure)
 - Java project packaged as JAR/WAR

Let's see an example. Assume we have this project structured just like above: (**TODO**: create the project in Github)
```
Tester
    ├ bin 
    |  └ io
    |     └ westerngun
    |            └ ReadingFiles.class
    ├ files 
    |  └ new.csv
    └ src 
       └ io
          └ westerngun
                 └ ReadingFiles.java
```
I have my `new.csv` file like:
```
Band, Song, Year
Toto, Africa, 1982
Starship, Nothing gonna stop us now, 1987
George Michael, Careless whisper, 1984
```
And to read the `new.csv`, we can use code like this: 
(First we have to add the resource folder (`file`) to classpath. The way to do it is slightly different in Eclipse and in Intellij, but the final effect is the same: adding all files under the folder to classpath.
- Eclipse: right click on the project - "Properties" - "Java build path" - "Add folder" - add "file" folder
- IntelliJ: right click on the "file" - "mark directory as..." - "source root"(older version)/"resource root"(new version)")
```java
package io.westerngun;

import java.io.InputStream;

public class ReadingResources {
    public static void main(String[] args) {
        try {
            InputStream in = ReadingResources.class.getResourceAsStream("/new.csv");
            if (in != null) {
                System.out.println(in.available());  // available() returns estimated number of characters in the file loaded
                InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
                int i;
                while ((i = reader.read()) != -1) {
                    char letter = (char)i;
                    System.out.print(letter);
                }
                reader.close();
                in.close();
            } else {
                System.out.println("Not available");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
The output:
```
119
Band, Song, Year
Toto, Africa, 1982
Starship, Nothing gonna stop us now, 1987
George Michael, Careless whisper, 1984
```
That is to say, the resource is loaded and read, and we managed to get the content of file as String/char.

We can also go with "try-with-resources" style:



> **Note**: here we use `ClassName.class.getResourceAsStream()` because we use it in a static method; normally we use `this.getClass().getResourcesAsStream()` to make it even more portable.

You may have seen another very similar but different method: `ClassLoader.getResource(AsStream())`. What is the difference and where can it be applied? 

I have written a complete answer [here](https://stackoverflow.com/questions/16374235/resources-and-config-loading-in-maven-project/46442105#46442105). Let me just copy it here:


> **You can use `getResourceAsStream()` method of `java.lang.Class` as you have done, but you have to add `/` before the path.**
> 
> 
> ----------
> 
> 
> This question is tricky.
> 
> ### 1. Two methods with same name
> 
> First of all, exist two methods **of same name and same signature** in these two classes:
> 
>     java.lang.Class
>     java.lang.ClassLoader
> 
> They have the same name: `getResource(String)` (and `getResourceAsStream(String)` is alike).
> 
> ### 2. They accept params of different format
> 
> Then, the param of them has different format:
> 
>  - The method `java.lang.Class.getResouce<asStream>()` accepts path with and without the leading `/`, resulting in different resources searching strategies. If a path has no `/`, Java will search the resource in the package/folder where the `.class` file resides. If it has `/`, Java will begin the searching from classpath root.
>  - The method `java.lang.ClassLoader.getResource<asStream>()` accepts only path without `/`, because it always search from classpath. In a classpath based path, `/` is not a valid character. \*
> 
>     <sup>*: As this answer states: https://stackoverflow.com/questions/3803326/this-getclass-getclassloader-getresource-and-nullpointerexception/7098501#7098501</sup>
> 
> 
> How to add a folder to classpath? In Eclipse we resolve to the context menu of a project: "Build path" - "Configure build path..." and add some folder to build path.
> 
> ### 3. When it comes to Maven
> 
> At last, if a project is a Maven project, by default `src/main/resources` is in the classpath, so we can use 
>     
>     Class.getResource("/path-to-your-res");
> 
> or, 
> 
>     ClassLoader.getResource("path-to-your-res");
> 
> , to load anything under `src/main/resources`.
> 
> If we want to add another resources folder, as you have mentioned, it is done in `pom.xml`. **And they are added into classpath as well, done by Maven. No extra config is needed.**
> 
> ### 4. Example
> 
> For example, if your `config.ini` is under `src/main/resources/settings`, `myAvatar.gif` under `src/main/images`, you can do:
> 
> In `pom.xml`:
> 
>     <build>
>         <resources>
>             <resource>
>                 <directory>src/main/images</directory>
>             </resource>
>         </resources>
>     </build>
> 
> In code:
> 
>     URL urlConfig = MyClass.class.getResource("/settings/config.ini"); //by default "src/main/resources/" is in classpath and no config needs to be changed.
>     InputStream inputAvatar = MyClass.class.getResourceAsStream("/myAvatar.gif"); //with changes in pom.xml now "src/main/images" is counted as resource folder, and added to classpath. So we use it directly.
> 
> We must use `/` above.
> 
> Or, with ClassLoader:
> 
>     URL urlConfig = MyClass.class.getClassLoader().getResource("settings/config.ini"); //no leading "/"!!!
>     InputStream inputAvatar = MyClass.class.getClassLoader().getResourceAsStream("myAvatar.gif"); //no leading "/"!!!
> 
>     

Like I have said, this can be divided into several parts:
 - difference between `ClassLoader.getXxx()` and `Class.getXxx()` methods are different in their signatures: the former one, with/without `/`(normally with `/`), and the latter: never with `/`.
 - When it comes to a Maven project(Gradle project applies too), `resources`  should be marked as resources root(IntelliJ)/added to source folder(Eclipse), to list the content under classpath, and then be used in code without any parent folder as prefix, with or without leading `/` depending on which method you use.
 - And, to include another folder as resources in Maven, we can configure as mentioned above in the answer. **Configuration only done at IDE level will not guarantee to work with JAR/WAR**. 
 - This works for JAR. Just generate a runnable JAR file, and go into the folder to run with `java -jar <jar_name>`. The result will be `119`. If we open the JAR with WinRAR or just unzip it, we can see the `new.csv` is packaged at the highest level of the JAR, along with package `io` and `META-INF`. **This also reminds us that we must pay attention to the resource's name; because if two files with same name and distinct content are listed in the classpath, they cannot coexist and only one will be chosen; and this one will be packaged into JAR. Which one to use is unknown. ** We can test like this: create another folder, mark as resource, copy the `new.csv` to another folder, change the content, and pack as a JAR. You may see unexpected result.





