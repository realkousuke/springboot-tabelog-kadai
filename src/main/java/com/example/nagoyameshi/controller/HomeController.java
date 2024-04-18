package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.repository.ShopRepository;

@Controller
public class HomeController {
 private final ShopRepository shopRepository;        
     
     public HomeController(ShopRepository shopRepository) {
         this.shopRepository = shopRepository;            
     } 
     
	@GetMapping("/")
	public String index(Model model) {
        List<Shop> newShop = shopRepository.findTop10ByOrderByCreatedAtDesc();
        model.addAttribute("newShop", newShop);
        return "index";
    }   
	
	@GetMapping("/company")
	public String company() {
		
		return "company";
	}

}
