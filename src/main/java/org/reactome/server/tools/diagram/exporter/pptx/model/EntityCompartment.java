package org.reactome.server.tools.diagram.exporter.pptx.model;

import com.aspose.slides.*;
import org.reactome.server.tools.diagram.data.layout.Bound;
import org.reactome.server.tools.diagram.data.layout.Compartment;
import org.reactome.server.tools.diagram.data.layout.Coordinate;
import org.reactome.server.tools.diagram.data.layout.NodeProperties;
import org.reactome.server.tools.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.server.tools.diagram.exporter.common.profiles.model.DiagramProfile;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class EntityCompartment {

    private IGroupShape iGroupShape;
    private float x;
    private float y;
    private float width = 1;
    private float height = 1;
    private String displayName;
    private Coordinate textPosition;
    private Bound insets = null;
    private Stylesheet stylesheet;
    private Adjustment adjustment;

    public EntityCompartment(Compartment compartment, DiagramProfile profile, Adjustment adjustment) {
        NodeProperties nodeProperties = NodePropertiesFactory.transform(compartment.getProp(), adjustment.getFactor(), adjustment.getCoordinate());
        this.x = nodeProperties.getX().floatValue();
        this.y = nodeProperties.getY().floatValue();
        this.width = nodeProperties.getWidth().floatValue();
        this.height = nodeProperties.getHeight().floatValue();
        this.displayName = compartment.getDisplayName();
        this.textPosition = compartment.getTextPosition();
        this.insets = compartment.getInsets();
        stylesheet = new Stylesheet(profile.getCompartment(), FillType.Solid, FillType.Solid, LineStyle.Single);
        this.adjustment = adjustment;
    }

    public void render(IShapeCollection shapes) {
        iGroupShape = shapes.addGroupShape();
        IAutoShape iAutoShape = iGroupShape.getShapes().addAutoShape(ShapeType.RoundCornerRectangle, x, y, width, height);
        ILineFormat lineFormat = iAutoShape.getLineFormat();
        lineFormat.setWidth(stylesheet.getLineWidth());
        lineFormat.setStyle(stylesheet.getLineStyle());
        lineFormat.getFillFormat().setFillType(stylesheet.getLineFillType());
        lineFormat.getFillFormat().getSolidFillColor().setColor(stylesheet.getLineColor());
        IFillFormat fillFormat = iAutoShape.getFillFormat();
        fillFormat.setFillType(stylesheet.getShapeFillType());
        fillFormat.getSolidFillColor().setColor(stylesheet.getFillColor());

        if (insets != null) {
            NodeProperties np = NodePropertiesFactory.transform(NodePropertiesFactory.get(insets.getX().floatValue(), insets.getY().floatValue(), insets.getWidth().floatValue(), insets.getHeight().floatValue()), adjustment.getFactor(), adjustment.getCoordinate());
            IAutoShape insetAutoShape = iGroupShape.getShapes().addAutoShape(ShapeType.RoundCornerRectangle, np.getX().floatValue(), np.getY().floatValue(), np.getWidth().floatValue(), np.getHeight().floatValue());
            ILineFormat insetLineFormat = insetAutoShape.getLineFormat();
            insetLineFormat.setWidth(stylesheet.getLineWidth());
            insetLineFormat.setStyle(stylesheet.getLineStyle());
            insetLineFormat.getFillFormat().setFillType(stylesheet.getLineFillType());
            insetLineFormat.getFillFormat().getSolidFillColor().setColor(stylesheet.getLineColor());
            IFillFormat insetFillFormat = insetAutoShape.getFillFormat();
            insetFillFormat.setFillType(stylesheet.getShapeFillType());
            insetFillFormat.getSolidFillColor().setColor(stylesheet.getFillColor());

            IAdjustValueCollection adjustments = insetAutoShape.getAdjustments();
            IAdjustValue adjustValue = null;
            if (adjustments != null && adjustments.size() > 0) {
                adjustValue = insetAutoShape.getAdjustments().get_Item(0);
            }

            if (adjustValue != null) {
                // 0 to 50000 - Where 0 correspond to a value that will reduce the distance between two points such that a rectangle is formed
                adjustValue.setRawValue(3000);

                //0 to 0.833333
                adjustValue.setAngleValue(0.0333f);
            }
        }

        IAdjustValueCollection adjustments = iAutoShape.getAdjustments();
        IAdjustValue adjustValue = null;
        if (adjustments != null && adjustments.size() > 0) {
            adjustValue = iAutoShape.getAdjustments().get_Item(0);
        }

        if (adjustValue != null) {
            // 0 to 50000 - Where 0 correspond to a value that will reduce the distance between two points such that a rectangle is formed
            adjustValue.setRawValue(3000);

            //0 to 0.833333
            adjustValue.setAngleValue(0.0333f);
        }
        addTextbox();
    }

    private void addTextbox() {
        NodeProperties np = NodePropertiesFactory.transform(NodePropertiesFactory.get(textPosition.getX().floatValue(), textPosition.getY().floatValue(), 110, 50), adjustment.getFactor(), adjustment.getCoordinate());
        IAutoShape textBox = iGroupShape.getShapes().addAutoShape(ShapeType.Rectangle, np.getX().floatValue(), np.getY().floatValue(), np.getWidth().floatValue(), np.getHeight().floatValue());
        textBox.getLineFormat().getFillFormat().setFillType(FillType.NoFill);
        textBox.getFillFormat().setFillType(FillType.NoFill);
        textBox.addTextFrame(" ");
        IParagraph iParagraph = textBox.getTextFrame().getParagraphs().get_Item(0);
        IPortion portion = iParagraph.getPortions().get_Item(0);
        portion.getPortionFormat().getFillFormat().setFillType(FillType.Solid);
        portion.getPortionFormat().getFillFormat().getSolidFillColor().setColor(stylesheet.getTextColor());
        portion.getPortionFormat().setFontHeight(10);
        portion.getPortionFormat().setFontBold(NullableBool.True);
        portion.setText(displayName);
    }
}