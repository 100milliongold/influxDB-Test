# influxDB-Test
influxDB-Java 을 Bean 에 등록하여 Spring Boot 와 연동

# 요구사항
 - spring boot : 1.5.10.RELEASE 이상
 - jdk : 1.8 이상
 - influxDB : 2.7


# influxDB 설치
- docker 에서 설치

```shell
docker pull influxdb
docker run --name  influxdb-test -p8086:8086 -d influxdb
```

# 샘플 데이터 생성하기
- influxDB 접속

```shell
docker exec -it influxdb-test /bin/bash
influx -precision rfc3339
Connected to http://localhost:8086 version 1.4.x
InfluxDB shell 1.4.x
```
- 데이터베이스 생성

```shell
CREATE DATABASE NOAA_water_database
exit
```

- 샘플 데이터 다운받기

```shell
curl https://s3.amazonaws.com/noaa.water-database/NOAA_data.txt -o NOAA_data.txt
```
- 데이터를 influxDB 에 import 하기

```shell
influx -import -path=NOAA_data.txt -precision=s -database=NOAA_water_database
```
- 테스트 하기

```shell
influx -precision rfc3339
use NOAA_water_database
SELECT * FROM h2o_feet LIMIT 5
```

# spring boot 에 연동하기
- spring boot 프로젝트 생성
- pom.xml 설정

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>influxDB-Test</artifactId>
    <version>0.1.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.5.RELEASE</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.influxdb</groupId>
            <artifactId>influxdb-java</artifactId>
            <version>2.14</version>
        </dependency>

        <dependency>
            <groupId>com.github.miwurster</groupId>
            <artifactId>spring-data-influxdb</artifactId>
            <version>1.8</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

- spring configure 설정 (InfluxDBConfiguration.java)

```java
@Configuration
@EnableConfigurationProperties(InfluxDBProperties.class)
public class InfluxDBConfiguration
{
  @Bean
  public InfluxDBConnectionFactory connectionFactory(final InfluxDBProperties properties)
  {
    return new InfluxDBConnectionFactory(properties);
  }

  @Bean
  public InfluxDBTemplate<Point> influxDBTemplate(final InfluxDBConnectionFactory connectionFactory)
  {
    /*
     * You can use your own 'PointCollectionConverter' implementation, e.g. in case
     * you want to use your own custom measurement object.
     */
    return new InfluxDBTemplate<>(connectionFactory, new PointConverter());
  }

  @Bean
  public DefaultInfluxDBTemplate defaultTemplate(final InfluxDBConnectionFactory connectionFactory)
  {
    /*
     * If you are just dealing with Point objects from 'influxdb-java' you could
     * also use an instance of class DefaultInfluxDBTemplate.
     */
    return new DefaultInfluxDBTemplate(connectionFactory);
  }
}
```

- application.properties 설정

```properties
spring.influxdb.url=http://localhost:8086
spring.influxdb.database=NOAA_water_database
spring.influxdb.username=~
spring.influxdb.retentionPolicy=autogen
```

- DTO 생성(H2oFeet.java)

```java
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
```

- controller 생성 (MainController.java)

```java
@Controller
@RequestMapping(path="/api/v1/NOAAWater")
public class MainController {

	@Autowired
	private MainService mainService;


	@GetMapping(path="/h2os")
	public @ResponseBody List<H2oFeet> h2o_list() {



		return mainService.h2o_list();
	}
}
```

- service 생성 (MainService.java)

```java
@Service
public class MainService {

	@Autowired
	private InfluxDBTemplate<Point> influxDBTemplate;

	public List<H2oFeet> h2o_list() {
		Query query = QueryBuilder.newQuery("SELECT * FROM h2o_feet LIMIT 1000")
		        .forDatabase("NOAA_water_database")
		        .create();


		QueryResult queryResult = influxDBTemplate.query(query);


		InfluxDBResultMapper resultMapper = new InfluxDBResultMapper(); // thread-safe - can be reused

		return resultMapper.toPOJO(queryResult, H2oFeet.class);
	}
}
```

# Test
- curl

```shell
$curl -X GET http://localhost:8080/api/v1/NOAAWater/h2os
```
