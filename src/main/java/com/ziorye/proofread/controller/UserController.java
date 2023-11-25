package com.ziorye.proofread.controller;

import com.ziorye.proofread.dto.PasswordResetDto;
import com.ziorye.proofread.dto.PasswordResetEmailDto;
import com.ziorye.proofread.dto.UserDto;
import com.ziorye.proofread.entity.PasswordResetToken;
import com.ziorye.proofread.entity.User;
import com.ziorye.proofread.service.PasswordResetTokenService;
import com.ziorye.proofread.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("dashboard")
    @PreAuthorize("isAuthenticated()")
    String dashboard() {
        return "user/dashboard";
    }

    @GetMapping("register")
    public String showRegistrationForm(Model model){
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "user/register";
    }

    @PostMapping("register")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model,
                               HttpServletRequest request){
        User existingUser = userService.findUserByEmail(userDto.getEmail());

        if(existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()){
            result.rejectValue("email", "exist", "该邮箱已被注册");
        }

        if(result.hasErrors()){
            model.addAttribute("user", userDto);
            return "user/register";
        }

        userService.saveUser(userDto);

        // === === ===
        try {
            request.login(userDto.getEmail(), userDto.getPassword());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        // === === ===

        return "redirect:/user/dashboard";
    }

    @GetMapping("password-reset")
    String showPasswordRestForm(Model model) {
        PasswordResetEmailDto passwordResetEmailDto = new PasswordResetEmailDto();
        model.addAttribute("passwordResetEmail", passwordResetEmailDto);
        return "user/password-reset";
    }

    @Autowired
    PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("password-reset")
    String passwordReset(@Valid @ModelAttribute("passwordResetEmail") PasswordResetEmailDto passwordResetEmailDto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes,
                         HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
        User existingUser = userService.findUserByEmail(passwordResetEmailDto.getEmail());
        if(existingUser == null){
            result.rejectValue("email", "non-existent", "找不到该邮箱对应的用户信息");
        }

        if(result.hasErrors()){
            model.addAttribute("passwordResetEmail", passwordResetEmailDto);
            return "user/password-reset";
        }

        // send email
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(existingUser);
        token.setToken(UUID.randomUUID().toString());
        token.setExpirationDate(LocalDateTime.now().plusMinutes(30));
        try {
            passwordResetTokenService.save(token);
        } catch (Exception e) {
            result.rejectValue("email", null, "未知错误，请联系管理员");
            model.addAttribute("passwordResetEmail", passwordResetEmailDto);
            return "user/password-reset";
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(new InternetAddress("admin@example.com", "Admin"));
        helper.setSubject("重置密码");
        assert existingUser != null;
        helper.setTo(existingUser.getEmail());
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        helper.setText("<html><body><p>点击以下链接进行密码重置</p><a href='" + baseUrl + "/user/do-password-reset?token=" + token.getToken() + "'>重置密码</a><p>链接将在 30 分钟后失效，请尽快操作。</p></body></html>", true);
        mailSender.send(message);

        attributes.addFlashAttribute("success", "密码重置邮件已发送，请注意查收");
        return "redirect:/user/password-reset";
    }

    @GetMapping("do-password-reset")
    String showDoPasswordRestForm(@RequestParam(name = "token", required = false) String token, Model model) {
        PasswordResetToken passwordResetToken = passwordResetTokenService.findFirstByTokenOrderByIdDesc(token);
        if (passwordResetToken == null) {
            model.addAttribute("error", "token 不存在");
        } else if (passwordResetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "token 已过期");
        }

        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setToken(token);
        model.addAttribute("passwordResetDto", passwordResetDto);

        return "user/do-password-reset";
    }

    @PostMapping("do-password-reset")
    public String resetPassword(@Valid @ModelAttribute("passwordResetDto") PasswordResetDto passwordResetDto,
                                BindingResult result,
                                RedirectAttributes attributes) {
        if(result.hasErrors()){
            attributes.addFlashAttribute("passwordResetDto", passwordResetDto);
            return "user/do-password-reset";
        }

        PasswordResetToken token = passwordResetTokenService.findByToken(passwordResetDto.getToken());
        User user = token.getUser();
        user.setPassword(passwordResetDto.getPassword());
        userService.updatePassword(user);

        //
        int expireThisTokenResult = passwordResetTokenService.expireThisToken(passwordResetDto.getToken());
        log.info("expireThisToken={}, return={}", passwordResetDto.getToken(), expireThisTokenResult);

        return "redirect:/login";
    }
}
