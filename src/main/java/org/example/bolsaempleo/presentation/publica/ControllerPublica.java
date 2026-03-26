package org.example.bolsaempleo.presentation.publica;


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
                            @RequestParam(value = "textoBusqueda", required = false) String textoBusqueda) {
        List<Puesto> resultados = service.buscarPuestosPublicos(textoBusqueda);
        model.addAttribute("textoBusqueda", textoBusqueda);
        model.addAttribute("resultados", resultados);
        return "presentation/publica/ViewBuscar";
    }

    @PostMapping("/buscar")
    public String buscarPost(Model model,
                             @RequestParam(value = "textoBusqueda", required = false) String textoBusqueda) {
        List<Puesto> resultados = service.buscarPuestosPublicos(textoBusqueda);
        model.addAttribute("textoBusqueda", textoBusqueda);
        model.addAttribute("resultados", resultados);
        return "presentation/publica/ViewBuscar";
    }

    // ----------------------------------------------------------------
    // Ver detalle de un puesto público
    // GET /publica/puesto/{id}
    // ----------------------------------------------------------------
    @GetMapping("/puesto/{id}")
    public String verPuesto(@PathVariable Long id, Model model,
                            @AuthenticationPrincipal UserDetails ud) {
        try {
            Puesto puesto = service.puestoById(id);
            if (!puesto.isActivo() || !"PUB".equals(puesto.getTipoPublicacion())) {
                return "redirect:/publica/inicio";
            }
            model.addAttribute("puesto", puesto);
            model.addAttribute("caracteristicas", service.caracteristicasByPuesto(id));

            // Verificar si el oferente ya aplicó
            if (ud != null) {
                try {
                    Usuario usuario = service.usuarioByCorreo(ud.getUsername());
                    Oferente oferente = service.oferenteByUsuario(usuario);
                    boolean yaAplico = service.yaAplico(puesto.getId(), oferente.getId());
                    model.addAttribute("yaAplico", yaAplico);
                } catch (Exception ignored) {}
            }

            return "presentation/publica/ViewDetallePuesto";
        } catch (Exception e) {
            return "redirect:/publica/inicio";
        }
    }
}