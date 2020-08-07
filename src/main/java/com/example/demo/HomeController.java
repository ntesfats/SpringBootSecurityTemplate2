package com.example.demo;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ResumeRepository resumeRepository;

    @Autowired
    CloudinaryConfig cloudinaryConfig;

//  Home Page
    @RequestMapping("/")
    public String index(Principal principal, Model model) {
        model.addAttribute("currentUser", principal.getName());
        return"index";
    }

//  Login Page
    @RequestMapping("/login")
    public String login() {
        return "login";
    }

//  Logout Page
    @RequestMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }

//  User Profile
    @RequestMapping("/profile")
    public String admin(Principal principal, Model model) {
        model.addAttribute("currentUser", principal.getName());
        return "profile";
    }

//  Registration
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
//  Process Registration
    @PostMapping("/processregister")
    public String processRegister(@Valid @ModelAttribute("user") User user,
                                  BindingResult result, Model model) {
        if (result.hasErrors()) {
            user.clearPassword();
            model.addAttribute("user", user);
            return "register";
        }
        model.addAttribute("user", user);
        model.addAttribute("message", "New user account created");

        user.setEnabled(true);
        userRepository.save(user);

        Role role = new Role(user.getUsername(), "ROLE_USER");
        roleRepository.save(role);

        return "redirect:/";

    }

    //    Page to add File
    @RequestMapping("/resume/add")
    public String addActor(Model model) {
        model.addAttribute("resume", new Resume());

        return "addResume";
    }

    //    Processing added Actor
    @RequestMapping("/resume/process")
    public String process(@ModelAttribute Resume resume,
                          @RequestParam("file") MultipartFile file ) {
//        First we check if the file submitted is empty
        if (file.isEmpty()) {
            return "redirect:/resume/add";
        }
//        Then we upload the file to cloudinary
        try {
            Map uploadResult = cloudinaryConfig.upload(file.getBytes(),
                    ObjectUtils.asMap("resourcetype", "auto"));
            resume.setFileUrl(uploadResult.get("url").toString());
            resumeRepository.save(resume);
//            We check if there was any error during upload; if so, redirect to the /add opage
        } catch (IOException e) {

            e.getStackTrace();
            return "redirect:/resume/add";
        }

//        If Everything went okay \, we redirect to the home page
        return "redirect:/resume/viewAll";
    }

    //    Page to view Actors
    @RequestMapping("/resume/viewAll")
    public String viewActor(Model model) {
        Iterable<Resume> resumes = resumeRepository.findAll();
        model.addAttribute("resumes", resumes);

        return "displayResume";
    }



}
