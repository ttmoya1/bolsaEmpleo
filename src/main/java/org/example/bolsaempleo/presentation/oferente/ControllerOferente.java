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

    private static final String PDF_DIR = "uploads/curricula/";

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


    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        Oferente oferente = getOferente(ud);
        model.addAttribute("oferente",    oferente);
        model.addAttribute("habilidades", service.habilidadesByOferente(oferente.getId()));
        return "presentation/oferente/ViewDashboard";
    }


    private List<Caracteristica> buildRuta(Long nodoId) {
        if (nodoId == null) return Collections.emptyList();
        List<Caracteristica> ruta = new ArrayList<>();
        Caracteristica actual = service.caracteristicaById(nodoId);
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

        List<Caracteristica> hijos = (nodoId == null)
                ? service.caracteristicasRaiz()
                : service.hijosDe(nodoId);

        model.addAttribute("oferente",    oferente);
        model.addAttribute("habilidades", service.habilidadesByOferente(oferente.getId()));
        model.addAttribute("hijos",       hijos);
        model.addAttribute("disponibles", hijos);
        model.addAttribute("ruta",        buildRuta(nodoId));
        model.addAttribute("nodoId",      nodoId);

        return "presentation/oferente/ViewHabilidades";
    }

    @PostMapping("/habilidades/guardar")
    public String habilidadesPost(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam("caracId")                           Long caracId,
            @RequestParam(value = "nivel", defaultValue = "1") int  nivel,
            @RequestParam(value = "nodoId", required = false)  Long nodoId) {

        Oferente oferente = getOferente(ud);
        List<OferenteHabilidad> existentes = service.habilidadesByOferente(oferente.getId());
        List<OferenteHabilidad> nueva      = new ArrayList<>();

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


    @GetMapping("/habilidades/confirmar-eliminar/{caracId}")
    public String confirmarEliminarHabilidad(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long caracId,
            @RequestParam(value = "nodoId", required = false) Long nodoId,
            Model model) {

        Caracteristica caract = service.caracteristicaById(caracId);
        model.addAttribute("caracId",        caracId);
        model.addAttribute("nombreHabilidad", caract.getNombre());
        model.addAttribute("nodoId",         nodoId);
        return "presentation/oferente/ViewConfirmarEliminarHabilidad";
    }


    @PostMapping("/habilidades/eliminar/{caracId}")
    public String eliminarHabilidad(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long caracId,
            @RequestParam(value = "nodoId", required = false) Long nodoId) {

        Oferente oferente = getOferente(ud);
        List<OferenteHabilidad> existentes = service.habilidadesByOferente(oferente.getId());
        List<OferenteHabilidad> nueva      = new ArrayList<>();

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


    @GetMapping("/curriculum")
    public String curriculumGet(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("oferente", getOferente(ud));
        return "presentation/oferente/ViewCurriculum";
    }

    @PostMapping("/curriculum/subir")
    public String curriculumPost(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam("archivo") MultipartFile archivo,
            RedirectAttributes redirectAttrs) {

        if (archivo.isEmpty() ||
                !archivo.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            redirectAttrs.addFlashAttribute("error", "Debe seleccionar un archivo PDF válido.");
            return "redirect:/oferente/curriculum";
        }

        Oferente oferente = getOferente(ud);
        try {
            Path dir = Paths.get(PDF_DIR);
            Files.createDirectories(dir);

            String nombreArchivo = "cv_" + oferente.getId()
                    + "_" + System.currentTimeMillis() + ".pdf";

            if (oferente.getCurriculumPdf() != null) {
                String nombreAnterior = oferente.getCurriculumPdf()
                        .replace("/curricula/", "");
                Files.deleteIfExists(dir.resolve(nombreAnterior));
            }

            Files.copy(archivo.getInputStream(),
                    dir.resolve(nombreArchivo),
                    StandardCopyOption.REPLACE_EXISTING);

            oferente.setCurriculumPdf("/curricula/" + nombreArchivo);
            service.guardarOferente(oferente);

            redirectAttrs.addFlashAttribute("mensaje", "Currículo actualizado correctamente.");
        } catch (IOException e) {
            redirectAttrs.addFlashAttribute("error",
                    "Error al guardar el archivo: " + e.getMessage());
        }
        return "redirect:/oferente/curriculum";
    }


    @GetMapping("/buscar")
    public String buscarGet(
            @AuthenticationPrincipal UserDetails ud,
            Model model,
            @RequestParam(defaultValue = "") String texto,
            @RequestParam(value = "caracIds", required = false) List<Long> caracIds) {

        model.addAttribute("caracteristicas",       service.caracteristicasRaiz());
        model.addAttribute("caracIdsSeleccionadas", caracIds != null ? caracIds : new ArrayList<>());
        model.addAttribute("texto", texto);

        if (caracIds != null && !caracIds.isEmpty()) {
            model.addAttribute("resultadosCarac",    service.buscarPuestosPublicosPorCaracteristicas(caracIds));
            model.addAttribute("totalSeleccionadas", caracIds.size());
        }
        if (!texto.isBlank()) {
            model.addAttribute("resultados", service.buscarTodosPuestos(texto));
        }

        return "presentation/oferente/ViewBuscarPuestos";
    }

    @PostMapping("/buscar")
    public String buscarPost(
            @AuthenticationPrincipal UserDetails ud,
            Model model,
            @RequestParam(defaultValue = "") String texto,
            @RequestParam(value = "caracIds", required = false) List<Long> caracIds) {

        model.addAttribute("caracteristicas",       service.caracteristicasRaiz());
        model.addAttribute("caracIdsSeleccionadas", caracIds != null ? caracIds : new ArrayList<>());
        model.addAttribute("texto", texto);

        if (caracIds != null && !caracIds.isEmpty()) {
            model.addAttribute("resultadosCarac",    service.buscarPuestosPublicosPorCaracteristicas(caracIds));
            model.addAttribute("totalSeleccionadas", caracIds.size());
        }
        if (!texto.isBlank()) {
            model.addAttribute("resultados", service.buscarTodosPuestos(texto));
        }

        return "presentation/oferente/ViewBuscarPuestos";
    }
}