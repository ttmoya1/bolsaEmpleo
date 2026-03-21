package org.example.bolsaempleo.presentation.registro;


import jakarta.validation.Valid;
import org.example.bolsaempleo.logic.Empresa;
import org.example.bolsaempleo.logic.Oferente;
import org.example.bolsaempleo.logic.Service;
import org.example.bolsaempleo.logic.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/registro")
public class ControllerRegistro {

    @Autowired
    private Service service;

    // ----------------------------------------------------------------
    // REGISTRO EMPRESA
    // ----------------------------------------------------------------
    @GetMapping("/empresa")
    public String registroEmpresaGet(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("empresa", new Empresa());
        return "presentation/registro/ViewRegistroEmpresa";
    }

    @PostMapping("/empresa")
    public String registroEmpresaPost(
            @ModelAttribute("usuario") @Valid Usuario usuario, BindingResult resUsuario,
            @ModelAttribute("empresa") @Valid Empresa empresa,  BindingResult resEmpresa,
            Model model) {

        if (resUsuario.hasErrors() || resEmpresa.hasErrors()) {
            return "presentation/registro/ViewRegistroEmpresa";
        }
        try {
            service.registrarEmpresa(usuario, empresa);
            model.addAttribute("mensaje", "Registro exitoso. Un administrador aprobará su cuenta pronto.");
            return "presentation/registro/ViewRegistroExito";
        } catch (IllegalArgumentException e) {
            resUsuario.addError(new FieldError("usuario", "correo", e.getMessage()));
            return "presentation/registro/ViewRegistroEmpresa";
        }
    }

    // ----------------------------------------------------------------
    // REGISTRO OFERENTE
    // ----------------------------------------------------------------
    @GetMapping("/oferente")
    public String registroOferenteGet(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("oferente", new Oferente());
        return "presentation/registro/ViewRegistroOferente";
    }

    @PostMapping("/oferente")
    public String registroOferentePost(
            @ModelAttribute("usuario") @Valid Usuario usuario, BindingResult resUsuario,
            @ModelAttribute("oferente") @Valid Oferente oferente, BindingResult resOferente,
            Model model) {

        if (resUsuario.hasErrors() || resOferente.hasErrors()) {
            return "presentation/registro/ViewRegistroOferente";
        }
        try {
            service.registrarOferente(usuario, oferente);
            model.addAttribute("mensaje", "Registro exitoso. Un administrador aprobará su cuenta pronto.");
            return "presentation/registro/ViewRegistroExito";
        } catch (IllegalArgumentException e) {
            resUsuario.addError(new FieldError("usuario", "correo", e.getMessage()));
            return "presentation/registro/ViewRegistroOferente";
        }
    }
}