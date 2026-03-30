package org.example.bolsaempleo.presentation.root;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {


    @GetMapping("/")
    public String root() {
        return "redirect:/publica/inicio";
    }




    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth == null) return "redirect:/publica/inicio";

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ADM")))
            return "redirect:/admin/dashboard";

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("EMP")))
            return "redirect:/empresa/dashboard";

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("OFE")))
            return "redirect:/oferente/dashboard";

        return "redirect:/publica/inicio";
    }


    @GetMapping("/login")
    public String login() {
        return "presentation/login/ViewLogin";
    }


    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "presentation/login/ViewAccesoDenegado";
    }
}