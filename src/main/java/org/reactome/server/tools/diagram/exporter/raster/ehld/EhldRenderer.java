package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.reactome.server.analysis.core.model.AnalysisType;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.raster.RasterRenderer;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EhldRuntimeException;
import org.reactome.server.tools.diagram.exporter.raster.gif.AnimatedGifEncoder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.batik.util.SVGConstants.*;

/**
 * Main class to create a render from an EHLD.
 */
public class EhldRenderer implements RasterRenderer {

	private static final Set<String> TRANSPARENT_FORMATS = new HashSet<>(Collections.singletonList("png"));
	private static final Set<String> NO_TRANSPARENT_FORMATS = new HashSet<>(Arrays.asList("jpg", "jpeg", "gif"));

	private static final float MARGIN = 15;
	private final SVGDocument document;
	private final RasterArgs args;
	private SvgAnalysis svgAnalysis;
	private AnalysisStoredResult result;

	public EhldRenderer(RasterArgs args, String ehldPath, AnalysisStoredResult result) throws EhldException {
		this.result = result;
		this.document = ResourcesFactory.getEhld(ehldPath, args.getStId());
		this.args = args;
		layout();
	}

	private void layout() {
		SvgDecoratorRenderer.selectAndFlag(document, args);
		svgAnalysis = new SvgAnalysis(document, args, result);
		svgAnalysis.analysis();
		fixFont();
		updateDocumentDimensions();
	}


	/**
	 * Defensive programming rule #324: Illustrator exports custom fonts.
	 * Instead of <pre><code>
	 * h1 {
	 *  font-family: Arial;
	 *  font-weight: 700;
	 * }
	 * </code></pre>
	 * it exports like this:
	 * <pre><code>
	 * h1 {
	 *  font-family: Arial-BoldMT, Arial;
	 *  font-weight: 700;
	 * }
	 * </code></pre>
	 * what, in some cases, applies Bold twice.
	 */
	private void fixFont() {
		final String arialBoldMT = "Arial-BoldMT,";
		final NodeList styleList = document.getRootElement().getElementsByTagNameNS(SVG_NAMESPACE_URI, SVG_STYLE_ATTRIBUTE);
		final Node style = styleList.getLength() > 0 ? styleList.item(0) : null;
		if (style != null) {
			String replace = style.getTextContent().replace(arialBoldMT, "");
			style.setTextContent(replace);
		}
	}

	@Override
	public Dimension getDimension() {
		final String viewBox = document.getRootElement().getAttribute(SVG_VIEW_BOX_ATTRIBUTE);
		final String[] values = viewBox.split(" ");
		final double width = Double.valueOf(values[2]);
		final double height = Double.valueOf(values[3]);
		return new Dimension((int) (width + 0.5), (int) (height + 0.5));
	}

	@Override
	public BufferedImage render() {
		disableMasks();
		return rasterize();
	}

	private void disableMasks() {
		// Remove each mask from its parent
		final NodeList masks = document.getElementsByTagNameNS(SVG_NAMESPACE_URI, SVG_MASK_TAG);
		final List<Element> maskNodes = IntStream.range(0, masks.getLength())
				.mapToObj(masks::item)
				.map(Element.class::cast)
				.collect(Collectors.toList());
		maskNodes.forEach(mask -> mask.getParentNode().removeChild(mask));
		// Remove from defs/style
		// This is not necessary, as they are not referenced anymore,
		// but will keep the SVG document clear
		final NodeList styleList = document.getRootElement().getElementsByTagNameNS(SVG_NAMESPACE_URI, SVG_STYLE_ATTRIBUTE);
		final Node style = styleList.getLength() > 0 ? styleList.item(0) : null;
		if (style != null)
			maskNodes.forEach(mask -> removeMaskFromStyle(style, mask.getAttribute(SVG_ID_ATTRIBUTE)));
	}

	private void removeMaskFromStyle(Node style, String maskId) {
		final String maskRef = String.format("mask:url(#%s);", maskId);
		style.setTextContent(style.getTextContent().replace(maskRef, ""));
	}

