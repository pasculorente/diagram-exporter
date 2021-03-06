package org.reactome.server.tools.diagram.exporter.raster.diagram;


import org.junit.Test;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.tools.diagram.exporter.raster.TestUtils;
import org.reactome.server.tools.diagram.exporter.raster.api.RasterArgs;
import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class DiagramRendererTest {

	@Test
	public void testSimpleDiagram() {
		// These diagrams contain all the types of nodes:
		// Chemical, Complex, Entity, EntitySet, Gene, ProcessNode, Protein and RNA
		final List<String> stIds = Arrays.asList("R-HSA-5687128", "R-HSA-376176", "R-HSA-69620");
		final List<String> formats = Arrays.asList("PNG", "jpg", "Gif");
		for (String stId : stIds)
			for (String format : formats)
				TestUtils.render(new RasterArgs(stId, format), null);
	}

	@Test
	public void testQuality() {
		IntStream.range(1, 11)
				.forEach(quality -> {
					final RasterArgs args = new RasterArgs("R-HSA-376176", "jpg");
					args.setQuality(quality);
					TestUtils.render(args, null);
				});
	}

	@Test
	public void testSelectNodes() {
		RasterArgs args = new RasterArgs("R-HSA-5687128", "png");
		// EntitySet, Protein, Chemical
		args.setSelected(Arrays.asList("R-HSA-5692706", "R-HSA-5687026", "R-ALL-29358"));
		TestUtils.render(args, null);

		args = new RasterArgs("R-HSA-376176", "png");
		// Gene, Complex, ProcessNode
		args.setSelected(Arrays.asList("R-HSA-9010561", "R-HSA-428873", "R-HSA-5627117"));
		TestUtils.render(args, null);

		args = new RasterArgs("R-HSA-69620", "png");
		// RNA, Entity
		args.setSelected(Arrays.asList("R-HSA-6803386", "R-ALL-176104"));
		TestUtils.render(args, null);
	}

	@Test
	public void testSelectReactions() {
		RasterArgs args = new RasterArgs("R-HSA-982772", "png");
		// Reactions: Transition, Omitted Process, Uncertain, Association
		// Connectors: filled stop
		args.setSelected(Arrays.asList("R-HSA-1168423", "R-HSA-1168459", "R-HSA-982810", "R-HSA-982775"));
		TestUtils.render(args, null);
		// REPORT: stoichiometry text color is always black in PathwayBrowser

		args = new RasterArgs("R-HSA-877300", "png");
		// Reactions: Association, Dissociation
		// Connectors: empty circle, filled arrow, empty arrow
		args.setSelected(Arrays.asList("R-HSA-877269", "R-HSA-873927"));
		TestUtils.render(args, null);
	}

	@Test
	public void testFlagNodes() {
		RasterArgs args = new RasterArgs("R-HSA-5687128", "png");
		// EntitySet, Protein, Chemical
		args.setFlags(Arrays.asList("R-HSA-5692706", "R-HSA-5687026", "R-ALL-29358"));
		TestUtils.render(args, null);
		// FIXME: chemical flag does not work with name (CTP)

		args = new RasterArgs("R-HSA-376176", "png");
		// Gene, Complex, ProcessNode
		args.setFlags(Arrays.asList("R-HSA-9010561", "R-HSA-428873", "R-HSA-5627117"));
		TestUtils.render(args, null);
		// REPORT: ProcessNode not flagged in PathwayBrowser

		args = new RasterArgs("R-HSA-69620", "png");
		// RNA, Entity
		args.setFlags(Arrays.asList("R-HSA-6803386", "R-ALL-176104"));
		TestUtils.render(args, null);
	}

	@Test
	public void testDiseases() {
		RasterArgs args = new RasterArgs("R-HSA-162587", "png");
		// Entity, RNA, EntitySet, Complex
		args.setSelected(Arrays.asList("R-HIV-165543", "R-HIV-173808", "R-HIV-173120", "R-HSA-167286"));
		args.setFlags(Arrays.asList("R-HIV-165543", "R-HIV-173808", "R-HIV-173120", "R-HSA-167286"));

		TestUtils.render(args, null);
		args = new RasterArgs("R-HSA-5467343", "png");
		// Gene
		args.setSelected(Collections.singletonList("R-HSA-5251547"));
		TestUtils.render(args, null);
		// FIXME: node overlay top-left
	}

	@Test
	public void testDiagramProfiles() {
		final String stId = "R-HSA-5687128";
		final List<String> diagramProfiles = Arrays.asList("Standard", "MODERN", "not valid");
		final List<String> formats = Arrays.asList("png", "jpeg", "gif");
		for (String diagramProfile : diagramProfiles) {
			for (String format : formats) {
				RasterArgs args = new RasterArgs(stId, format);
				args.setProfiles(new ColorProfiles(diagramProfile, null, null));
				TestUtils.render(args, null);
			}
		}
	}

	@Test
	public void testSpeciesComparison() {
		RasterArgs args = new RasterArgs("R-HSA-5687128", "png");
		args.setToken(TestUtils.TOKEN_SPECIES);
		args.setWriteTitle(true);
		TestUtils.render(args, null);
	}

	@Test
	public void testEnrichment() {
		RasterArgs args = new RasterArgs("R-HSA-69620", "png");
		args.setToken(TestUtils.TOKEN_OVER_1);
		args.setWriteTitle(true);
		TestUtils.render(args, null);
	}

	@Test
	public void testExpression() {
		RasterArgs args = new RasterArgs("R-HSA-69620", "png");
		args.setToken(TestUtils.TOKEN_EXPRESSION_1);
		args.setWriteTitle(true);
		TestUtils.render(args, null);
	}

	@Test
	public void testExpressionSelectUnhit() {
		// My favourite diagram had to be here
		final RasterArgs args = new RasterArgs("R-HSA-432047", "gif");
		args.setToken(TestUtils.TOKEN_EXPRESSION_1);
		args.setSelected(Collections.singletonList("R-HSA-432253"));
		TestUtils.render(args, null);
	}

	@Test
	public void testAnimatedGif() {
		final ColorProfiles profiles = new ColorProfiles("modern", "copper plus", "teal");
		final RasterArgs args = new RasterArgs("R-HSA-109606", "gif");
		args.setSelected(Collections.singletonList("R-HSA-114255"));
		args.setToken(TestUtils.TOKEN_EXPRESSION_2);
		args.setProfiles(profiles);
		TestUtils.renderGif(args, null);
	}

	@Test
	public void testAnimatedGif2() {
		final ColorProfiles profiles = new ColorProfiles("modern", "copper plus", "teal");
		final RasterArgs args = new RasterArgs("R-HSA-432047", "gif");
		args.setProfiles(profiles);
		args.setSelected(Collections.singleton("R-ALL-879874"));
		args.setToken(TestUtils.TOKEN_EXPRESSION_2);
		TestUtils.renderGif(args, null);
	}

	@Test
	public void testDisease() {
		// EntitySet hiding another EntitySet
		final RasterArgs args = new RasterArgs("R-HSA-5657560", "png");
		args.setSelected(Collections.singletonList("R-HSA-5656438"));
		TestUtils.render(args, null);
	}

	@Test
	public void testDiseaseProcessNodeWithAnalysis() {
		// This could be the definition of a corner case
		final RasterArgs args = new RasterArgs("R-HSA-1643713", "png");
		args.setToken(TestUtils.TOKEN_EXPRESSION_2);
		args.setSelected(Collections.singletonList("R-HSA-5637815"));
		TestUtils.render(args, null);
		// report: processNodes have no outer red border when hit by analysis
	}

	@Test
	public void testDecoratedFadeout() {
		// Fadeout elements can't be decorated (selected, haloed, flagged)
		final RasterArgs args = new RasterArgs("R-HSA-5683371", "jpg");
		args.setSelected(Arrays.asList("29356", "71185"));
		TestUtils.render(args, null);
	}

	@Test
	public void testWithAnalysisResult() {
		final AnalysisStoredResult result = TestUtils.getResult(TestUtils.TOKEN_OVER_1);
		final RasterArgs args = new RasterArgs("R-HSA-1643713", "png");
		TestUtils.render(args, result);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testScaleLimits() {
		final RasterArgs args = new RasterArgs("stid", "png");
		args.setQuality(0);
	}

	@Test
	public void testEncapsulatedPathways() {
		final RasterArgs args = new RasterArgs("R-HSA-168164", "png");
		final AnalysisStoredResult result = TestUtils.getResult(TestUtils.TOKEN_OVER_1);
		TestUtils.render(args, result);
	}

}
