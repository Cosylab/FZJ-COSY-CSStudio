# FZJ-COSY CSStudio
Control System Studio for FZJ COSY

To be able to build it the [cs-studio](https://github.com/ControlSystemStudio/cs-studio) plugins need to be available. They are located on the [css p2 update site](http://download.controlsystemstudio.org/updates/4.3). Alternatively they can be installed locally using maven tycho (consult the CS-Studio [docbook](http://cs-studio.sourceforge.net/docbook/) for details on how to do that).

After the above requirements are met, you can proceed to build the FZJ-COSY distribution. The build machine requires to have Java 1.8 update 40 or later installed. [Maven](https://maven.apache.org/) build tool is also required.

To build FZJ-COSY CS-Studio checkout this project and run the **mvn clean verify** command in the root of the project. This will compile all features and plugins in the FZJ-COSY repository. After that it will package all products defined in the **repository** folder. The final packages will be available in **repository/target/products/** where a zip or a tarball will be created for each product for all selected configurations. Currently only 64-bit Windows and Linux products are enabled. Others (win 32-bit, linux-gtk 32-bit, macos) can be enabled by modifying the pom.xml in the root of this repository; look for the *environments* segment and uncomment the desired platform.
