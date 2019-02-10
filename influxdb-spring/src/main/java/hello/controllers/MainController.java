package hello.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import hello.domain.H2oFeet;
import hello.services.MainService;

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
