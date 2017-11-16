package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.reactome.server.tools.diagram.exporter.common.ResourcesFactory;
import org.reactome.server.tools.diagram.exporter.common.analysis.AnalysisClient;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisException;
import org.reactome.server.tools.diagram.exporter.common.analysis.exception.AnalysisServerError;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisResult;
import org.reactome.server.tools.diagram.exporter.common.analysis.model.AnalysisType;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDException;
import org.reactome.server.tools.diagram.exporter.raster.ehld.exception.EHLDRuntimeException;
import org.reactome.server.tools.diagram.exporter.raster.gif.AnimatedGifEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;

import static org.apache.batik.util.SVGConstants.*;

public class EHLDRenderer {

	private static final Set<String> TRANSPARENT_FORMATS = new HashSet<>(Collections.singletonList("png"));
	private static final Set<String> NO_TRANSPARENT_FORMATS = new HashSet<>(Arrays.asList("jpg", "jpeg", "gif"));

	private static final float MARGIN = 15;
	private final SVGDocument document;
	private final RasterArgs args;

	public EHLDRenderer(RasterArgs args, String ehldPath) throws EHLDException {
		this.document = ResourcesFactory.getEHLD(ehldPath, args.getStId());
		this.args = args;
	}

	public BufferedImage render() throws EHLDException {
//		SVGDecoratorRenderer.selectAndFlag(document, args);
		final SVGAnalysis svgAnalysis = new SVGAnalysis(document, args);
		svgAnalysis.analysis();
		return renderImage();
	}

	private BufferedImage renderImage() throws EHLDException {
		updateDocumentDimensions();
		return toImage(document);
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
//		transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, w);
//		transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, h);
	}

	private BufferedImage toImage(Document document) throws EHLDException {
		try {
			final TranscoderInput input = new TranscoderInput(document);
			final BufferedImageTranscoder transcoder = new BufferedImageTranscoder(args);
			transcoder.transcode(input, null);
			return transcoder.getImage();
		} catch (TranscoderException e) {
			throw new EHLDRuntimeException(e.getMessage());
		}
	}

	public void renderToAnimatedGif(OutputStream os) throws AnalysisServerError, EHLDException, IOException, AnalysisException {
		// Check if analysis token is OK
		final AnalysisResult result = AnalysisClient.getAnalysisResult(args.getToken());
		AnalysisType analysisType = AnalysisType.getType(result.getSummary().getType());
		if (analysisType != AnalysisType.EXPRESSION)
			throw new IllegalStateException("Only EXPRESSION analysis can be rendered into animated GIFs");
//		SVGDecoratorRenderer.selectAndFlag(document, args);
		final SVGAnalysis svgAnalysis = new SVGAnalysis(document, args);
		svgAnalysis.analysis();
		updateDocumentDimensions();

		final AnimatedGifEncoder encoder = new AnimatedGifEncoder();
		encoder.setDelay(1000);
		encoder.setRepeat(0);
		encoder.setQuality(1);
		encoder.start(os);
		for (int expressionColumn = 0; expressionColumn < result.getExpression().getColumnNames().size(); expressionColumn++) {
			svgAnalysis.setColumn(expressionColumn);
			final BufferedImage image = toImage(document);
			encoder.addFrame(image);
		}
		encoder.finish();
	}

	/**
	 * There is no a standard BufferedImageTranscoder, although all Transcorders
	 * use BufferedImages as the raster. This class exposes that BufferedImage,
	 * so no need to store in a File.
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
				final BufferedImage image;
				image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
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
