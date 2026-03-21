package org.example.bolsaempleo.presentation.publica;


import org.example.bolsaempleo.logic.Puesto;
import org.example.bolsaempleo.logic.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@SessionAttributes("textoBusqueda")
@RequestMapping("/publica")
public class ControllerPublica {

    @Autowired
    private Service service;

    // ----------------------------------------------------------------
    // Inicializa el texto de búsqueda en sesión
    // ----------------------------------------------------------------
    @ModelAttribute("textoBusqueda")
    public String textoBusqueda() {
        return "";
    }

    // ----------------------------------------------------------------
    // Portada pública: muestra los 5 puestos más recientes
    // GET /publica/inicio  (también redirige desde /)
    // ----------------------------------------------------------------
    @GetMapping("/inicio")
    public String inicio(Model model) {
        List<Puesto> recientes = service.puestosRecientes();
        model.addAttribute("puestosRecientes", recientes);
        return "presentation/publica/ViewInicio";
    }

    // ----------------------------------------------------------------
    // Búsqueda pública de puestos
    // GET  /publica/buscar  → muestra el formulario vacío
    // POST /publica/buscar  → ejecuta la búsqueda y recarga la vista
    // ----------------------------------------------------------------
    @GetMapping("/buscar")
    public String buscarGet(Model model,
                            @ModelAttribute("textoBusqueda") String textoBusqueda) {
        List<Puesto> resultados = service.buscarPuestosPublicos(textoBusqueda);
        model.addAttribute("resultados", resultados);
        return "presentation/publica/ViewBuscar";
    }

    @PostMapping("/buscar")
    public String buscarPost(Model model,
                             @ModelAttribute("textoBusqueda") String textoBusqueda) {
        List<Puesto> resultados = service.buscarPuestosPublicos(textoBusqueda);
        model.addAttribute("resultados", resultados);
        return "presentation/publica/ViewBuscar";
    }

    // ----------------------------------------------------------------
    // Ver detalle de un puesto público
    // GET /publica/puesto/{id}
    // ----------------------------------------------------------------
    @GetMapping("/puesto/{id}")
    public String verPuesto(@PathVariable Long id, Model model) {
        try {
            Puesto puesto = service.puestoById(id);
            // Solo se puede ver si es público y está activo
            if (!puesto.isActivo() || !"PUB".equals(puesto.getTipoPublicacion())) {
                return "redirect:/publica/inicio";
            }
            model.addAttribute("puesto", puesto);
            model.addAttribute("caracteristicas", service.caracteristicasByPuesto(id));
            return "presentation/publica/ViewDetallePuesto";
        } catch (Exception e) {
            return "redirect:/publica/inicio";
        }
    }
}