	private void updateDocumentDimensions() {
		final String viewBox = document.getRootElement().getAttribute(SVG_VIEW_BOX_ATTRIBUTE);
		final Scanner scanner = new Scanner(viewBox);
		scanner.useLocale(Locale.UK);
		scanner.nextFloat();  // x
		scanner.nextFloat();  // y
		float width = scanner.nextFloat();
		float height = scanner.nextFloat();

		width += 2 * MARGIN;
		height += 2 * MARGIN;
		// 1 increase image dimensions
		final String newVB = String.format(Locale.UK, "0 0 %.3f %.3f", width, height);
		document.getRootElement().setAttribute(SVG_VIEW_BOX_ATTRIBUTE, newVB);
		// 2 create a g translated (margin, margin)
		final Element group = document.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		final String translate = String.format(Locale.UK, "%s(%f,%f)", SVG_TRANSLATE_VALUE, MARGIN, MARGIN);
		group.setAttribute(SVG_TRANSFORM_ATTRIBUTE, translate);

		// 3 append to the translated group all the g elements in the root
		final List<Node> children = new LinkedList<>();
		for (int i = 0; i < document.getRootElement().getChildNodes().getLength(); i++)
			children.add(document.getRootElement().getChildNodes().item(i));
		children.stream()
				.filter(node -> node.getNodeName().equals(SVG_G_TAG))
				.forEach(group::appendChild);

		document.getRootElement().appendChild(group);

		// Apply factor/scale
		// NOTE: there are 3 ways of scaling:
		//  * 1: set width and height on svg root
		//    2: apply a transform to the elements transform: scale(factor)
		//    3: set width and height on Transcoder
		document.getRootElement().setAttribute(SVG_WIDTH_ATTRIBUTE, String.format(Locale.UK, "%.3f", width * args.getFactor()));
		document.getRootElement().setAttribute(SVG_HEIGHT_ATTRIBUTE, String.format(Locale.UK, "%.3f", height * args.getFactor()));
	}

	/**
	 * Generates a raster from document.
	 */
	private BufferedImage rasterize() {
		try {
			final TranscoderInput input = new TranscoderInput(document);
			final BufferedImageTranscoder transcoder = new BufferedImageTranscoder(args);

			transcoder.transcode(input, null);
			return transcoder.getImage();
		} catch (TranscoderException e) {
			throw new EhldRuntimeException(e.getMessage());
		}
	}

	@Override
	public void renderToAnimatedGif(OutputStream os) throws IOException {
		if (svgAnalysis.getAnalysisType() != AnalysisType.EXPRESSION)
			throw new IllegalStateException("Only EXPRESSION analysis can be rendered into animated GIFs");

		disableMasks();
		final AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		encoder.setDelay(1000);
		encoder.setRepeat(0);
		encoder.start(os);
		for (int expressionColumn = 0; expressionColumn < svgAnalysis.getExpressionSummary().getColumnNames().size(); expressionColumn++) {
			svgAnalysis.setColumn(expressionColumn);
			final BufferedImage image = rasterize();
			encoder.addFrame(image);
		}
		encoder.finish();
	}

	@Override
	public SVGDocument renderToSvg() {
		return document;
	}

	/**
	 * There is no a standard BufferedImageTranscoder, although all Transcorders
	 * use BufferedImages as the raster. This class exposes that BufferedImage,
	 * so ther is no need to store them in a File.
	 */
	private static class BufferedImageTranscoder extends ImageTranscoder {

		private BufferedImage image;
		private String format;
		private Color background;

		BufferedImageTranscoder(RasterArgs args) {
			this.background = args.getBackground() == null
					? Color.WHITE
					: args.getBackground();
			this.format = args.getFormat();
		}

		@Override
		public BufferedImage createImage(int w, int h) {
			if (TRANSPARENT_FORMATS.contains(format)) {
				return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			} else if (NO_TRANSPARENT_FORMATS.contains(format)) {
				final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				final Graphics2D graphics = image.createGraphics();
				graphics.setBackground(background);
				graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
				return image;
			} else
				throw new IllegalArgumentException("Unsupported file extension " + format);
		}

		@Override
		public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
			this.image = image;
		}

		public BufferedImage getImage() {
			return image;
		}
	}
}
