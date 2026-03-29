package org.example.bolsaempleo.presentation.oferente;

import org.example.bolsaempleo.logic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/oferente")
public class ControllerOferente {

    @Autowired
    private Service service;

    private static final String PDF_DIR = "src/main/resources/static/curricula/";

    private Oferente getOferente(UserDetails ud) {
        Usuario usuario = service.usuarioByCorreo(ud.getUsername());
        return service.oferenteByUsuario(usuario);
    }

    @GetMapping("/aplicar/{puestoId}")
    public String aplicar(@AuthenticationPrincipal UserDetails ud,
                          @PathVariable Long puestoId,
                          RedirectAttributes redirectAttrs) {
        Oferente oferente = getOferente(ud);
        try {
            service.aplicarAPuesto(puestoId, oferente.getId());
            redirectAttrs.addFlashAttribute("mensaje", "¡Aplicación enviada con éxito!");
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/oferente/buscar";
    }

    // ----------------------------------------------------------------
    // DASHBOARD
    // ----------------------------------------------------------------
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        Oferente oferente = getOferente(ud);
        model.addAttribute("oferente",    oferente);
        model.addAttribute("habilidades", service.habilidadesByOferente(oferente.getId()));
        return "presentation/oferente/ViewDashboard";
    }

    // ----------------------------------------------------------------
    // HABILIDADES  –  navegación por árbol (sin JavaScript)
    //
    // La raíz es VIRTUAL (no existe en BD).
    // Los nodos con padre=null son el primer nivel navegable
    // (Lenguajes de programación, Web, Bases de datos, Idiomas, etc.)
    //
    // nodoId ausente o nodoId=0  →  mostrar los nodos con padre=null
    // nodoId=X                   →  mostrar los hijos de X
    // ----------------------------------------------------------------

    /**
     * Construye la ruta desde el primer nivel hasta el nodo actual.
     * Para cada nodo sube por padre hasta llegar a un nodo con padre=null
     * (que es el primer nivel visible, no la raíz virtual).
     * Devuelve lista vacía si nodoId es null (estamos en la raíz virtual).
     */
    private List<Caracteristica> buildRuta(Long nodoId) {
        if (nodoId == null) return Collections.emptyList();
        List<Caracteristica> ruta = new ArrayList<>();
        Caracteristica actual = service.caracteristicaById(nodoId);
        // Sube incluyendo el nodo actual hasta llegar a un nodo raíz (padre null)
        while (actual != null) {
            ruta.add(0, actual);
            actual = actual.getPadre();
        }
        return ruta;
    }

    @GetMapping("/habilidades")
    public String habilidadesGet(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(value = "nodoId", required = false) Long nodoId,
            Model model) {

        Oferente oferente = getOferente(ud);

        // Hijos a mostrar en el panel central
        List<Caracteristica> hijos;
        if (nodoId == null) {
            // Raíz virtual → mostrar todos los nodos con padre=null
            hijos = service.caracteristicasRaiz();
        } else {
            hijos = service.hijosDe(nodoId);
        }

        // Ruta del breadcrumb (vacía cuando estamos en la raíz virtual)
        List<Caracteristica> ruta = buildRuta(nodoId);

        // Panel derecho: todos los hijos disponibles para agregar
        List<Caracteristica> disponibles = hijos;

        model.addAttribute("oferente",    oferente);
        model.addAttribute("habilidades", service.habilidadesByOferente(oferente.getId()));
        model.addAttribute("hijos",       hijos);
        model.addAttribute("disponibles", disponibles);
        model.addAttribute("ruta",        ruta);
        // nodoId null se pasa tal cual — la vista lo usa para saber si estamos en raíz
        model.addAttribute("nodoId",      nodoId);

        return "presentation/oferente/ViewHabilidades";
    }

