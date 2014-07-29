CYBORG
======

Speech Alignment Engine

Refer the manual to use the Speech Alignment Engine

Cyborg 1.3

Required installations

Windows
=======
1. Java version 1.7.0 or above
   
  Installation Guide:
  http://www.java.com/en/download/help/windows_offline_download.xml 
	
2. OpenFST version 1.3.1 or above
  
  Download from here : http://www.openfst.org/twiki/bin/view/Contrib/OpenFstWin
  
  Installation Guide :
  
  I. OpenFst package for windows comes with compiled binaries. Download OpenFst binaries from above 
  specified link and add this binaries to windows path variable.
  
  Directory name of the compiled binaries : ./Release	
  
  II. Make sure that OpenFst libraries has been added correctly to path variable by executing some of the commands.
      eg: fstconnect
      	  fstcompile  
      
      If these above commands not throwing any error or output on screen, you have successfully added binaries to path variable.

  Note: If you are getting some dll missing error eg: msvcr100.dll, install latest version of "Microsoft Visual C++ Redistributable 
  Package" from following link
  
  http://answers.microsoft.com/en-us/windows/forum/windows_7-windows_programs/trying-to-open-computer-management-the-program/5c9d301a-2191-4edb-916e-5e4958558090      

Linux
=====
1. Java version 1.7.0 or above

  Installation Guide:
  http://www.java.com/en/download/help/linux_install.xml
  
  To upgrade Java in Linux:
  http://www.liberiangeek.net/2012/04/install-oracle-java-runtime-jre-7-in-ubuntu-12-04-precise-pangolin/

2. OpenFST version 1.3.3 or above

  Download from here : http://www.openfst.org/twiki/bin/view/FST/FstDownload
  
  Installation Guide:
  The simplest way to compile this package is:

  I. `cd' to the directory containing the package's source code and type
     `./configure' to configure the package for your system.

     Running `configure' might take a while.  While running, it prints
     some messages telling which features it is checking for.

  II. Type `make' to compile the package.

  III. Optionally, type `make check' to run any self-tests that come with
     the package.

  IV. Type `make install' to install the programs and any data files and
     documentation.

  V. You can remove the program binaries and object files from the
     source code directory by typing `make clean'.

  VI. Often, you can also type `make uninstall' to remove the installed
     files again.
	
  Note: If you installed openfst on non-standard path, or installed it to the default /usr/local/lib, but your variant of Linux 
  doesn't support libraries there, you may need to set it up and/or ldconfig the directory explicitly

	#export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:/usr/local/lib
	#ldconfig -v -n /usr/local/lib

  If all else fails, try installing openfst to wherever your blend of Linux installs all its libraries, such as /usr/lib.

For more help refer : http://sourceforge.net/p/hfst/wiki/README/

Download and usage of Cyborg force aligner
==========================================

Download Cyborg sphinx aligner from here : http://home.iitb.ac.in/~nicool/cyborg-1.1.jar 

Command to run cyborg FA in Linux and Windows :
$ java -jar cyborg-1.0.jar <compalsory and optional arguments>

Example usage:
$ java -jar cyborg-1.0.jar -models modelDir -ctl ./fileIDs.txt -in wavFilesDir -cepdir featDir -t ./trans.txt -dict dictionary.dic

Arguments list definition for alignment:
[NAME]  [DEFLT] [DESCR]
-models         Input model files directory
-ctl            Control file listing utterances to be processed
-t              Input transcriptions file corresponds to audio
-dict           Main pronunciation dictionary (lexicon) input file
-in     .       Input audio wav file(s) directory
-cepdir .       Input cepstrum files directory
-triphn yes     Determines whether to use triphone search or monophone search
-phseg  .       Output directory for phone segmentation files
-wdseg  .       Output directory for word segmentation files

Helper arguments
[NAME]          [DESCR]
-version        Shows the current version.
-help           Shows the help on usage.
