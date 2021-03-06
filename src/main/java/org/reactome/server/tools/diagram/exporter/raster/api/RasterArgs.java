package org.reactome.server.tools.diagram.exporter.raster.api;

import org.reactome.server.tools.diagram.exporter.raster.profiles.ColorProfiles;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RasterArgs {

	private String stId;
	private String format;
	private String token;
	private Set<String> flags;
	private Set<String> selected;
	private ColorProfiles profiles;
	private Color background;
	private Integer column;
	private String resource;
	private Boolean writeTitle;
	private Integer quality = 5;
	private Double factor = scale(quality);

	public RasterArgs(String stId, String format) {
		this.stId = stId;
		setFormat(format);
	}

	/** diagram stable identifier */
	public String getStId() {
		return stId;
	}

	public void setStId(String stId) {
		this.stId = stId;
	}

	/**
	 * Only for internal purpose. The quality is interpreted as a factor to
	 * scale the image. The scale goes from 0.1 to 3.
	 */
	public Double getFactor() {
		return factor;
	}

	private double scale(int quality) {
		if (quality < 1 || quality > 10)
			throw new IllegalArgumentException("quality must be in the range [1-10]");
		if (quality < 5) {
			return interpolate(quality, 1, 5, 0.1, 1);
		} else return interpolate(quality, 5, 10, 1, 3);
	}

	private double interpolate(double x, double min, double max, double dest_min, double dest_max) {
		return (x - min) / (max - min) * (dest_max - dest_min) + dest_min;
	}

	/** output image format (png, jpg, gif) */
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format == null ? "png" : format.trim().toLowerCase();
	}

	/** color profiles for diagram, analysis and interactors */
	public ColorProfiles getProfiles() {
		if (profiles == null)
			profiles = new ColorProfiles(null, null, null);
		return profiles;
	}

	public void setProfiles(ColorProfiles profiles) {
		this.profiles = profiles;
	}

	public String getToken() {
		return token;
	}

	/** Analysis token */
	public void setToken(String token) {
		this.token = token;
	}

	/** Background color when no transparency is available */
	public Color getBackground() {
		return background;
	}

	public void setBackground(Color color) {
		this.background = color;
	}

	/**
	 * In case an expression analysis is run, the column to show. Leave it null
	 * to generate a GIF with all the columns. May take longer.
	 */
	public Integer getColumn() {
		return column;
	}

	public void setColumn(Integer column) {
		this.column = column;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public Boolean getWriteTitle() {
		return writeTitle;
	}

	public void setWriteTitle(Boolean writeTitle) {
		this.writeTitle = writeTitle;
	}

	public Set<String> getFlags() {
		return flags;
	}

	public void setFlags(Collection<String> flags) {
		if (flags != null)
			this.flags = new HashSet<>(flags);
	}

	public Set<String> getSelected() {
		return selected;
	}

	public void setSelected(Collection<String> selected) {
		if (selected != null)
			this.selected = new HashSet<>(selected);
	}

	public Integer getQuality() {
		return quality;
	}

	public void setQuality(Integer quality) {
		if (quality != null) {
			this.quality = quality;
			this.factor = scale(quality);
		}
	}
}
