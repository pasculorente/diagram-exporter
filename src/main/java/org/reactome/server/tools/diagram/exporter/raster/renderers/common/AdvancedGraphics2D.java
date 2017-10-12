package org.reactome.server.tools.diagram.exporter.raster.renderers.common;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders Reactome elements over a Graphics object. As Reactome elements use
 * shared figures, such as corned rectangles or ovals, this class offers common
 * methods to easily draw them. It has to be initialized with a factor, so every
 * time a draw method is used, shapes are properly scaled.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class AdvancedGraphics2D {

	private final Graphics2D graphics;
	private final BufferedImage image;

	/**
	 * Creates a AdvancedGraphics2D that contains an BufferedImage. The
	 * AdvancedGraphics2D perform an offset to the image, so renderers will not
	 * need to manually offset. It also contains the factor by which all
	 * renderers have to scale elements.
	 */
	public AdvancedGraphics2D(double width, double height, double x, double y, double margin, String format) {
		switch (format.toLowerCase()) {
			case "gif":
				this.image = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB);
				this.graphics = this.image.createGraphics();
				this.graphics.setBackground(Color.WHITE);
				this.graphics.clearRect(0, 0, (int) width, (int) height);
				break;
			case "jpeg":
			case "jpg":
				this.image = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB);
				this.graphics = image.createGraphics();
				this.graphics.setBackground(Color.WHITE);
				this.graphics.clearRect(0, 0, (int) width, (int) height);
				break;
			case "png":
			default:
				this.image = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
				this.graphics = image.createGraphics();
		}
		graphics.translate(margin, margin);
		graphics.translate((int) -x, (int) -y);
		this.graphics.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
	}

	/**
	 * Gets the inner Graphics element. This will allow you to directly draw
	 * into the image. Be careful, you have to manually apply the factor.
	 * Usually you can factor NodeProperties by using ScaledNodeProperties:
	 * <code>
	 * <pre>
	 * ScaledNodeProperties prop = new ScaledNodeProperties(properties, factor)
	 * </pre>
	 * </code> And, as Graphics only work with ints, you should also cast values
	 * to int. We provide a decorator for that, the IntNodeProperties: <code>
	 * <pre>
	 * IntNodeProperties prop = new IntNodeProperties(properties)
	 * int x = prop.intX();
	 * </pre>
	 * </code> You can nest both decorators: <code>
	 * <pre>
	 * IntNodeProperties prop = new IntNodeProperties(
	 *      new ScaledNodeProperties(properties, factor));
	 * int scaledX = prop.intX();
	 * </pre>
	 * </code>
	 *
	 * @return the inner Graphics
	 */
	public Graphics2D getGraphics() {
		return graphics;
	}

	public BufferedImage getImage() {
		return image;
	}

}
