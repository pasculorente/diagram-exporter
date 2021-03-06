[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# Image exporter
Exports diagrams as images. Current supported formats are:

* png (transparent background)
* jpg, jpeg, gif (white background)
* svg

Diagrams are generated using the diagram source files, so you may notice small differences with Pathway Browser. With the raster exporter you can generate High Definition images up to 100 megapixels.
When an EHLD source is available
Images are generated from the EHLD if it is available.

## Exporting a diagram

### Basic usage
```java
// This path must contain the layout and graph json files.
// You can download them from https://reactome.org/download/current/diagram/
String diagramPath = "path/to/diagram";
// This path must contain the EHLD svg file
// You can download them from https://reactome.org/download/current/ehld/
// You will also find a file containing a list of available EHLD: https://reactome.org/download/current/ehld/svgsummary.txt
String ehldPath = "path/to/ehld";

final RasterArgs args = new RasterArgs("R-HSA-169911", "jpg");
args.setFactor(3.);
args.setProfiles(new ColorProfiles("standard", null, null));

final BufferedImage image = RasterExporter.export(args, diagramPath, ehldPath);

// If saving to a file
final File file = new File(args.getStId() + "." + args.getFormat());
ImageIO.write(image, args.getFormat(), file);

// If sending through an URL
URL url = new URL("...");
HttpUrlConnection connection = (HttpUrlConnection) url.openConnection();
connection.setDoOutput(true);  // your url must support writing
ImageIO.write(image, args.getFormat(), connection.getOutputStream());    

```

### Decorators
Raster exporter supports decoration: selection and flags.

Selected items are highlighted in blue. If the selected item is a reaction, its participants are haloed in yellow. If a node is selected, the reactions where it participates and the nodes that participate in those reactions are also haloed.

Flag items are highlighted with a pink border. When a molecule is flagged, the nodes that represent that molecule and the complexes and sets that contain the molecule are flagged.

You can specify dbIds, stIds, interactors or geneNames, or a mix of them.

```java
args.setSelected(Arrays.asList("1234", "R-HSA-1234"));
args.setFlags(Arrays.asList("PTEN"));
```

### Analysis
Reactome offers a pathway analysis service that supports enrichment and expression analysis. The diagram exporter allows you to overlay the results of the analysis on top of the exported diagrams. To do so, use the token argument to specify the unique token associated with the performed analysis. To learn more about our Analysis Service and how to use it have a look to this [page](https://reactomere.org/dev/analysis).
```java
args.setToken("MjAxNzExMDYxMDQ3MzBfMzA%253D");
```
Diagram exporter automatically detects which type of analysis you run and properly mimics PathwayBrowser appearance.

### Animated GIFs
The expression analysis contains timeseries data. It is not possible to see all the timeseries in 1 image. If want to obtain and animated image with a timeserie per frame, use the *exportToGif* method.
```java
String stId = "R-HSA-1234";
File file = new File(stId + ".gif");
OutputStream os = new FileOutputStream(file);
RasterRenderer.renderToGif(stId, diagramPath, ehldPath, os);
os.close();
```
