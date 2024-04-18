package com.example.nagoyameshi.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Controller
@RequestMapping("/subscription")
public class SubscriptionController {
	private final UserRepository userRepository;
	private final UserService userService;
	
	
	public SubscriptionController (UserRepository userRepository,UserService userService){
		this.userRepository = userRepository;
		this.userService = userService;
		
	}
	@Autowired
	private StripeService stripeService;
		
	@GetMapping("/register")
	public String index(Model model, HttpServletRequest httpServletRequest, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
	    User user = userDetailsImpl.getUser(); // ログインしているユーザーを取得
	    String sessionId = stripeService.createStripeSession(httpServletRequest, user);
	    model.addAttribute("sessionId", sessionId);
	    return "subscription/register";
	}
			
		
	
	@PostMapping("/create")
	public String create(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {
	    // ユーザーのロールを更新
	    User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
	    Map<String, String> paymentIntentObject = new HashMap<>();
	    paymentIntentObject.put("userId", String.valueOf(user.getId()));
	    paymentIntentObject.put("roleName", "ROLE_PREMIUM");
	    userService.updateRole(paymentIntentObject);
	    
	 
	    userService.refreshAuthenticationByRole("ROLE_PREMIUM");
	    
	    // Spring Securityのログアウト処理を実行
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication != null) {
	        new SecurityContextLogoutHandler().logout(request, response, authentication);
	    }
	    request.getSession().invalidate();
	    

	    return "redirect:/login?reserved";
	}
		
		@GetMapping("/cancel")
		public String cancel() {
			return "subscription/cancel";
		}
		
		@PostMapping("/delete")
		public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes) {
			User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
			
		
			

			Map<String, String> paymentIntentObject = new HashMap<>();
		    paymentIntentObject.put("userId", String.valueOf(user.getId()));
		    paymentIntentObject.put("roleName", "ROLE_GENERAL");

		
		    userService.updateRole(paymentIntentObject);
				userService.refreshAuthenticationByRole("ROLE_GENERAL");
				redirectAttributes.addFlashAttribute("successMessage", "有料プランを解約しました。");
			
			
			return "redirect:/";
		}

}