    @PostMapping("/habilidades/guardar")
    public String habilidadesPost(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam("caracId")                           Long  caracId,
            @RequestParam(value = "nivel", defaultValue = "1") int   nivel,
            @RequestParam(value = "nodoId", required = false)  Long  nodoId) {

        Oferente oferente = getOferente(ud);

        List<OferenteHabilidad> existentes = service.habilidadesByOferente(oferente.getId());
        List<OferenteHabilidad> nueva = new ArrayList<>();

        boolean yaExiste = false;
        for (OferenteHabilidad h : existentes) {
            OferenteHabilidad copia = new OferenteHabilidad();
            copia.setCaracteristica(service.caracteristicaById(h.getCaracteristica().getId()));
            if (h.getCaracteristica().getId().equals(caracId)) {
                copia.setNivel(nivel);
                yaExiste = true;
            } else {
                copia.setNivel(h.getNivel());
            }
            nueva.add(copia);
        }

        if (!yaExiste) {
            OferenteHabilidad agregada = new OferenteHabilidad();
            agregada.setCaracteristica(service.caracteristicaById(caracId));
            agregada.setNivel(nivel);
            nueva.add(agregada);
        }

        service.guardarHabilidades(oferente, nueva);

        String redirect = "/oferente/habilidades";
        if (nodoId != null) redirect += "?nodoId=" + nodoId;
        return "redirect:" + redirect;
    }

    @GetMapping("/habilidades/eliminar/{caracId}")
    public String eliminarHabilidad(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long caracId,
            @RequestParam(value = "nodoId", required = false) Long nodoId) {

        Oferente oferente = getOferente(ud);

        List<OferenteHabilidad> existentes = service.habilidadesByOferente(oferente.getId());
        List<OferenteHabilidad> nueva = new ArrayList<>();

        for (OferenteHabilidad h : existentes) {
            if (!h.getCaracteristica().getId().equals(caracId)) {
                OferenteHabilidad copia = new OferenteHabilidad();
                copia.setCaracteristica(service.caracteristicaById(h.getCaracteristica().getId()));
                copia.setNivel(h.getNivel());
                nueva.add(copia);
            }
        }

        service.guardarHabilidades(oferente, nueva);

        String redirect = "/oferente/habilidades";
        if (nodoId != null) redirect += "?nodoId=" + nodoId;
        return "redirect:" + redirect;
    }

    // ----------------------------------------------------------------
    // SUBIR CURRÍCULO PDF
    // ----------------------------------------------------------------
    @GetMapping("/curriculum")
    public String curriculumGet(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("oferente", getOferente(ud));
        return "presentation/oferente/ViewCurriculum";
    }

    @PostMapping("/curriculum/subir")
    public String curriculumPost(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam("archivo") MultipartFile archivo,
            Model model) {

        if (archivo.isEmpty() || !archivo.getOriginalFilename().endsWith(".pdf")) {
            model.addAttribute("error", "Debe seleccionar un archivo PDF válido.");
            model.addAttribute("oferente", getOferente(ud));
            return "presentation/oferente/ViewCurriculum";
        }

        Oferente oferente = getOferente(ud);
        try {
            Path dir = Paths.get(PDF_DIR);
            Files.createDirectories(dir);
            String nombreArchivo = "cv_" + oferente.getId() + ".pdf";
            Files.copy(archivo.getInputStream(),
                    dir.resolve(nombreArchivo),
                    StandardCopyOption.REPLACE_EXISTING);
            oferente.setCurriculumPdf("/curricula/" + nombreArchivo);
            service.guardarOferente(oferente);
        } catch (IOException e) {
            model.addAttribute("error", "Error al guardar el archivo: " + e.getMessage());
            model.addAttribute("oferente", oferente);
            return "presentation/oferente/ViewCurriculum";
        }
        return "redirect:/oferente/dashboard";
    }

    // ----------------------------------------------------------------
    // BUSCAR PUESTOS
    // ----------------------------------------------------------------
    @GetMapping("/buscar")
    public String buscarGet(Model model,
                            @RequestParam(defaultValue = "") String texto) {
        model.addAttribute("texto",      texto);
        model.addAttribute("resultados", service.buscarTodosPuestos(texto));
        return "presentation/oferente/ViewBuscarPuestos";
    }

    @PostMapping("/buscar")
    public String buscarPost(Model model,
                             @RequestParam(defaultValue = "") String texto) {
        model.addAttribute("texto",      texto);
        model.addAttribute("resultados", service.buscarTodosPuestos(texto));
        return "presentation/oferente/ViewBuscarPuestos";
    }
}