package org.example.bolsaempleo.presentation.publica;

import org.example.bolsaempleo.logic.Caracteristica;
import org.example.bolsaempleo.logic.Puesto;
import org.example.bolsaempleo.logic.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.example.bolsaempleo.logic.Usuario;
import org.example.bolsaempleo.logic.Oferente;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

@Controller
@SessionAttributes("textoBusqueda")
@RequestMapping("/publica")
public class ControllerPublica {

    @Autowired
    private Service service;

    @ModelAttribute("textoBusqueda")
    public String textoBusqueda() {
        return "";
    }


    @GetMapping("/inicio")
    public String inicio(Model model) {
        model.addAttribute("puestosRecientes", service.puestosRecientes());
        return "presentation/publica/ViewInicio";
    }


    @GetMapping("/buscar")
    public String buscarGet(Model model,
                            @RequestParam(value = "textoBusqueda", required = false) String textoBusqueda,
                            @RequestParam(value = "caracIds", required = false) List<Long> caracIds) {


        model.addAttribute("caracteristicas",       service.caracteristicasRaiz());
        model.addAttribute("caracIdsSeleccionadas", caracIds != null ? caracIds : new ArrayList<>());


        if (caracIds != null && !caracIds.isEmpty()) {
            List<Object[]> resultados = service.buscarPuestosPublicosPorCaracteristicas(caracIds);
            model.addAttribute("resultados",          resultados);
            model.addAttribute("totalSeleccionadas",  caracIds.size());
        }


        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            model.addAttribute("resultadosTexto", service.buscarPuestosPublicos(textoBusqueda));
            model.addAttribute("textoBusqueda",   textoBusqueda);
        }

        return "presentation/publica/ViewBuscar";
    }

    @PostMapping("/buscar")
    public String buscarPost(Model model,
                             @RequestParam(value = "textoBusqueda", required = false) String textoBusqueda,
                             @RequestParam(value = "caracIds", required = false) List<Long> caracIds) {

        model.addAttribute("caracteristicas",       service.caracteristicasRaiz());
        model.addAttribute("caracIdsSeleccionadas", caracIds != null ? caracIds : new ArrayList<>());

        if (caracIds != null && !caracIds.isEmpty()) {
            List<Object[]> resultados = service.buscarPuestosPublicosPorCaracteristicas(caracIds);
            model.addAttribute("resultados",         resultados);
            model.addAttribute("totalSeleccionadas", caracIds.size());
        }

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            model.addAttribute("resultadosTexto", service.buscarPuestosPublicos(textoBusqueda));
            model.addAttribute("textoBusqueda",   textoBusqueda);
        }

        return "presentation/publica/ViewBuscar";
    }


    @GetMapping("/puesto/{id}")
    public String verPuesto(@PathVariable Long id, Model model,
                            @AuthenticationPrincipal UserDetails ud) {
        try {
            Puesto puesto = service.puestoById(id);
            if (!puesto.isActivo() || !"PUB".equals(puesto.getTipoPublicacion())) {
                return "redirect:/publica/inicio";
            }
            model.addAttribute("puesto",          puesto);
            model.addAttribute("caracteristicas", service.caracteristicasByPuesto(id));

            if (ud != null) {
                try {
                    Usuario  usuario  = service.usuarioByCorreo(ud.getUsername());
                    Oferente oferente = service.oferenteByUsuario(usuario);
                    model.addAttribute("yaAplico", service.yaAplico(puesto.getId(), oferente.getId()));
                } catch (Exception ignored) {}
            }

            return "presentation/publica/ViewDetallePuesto";
        } catch (Exception e) {
            return "redirect:/publica/inicio";
        }
    }
}