package hello.domain;

import java.time.Instant;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;


@Measurement(name = "h2o_feet")
public class H2oFeet {
	@Column(name = "water_level")
	private Double water_level;
	@Column(name = "level description")
	private String level_description;
	@Column(name = "location")
	private String location;
	@Column(name = "time")
	private Instant time;
	
	
	public Double getWater_level() {
		return water_level;
	}
	public void setWater_level(Double water_level) {
		this.water_level = water_level;
	}
	public String getLevel_description() {
		return level_description;
	}
	public void setLevel_description(String level_description) {
		this.level_description = level_description;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Instant getTime() {
		return time;
	}
	public void setTime(Instant time) {
		this.time = time;
	}
	
	
	
}
