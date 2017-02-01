package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.FillType;
import com.aspose.slides.IShapeCollection;
import com.aspose.slides.LineStyle;
import com.aspose.slides.ShapeType;
import org.reactome.server.tools.diagram.data.layout.Node;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

@SuppressWarnings("ALL")
public class OtherEntity extends PPTXNode {

    private final int shapeType = ShapeType.Rectangle;
    private byte shapeFillType = FillType.Solid;
    private byte lineFillType = FillType.Solid;
    private byte lineStyle = LineStyle.Single;

    public OtherEntity(Node node, Adjustment adjustment) {
        super(node, adjustment);
    }

    @Override
    public void render(IShapeCollection shapes, DiagramProfile profile) {
        render(shapes, shapeType, new Stylesheet(profile.getOtherentity(), shapeFillType, lineFillType, lineStyle));
    }
}
