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


    @GetMapping("/oferente")
    public String registroOferenteGet(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("oferente", new Oferente());
        return "presentation/registro/ViewRegistroOferente";
    }

    @PostMapping("/oferente")
    public String registroOferentePost(
            @RequestParam String correo,
            @RequestParam String clave,
            @RequestParam String identificacion,
            @RequestParam String nombre,
            @RequestParam String primerApellido,
            @RequestParam(required = false) String nacionalidad,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String lugarResidencia,
            Model model) {

        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setClave(clave);

        Oferente oferente = new Oferente();
        oferente.setIdentificacion(identificacion);
        oferente.setNombre(nombre);
        oferente.setPrimerApellido(primerApellido);
        oferente.setNacionalidad(nacionalidad);
        oferente.setTelefono(telefono);
        oferente.setLugarResidencia(lugarResidencia);

        try {
            service.registrarOferente(usuario, oferente);
            model.addAttribute("mensaje", "Registro exitoso. Un administrador aprobará su cuenta pronto.");
            return "presentation/registro/ViewRegistroExito";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("oferente", oferente);
            return "presentation/registro/ViewRegistroOferente";
        }
    }
}