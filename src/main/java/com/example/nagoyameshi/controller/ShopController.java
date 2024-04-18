package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationInputForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReviewService;

@Controller
@RequestMapping("/shop")
public class ShopController {
	private final ShopRepository shopRepository;
	private final ReviewRepository reviewRepository;   
    private final ReviewService reviewService;
    private final CategoryRepository categoryRepository;
	
    public ShopController(ShopRepository shopRepository, ReviewRepository reviewRepository, ReviewService reviewService, CategoryRepository categoryRepository) {
        this.shopRepository = shopRepository;      
         this.reviewRepository = reviewRepository;
         this.reviewService = reviewService;
         this.categoryRepository = categoryRepository;
	}
	
	 @GetMapping
     public String index(@RequestParam(name = "keyword", required = false) String keyword,
                         @RequestParam(name = "area", required = false) String area,
                         @RequestParam(name = "category_id" , required = false) Integer category_id,
                         @RequestParam(name = "price", required = false) Integer price,   
                         @RequestParam(name = "order", required = false) String order,
                         @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                         Model model) 
     {
		 Page<Shop>shopPage;
		 List<Category> category = categoryRepository.findAll();
		 
		 if (keyword != null && !keyword.isEmpty()) {
			 if (order != null && order.equals("priceAsc")) {
                 shopPage = shopRepository.findByNameLikeOrAddressLikeOrderByPriceAsc("%" + keyword + "%", "%" + keyword + "%", pageable);
             } else {
                 shopPage = shopRepository.findByNameLikeOrAddressLikeOrderByCreatedAtDesc("%" + keyword + "%", "%" + keyword + "%", pageable);
             } 
         } else if (area != null && !area.isEmpty()) {
        	 if (order != null && order.equals("priceAsc")) {
                 shopPage = shopRepository.findByAddressLikeOrderByPriceAsc("%" + area + "%", pageable);
             } else {
            	 shopPage = shopRepository.findByAddressLikeOrderByCreatedAtDesc("%" + area + "%", pageable);
             } 
         } else if (category_id != null) {
         	if (order != null && order.equals("priceAsc")) {
             shopPage = shopRepository.findByCategoryIdOrderByPriceAsc(category_id , pageable);
         	} else if (order != null && order.equals("priceDesc")){
         		shopPage = shopRepository.findByCategoryIdOrderByPriceDesc(category_id , pageable);
         	} else {
         		shopPage = shopRepository.findByCategoryIdOrderByCreatedAtDesc(category_id , pageable);
         	}
         } else if (price != null) {
        	 if (order != null && order.equals("priceAsc")) {
                 shopPage = shopRepository.findByPriceLessThanEqualOrderByPriceAsc(price, pageable);
             } else {
            	 shopPage = shopRepository.findByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
             }            
         } else {
        	 if (order != null && order.equals("priceAsc")) {
                 shopPage = shopRepository.findAllByOrderByPriceAsc(pageable);
             } else {
            	 shopPage = shopRepository.findAllByOrderByCreatedAtDesc(pageable);   
             }     
         }                
         
         model.addAttribute("shopPage", shopPage);
         model.addAttribute("keyword", keyword);
         model.addAttribute("area", area);
         model.addAttribute("caregory_id",category_id);
         model.addAttribute("price", price);
         model.addAttribute("order", order);
         model.addAttribute("category", category);
         
         return "shop/index";
     }
	 
	 @GetMapping("/{id}")
	 public String show(@PathVariable(name = "id") Integer id, Model model, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
         Shop shop = shopRepository.getReferenceById(id);
         boolean hasUserAlreadyReviewed = false;        
         
         if (userDetailsImpl != null) {
             User user = userDetailsImpl.getUser();
             hasUserAlreadyReviewed = reviewService.hasUserAlreadyReviewed(shop, user);           
         }
         
         List<Review> newReview = reviewRepository.findTop6ByShopOrderByCreatedAtDesc(shop);        
         long totalReviewCount = reviewRepository.countByShop(shop); 
         
         model.addAttribute("shop", shop);
         model.addAttribute("reservationInputForm", new ReservationInputForm());
         model.addAttribute("hasUserAlreadyReviewed", hasUserAlreadyReviewed);
         model.addAttribute("newReview", newReview);        
         model.addAttribute("totalReviewCount", totalReviewCount); 
         
         return "shop/show";
     }    
     

}
