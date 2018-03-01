package org.reactome.server.tools.diagram.exporter.svg;

import org.junit.Test;
import org.reactome.server.tools.diagram.exporter.raster.TestUtils;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.util.Collections;

public class SvgRendererTest {

	@Test
	public void testDiagram() {
		final RasterArgs args = new RasterArgs("R-HSA-109606", "svg");
		args.setToken(TestUtils.TOKEN_OVER_2);
		TestUtils.renderSvg(args, null);
	}

	@Test
	public void testEhld() {
		final RasterArgs args = new RasterArgs("R-HSA-74160", "svg");
		args.setToken(TestUtils.TOKEN_EXPRESSION_1);
		TestUtils.renderSvg(args, null);
	}

	@Test
	public void testEhldFont() {
		TestUtils.renderSvg(new RasterArgs("R-HSA-69278", "svg"), null);
		// REPORT: partial fix. EHLDs don't follow SVG standards (Illustrator)
		TestUtils.render(new RasterArgs("R-HSA-69278", "png"), null);
		TestUtils.renderSvg(new RasterArgs("R-HSA-69620", "svg"), null);
		TestUtils.render(new RasterArgs("R-HSA-69620", "png"), null);
	}

	@Test
	public void testLegend() {
		final ColorProfiles profiles = new ColorProfiles("modern", "copper plus", "teal");
		final RasterArgs args = new RasterArgs("R-HSA-432047", "png");
		args.setProfiles(profiles);
		args.setSelected(Collections.singleton("R-ALL-879874"));
		args.setToken(TestUtils.TOKEN_OVER_1);
		TestUtils.render(args, null);
	}

}
