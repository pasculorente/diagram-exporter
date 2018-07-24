package org.reactome.server.tools.diagram.exporter.raster.ehld;

import org.junit.Test;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.exporter.raster.TestUtils;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

public class EhldRendererTest {

	@Test
	public void testBasicEHLD() {
		final RasterArgs args = new RasterArgs("R-HSA-382551", "png");
		TestUtils.render(args, null);
	}

	@Test
	public void testFormat() {
		final String[] formats = {"PNg", "JpG", "gif", "JPEG"};
		final String[] stIds = {"R-HSA-449147", "R-HSA-1640170"};
		for (String stId : stIds) {
			for (String format : formats) {
				final RasterArgs args = new RasterArgs(stId, format);
				TestUtils.render(args, null);
			}
		}
	}

	@Test
	public void testSize() {
		final RasterArgs args = new RasterArgs("R-HSA-109581", "png");
		IntStream.range(1, 11)
				.forEach(quality -> {
					args.setQuality(quality);
					TestUtils.render(args, null);
				});
	}

	@Test
	public void testSelected() {
		final RasterArgs args = new RasterArgs("R-HSA-109581", "png");
		args.setQuality(8);
		args.setSelected(Collections.singletonList("R-HSA-109606"));
		TestUtils.render(args, null);
	}

	@Test
	public void testFlags() {
		final RasterArgs args = new RasterArgs("R-HSA-74160", "png");
		args.setSelected(Arrays.asList("R-HSA-109606", "R-HSA-169911"));
		args.setFlags(Collections.singletonList("CTP"));
		TestUtils.render(args, null);
	}

	@Test
	public void testEnrichment() {
		final RasterArgs args = new RasterArgs("R-HSA-109582", "png");
		args.setToken(TestUtils.TOKEN_OVER_1);
		TestUtils.render(args, null);
	}

	@Test
	public void testExpression() {
		final RasterArgs args = new RasterArgs("R-HSA-6806667", "gif");
		args.setToken(TestUtils.TOKEN_EXPRESSION_1);
		TestUtils.render(args, null);
	}

	@Test
	public void testAnimatedGif() {
		final RasterArgs args = new RasterArgs("R-HSA-69278", "gif");
		args.setToken(TestUtils.TOKEN_EXPRESSION_1);
		args.setSelected(Arrays.asList("R-HSA-69242", "R-HSA-68886"));
		args.setProfiles(new ColorProfiles("modern", "copper plus", "cyan"));
		TestUtils.render(args, null);
	}

	@Test
	public void testVisualArtifacts() {
		final RasterArgs args = new RasterArgs("R-HSA-69278", "png");
		// FIXME: text in server not properly shown
		// FIXME: masks!!
		TestUtils.render(args, null);
	}


	@Test
	public void testParallel() {
		// Batik is not thread safe. In this case, we convert a SVG diagram
		// with analysis to JPG, invoking batik for that.
		final AnalysisStoredResult result = TestUtils.getResult(TestUtils.TOKEN_OVER_1);
		final RasterArgs args = new RasterArgs("R-HSA-109581", "jpg");
		IntStream.range(0, 10)
				.parallel()
				.forEach(value -> TestUtils.render(args, result));
	}

	@Test
	public void testPdf() {
		final AnalysisStoredResult result = TestUtils.getResult(TestUtils.TOKEN_OVER_1);
		final RasterArgs args = new RasterArgs("R-HSA-74160", "pdf");
		TestUtils.render(args, result);
	}


}
