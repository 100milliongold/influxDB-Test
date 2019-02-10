package hello.services;

import java.util.List;

import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.BoundParameterQuery.QueryBuilder;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.stereotype.Service;

import hello.domain.H2oFeet;

@Service
public class MainService {
	
	@Autowired
	private InfluxDBTemplate<Point> influxDBTemplate;

	public List<H2oFeet> h2o_list() {
		Query query = QueryBuilder.newQuery("SELECT * FROM h2o_feet LIMIT 1000")
		        .forDatabase("NOAA_water_database")
		        .create();
		
		
		QueryResult queryResult = influxDBTemplate.query(query);
		
		
		return new InfluxDBResultMapper().toPOJO(queryResult, H2oFeet.class);
	}

	
	
}
