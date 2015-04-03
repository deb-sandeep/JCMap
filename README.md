# JCMap
###### An implementation of scripted Concept Map editor in Java

A concept map is a type of graphic organizer used to help students organize and represent knowledge of a subject. Concept maps begin with a main idea (or concept) and then branch out to show how that main idea can be broken down into specific topics.

http://en.wikipedia.org/wiki/Concept_map

- - -
JCMap can be used both as a visual scripting editor and as an embedded library to produce visual concept maps. The syntax of concept mapping is quite simple, but lends itself to embrace a rich visual representation using graphviz rendering attributes.

Below is an example of the concept mapping the answer to the question "What is an Autotroph?"

````
Autotrophs >are> Living Organisms ;
Autotrophs >can make> Complex Organic Compounds ;
Complex Organic Compounds  >such as> "<l>*Carbohydrades *Proteins *Fats</l>" ;
Complex Organic Compounds  >from> Simple substances ;
Simple substances  >by using> Light, Inorganic chemical reactions ;
Light >a.k.a> Photosynthesis ;
Inorganic chemical reactions >a.k.a> Chemosynthesis ;
````

![What is an Autotroph?](/docs/sample-cmap.png?raw=true)

## Installation
1. Build using maven - $mvn package. This will generate an assembly in the target directory (jcmap-*-bin.tgz/zip)
2. Install Graphviz
3. Untar the jcmap assembly
4. Modify the config/config.properties
5. Execute the script jcmap or jcmap.bat
 
## Embedded usage
I will wrap this in a nice API in some time. Till then, you can embed it using the following steps.

```java
// 1. Convert the JCMap script to the DOT equivalent
CMapBuilder cmapUtil = new CMapBuilder() ;
CMapElement cmap = cmapUtil.buildCMapElement( rawCMapScript ) ;
CMapDotSerializer cmapSerializer = new CMapDotSerializer( cmap ) ;
String dotFileContent = cmapSerializer.convertCMaptoDot() ;

// 2. Save the DOT contents to a file
FileUtils.writeStringToFile( dotFile, dotFileContent, "UTF-8" ) ;

// 3. Use the GraphvizAdapter to transform the DOT content to an image.
//    dotExecPath is the path to the dot executable.
GraphvizAdapter gvAdapter = new GraphvizAdapter( dotExecPath ) ;
gvAdapter.generateGraph( dotFile, imgFile ) ;
````

## Acknowledgements
1. Andreas Tetzl - for his ImageMap program (http://www.tetzl.de/jimagemap.html)



